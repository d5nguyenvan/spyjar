// Copyright (c) 2000  Dustin Sallings <dustin@spy.net>
//
// arch-tag: 79627946-1110-11D9-8090-000A957659CC

package net.spy.nmap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * NMAP log parser.  After creating an nmap machine parseable log, you can
 * parse the log like this:
 * <p>
 * <pre>
 * NmapParser np = new NmapParser("/path/to/log/file");
 *
 * for(Enumeration e = p.entries(); e.hasMoreElements(); ) {
 * 	NmapEntry ne = (NmapEntry)e.nextElement();
 * 	System.out.println(ne);
 * }
 * </pre>
 * <p>
 * NOTE:  That's not a very useful example, please read the rest of the
 * documentation and play around before complaining.
 */

public class NmapParser extends Object {
	private String inputFile=null;
	private Vector entriesV=null;
	private Hashtable entriesByIP=null;
	/**
	 * Construct an NmapParser object to parse a given text file.
	 *
	 * @param inputFile the path to a machine readable NMAP log file.
	 *
	 * @exception IOException if an error occurs while opening or reading
	 * the log file.
	 */
	public NmapParser(String inputFile) throws IOException {
		super();
		this.inputFile=inputFile;
		entriesV=new Vector();
		entriesByIP=new Hashtable();
		BufferedReader in=new BufferedReader(new FileReader(inputFile));
		String line=null;
		while( (line=in.readLine()) != null) {
			NmapEntry ne = new NmapEntry(line);
			entriesV.addElement(ne);
			entriesByIP.put(ne.getIP(), ne);
		}
	}

	/**
	 * gets an enumeration of all of the entries.
	 */
	public Enumeration entries() {
		return(entriesV.elements());
	}

	/**
	 * gets an NmapEntry for a specific IP address
	 *
	 * @param ip IP address to look up
	 *
	 * @return specified NmapEntry, or null if no such entry exists
	 */
	public NmapEntry entry(String ip) {
		return( (NmapEntry)entriesByIP.get(ip));
	}

	/**
	 * gets a list of NmapEntries for hosts listening on a given port.
	 *
	 * @param port port number we're looking for
	 */
	public Vector listeningOn(int port) {
		Vector ret=new Vector();

		// Flip through the entries, look at the ports.
		for(Enumeration e = entries(); e.hasMoreElements(); ) {
			NmapEntry ne=(NmapEntry)e.nextElement();
			if(ne.port(port)!=null) {
				ret.addElement(ne);
			}
		}

		return(ret);
	}

	public String toString() {
		return(this.inputFile);
	}

	public static void main(String args[]) throws Exception {
		NmapParser p = new NmapParser(args[0]);
		if(args.length>1) {
			// Look one up
			System.out.println("Requested specfic entry for " + args[1]);
			NmapEntry ne = p.entry(args[1]);
			System.out.println(ne);
		} else {
			// List them all
			for(Enumeration e = p.entries(); e.hasMoreElements(); ) {
				NmapEntry ne = (NmapEntry)e.nextElement();
				System.out.println(ne);
			}
		}
	}
}
