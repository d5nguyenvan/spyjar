/*
 * Copyright (c) 2000  Dustin Sallings <dustin@spy.net>
 *
 * $Id: CachePreparedStatementStub.java,v 1.4 2003/04/02 05:55:02 dustin Exp $
 */

package net.spy.db;

import java.math.BigDecimal;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import net.spy.SpyDB;
import net.spy.SpyObject;

import net.spy.cache.SpyCache;

/**
 * Prepared statement for executing cached queries
 */
public class CachePreparedStatementStub extends SpyObject {

	// Stored DB handle
	private SpyDB db=null;

	// Where we store the prepared query
	private String queryStr=null;

	// Where we can put arguments.
	private Object args[]=null;
	// What are our argument types
	private int types[]=null;

	// How long the results of this statement should be cached
	private long cacheTime=60*60*1000;

	// Query timeout
	private int timeout=0;

	/**
	 * Create a CachePreparedStatement object for the given query (you
	 * probably don't want to do this directly).
	 */
	public CachePreparedStatementStub(SpyDB db, String query, long cacheTime) {
		super();
		this.db=db;
		this.queryStr=query;
		this.cacheTime=cacheTime;

		// Figure out how many arguments may be used.
		int ntokens=countQs(query);
		args=new Object[ntokens];
		types=new int[ntokens];
	}

	// Count the number of question marks
	private int countQs(String query) {
		int qs=0;
		int currentIndex=-1;

		do {
			currentIndex=query.indexOf("?", currentIndex+1);
			if(currentIndex>=0) {
				qs++;
			}
		} while(currentIndex>=0);

		return(qs);
	}

	// Set a given argument
	private void setArg(int index, Object what, int type)
		throws SQLException {
		// Our base is 0, JDBC base is 1
		index--;

		if(index<0) {
			throw new SQLException("Illegal index, they start at 1, G");
		}
		if(index>=args.length) {
			throw new SQLException("Illegal index, this statement takes a "
				+ "maximum of " + args.length + " arguments.");
		}

		// Set the vars
		args[index]=what;
		types[index]=type;
	}

	/**
	 * Get an integer hash code to uniquely identify this object.
	 */
	public int hashCode() {
		int hc=0;

		hc+=queryStr.hashCode();
		StringBuffer sb=new StringBuffer(256);
		for(int i=0; i<args.length; i++) {
			try {
				sb.append(args[i]);
				sb.append((char)0x00);
			} catch(Exception e) {
				// Ignore it, we'll get close.
			}
		}
		// Hashcode of all of the args run together.
		hc+=sb.toString().hashCode();

		return(hc);
	}

	// Implemented
	public ResultSet executeQuery() 
		throws SQLException {

		int hc=hashCode();
		String key="dbcache_prepared_" + hc;
		SpyCache cache=SpyCache.getInstance();
		CachedResultSet crs=(CachedResultSet)cache.get(key);
		if(crs==null) {
			crs=realExecuteQuery();
			cache.store(key, crs, cacheTime*1000);
		}
		ResultSet crsret=(ResultSet)crs.newCopy();
		return(crsret);
	}

	/** 
	 * Set the query timeout.
	 * 
	 * @param to the maximum number of seconds
	 * @throws SQLException if the number is not greater than or equal to 0
	 */
	public void setQueryTimeout(int to) throws SQLException {
		timeout=to;
	}

	// OK, here's what happens when we determine that we really don't have
	// the data and need to come up with it.
	private CachedResultSet realExecuteQuery() throws SQLException {
		PreparedStatement pst=db.prepareStatement(queryStr);
		pst.setQueryTimeout(timeout);

		// Set allllllll the types
		for(int i=0; i<args.length; i++) {
			try {
				switch(types[i]) {
					case Types.BIT:
						pst.setBoolean(i+1, ((Boolean)args[i]).booleanValue());
						break;
					case Types.DATE:
						pst.setDate(i+1, (Date)args[i]);
						break;
					case Types.DOUBLE:
						pst.setDouble(i+1, ((Double)args[i]).doubleValue());
						break;
					case Types.FLOAT:
						pst.setFloat(i+1, ((Float)args[i]).floatValue());
						break;
					case Types.INTEGER:
						pst.setInt(i+1, ((Integer)args[i]).intValue());
						break;
					case Types.BIGINT:
						pst.setLong(i+1, ((Long)args[i]).longValue());
						break;
					case Types.TINYINT:
						pst.setShort(i+1, (short)((Integer)args[i]).intValue());
						break;
					case Types.NULL:
						pst.setNull(i+1, ((Integer)args[i]).intValue());
						break;
					case Types.OTHER:
						pst.setObject(i+1, args[i]);
						break;
					case Types.VARCHAR:
						pst.setString(i+1, (String)args[i]);
						break;
					case Types.TIME:
						pst.setTime(i+1, (Time)args[i]);
						break;
					case Types.TIMESTAMP:
						pst.setTimestamp(i+1, (Timestamp)args[i]);
						break;
					case Types.DECIMAL:
						pst.setBigDecimal(i+1, (BigDecimal)args[i]);
						break;
					default:
						throw new SQLException("Whoops, type "
							+ types[i] + " (" + TypeNames.getTypeName(types[i])
							+ ") seems to have been overlooked.");
				}
			} catch (NullPointerException ex) {
				getLogger().error("error with "+args[i]+" in type "+
					types[i]+" at param postition "+i);
				throw ex;
			}
		}

		// OK, at this point, all the arguments should be set, proceed to
		// execute the query.
		return(new CachedResultSet(pst.executeQuery()));

	}

	// Implemented (sorta)
	public int executeUpdate() 
		throws SQLException {
		throw new SQLException("Illegal?  This operation makes no sense!");
	}

	// Implemented
	public void setBoolean(int a0,boolean a1) 
		throws SQLException {
		setArg(a0, new Boolean(a1), Types.BIT);
	}

	// Implemented
	public void setDate(int a0,Date a1) 
		throws SQLException {
		setArg(a0, a1, Types.DATE);
	}

	// Implemented
	public void setDouble(int a0,double a1) 
		throws SQLException {
		setArg(a0, new Double(a1), Types.DOUBLE);
	}

	// Implemented
	public void setFloat(int a0,float a1) 
		throws SQLException {
		setArg(a0, new Float(a1), Types.FLOAT);
	}

	// Implemented
	public void setInt(int a0,int a1) 
		throws SQLException {
		setArg(a0, new Integer(a1), Types.INTEGER);
	}

	// Implemented
	public void setLong(int a0,long a1) 
		throws SQLException {
		setArg(a0, new Long(a1), Types.BIGINT);
	}

	// Implemented
	public void setNull(int a0,int a1) 
		throws SQLException {
		// This one works a bit different because we have to store the
		// original type
		setArg(a0, new Integer(a1), Types.NULL);
	}

	// Implemented
	public void setBigDecimal(int a0, BigDecimal a1) throws SQLException {
		setArg(a0, a1, Types.DECIMAL);
	}

	// Implemented
	public void setObject(int a0,java.lang.Object a1) 
		throws SQLException {
		setArg(a0, a1, Types.OTHER);
	}

	// Implemented
	public void setShort(int a0,short a1) 
		throws SQLException {
		setArg(a0, new Integer(a1), Types.TINYINT);
	}

	// Implemented
	public void setString(int a0,java.lang.String a1) 
		throws SQLException {
		setArg(a0, a1, Types.VARCHAR);
	}

	// Implemented
	public void setTime(int a0,Time a1) 
		throws SQLException {
		setArg(a0, a1, Types.TIME);
	}

	// Implemented
	public void close() throws SQLException {
		db=null;
		queryStr=null;
		args=null;
		types=null;
	}
}
