// Copyright (c) 2000  Dustin Sallings <dustin@spy.net>
//
// $Id: SpyDoubleComparable.java,v 1.1 2002/08/28 00:34:57 dustin Exp $

package net.spy.util;


/**
 * Compare Doubles for SpySort.
 */
public class SpyDoubleComparable extends Object implements SpyComparable {
	public int compare(Object obj1, Object obj2) {
		Double o1=null, o2=null;
		int ret=0;

		o1=(Double)obj1;
		o2=(Double)obj2;

		double d1=o1.doubleValue();
		double d2=o2.doubleValue();

		if(d1 == d2) {
			ret=0;
		} else {
			if(d1 < d2) {
				ret=-1;
			} else {
				ret=1;
			}
		}

		return(ret);
	}
}
