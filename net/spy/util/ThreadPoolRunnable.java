// Copyright (c) 2002  Dustin Sallings <dustin@spy.net>
//
// $Id: ThreadPoolRunnable.java,v 1.3 2003/04/12 00:59:50 dustin Exp $

package net.spy.util;

/**
 * Interface that flags a class as having overridden toString() for debug
 * display in a thread list.
 *
 * <p>
 *  Runnables not implementing this interface will be displayed by their
 *  class name only.
 * </p>
 */
public interface ThreadPoolRunnable extends Runnable {

	/** 
	 * Subclasses must override toString() to produce an informative debug
	 * string.  It's a good idea to have this also include the class name
	 * to be a bit more informative as the class name will not be displayed
	 * along with it.  This is optional, however.
	 */
	String toString();

}
