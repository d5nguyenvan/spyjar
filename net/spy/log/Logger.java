// Copyright (c) 2002  SPY internetworking <dustin@spy.net>
//
// $Id: Logger.java,v 1.1 2002/11/04 21:13:57 dustin Exp $

package net.spy.log;

/**
 * Abstract mechanism for dealing with logs from various objects.
 *
 * @see LoggerFactory
 */
public interface Logger {

	/** 
	 * Get the name of this logger.
	 */
	String getName();

	/** 
	 * Is debug enabled for this logger?
	 * 
	 * @return true if debug messages would be displayed
	 */
	boolean isDebugEnabled();

	/** 
	 * Is info enabled for this logger?
	 * 
	 * @return true if info messages would be displayed
	 */
	boolean isInfoEnabled();

	/** 
	 * Log a message at debug level.
	 * 
	 * @param message the message to log
	 * @param exception the exception that caused the message to be generated
	 */
	void debug(Object message, Throwable exception);
	/** 
	 * Log a message at debug level.
	 * 
	 * @param message the message to log
	 */
	void debug(Object message);

	/** 
	 * Log a message at info level.
	 * 
	 * @param message the message to log
	 * @param exception the exception that caused the message to be generated
	 */
	void info(Object message, Throwable exception);
	/** 
	 * Log a message at info level.
	 * 
	 * @param message the message to log
	 */
	void info(Object message);

	/** 
	 * Log a message at warning level.
	 * 
	 * @param message the message to log
	 * @param exception the exception that caused the message to be generated
	 */
	void warn(Object message, Throwable exception);
	/** 
	 * Log a message at warning level.
	 * 
	 * @param message the message to log
	 */
	void warn(Object message);

	/** 
	 * Log a message at error level.
	 * 
	 * @param message the message to log
	 * @param exception the exception that caused the message to be generated
	 */
	void error(Object message, Throwable exception);
	/** 
	 * Log a message at error level.
	 * 
	 * @param message the message to log
	 */
	void error(Object message);

	/** 
	 * Log a message at fatal level.
	 * 
	 * @param message the message to log
	 * @param exception the exception that caused the message to be generated
	 */
	void fatal(Object message, Throwable exception);

	/** 
	 * Log a message at fatal level.
	 * 
	 * @param message the message to log
	 */
	void fatal(Object message);

}