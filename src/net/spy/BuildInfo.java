// Copyright (c) 2001  Dustin Sallings <dustin@spy.net>
//
// $Id: BuildInfo.java,v 1.2 2003/08/05 09:01:02 dustin Exp $

package net.spy;

import java.net.URL;
import java.util.Properties;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import net.spy.util.NestedRuntimeException;

/**
 * Information regarding this spy.jar build.
 *
 * The following properties will be set at build time:
 *
 * <ul>
 *  <li>build.number - Number of this build</li>
 *  <li>build.date - Date of this build (yyyy/MM/dd HH:mm)</li>
 *  <li>java.vendor - Java vendor who made the VM that built this jar</li>
 *  <li>java.version - Java version of the the VM that built this jar</li>
 *  <li>os.name - Name of the OS on which this jar was built</li>
 *  <li>os.version - Version of the OS on which this jar was built</li>
 * </ul>
 */
public class BuildInfo extends Properties {

	/**
	 * Get an instance of BuildInfo that describes the spy.jar build.
	 */
	public BuildInfo() throws IOException {
		this("net/spy/build.properties");
	}

	/**
	 * Get an instance of BuildInfo that describes the build info found in
	 * the given resource.
	 */
	protected BuildInfo(String resource) throws IOException {
		super();
		// Grab the build properties
		ClassLoader cl=getClass().getClassLoader();
		InputStream is=cl.getResourceAsStream(resource);
		if(is==null) {
			throw new IOException("No resources found for " + resource);
		}
		load(is);
		is.close();
	}

	/**
	 * Get the date of this build.
	 */
	public Date getBuildDate() {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd HH:mm");
		Date rv=null;

		try {
			rv=sdf.parse(getProperty("build.date"));
		} catch(ParseException pe) {
			throw new NestedRuntimeException(
				"Invalid date from build properties file", pe);
		}
		return(rv);
	}

	/** 
	 * Get a URL to a file within this classloader.
	 * 
	 * @param rel the relative name (i.e. net.spy.changelog)
	 * @return the URL
	 * @throws FileNotFoundException if the file cannot be found
	 */
	public URL getFile(String rel) throws FileNotFoundException {
		ClassLoader cl=getClass().getClassLoader();
		URL u=cl.getResource(rel);
		if(u == null) {
			throw new FileNotFoundException("Can't find " + rel);
		}
		return(u);
	}

	/**
	 * String me.
	 */
	public String toString() {
		StringBuffer sb=new StringBuffer(256);

		sb.append("build ");
		sb.append(getProperty("build.number"));
		sb.append(" on ");
		sb.append(getBuildDate());
		sb.append("\nBuild platform:  java ");
		sb.append(getProperty("java.version"));
		sb.append(" from ");
		sb.append(getProperty("java.vendor"));
		sb.append(" on ");
		sb.append(getProperty("os.name"));
		sb.append(" version ");
		sb.append(getProperty("os.version"));
		if(getProperty("tree.version") != null) {
			sb.append("\nTree version:  ");
			sb.append(getProperty("tree.version"));
		}

		return(sb.toString());
	}

	/**
	 * Print out the build properties.
	 */
	public static void main(String args[]) throws Exception {
		BuildInfo bi=new BuildInfo();

		System.out.println("spy.jar " + bi);

		if(args.length > 0 && args[0].equals("-c")) {
			System.out.println(" -- Changelog:\n");

			URL u=bi.getFile("net/spy/changelog.txt");
			InputStream is=u.openStream();
			byte data[]=new byte[8192];
			int bread=0;
			do {
				bread=is.read(data);
				if(bread > 0) {
					System.out.write(data, 0, bread);
				}
			} while(bread != -1);
			is.close();
		} else {
			System.out.println("(add -c to see the recent changelog)");
		}
	}

}
