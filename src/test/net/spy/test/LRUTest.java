// Copyright (c) 2002  Dustin Sallings <dustin@spy.net>
//
// arch-tag: 13FD9F8D-1110-11D9-A39E-000A957659CC

package net.spy.test;

import java.lang.ref.SoftReference;

import java.util.Map;
import java.util.List;

import java.lang.reflect.Field;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import net.spy.cache.LRUCache;
import net.spy.cache.CacheListener;

/**
 * Test the LRU cache implementation
 */
public class LRUTest extends TestCase {

	/**
	 * Get an instance of LRUTest.
	 */
	public LRUTest(String name) {
		super(name);
	}

	/** 
	 * Get this test suite.
	 */
	public static Test suite() {
		return new TestSuite(LRUTest.class);
	}

	/** 
	 * Run this test.
	 */
	public static void main(String args[]) {
		junit.textui.TestRunner.run(suite());
	}

	private void validateCacheSize(LRUCache instance) {
		Map map=null;
		List list=null;
		try {
			Field mapField=instance.getClass().getDeclaredField("map");
			Field listField=instance.getClass().getDeclaredField("list");
			mapField.setAccessible(true);
			listField.setAccessible(true);

			map=(Map)mapField.get(instance);
			list=(List)listField.get(instance);
		} catch(Exception e) {
			e.printStackTrace();
			fail("Couldn't validate the cache size.");
		}

		assertEquals("Cache size invalid", map.size(), list.size());
	}

	/** 
	 * Perform basic LRU testing.
	 */
	public void testBasicLRU() {
		Integer zero=new Integer(0);
		LRUCache cache=new LRUCache(10);

		assertNull("Zero shouldn't be there.", cache.get(zero));

		// Keep this value up-to-date
		cache.put(zero, zero);
		assertNotNull("Zero should be there.", cache.get(zero));

		for(int i=1; i<100; i++) {
			validateCacheSize(cache);
			Integer ii=new Integer(i);
			assertNull(cache.get(ii));
			cache.put(ii, ii);
			assertNotNull(cache.get(zero));
			assertNotNull(cache.get(ii));

			if(i>10) {
				assertNull(cache.get(new Integer(i-10)));
			}
		}
	}

	/** 
	 * Perform reference LRU testing.
	 */
	public void testReferenceLRU() {
		Integer zero=new Integer(0);
		LRUCache cache=new LRUCache(10);

		assertNull("Zero shouldn't be there.", cache.get(zero));

		// Keep this value up-to-date
		cache.put(zero, new SoftReference(zero));
		assertNotNull("Zero should be there.", cache.get(zero));

		for(int i=1; i<100; i++) {
			validateCacheSize(cache);
			Integer ii=new Integer(i);
			assertNull(cache.get(ii));
			cache.put(ii, new SoftReference(ii));
			assertNotNull(cache.get(zero));
			assertNotNull(cache.get(ii));

			if(i>10) {
				assertNull(cache.get(new Integer(i-10)));
			}
		}
	}

	/** 
	 * Test cache listener.
	 */
	public void testCacheListener() {
		Integer zero=new Integer(0);
		LRUCache cache=new LRUCache(10);

		TestListener tl=new TestListener();
		cache.put("listener", tl);
		for(int i=1; i<100; i++) {
			Integer ii=new Integer(i);
			assertNull(cache.get(ii));
			cache.put(ii, ii);
			assertNotNull(cache.get(ii));
		}
		assertNull(cache.get("listener"));
		assertEquals(1, tl.cached);
		assertEquals(1, tl.uncached);
	}

	private static final class TestListener extends Object
		implements CacheListener {

		public int cached=0;
		public int uncached=0;

		public TestListener() {
			super();
		}

		public void cachedEvent(Object key) {
			cached++;
		}

		public void uncachedEvent(Object key) {
			uncached++;
		}
	}

}