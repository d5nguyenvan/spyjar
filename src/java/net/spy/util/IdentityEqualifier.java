// Copyright (c) 2003  Dustin Sallings <dustin@spy.net>

package net.spy.util;

/**
 * A wrapper object used to compare two objects on the basis of identity
 * rather than equality.
 */
public class IdentityEqualifier extends Object {

	private final Object object;

	/**
	 * Get an IdentityEqualifier wrapping the given object.
	 */
	public IdentityEqualifier(Object o) {
		super();
		this.object=o;
	}

	/**
	 * Get the object this IdentityEqualifier is wrapping.
	 *
	 * @return the object
	 */
	public Object get() {
		return(object);
	}

	/**
	 * True if this object has the same identity as the given object.
	 */
	@Override
	public boolean equals(Object other) {
		boolean rv=false;

		// If the other object is an IdentityEqualifier, see if its
		// contained object is the same, otherwise, see if this object is
		// the same.
		if(other instanceof IdentityEqualifier) {
			IdentityEqualifier ie=(IdentityEqualifier)other;
			rv = (object == ie.object);
		} else {
			rv = (object == other);
		}

		return(rv);
	}

	/**
	 * The hashcode of this object.
	 *
	 * @return System.identityHashCode for this object.
	 */
	@Override
	public int hashCode() {
		return(System.identityHashCode(object));
	}


}
