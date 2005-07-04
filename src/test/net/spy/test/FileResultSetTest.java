// Copyright (c) 2005 Dustin Sallings <dustin@spy.net>
// arch-tag: 9A534445-7167-4349-A6F8-9D25875525DC

package net.spy.test;

import java.io.File;
import java.util.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import net.spy.db.FileResultSet;

import junit.framework.TestCase;

public class FileResultSetTest extends TestCase {

	private void assertColumn(ResultSet rs, int val, String col, int colInt)
		throws Exception {
		assertEquals(val, rs.getInt(col));
		assertEquals(val, rs.getInt(colInt));
	}

	private void assertColumn(ResultSet rs, String val, String col, int colInt)
		throws Exception {
		assertEquals(val, rs.getString(col));
		assertEquals(val, rs.getString(colInt));
	}

	private void assertColumn(ResultSet rs, boolean val, String col, int colInt)
		throws Exception {
		assertEquals(val, rs.getBoolean(col));
		assertEquals(val, rs.getBoolean(colInt));
	}

	private void assertTimestampColumn(
		ResultSet rs, String val, String col, int colInt) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
		Date parsedDate = new java.sql.Timestamp(sdf.parse(val).getTime());
		assertEquals(parsedDate, rs.getTimestamp(col));
		assertEquals(parsedDate, rs.getTimestamp(colInt));
	}

	private void assertTimeColumn(
		ResultSet rs, String val, String col, int colInt) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		java.sql.Time parsedTime = new java.sql.Time(sdf.parse(val).getTime());
		assertEquals(parsedTime, rs.getTime(col));
		assertEquals(parsedTime, rs.getTime(colInt));
	}

	private void assertDateColumn(
		ResultSet rs, String val, String col, int colInt) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		java.sql.Date parsedDate = new java.sql.Date(sdf.parse(val).getTime());
		assertEquals(parsedDate, rs.getDate(col));
		assertEquals(parsedDate, rs.getDate(colInt));
	}

	public void testResultSet1() throws Exception {
		String path = System.getProperty("basedir")
			+ "/src/test/net/spy/test/db/resulttest.txt";
		FileResultSet rs = new FileResultSet(new File(path));
		
		try {
			String s=rs.getString("col_two");
			fail("Shouldn't be able to get a column before rs.next(), got " + s);
		} catch(SQLException e) {
			assertEquals("No current result.", e.getMessage());
		}

		assertTrue(rs.next());
		try {
			String s=rs.getString("nonexistent");
			fail("Should not be able to request nonexistent column, got " + s);
		} catch(SQLException e) {
			assertEquals("No such column:  nonexistent", e.getMessage());
		}
		try {
			String s=rs.getString(7);
			fail("Should not be able to request not existent column, got " + s);
		} catch(SQLException e) {
			assertEquals("There are only 6 columns in this set.", e.getMessage());
		}
		assertColumn(rs, 28, "col_one", 1);
		assertColumn(rs, "I'll be twenty-eight.", "col_two", 2);
		assertDateColumn(rs, "20051005", "col_three", 3);
		assertTimeColumn(rs, "12:15:27", "col_four", 4);
		assertTimestampColumn(rs, "20051005T12:15:27", "col_five", 5);
		assertColumn(rs, true, "col_six", 6);

		assertTrue(rs.next());
		assertColumn(rs, 10, "col_one", 1);
		assertColumn(rs, "Jennalynn will be ten.", "col_two", 2);
		assertDateColumn(rs, "20050710", "col_three", 3);
		assertTimeColumn(rs, "03:18:05", "col_four", 4);
		assertTimestampColumn(rs, "20050710T03:18:05", "col_five", 5);
		assertColumn(rs, false, "col_six", 6);
		assertFalse(rs.wasNull());

		assertTrue(rs.next());
		assertColumn(rs, -5, "col_one", 1);
		assertColumn(rs, "This has a\ttab and\nnewline and a \\N", "col_two", 2);
		assertDateColumn(rs, "19771005", "col_three", 3);
		assertTimeColumn(rs, "00:00:00", "col_four", 4);
		assertTimestampColumn(rs, "19771005T00:00:00", "col_five", 5);
		assertColumn(rs, false, "col_six", 6);
		assertTrue(rs.wasNull());
		
		assertFalse(rs.next());
	}

	public void testBadResultSet1() {
		File f = new File("/tmp/nonExistentPath.txt");
		try {
			FileResultSet rs = new FileResultSet(f);
			fail("Got a result set from a non-existent file:  "
				+ rs);
		} catch(SQLException e) {
			assertEquals("Could not initialize results from "
				+ f, e.getMessage());
		}
	}

}