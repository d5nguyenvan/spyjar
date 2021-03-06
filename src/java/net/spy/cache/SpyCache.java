/*
 * Copyright (c) 1999 Dustin Sallings
 */

package net.spy.cache;

import java.io.IOException;
import java.lang.ref.Reference;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;

import net.spy.SpyObject;
import net.spy.SpyThread;
import net.spy.util.TimeStampedHashMap;

/**
 * Spy in-memory cache object.
 *
 * <p>
 *
 * If the system properties <tt>net.spy.cache.multi.addr</tt> and
 * <tt>net.spy.cache.multi.port</tt> are both set, requests may be sent as
 * ASCII strings on that multicast group and port to clear cache entries
 * based on prefix.
 */
public class SpyCache extends SpyObject {

	final TimeStampedHashMap<String, Cachable> cacheStore;
	private SpyCacheCleaner cacheCleaner=null;

	CacheDelegate delegate=null;

	private static SpyCache instance=null;

	// how frequently to clean up the cache
	private static final int CACHE_CLEAN_SLEEP_TIME=60000;

	/**
	 * Construct a new instance of SpyCache.  This allows subclasses to
	 * override certain methods, allowing smarter cache handling.
	 */
	protected SpyCache() {
		super();
		cacheStore=new TimeStampedHashMap<String, Cachable>();
		delegate=new DummyDelegate();
	}

	private synchronized void checkThread() {
		if(cacheCleaner==null || (!cacheCleaner.isAlive())) {
			cacheCleaner=new SpyCacheCleaner();
		}
	}

	/**
	 * Get the instance of SpyCache.
	 *
	 * @return the instance of SpyCache, or a new instance if required
	 */
	public static synchronized SpyCache getInstance() {
		if(instance==null) {
			instance=new SpyCache();
		}

		instance.checkThread();

		return(instance);
	}

	/**
	 * Shut down the cache.
	 */
	public static synchronized void shutdown() {
		if(instance != null && instance.cacheCleaner != null) {
			instance.cacheCleaner.shutdown();
		}
		instance=null;
	}

	/**
	 * Set the delegate for this SpyCache.
	 *
	 * @param del
	 */
	public void setDelegate(CacheDelegate del) {
		if(del == null) {
			throw new NullPointerException("Invalid delegate <null>");
		}
		delegate=del;
	}

	/**
	 * Store a Cachable object in the cache.
	 *
	 * @param key the key for storing this object
	 * @param value the object to store
	 */
	public void store(String key, Cachable value) {
		synchronized(cacheStore) {
			// Send the cached event notify to the cachable itself
			value.cachedEvent(key);
			cacheStore.put(key, value);
		}
		delegate.cachedObject(key, value);
	}

	/**
	 * Store an object in the cache with the specified timeout.
	 *
	 * <p>
	 *  Objects will be wrapped in a private subclass of {@link Cachable}
	 *  that expires based on the time.
	 * </p>
	 *
	 * @param key Cache key
	 * @param value Object to cache
	 * @param cacheTime Amount of time (in milliseconds) to store object.
	 */
	public void store(String key, Object value, long cacheTime) {
		Cachable i=new SpyCacheItem(key, value, cacheTime);
		store(key, i);
	}

	/**
	 * Get an object from the cache, returns null if there's not a valid
	 * object in the cache with this key.
	 *
	 * @param key key of the object to return
	 * @return the object, else null
	 */
	public Object get(String key) {
		Object ret=null;
		long t=System.currentTimeMillis();
		synchronized(cacheStore) {
			Cachable i=cacheStore.get(key);
			if(i!=null && (!i.isExpired())) {
				// mark the object as seen
				i.setAccessTime(t);
				// get the object from the cache
				ret=i.getCachedObject();
				// If the stored object is a reference, dereference it.
				if((ret!=null) && (ret instanceof Reference)) {
					Reference<?> ref=(Reference<?>)ret;
					ret=ref.get();
				} // Object was a reference
			} // Found object in cache
		} // Locked the cache store
		return(ret);
	}

	/**
	 * Manually remove an object from the cache.
	 *
	 * @param key key to remove
	 */
	public void uncache(String key) {
		Cachable unc=null;
		synchronized(cacheStore) {
			unc=cacheStore.remove(key);
		}
		if(unc!=null) {
			unc.uncachedEvent(key);
			delegate.uncachedObject(key, unc);
		}
	}

	/**
	 * Remove all objects from the cache that begin with the passed in
	 * string.
	 *
	 * @param keystart string to match in the key name
	 */
	public void uncacheLike(String keystart) {
		synchronized(cacheStore) {
			for(Iterator<Map.Entry<String, Cachable>> i
					=cacheStore.entrySet().iterator(); i.hasNext();) {

				Map.Entry<String, Cachable> me=i.next();

				String key=me.getKey();

				// If this matches, kill it.
				if(key.startsWith(keystart)) {
					i.remove();
					Cachable c=me.getValue();
					c.uncachedEvent(key);
					delegate.uncachedObject(key, c);
				}
			} // for loop
		} // lock
	}

	////////////////////////////////////////////////////////////////////
	//                       Private Classes                          //
	////////////////////////////////////////////////////////////////////

	private class SpyCacheCleaner extends SpyThread {

		// How many cleaning passes we've done.
		private int passes=0;

		// This is so we can only report multicast security exceptions
		// once.
		private boolean reportedMulticastSE=false;

		// Insert multicast listener here.
		private CacheClearRequestListener listener=null;

		// This indicates whether we need to go through the multicast cache
		// clearer loop.  It's true by default so we'll try it the first time.
		private boolean wantMulticastListener=true;

		private boolean shutdown=false;

		public SpyCacheCleaner() {
			super();
			setName("SpyCacheCleaner");
			setDaemon(true);
			start();
		}

		public void shutdown() {
			getLogger().debug("Shutting down %s", this);
			shutdown=true;
			synchronized(this) {
				notifyAll();
			}
		}

		@Override
		public String toString() {
			return(super.toString() + " - "
				+ passes + " runs, mod age:  " + cacheStore.getUseAge()
				+ ", cur size:  " + cacheStore.size()
				+ ", tot stored:  " + cacheStore.getNumPuts()
				+ ", watermark:  " + cacheStore.getWatermark()
				+ ", hits:  " + cacheStore.getHits()
				+ ", misses:  " + cacheStore.getMisses()
				);
		}

		private void cleanup() throws Exception {
			synchronized(cacheStore) {
				for(Iterator<Map.Entry<String, Cachable>> i
						=cacheStore.entrySet().iterator(); i.hasNext();){
					Map.Entry<String, Cachable> me=i.next();
					String key=me.getKey();
					Cachable it=me.getValue();
					if(it.isExpired()) {
						getLogger().debug("%s expired", it.getCacheKey());
						i.remove();
						it.uncachedEvent(key);
						delegate.uncachedObject(key, it);
					}
				}
			}
			passes++;
		}

		private boolean shouldIContinue() {
			boolean rv=false;

			// Return true if the difference between now and the last
			// time the cache was touched is less than an hour.
			if((!shutdown) && (cacheStore.getUseAge()) < (3600*1000)) {
				rv=true;
			}

			return(rv);
		}

		// Make sure our multicast listener is still running if it should be.
		private void checkMulticastThread() {
			try {
				String addrS=System.getProperty("net.spy.cache.multi.addr");
				String portS=System.getProperty("net.spy.cache.multi.port");

				if(addrS!=null && portS!=null) {
					wantMulticastListener=true;
					int port=Integer.parseInt(portS);

					InetAddress group = InetAddress.getByName(addrS);
					listener=new CacheClearRequestListener(group, port);
				} else {
					wantMulticastListener=false;
				}

			} catch(SecurityException se) {
				// Only do this the first time.
				if(!reportedMulticastSE) {
					getLogger().error("Couldn't create multicast listener", se);
					reportedMulticastSE=true;
				}
			} catch(IOException ioe) {
				getLogger().error("Couldn't create multicast listener", ioe);
			}
		}

		/**
		 * Loop until there's no need to loop any more.
		 */
		@Override
		public void run() {

			// It will keep going until nothing's been touched in the cache
			// for an hour, at which point it'll dump the whole cache and join.
			while(shouldIContinue()) {
				try {
					// Just for throttling, sleep a second.
					synchronized(this) {
						wait(CACHE_CLEAN_SLEEP_TIME);
					}
					cleanup();
					// Check to see if we want a multicast listener
					if(wantMulticastListener
						&& (listener==null || (!listener.isAlive()))) {
						checkMulticastThread();
					}
				} catch(Exception e) {
					getLogger().warn("Exception in cleanup loop", e);
				}
			}

			getLogger().info("Shutting down.");

			// OK, we're about to bail, let's dump the cache and go.
			synchronized(cacheStore) {
				for(Iterator<Map.Entry<String, Cachable>> i
						=cacheStore.entrySet().iterator(); i.hasNext();){
					Map.Entry<String, Cachable> me=i.next();
					String key=me.getKey();
					Cachable it=me.getValue();
					i.remove();
					it.uncachedEvent(key);
					delegate.uncachedObject(key, it);
				}
			}

			// Tell the multicast listener to stop if we have one
			if(listener!=null) {
				listener.stopRunning();
			}
		}
	} // Cleaner class

	static class SpyCacheItem extends AbstractCachable {
		private final long exptime;

		public SpyCacheItem(Object key, Object value, long cacheTime) {
			super(key, value);

			exptime=getCacheTime()+cacheTime;
		}

		@Override
		public String toString() {
			String out="{Cached item:  " + getCacheKey();
			if(exptime>0) {
				out+=" Expires:  " + new java.util.Date(exptime)
					+ " - expired? " + isExpired();
			}
			out+="}";
			return(out);
		}

		public boolean isExpired() {
			boolean ret=false;
			if(exptime>0) {
				long t=System.currentTimeMillis();
				ret=(t>exptime);
			}
			// If the value is a reference that is no longer valid,
			// the object has expired
			Object v=getCachedObject();
			if(v instanceof Reference) {
				Reference<?> rvalue=(Reference<?>)v;
				if(rvalue.get()==null) {
					ret=false;
				}
			}
			return(ret);
		}

	} // SpyCacheItem

	// This allows us to always have a delegate object registered.
	static class DummyDelegate extends Object implements CacheDelegate {
		public void cachedObject(String key, Cachable value) {
			// Dummy implementation
		}
		public void uncachedObject(String key, Cachable value) {
			// dummy implementation
		}
	}

}
