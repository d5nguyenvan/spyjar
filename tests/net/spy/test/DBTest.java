// Copyright (c) 2001  Dustin Sallings <dustin@spy.net>
//
// $Id: DBTest.java,v 1.2 2002/08/28 06:41:10 dustin Exp $

package net.spy.test;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import java.math.BigDecimal;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import net.spy.SpyConfig;
import net.spy.db.SpyCacheDB;

import net.spy.test.db.DumpTestTable;
import net.spy.test.db.GetTestByNumber;
import net.spy.test.db.CallTestFunc;

/**
 * Test various DB functionality.
 */
public class DBTest extends TestCase {

	private SpyConfig conf=null;

	/**
	 * Get an instance of DBTest.
	 */
	public DBTest(String name) {
		super(name);
		conf=new SpyConfig(new java.io.File("test.conf"));
	}

	/**
	 * Get this test suite.
	 */
	public static Test suite() {
		return new TestSuite(DBTest.class);
	}

	/**
	 * Run this test.
	 */
	public static void main(String args[]) {
		junit.textui.TestRunner.run(suite());
	}

	private void checkRow(ResultSet rs) throws SQLException {
		int id=rs.getInt("id");
		String test_vc=rs.getString("test_vc");
		Date d=rs.getDate("test_d");
		Time t=rs.getTime("test_t");
		Timestamp ts=rs.getTimestamp("test_ts");
		BigDecimal n=rs.getBigDecimal("test_n");
		int i=rs.getInt("test_i");
		boolean iWasNull=rs.wasNull();
		float test_f=rs.getFloat("test_f");
		boolean fWasNull=rs.wasNull();
		boolean test_b=rs.getBoolean("test_b");
		boolean bWasNull=rs.wasNull();

		if(test_vc.equals("full1") || test_vc.equals("full2")) {
			assertNotNull(d);
			assertNotNull(t);
			assertNotNull(ts);
			assertNotNull(n);
			assertEquals(iWasNull, false);
			assertEquals(fWasNull, false);
			assertEquals(bWasNull, false);
		} else if(test_vc.equals("nulldate")) {
			assertNull(d);
			assertNotNull(t);
			assertNotNull(ts);
			assertNotNull(n);
			assertEquals(iWasNull, false);
			assertEquals(fWasNull, false);
			assertEquals(bWasNull, false);
		} else if(test_vc.equals("nullbool")) {
			assertNotNull(d);
			assertNotNull(t);
			assertNotNull(ts);
			assertNotNull(n);
			assertEquals(iWasNull, false);
			assertEquals(fWasNull, false);
			assertEquals(bWasNull, true);
		} else if(test_vc.equals("nullnums")) {
			assertNotNull(d);
			assertNotNull(t);
			assertNotNull(ts);
			assertNotNull(n);
			assertEquals(iWasNull, true);
			assertEquals(fWasNull, true);
			assertEquals(bWasNull, false);
		} else {
			fail("Unexpected result type:  " + test_vc);
		}
	}

	/**
	 * Test a regular DB thingy.
	 */
	public void testDBNoCache() throws SQLException {
		SpyCacheDB db=new SpyCacheDB(conf);

		ResultSet rs=db.executeQuery("select * from testtable");
		assertTrue(! (rs instanceof net.spy.db.CachedResultSet));
		while(rs.next()) {
			checkRow(rs);
		}
		rs.close();
		db.close();
	}

	/**
	 * Test a cached query.
	 */
	public void testDBCache() throws SQLException {
		SpyCacheDB db=new SpyCacheDB(conf);

		ResultSet rs=db.executeQuery("select * from testtable", 30);
		assertTrue(rs instanceof net.spy.db.CachedResultSet);
		while(rs.next()) {
			checkRow(rs);
		}
		rs.close();
		db.close();
	}

	/**
	 * Test an SPT with no cache.
	 */
	public void testSPTNoCache() throws SQLException {
		DumpTestTable db=new DumpTestTable(conf);
		ResultSet rs=db.executeQuery();
		assertTrue(! (rs instanceof net.spy.db.CachedResultSet));
		while(rs.next()) {
			checkRow(rs);
		}
		rs.close();
		db.close();
	}

	/**
	 * Test an SPT with cache.
	 */
	public void testSPTWithCache() throws SQLException {
		DumpTestTable db=new DumpTestTable(conf);
		db.setCacheTime(30);
		ResultSet rs=db.executeQuery();
		assertTrue(rs instanceof net.spy.db.CachedResultSet);
		while(rs.next()) {
			checkRow(rs);
		}
		rs.close();
		db.close();
	}

	/** 
	 * Test an SPT with numeric parameters.
	 */
	public void testSPTNoCacheWithNumberParam() throws SQLException {
		GetTestByNumber db=new GetTestByNumber(conf);
		db.set("test_n", new BigDecimal(1234567));
		ResultSet rs=db.executeQuery();
		assertTrue(! (rs instanceof net.spy.db.CachedResultSet));
		int nrows=0;
		while(rs.next()) {
			nrows++;
			checkRow(rs);
		}
		rs.close();
		db.close();

		assertEquals("Incorrect number of rows returned for number match",
			nrows, 2);
	}

	/** 
	 * Test an SPT with numeric parameters.
	 */
	public void testSPTNoCacheWithCoercedNumberParam() throws SQLException {
		GetTestByNumber db=new GetTestByNumber(conf);
		db.setCoerced("test_n", "1234567");
		ResultSet rs=db.executeQuery();
		assertTrue(! (rs instanceof net.spy.db.CachedResultSet));
		int nrows=0;
		while(rs.next()) {
			nrows++;
			checkRow(rs);
		}
		rs.close();
		db.close();

		assertEquals("Incorrect number of rows returned for number match",
			nrows, 2);
	}

	/** 
	 * Test DBCP functionality.
	 */
	public void testDBCP() throws SQLException {
		CallTestFunc gtf=new CallTestFunc(conf);
		gtf.set("num", 5);
		ResultSet rs=gtf.executeQuery();
		assertTrue("Too few results", rs.next());
		int rv=rs.getInt(1);
		assertEquals(rv, 6);
		assertTrue("Too many results", (!rs.next()));
		rs.close();
		gtf.close();
	}
	
}
