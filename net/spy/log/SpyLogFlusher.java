/*
 * Copyright (c) 1999 Dustin Sallings
 *
 * $Id: SpyLogFlusher.java,v 1.2 2002/11/20 04:52:33 dustin Exp $
 */

package net.spy.log;

import java.io.BufferedWriter;
import java.io.FileWriter;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import net.spy.SpyThread;

/**
 * SpyLogFlusher does the actual work of SpyLog.  This is where the log
 * queue is flushed and placed in permanent storage.
 * <p>
 * By default, log entries are written to the file described in the
 * variable <code>logfile</code>.
 * <p>
 * Classes overriding this will be most interested in the protected method
 * doFlush().  doFlush is called on a given interval when it's time to
 * flush the logs.  The logs are available via a SpyLog object called
 * ``log_object.''  To get a list of all log entries that need to be
 * flushed, the following piece of code may be executed:<br>
 * <code>Vector v = log_object.flush();</code>
 */
public class SpyLogFlusher extends SpyThread {

	// private static BufferedWriter logFile=null;
	private SpyLogQueue logQueue=null;
	private boolean keepGoing=true;

	private long sleepTime=900000;

	/**
	 * Path to the logfile.  The default logfile is /tmp/spy.log, but this
	 * can be overridden.
	 */
	public String logfile = "/tmp/spy.log";
	private String queueName=null;

	private Date lastRun=null;
	private Date lastErrorTime=null;
	private Exception lastError=null;

	/**
	 * Get a SpyFlusher for the given queue.
	 */
	public SpyLogFlusher(String name) {
		super();
		setDaemon(true);
		setName("SpyLogFlusher-" + name);
		this.queueName=name;
		// Exception e=new Exception("Instantiated SpyLogFlusher-" + name);
		// e.printStackTrace();
		configure();
	}

	/**
	 * Get a SpyFlusher for the given queue in the given ThreadGroup
	 */
	public SpyLogFlusher(String name, ThreadGroup t) {
		super(t, "SpyLogFlusher");
		setDaemon(true);
		setName("SpyLogFlusher-" + name);
		this.queueName=name;
		// Exception e=new Exception("Instantiated SpyLogFlusher-" + name);
		// e.printStackTrace();
		configure();
	}

	/**
	 * Set the sleep time between flushes.
	 */
	protected void setSleepTime(long sleepTime) {
		this.sleepTime=sleepTime;
	}

	/**
	 * Get the current sleep time.
	 */
	public long getSleepTime() {
		return(sleepTime);
	}

	/**
	 * Get a string describing this thingy.
	 */
	public String toString() {
		StringBuffer sb=new StringBuffer(super.toString());
		sb.append(" - ");
		sb.append(queueSize());
		sb.append(" items queued");
		if(lastRun!=null) {
			sb.append(", last run:  ");
			sb.append(lastRun);
		}
		if(lastError!=null) {
			sb.append(", last error:  ");
			sb.append(lastError);
			sb.append(" - at - ");
			sb.append(lastErrorTime);
		}
		return(sb.toString());
	}

	/**
	 * Do additional configuration stuff here.
	 */
	protected void configure() {
	}

	/**
	 * Return the current queue of things to be logged
	 */
	protected Vector flush() {
		return(logQueue.flush());
	}

	/**
	 * Get the current size of the queue.
	 */
	public int queueSize() {
		return(logQueue.size());
	}

	/**
	 * This method should writes the log entries to their final
	 * destination.  The default implementation writes the log entries to a
	 * file.  This method should probably be overridden to be useful.
	 */
	protected void doFlush() throws Exception {
		Vector v = flush();
		// Only do all this crap if there's something to log.
		if(v.size() > 0) {
			// The logfile is only open long enough for us to write our log
			// entries to it.
			BufferedWriter logFile=logFile=new BufferedWriter(
				new FileWriter(logfile, true));
			for(Enumeration e=v.elements(); e.hasMoreElements(); ) {
				SpyLogEntry l = (SpyLogEntry)e.nextElement();
				logFile.write(l.toString() + "\n");
			}
			logFile.flush();
			logFile.close(); // Close it, we're done!
		}
	}

	/**
	 * Stop was taken and deprecated by those fools at Javasoft.  I use
	 * close here because it's kinda the right thing to do, as we're
	 * closing the log...sorta.
	 */
	public void close() {
		keepGoing=false;
	}

	/**
	 * Periodically process the log queue.  You probably don't want to
	 * override this.
	 */
	public void run() {
		logQueue = new SpyLogQueue(queueName);

		while(keepGoing) {
			try {
				// Flush first, ask questions later.
				doFlush();
				lastRun=new Date();

				// Wait for something to get added...with timeout
				logQueue.waitForQueue(sleepTime);

			} catch(Exception e) {
				lastError=e;
				lastErrorTime=new Date();
				getLogger().error("Problem flushing logs", e);
			}
		}
	}

	protected void finalize() throws Throwable {
		doFlush();
		super.finalize();
	}
}
