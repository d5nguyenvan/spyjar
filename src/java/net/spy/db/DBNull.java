// Copyright (c) 2001  SPY internetworking <dustin@spy.net>
//
// arch-tag: 66DD0326-1110-11D9-9229-000A957659CC

package net.spy.db;

/**
 * Represents NULL data in DB parameters and stuff.
 */
public class DBNull extends Object {
	private int type=-1;

	/**
	 * Get a new null object.
	 */
	public DBNull(int t) {
		super();
		this.type=t;
	}

	/**
	 * Get the data type of this nullness.
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * Get the hashcode for this object.
	 */
	public int hashCode() {
		return(type);
	}
	
	/**
	 *  String me.
	 */
	public String toString() {
		return("{DBNull type=" + TypeNames.getTypeName(type) + "}");
	}
	
	/**
	 * True if o is a DBNull of the same type.
	 */
	public boolean equals(Object o) {
		boolean rv=false;
		if(o instanceof DBNull) {
			DBNull n=(DBNull)o;
			rv= (type == n.getType());
		}
		return(rv);
	}
}


