// Copyright (c) 2002  Dustin Sallings <dustin@spy.net>
//
// $Id: SunLogger.java,v 1.1 2002/11/05 04:59:09 dustin Exp $

package net.spy.log;

import java.util.logging.Level;

/**
 * Logging implementation using the sun logger.
 */
public class SunLogger extends AbstractLogger {

	// Can't really import this without confusion as there's another thing
	// by this name in here.
	java.util.logging.Logger sunLogger=null;

	/**
	 * Get an instance of SunLogger.
	 */
	public SunLogger(String name) {
		super(name);

		// Get the sun logger instance.
		sunLogger=java.util.logging.Logger.getLogger(name);
	}

	/** 
	 * True if the underlying logger would allow Level.FINE through.
	 */
	public boolean isDebugEnabled() {
		return(sunLogger.isLoggable(Level.FINE));
	}

	/** 
	 * True if the underlying logger would allow Level.INFO through.
	 */
	public boolean isInfoEnabled() {
		return(sunLogger.isLoggable(Level.INFO));
	}

	/** 
	 * Wrapper around sun logger.
	 * 
	 * @param level net.spy.log.AbstractLogger level.
	 * @param message object message
	 * @param e optional throwable
	 */
	protected void logAt(int level, Object message, Throwable e) {
		Level sLevel=null;
		switch(level) {
			case DEBUG:
				sLevel=Level.FINE;
				break;
			case INFO:
				sLevel=Level.INFO;
				break;
			case WARN:
				sLevel=Level.WARNING;
				break;
			case ERROR:
				sLevel=Level.SEVERE;
				break;
			case FATAL:
				sLevel=Level.SEVERE;
				break;
		}
		if(e != null) {
			sunLogger.log(sLevel, message.toString(), e);
		} else {
			sunLogger.log(sLevel, message.toString());
		}
	}

}
