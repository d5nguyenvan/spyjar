// Copyright (c) 2001  Dustin Sallings <dustin@spy.net>
//
// arch-tag: 0F8BBA2B-1110-11D9-BBB3-000A957659CC

package net.spy.test;

import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.spy.util.Digest;
import net.spy.util.PwGen;

/**
 * Test the digest imlementation and password generator.
 */
public class DigestTest extends TestCase {

	/**
	 * Get an instance of DigestTest.
	 */
	public DigestTest(String name) {
		super(name);
	}

	/** 
	 * Get the test suite.
	 * 
	 * @return this test
	 */
	public static Test suite() {
		return new TestSuite(DigestTest.class);
	}

	/** 
	 * Run this test.
	 */
	public static void main(String args[]) {
		junit.textui.TestRunner.run(suite());
	}

	/** 
	 * A basic test of the password generator.  Ensure the password
	 * generator won't generate the same password if called several times.
	 */
	public void testPasswordGenerator() {
		HashSet words=new HashSet();

		for(int i=0; i<1000; i++) {
			String pw=PwGen.getPass(8);
			assertTrue("Generated a duplicate password on attempt " + i,
				(!words.contains(pw)));
			words.add(pw);
		}
	}

	/** 
	 * Test the password hashing.  Do a couple rounds of passwords and make
	 * sure the hashing consistently works.
	 */
	public void testPasswordHash() {
		Digest d=new Digest();

		for(int i=0; i<10; i++) {
			String pw=PwGen.getPass(8);
			String hpw=d.getHash(pw);
			assertTrue("Password checking failed", d.checkPassword(pw, hpw));
		}
	}

	/** 
	 * Test salt-free hashes.
	 */
	public void testSaltFree() throws Exception {
		Digest d=new Digest();
		assertEquals("{SHA}qUqP5cyxm6YcTAhz05Hph5gvu9M=",
			d.getSaltFreeHash("test"));
		d.prefixHash(false);
		assertEquals("qUqP5cyxm6YcTAhz05Hph5gvu9M=", d.getSaltFreeHash("test"));
	}

}
