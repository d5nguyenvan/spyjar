// Copyright (c) 2001  Dustin Sallings <dustin@spy.net>
//
// arch-tag: 853B71B6-1110-11D9-B415-000A957659CC

package net.spy.util;

/**
 * A promise, continuation style code for java.
 */
public abstract class Promise extends Object {

	private Object rv=null;
	private boolean hasRun=false;
	private BrokenPromiseException bpe=null;

	/**
	 * Get an instance of Promise.
	 */
	public Promise() {
		super();
	}

	/**
	 * Get the object.
	 *
	 * @return the Object we were promised.
	 *
	 * @exception BrokenPromiseException if there's a problem getting what
	 * we were promised.
	 */
	public final Object getObject() throws BrokenPromiseException {
		if(hasRun == false) {
			try {
				rv=execute();
			} catch(BrokenPromiseException bpe) {
				this.bpe=bpe;
				throw bpe;
			} finally {
				hasRun=true;
			}
		}

		// If there was a broken promise, toss it.
		if(bpe!=null) {
			throw bpe;
		}

		return(rv);
	}

	/**
	 * Print me.
	 */
	public String toString() {
		String rvs=null;

		if(hasRun) {
			if(rv!=null) {
				rvs="Promise {" + rv.toString() + "}";
			} else {
				if(bpe!=null) {
					rvs="Broken Promise {" + bpe.toString() + "}";
				} else {
					rvs="Promise {null}";
				}
			}
		} else {
			rvs="Promise {not yet executed}";
		}

		return(rvs);
	}

	/**
	 * Do the actual work required to get the Object we're promised.
	 */
	protected abstract Object execute() throws BrokenPromiseException;

}

