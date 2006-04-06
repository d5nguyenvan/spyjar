/*
 * Copyright (c) 2000 Dustin Sallings <dustin@spy.net>
 *
 * arch-tag: 6EA3CD36-1110-11D9-B5A1-000A957659CC
 */

package net.spy.db;

import java.lang.ref.SoftReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.spy.cache.SpyCache;
import net.spy.util.SpyConfig;

/**
 * Extensions to DB that allow for result set caching.  <b>Use wisely!</b>
 */

public class SpyCacheDB extends SpyDB {

	/**
	 * Get a SpyCacheDB object as specified in the passed in config file.
	 *
	 * @see SpyConfig
	 *
	 * @exception SQLException never, but it might someday.
	 */
	public SpyCacheDB(SpyConfig conf) throws SQLException {
		super(conf);
	}

	/**
	 * Get a SpyCacheDB object that will use the given Connection object
	 * for any needed queries.
	 */
	public SpyCacheDB(Connection conn) {
		super(conn);
	}

	/**
	 * Execute if we don't have valid cache.
	 *
	 * @param query Query to execute
	 * @param lifetime How long (in seconds) the results can live
	 * 
	 * @exception SQLException when bad stuff happens
	 */
	public ResultSet executeQuery(String query, long lifetime)
		throws SQLException {

		SpyCache cache=SpyCache.getInstance();
		String key="cachedb_" + query;
		CachedResultSet crs=(CachedResultSet)cache.get(key);
		if(crs==null) {
			ResultSet rs=executeQuery(query);
			crs=new CachedResultSet(rs);
			cache.store(key, new SoftReference<CachedResultSet>(crs),
					lifetime*1000);
		}

		ResultSet crsret=(ResultSet)crs.newCopy();
		return(crsret);
	}

	/**
	 * Prepare a statment for caching.
	 *
	 * @param query Query to prepare
	 * @param lifetime How long (in seconds) the results can live
	 *
	 * @exception SQLException when bad stuff happens
	 */
	public PreparedStatement prepareStatement(String query, long lifetime)
		throws SQLException {

		return(new CachePreparedStatement(this, query, lifetime));
	}
}
