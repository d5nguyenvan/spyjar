// Copyright (c) 2002  Dustin Sallings <dustin@spy.net>
//
// arch-tag: 3FF58B8A-1110-11D9-9F8C-000A957659CC

package net.spy;

import net.spy.log.Logger;
import net.spy.log.LoggerFactory;

/**
 * Superclass for all Spy Objects.
 */
public class SpyObject extends Object {

	private Logger logger=null;

	/**
	 * Get an instance of SpyObject.
	 */
	public SpyObject() {
		super();
	}

	/** 
	 * Get a Logger instance for this class.
	 * 
	 * @return an appropriate logger instance.
	 */
	protected Logger getLogger() {
		if(logger==null) {
			logger=LoggerFactory.getLogger(getClass());
		}
		return(logger);
	}

}
