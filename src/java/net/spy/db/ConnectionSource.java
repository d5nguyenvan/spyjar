// Copyright (c) 2001  SPY internetworking <dustin@spy.net>

package net.spy.db;

import java.sql.Connection;
import java.sql.SQLException;

import net.spy.util.SpyConfig;

/**
 * Interface for classes that will create database connections.
 */
public interface ConnectionSource {

	/**
	 * Get a new connection.
	 *
	 * @param conf a SpyConfig describing the connection that needs to be
	 * fetched.
	 *
	 * @exception SQLException if a connection could not be obtained
	 */
	Connection getConnection(SpyConfig conf) throws SQLException;

	/**
	 * Return a connection.  This method is used to inform whatever pooling
	 * mechanism is in use that the connection is no longer wanted, or
	 * needed, or even cared about here.
	 */
	void returnConnection(Connection conn);

}
