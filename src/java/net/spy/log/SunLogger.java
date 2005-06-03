// Copyright (c) 2002  Dustin Sallings <dustin@spy.net>
//
// arch-tag: CCEE8C42-1110-11D9-ADD2-000A957659CC

package net.spy.log;

/**
 * Logging implementation using the sun logger.
 */
public class SunLogger extends AbstractLogger {

	// Can't really import this without confusion as there's another thing
	// by this name in here.
	private java.util.logging.Logger sunLogger=null;

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
		return(sunLogger.isLoggable(java.util.logging.Level.FINE));
	}

	/** 
	 * True if the underlying logger would allow Level.INFO through.
	 */
	public boolean isInfoEnabled() {
		return(sunLogger.isLoggable(java.util.logging.Level.INFO));
	}

	/** 
	 * Wrapper around sun logger.
	 * 
	 * @param level net.spy.log.AbstractLogger level.
	 * @param message object message
	 * @param e optional throwable
	 */
	public void log(Level level, Object message, Throwable e) {
		java.util.logging.Level sLevel=java.util.logging.Level.SEVERE;

		if(level==level.DEBUG) {
			sLevel=java.util.logging.Level.FINE;
		} else if(level==Level.INFO) {
			sLevel=java.util.logging.Level.INFO;
		} else if(level==Level.WARN) {
			sLevel=java.util.logging.Level.WARNING;
		} else if(level==Level.ERROR) {
			sLevel=java.util.logging.Level.SEVERE;
		} else if(level==Level.FATAL) {
			sLevel=java.util.logging.Level.SEVERE;
		} else {
			// XXX:  Need to do something here.
		}

		// Figure out who was logging.
		Throwable t=new Throwable();
		StackTraceElement ste[]=t.getStackTrace();
		StackTraceElement logRequestor=null;
		String alclass=AbstractLogger.class.getName();
		for(int i=0; i<ste.length && logRequestor==null; i++) {
			if(ste[i].getClassName().equals(alclass)) {
				// Make sure there's another stack frame.
				if(i+1<ste.length) {
					logRequestor=ste[i+1];
					if(logRequestor.getClassName().equals(alclass)) {
						logRequestor=null;
					} // Also AbstractLogger
				} // Found something that wasn't abstract logger
			} // check for abstract logger
		}

		// See if we could figure out who was doing the original logging,
		// if we could, we want to include a useful class and method name
		if(logRequestor!=null) {
			if(e != null) {
				sunLogger.logp(sLevel, logRequestor.getClassName(),
					logRequestor.getMethodName(), message.toString(), e);
			} else {
				sunLogger.logp(sLevel, logRequestor.getClassName(),
					logRequestor.getMethodName(), message.toString());
			}
		} else {
			if(e != null) {
				sunLogger.log(sLevel, message.toString(), e);
			} else {
				sunLogger.log(sLevel, message.toString());
			}
		}
	}

}