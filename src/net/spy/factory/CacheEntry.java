// Copyright (c) 2005  Dustin Sallings <dustin@spy.net>
// arch-tag: F6EEA6C5-5EE8-11D9-9AAA-000A957659CC

package net.spy.factory;

import java.util.Collection;

/**
 * Interface for factory cache implementations.
 */
public interface CacheEntry {

	/** 
	 * Cache the given instance.
	 */
	void cacheInstance(Instance i);

	/** 
	 * Get an object by id.
	 * @param id the object ID
	 * @return the object, or null if there's no object by this ID
	 */
	Object getById(int id);

	/** 
	 * Get all objects in this cache.
	 */
	Collection getAllObjects();

}