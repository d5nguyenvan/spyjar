// Copyright (c) 2001  Dustin Sallings <dustin@spy.net>
//
// arch-tag: 8CA51A09-1110-11D9-8085-000A957659CC

package net.spy.util;

import java.util.Date;

/**
 * Creates a representable timespan.
 */
public class TimeSpan extends Object {

	private long days=0;
	private long hours=0;
	private long minutes=0;
	private long seconds=0;
	private long mseconds=0;

	/**
	 * Get an instance of TimeSpan representing the span between the given
	 * from and to dates.
	 */
	public TimeSpan(Date from, Date to) {
		super();

		long diff=Math.abs(to.getTime()-from.getTime());

		/*
		if(diff<=0) {
			future=false;
		}
		*/

		// Days
		if(diff>86400000L) {
			days=diff/86400000L;
			diff=diff%86400000L;
		}

		// Hours
		if(diff>3600000L) {
			hours=diff/3600000L;
			diff=diff%3600000L;
		}

		// Minutes
		if(diff>60000L) {
			minutes=diff/60000L;
			diff=diff%60000L;
		}

		// Seconds
		if(diff>1000L) {
			seconds=diff/1000L;
			diff=diff%1000L;
		}

		mseconds=diff;
	}

	// Provide a two-digit number as a String.
	private String zeroNumber(long in) {
		StringBuffer sb=new StringBuffer(2);
		if(in<10) {
			sb.append("0");
		}
		sb.append(in);
		return(sb.toString());
	}

	/**
	 * Print out the timespan.
	 */
	public String toString() {
		StringBuffer sb=new StringBuffer(64);
		boolean hasDays=false;

		if(days>0) {
			hasDays=true;
			sb.append(days);
			sb.append(days>1?" days":" day");
		}

		if(hours>0 || minutes>0 || seconds>0 || mseconds>0) {
			if(hasDays) {
				sb.append(" ");
			}
			sb.append(zeroNumber(hours));
			sb.append(":");
			sb.append(zeroNumber(minutes));
			sb.append(":");
			sb.append(zeroNumber(seconds));
			sb.append(".");
			sb.append(mseconds);
		}

		return(sb.toString());
	}

	/**
	 * Testing and what not.
	 */
	public static void main(String args[]) throws Exception {
		java.text.SimpleDateFormat sdf=new java.text.SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
		TimeSpan ts=new TimeSpan(sdf.parse(args[0]), sdf.parse(args[1]));
		System.out.println(ts);
	}

}
