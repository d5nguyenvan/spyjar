// Copyright (c) 2001  Dustin Sallings <dustin@spy.net>
//
// $Id: URLItem.java,v 1.6 2003/07/26 07:46:52 dustin Exp $

package net.spy.net;

import java.util.HashMap;
import java.util.Date;

import java.io.IOException;

import java.net.URL;

import net.spy.cron.Job;
import net.spy.cron.TimeIncrement;
import net.spy.cron.SimpleTimeIncrement;

import net.spy.util.ThreadPoolRunnable;

/**
 * A particular URL that's being watched.
 */
public class URLItem extends Job implements ThreadPoolRunnable {

	// How long a URL will be watched if nobody wants it (defaults to a
	// half hour).
	private int maxIdleTime=1800000;

	private long lastRequest=0;

	private int numUpdates=0;

	private URL url=null;

	private String content=null;

	private IOException lastError=null;

	/** 
	 * Get a new URLItem at the default interval.
	 * 
	 * @param u URL to watch
	 */
	public URLItem(URL u) {
		this(u, new Date(), new SimpleTimeIncrement(1800000));
	}

	/** 
	 * Get a new URLItem with the given interval.
	 * 
	 * @param u URL to watch
	 * @param ti the increment
	 */
	public URLItem(URL u, TimeIncrement ti) {
		this(u, new Date(), ti);
	}

	/**
	 * Get an instance of URLItem.
	 *
	 * @param u URL to watch
	 * @param startDate time to start
	 * @param ti the increment
	 */
	public URLItem(URL u, Date startDate, TimeIncrement ti) {
		super(u.toString(), startDate, ti);
		this.url=u;
		lastRequest=System.currentTimeMillis();
	}

	/** 
	 * Ask the URL to update itself if it needs to.
	 */
	public void runJob() {
		HashMap headers=new HashMap();
		// make sure the stuff isn't cached
		headers.put("Pragma", "no-cache");

		numUpdates++;

		try {
			HTTPFetch hf=new HTTPFetch(url, headers);
			content=hf.getData();
		} catch(IOException e) {
			lastError=e;
		}

		// Notify listeners that this has been updated.
		synchronized(this) {
			notifyAll();
		}
	}

	/** 
	 * Get the content from the last fetch.
	 */
	public String getContent() throws IOException {
		lastRequest=System.currentTimeMillis();
		if(lastError!=null) {
			throw lastError;
		}
		return(content);
	}

	/** 
	 * Find out when the last request was.
	 * 
	 * @return the timestamp of the last request.
	 */
	public long getLastRequest() {
		return(lastRequest);
	}

	/** 
	 * Get the URL this thing is watching.
	 * 
	 * @return the URL
	 */
	public URL getURL() {
		return(url);
	}

	/** 
	 * Override the finished mark to also stop this job if it hasn't been
	 * touched recently enough.
	 */
	protected void markFinished() {
		long now=System.currentTimeMillis();
		// If it's been too long since this thing was touched, toss it.
		if( (now-lastRequest) > maxIdleTime) {
			stopRunning();
		}
		super.markFinished();
	}

	/** 
	 * Set the maximum number of milliseconds this URL will remain in the
	 * container if nothing requests it.
	 */
	public void setMaxIdleTime(int maxIdleTime) {
		this.maxIdleTime=maxIdleTime;
	}

	/** 
	 * Get the maximum number of milliseconds this URL will remain in the
	 * container if nothing requests it.
	 */
	public int getMaxIdleTime() {
		return(maxIdleTime);
	}

}