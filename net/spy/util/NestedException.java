// Copyright (c) 2001  Dustin Sallings <dustin@spy.net>
//
// $Id: NestedException.java,v 1.2 2003/01/14 18:11:34 dustin Exp $

package net.spy.util;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * An Exception that will allow chaining of another Throwable.
 */
public class NestedException extends Exception {

	private Throwable root=null;

	/**
	 * Get an instance of NestedException with a given message.
	 */
	public NestedException(String msg) {
		super(msg);
	}

	/**
	 * Get a NestedException with a given message and root cause throwable.
	 */
	public NestedException(String msg, Throwable t) {
		super(msg);
		root=t;
	}

	/**
	 * Get the root cause of this problem.
	 */
	public Throwable getRootCause() {
		return(root);
	}

	/**
	 * Get the root cause of this problem.
	 */
	public Throwable getCause() {
		return(root);
	}

	/**
	 * Set the root cause of this problem.
	 */
	protected void setRootCause(Throwable root) {
		this.root=root;
	}

	/**
	 * String me.
	 */
	public String toString() {
		String rv=null;

		if(root==null) {
			rv=super.toString();
		} else {
			rv=super.toString() + " because of " + root.toString();
		}

		return(rv);
	}

	/**
	 * Print the stack and the root stack (if any).
	 */
	public void printStackTrace(PrintStream s) {
		super.printStackTrace(s);
		if(root!=null) {
			s.println("*** Root Cause Stack:");
			root.printStackTrace(s);
		}
	}

	/**
	 * Print the stack and the root stack (if any).
	 */
	public void printStackTrace(PrintWriter s) {
		super.printStackTrace(s);
		if(root!=null) {
			s.println("*** Root Cause Stack:");
			root.printStackTrace(s);
		}
	}

	/**
	 * Print the stack and the root stack (if any).
	 */
	public void printStackTrace() {
		super.printStackTrace();
		if(root!=null) {
			System.err.println("*** Root Cause Stack:");
			root.printStackTrace();
		}
	}

}
