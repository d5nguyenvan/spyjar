// Copyright (c) 2003  Dustin Sallings <dustin@spy.net>
//
// $Id: SavableHashMap.java,v 1.2 2003/09/04 07:18:25 dustin Exp $

package net.spy.db.savables;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import net.spy.db.SaveException;
import net.spy.db.SavableNode;
import net.spy.db.SaveContext;

/**
 * A subclass of HashMap that implements SavableNode.
 *
 * The save() method does nothing (and should not be called), but all of
 * the values in the Map will be returned from getPostSavables().
 *
 * @author <a href="mailto:dsallings@2wire.com">Dustin Sallings</a>
 */
public class SavableHashMap extends HashMap implements SavableNode {

    /**
     * Get an instance of SavableHashMap.
     */
    public SavableHashMap() {
        super();
    }

	/** 
	 * Get an instance of SavableHashMap populated with the given
	 * Map of objects.
	 */
	public SavableHashMap(Map map) {
		super(map);
	}

	/** 
	 * Get an instance of SavableHashMap with the given initial capacity.
	 */
	public SavableHashMap(int initCap) {
		super(initCap);
	}

	/** 
	 * Get an instance of SavableHashMap with the given initial capacity
	 * and load factors.
	 */
	public SavableHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	// Savable implementation

	/** 
	 * @return false
	 */
	public boolean isNew() {
		return(false);
	}

	/** 
	 * @return false
	 */
	public boolean isModified() {
		return(false);
	}

	/** 
	 * Do nothing.
	 */
	public void save(Connection conn, SaveContext context)
		throws SaveException, SQLException {

		// Ignored
	}

	/** 
	 * @return null
	 */
	public Collection getPreSavables(SaveContext context) {
		return(null);
	}

	/** 
	 * @return values()
	 */
	public Collection getPostSavables(SaveContext context) {
		return(values());
	}

	/** 
	 * @return values()
	 */
	public Collection getSavables(SaveContext context) {
		return(values());
	}
}
