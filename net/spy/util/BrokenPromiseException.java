// Copyright (c) 2001  Dustin Sallings <dustin@spy.net>
//
// $Id: BrokenPromiseException.java,v 1.1 2002/08/28 00:34:56 dustin Exp $

package net.spy.util;

/**
 * Report on a broken promise.
 */
public class BrokenPromiseException extends NestedException {

	/**
	 * Get an instance of BrokenPromiseException with a message.
	 */
	public BrokenPromiseException(String msg) {
		super(msg);
	}

	/**
	 * Get an instance of BrokenPromiseException with a message and a root
	 * cause Throwable.
	 */
	public BrokenPromiseException(String msg, Throwable t) {
		super(msg, t);
	}

}

