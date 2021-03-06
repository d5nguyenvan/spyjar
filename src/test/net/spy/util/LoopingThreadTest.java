// Copyright (c) 2006  Dustin Sallings <dustin@spy.net>

package net.spy.util;

import junit.framework.TestCase;

/**
 * Test the looping thread.
 */
public class LoopingThreadTest extends TestCase {

	public void testSimple() throws Exception {
		TestLoopingThread tlt=new TestLoopingThread();
		tlt.setMsPerLoop(10);
		assertFalse(tlt.started);
		tlt.start();
		Thread.sleep(100);
		tlt.requestStop();
		Thread.sleep(250);
		assertTrue("Didn't start?", tlt.started);
		assertTrue("Didn't finish?", tlt.shutdown);
		assertTrue("Didn't run enough", tlt.runs > 8);
		assertTrue("Ran too many times", tlt.runs < 12);
	}

	public void testNameConstructor() throws Exception {
		TestLoopingThread tlt=new TestLoopingThread("X");
		assertEquals("X", tlt.getName());
	}

	public void testThreadGroupConstructor() throws Exception {
		ThreadGroup tg=new ThreadGroup("Test");
		tg.setDaemon(true);
		TestLoopingThread tlt=new TestLoopingThread(tg, "Y");
		assertEquals("Y", tlt.getName());
		assertSame(tg, tlt.getThreadGroup());
		tg.destroy();
	}

	public void testInterruption() throws Exception {
		TestLoopingThread tlt=new TestLoopingThread();
		tlt.setMsPerLoop(10000);
		tlt.start();
		Thread.sleep(100);
		assertTrue(tlt.started);
		assertFalse(tlt.wasInterrupted);
		tlt.interrupt();
		Thread.sleep(100);
		assertTrue(tlt.wasInterrupted);
		tlt.requestStop();
		Thread.sleep(100);
		assertTrue(tlt.shutdown);
	}

	private static class TestLoopingThread extends LoopingThread {

		public volatile int runs=0;
		public volatile boolean started=false;
		public volatile boolean shutdown=false;
		public volatile boolean wasInterrupted=false;

		public TestLoopingThread() {
			super();
		}
		public TestLoopingThread(String name) {
			super(name);
		}
		public TestLoopingThread(ThreadGroup tg, String name) {
			super(tg, name);
		}

		@Override
		protected void runLoop() {
			runs++;
		}
		@Override
		protected void shuttingDown() {
			super.shuttingDown();
			shutdown=true;
		}
		@Override
		protected void startingUp() {
			super.startingUp();
			started=true;
		}
		@Override
		protected void delayInterrupted(InterruptedException e) {
			super.delayInterrupted(e);
			wasInterrupted=true;
		}

	}
}
