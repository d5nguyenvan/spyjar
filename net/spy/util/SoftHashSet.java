// Copyright (c) 2001  Dustin Sallings <dustin@spy.net>
//
// $Id: SoftHashSet.java,v 1.1 2002/10/16 07:20:26 dustin Exp $

package net.spy.util;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

import java.util.Collection;

/**
 * Implementation of ReferenceSet that uses soft references.
 */
public class SoftHashSet extends ReferenceSet {

	/**
	 * Get an instance of SoftHashSet.
	 */
	public SoftHashSet() {
		super();
	}

	/** 
	 * Create a SoftHashSet with the given capacity.
	 * 
	 * @param n the capacity
	 */
	public SoftHashSet(int n) {
		super(n);
	}

	/** 
	 * Get a SoftHashSet with the contents from the given Collection.
	 * 
	 * @param c the collection
	 */
	public SoftHashSet(Collection c) {
		super(c);
	}

	/** 
	 * Return a soft reference.
	 */
	protected Reference getReference(Object o) {
		return(new MySoftReference(o));
	}

	private class MySoftReference extends SoftReference {

		public MySoftReference(Object o) {
			super(o);
		}

		public int hashCode() {
			int rv=0;
			Object o=get();
			if (o != null) {
				rv=o.hashCode();
			}

			return (rv);
		}

		public boolean equals(Object o) {
			boolean rv=false;
			Object me=get();
			if(me!=null) {
				rv=me.equals(o);
			}

			return(rv);
		}

	}

}