// Copyright (c) 2000  Dustin Sallings
//
// $Id: PackageInfo.java,v 1.1 2002/08/28 00:34:56 dustin Exp $

package net.spy.info;

/**
 * Abstract class for Info classes that describe shipping information.
 */
public abstract class PackageInfo extends Info {

	private boolean delivered=false;

	/**
	 * True if the package has been delivered.
	 */
	public boolean isDelivered() {
		return(delivered);
	}

	/**
	 * Set the delivered status.
	 */
	protected void setDelivered() {
		delivered=true;
	}
}
