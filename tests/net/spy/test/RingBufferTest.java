// Copyright (c) 2001  Dustin Sallings <dustin@spy.net>
//
// arch-tag: 272DF956-1110-11D9-B0CD-000A957659CC

package net.spy.test;

import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import net.spy.util.RingBuffer;

/**
 * Test the ring buffer functionality.
 */
public class RingBufferTest extends TestCase {

	/**
	 * Get an instance of RingBufferTest.
	 */
	public RingBufferTest(String name) {
		super(name);
	}

	/**
	 * Get the test suite.
	 */
	public static Test suite() {
		return new TestSuite(RingBufferTest.class);
	}

	/**
	 * Run this test.
	 */
	public static void main(String args[]) {
		junit.textui.TestRunner.run(suite());
	}

	private void verify(RingBuffer<Integer> rb) {
		ArrayList<Integer> a=new ArrayList(rb);

		// The buffer should be at capacity here
		assertTrue("Size is incorrect.", (rb.size() == a.size()));
		assertTrue("Capacity not filled.", (rb.size() == rb.getCapacity()));

		int i=((Integer)a.get(0)).intValue();
		for(int tmp : a) {
			assertEquals("Out of sequence", tmp, i);
			i++;
		}
	}

	/** 
	 * Basic RingBuffer test.
	 */
	public void testRingBuffer() {
		int cap=256;
		RingBuffer<Integer> rb=new RingBuffer(cap);

		// Fill 'er up
		for(int i=1; i<cap; i++) {
			rb.add(i);
			assertTrue("Capacity filled prematurely", rb.size() < cap);
		}

		for(int i=cap; i<2048; i++) {
			rb.add(i);
			assertTrue("Exceeded capacity", rb.size() <= cap);
			verify(rb);
		}
	}

}
