// Copyright (c) 2005 Dustin Sallings <dustin@spy.net>
// arch-tag: D792B0CB-8492-4035-9BDE-8124BDE7EF09

package net.spy.test;

import junit.framework.TestCase;

/**
 * Test some various assumptions about java.
 */
public class AssumptionTest extends TestCase {

	public void testStringBuilderDeleteChar() {
		StringBuilder sb=new StringBuilder("blah.");
		sb.deleteCharAt(sb.length()-1);
		assertEquals("blah", sb.toString());
	}

	public void testStringBuilderDelete() {
		StringBuilder sb=new StringBuilder("blah..");
		sb.delete(sb.length()-2, sb.length());
		assertEquals("blah", sb.toString());
	}
}