// Copyright (c) 2001  Dustin Sallings <dustin@spy.net>
//
// $Id: SPGen.java,v 1.10 2002/09/04 02:02:13 dustin Exp $

package net.spy.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.HashSet;
import java.util.Date;

import java.lang.reflect.Field;

import java.text.NumberFormat;

import net.spy.SpyUtil;

/**
 * Generator for .spt-&gt;.java.
 */
public class SPGen extends Object {

	private BufferedReader in=null;
	private PrintWriter out=null;
	private String classname=null;

	private String section="";
	private String description="";
	private String procname="";
	private String pkg="";
	private String superclass="DBSP";
	private String version="$Revision: 1.10 $";
	private long cachetime=0;
	private ArrayList sqlquery=null;
	private ArrayList required=null;
	private ArrayList optional=null;
	private ArrayList output=null;
	private ArrayList results=null;
	private ArrayList args=null;
	private boolean debug=false;

	private static HashSet types=null;

	private boolean looseTypes=false;

	/**
	 * Get a new SPGen from the given BufferedReader.
	 */
	public SPGen(String classname, BufferedReader in, PrintWriter out) {
		super();
		this.in=in;
		this.out=out;
		this.classname=classname;
		sqlquery=new ArrayList();
		required=new ArrayList();
		optional=new ArrayList();
		output=new ArrayList();
		results=new ArrayList();
		args=new ArrayList();

		if(types==null) {
			initTypes();
		}
	}

	private static synchronized void initTypes() {
		if(types==null) {
			Field fields[]=java.sql.Types.class.getDeclaredFields();
			types=new HashSet();

			for(int i=0; i<fields.length; i++) {
				types.add(fields[i].getName());
			}
		}
	}

	/** 
	 * Return true if this is a valid JDBC type.
	 * 
	 * @param name the name of the field to test
	 * @return true if the field is valid
	 */
	public static boolean isValidJDBCType(String name) {
		return(types.contains(name));
	}

	/** 
	 * Perform the actual generation.
	 * 
	 * @throws Exception if there's a problem parsing or writing
	 */
	public void generate() throws Exception {
		parse();
		write();
	}

	// Make a pretty string out of the cache time for the documentation.
	private String formatCacheTime() {
		long nowT=System.currentTimeMillis();
		long thenT=nowT-(cachetime * 1000);
		Date now=new Date(nowT);
		Date then=new Date(thenT);
		TimeSpan ts=new TimeSpan(now, then);
		return(ts.toString());
	}

	// Create a methodable name (i.e. blah returns Blah so you can make
	// getBlah.
	private String methodify(String word) {
		StringBuffer sb=new StringBuffer(word.length());

		StringTokenizer st=new StringTokenizer(word, "_");
		while(st.hasMoreTokens()) {
			String part=st.nextToken();
			StringBuffer mntmp=new StringBuffer(part);
			char c=Character.toUpperCase(mntmp.charAt(0));
			mntmp.setCharAt(0, c);

			sb.append(mntmp.toString());
		}

		return(sb.toString());
	}

	// Create a specific set method for a given parameter.
	private String createSetMethod(Parameter p) throws Exception {
		String rv=null;
		String type=null;

		// TODO:  Replace this with an extensible Map.
		if(p.getType().equals("java.sql.Types.BIT")) {
			type="boolean";
		} else if(p.getType().equals("java.sql.Types.DATE")) {
			type="java.sql.Date";
		} else if(p.getType().equals("java.sql.Types.DOUBLE")) {
			type="double";
		} else if(p.getType().equals("java.sql.Types.FLOAT")) {
			type="float";
		} else if(p.getType().equals("java.sql.Types.INTEGER")) {
			type="int";
		} else if(p.getType().equals("java.sql.Types.BIGINT")) {
			type="long";
		} else if(p.getType().equals("java.sql.Types.NUMERIC")) {
			type="java.math.BigDecimal";
		} else if(p.getType().equals("java.sql.Types.DECIMAL")) {
			type="java.math.BigDecimal";
		} else if(p.getType().equals("java.sql.Types.SMALLINT")) {
			type="int";
		} else if(p.getType().equals("java.sql.Types.TINYINT")) {
			type="short";
		} else if(p.getType().equals("java.sql.Types.OTHER")) {
			type="java.lang.Object";
		} else if(p.getType().equals("java.sql.Types.VARCHAR")) {
			type="java.lang.String";
		} else if(p.getType().equals("java.sql.Types.TIME")) {
			type="java.sql.Time";
		} else if(p.getType().equals("java.sql.Types.TIMESTAMP")) {
			type="java.sql.Timestamp";
		} else {
			throw new Exception("Whoops, type " + p.getType() 
				+ " seems to have been overlooked.");
		}

		String methodName=methodify(p.getName());

		rv="\t/**\n"
			+ "\t * Set the ``" + p.getName() + "'' parameter.\n"
			+ "\t * " + p.getDescription() + "\n"
			+ "\t *\n"
			+ "\t * @param to the value to which to set the parameter\n"
			+ "\t */\n"
			+ "\tpublic void set" + methodName + "(" + type + " to)\n"
			+ "\t\tthrows SQLException {\n\n"
			+ "\t\tset(\"" + p.getName() + "\", to);\n"
			+ "\t}\n";
	
		return(rv);
	}

	private void write() throws Exception {
		System.out.println("Writing out " + pkg + "." + classname);
		// Copyright info
		out.println(
			"// Copyright (c) 2001  SPY internetworking <dustin@spy.net>\n"
		    + "// Written by Dustin's SQL generator version " + version +"\n"
			+ "//\n"
			+ "// $" + "Id" + "$\n");
		out.flush();

		// Package info
		out.println("package " + pkg + ";\n");

		// Imports
		out.println("import java.sql.Types;\n"
			+ "import java.sql.Connection;\n"
			+ "import java.sql.SQLException;\n"
			+ "import net.spy.db." + superclass + ";\n"
			+ "import net.spy.SpyConfig;\n");

		// Generate the documentation.
		out.println("/**\n"
			+ " * \n"
			+ " * " + description + "\n"
			+ " *\n"
			+ " * <p>\n"
			+ " *\n"
			+ " * Generated by SPGen " + version + " on "
				+ new java.util.Date() + "\n"
			+ " *\n"
			+ " * </p>\n"
			+ " * <p>\n"
			+ " *");

		// Debug mode.
		if(debug) {
			out.println(" * <font color=\"red\" size=\"+2\"><b>"
				+ "Debug is on!</b></font>\n"
				+ " *\n"
				+ " * </p>\n"
				+ " *\n"
				+ " * <p>\n"
				+ " *");
		}

		// Different stuff for different classes
		if(superclass.equals("DBSP")) {
			out.println(" * <b>Procedure Name</b>\n"
				+ " *\n"
				+ " * <ul>\n"
				+ " *  <li>" + procname + "</li>\n"
				+ " * </ul>");
		} else if (superclass.equals("DBCP")) {
			out.println(" * <b>Callable Name</b>\n"
				+ " *\n"
				+ " * <ul>\n"
				+ " *  <li>" + procname + "</li>\n"
				+ " * </ul>");
		} else {
			out.println(" * <b>SQL Query</b>\n"
				+ " *\n"
				+ " * <ul>\n"
				+ " " + getDocQuery() + "\n"
				+ " * </ul>");
		}

		// Required parameters
		out.println(" *\n"
			+ " * <b>Required Parameters</b>\n"
			+ " * <ul>");
		if(required.size()==0) {
			out.println(" *  <li><i>none</i></li>");
		} else {
			for(Iterator i=required.iterator(); i.hasNext(); ) {
				Parameter p=(Parameter)i.next();
				out.println(" * <li>" + p.getName() + " - "
					+ "{@link java.sql.Types#" + p.getType() + " "
						+ p.getType() + "}\n * "
					+ " - " + p.getDescription() + "</li>");
			}
		}
		out.println(" * </ul>\n"
			+ " *\n"
			+ " * </p>\n"
			+ " * <p>\n"
			+ " *");

		// Optional parameters
		out.println(" *\n"
			+ " * <b>Optional Parameters</b>\n"
			+ " * <ul>");
		if(optional.size()==0) {
			out.println(" *  <li><i>none</i></li>");
		} else {
			for(Iterator i=optional.iterator(); i.hasNext(); ) {
				Parameter p=(Parameter)i.next();
				out.println(" * <li>" + p.getName() + " - "
					+ "{@link java.sql.Types#" + p.getType() + " "
						+ p.getType() + "}\n * "
					+ " - " + p.getDescription() + "</li>");
			}
		}
		out.println(" * </ul>\n"
			+ " *\n"
			+ " * </p>\n"
			+ " * <p>\n"
			+ " *");

		if (superclass.equals("DBCP")) {
			// Output parameters
			out.println(" *\n"
				+ " * <b>Output Parameters</b>\n"
				+ " * <ul>");
			if(output.size()==0) {
				out.println(" *  <li><i>none</i></li>");
			} else {
				for(Iterator i=output.iterator(); i.hasNext(); ) {
					Parameter p=(Parameter)i.next();
					out.println(" * <li>" + p.getName() + " - "
						+ "{@link java.sql.Types#" + p.getType() + " "
							+ p.getType() + "}\n * "
						+ " - " + p.getDescription() + "</li>");
				}
			}
			out.println(" * </ul>\n"
				+ " *\n"
				+ " * </p>\n"
				+ " * <p>\n"
				+ " *");
		}

		// Results
		if(results.size() > 0) {
			out.println(" * <b>Results</b>\n"
				+ " * <ul>");

			for(Iterator i=results.iterator(); i.hasNext(); ) {
				Result r=(Result)i.next();

				out.print(" *  <li>"
					+ r.getName() + " - ");
				if(isValidJDBCType(r.getType())) {
					out.print("{@link java.sql.Types#" + r.getType() + " "
						+ r.getType() + "}\n *   ");
				} else {
					out.print(r.getType());
				}
				out.println(" - " + r.getDescription() + "</li>");
			}

			out.println(" * </ul>\n"
				+ " *");
		}

		// Document the cache time
		out.println(" * <b>Cache Time</b>\n"
			+ " * <ul>");
		if(cachetime > 0) {
			NumberFormat nf=NumberFormat.getNumberInstance();
			out.println(" *  <li>The results of this call will be cached for "
				+ formatCacheTime() 
				+ " (" + nf.format(cachetime) + " seconds) by default.</li>");
		} else {
			out.println(" *  <li>The results of this call will not "
				+ "be cached by default.</li>");
		}
		out.println(" * </ul>");

		// end the class documentation comment
		out.println(" * </p>\n"
			+ " */");

		// Actual code generation
		out.println("public class " + classname + " extends "
		+ superclass + " {\n");

		// Constructor documentation
		out.println("\t/**\n"
			+ "\t * Construct a DBSP which will get its connections from\n"
			+ "\t *   SpyDB using the given config.\n"
			+ "\t * @param conf the configuration to use\n"
			+ "\t * @exception SQLException if there's a failure to "
			+ "construct\n"
			+ "\t */");

		// SpyConfig constructor
		out.println("\tpublic " + classname + "(SpyConfig conf) "
			+ "throws SQLException {\n"
			+ "\t\t// Super constructor\n"
			+ "\t\tsuper(conf);\n"
			+ "\t\tspinit();\n"
			+ "\t}\n");

		// Constructor documentation
		out.println("\t/**\n"
			+ "\t * Construct a DBSP which use the existing Connection\n"
			+ "\t *   for database operations.\n"
			+ "\t * @param conn the connection to use\n"
			+ "\t * @exception SQLException if there's a failure to "
			+ "construct\n"
			+ "\t */");

		// Connection constructor
		out.println("\tpublic " + classname + "(Connection conn) "
			+ "throws SQLException {\n"
			+ "\t\t// Super constructor\n"
			+ "\t\tsuper(conn);\n"
			+ "\t\tspinit();\n"
			+ "\t}\n");

		// Initializer
		out.println("\tprivate void spinit() throws SQLException {");

		// Set the debug mode if we're supposed to
		if(debug) {
			out.println("\t\t// Debug is on for this DBSP\n"
				+ "\t\tsetDebug(true);\n");
		}

		// Figure out whether we're a DBSP or a DBSQL
		if(superclass.equals("DBSP") || superclass.equals("DBCP")) {
			out.println("\t\t// Set the stored procedure name\n"
				+ "\t\tsetSPName(\"" + procname + "\");");
		} else {
			out.println("\t\t// Set the SQL\n"
				+ "\t\t" + getJavaQuery()
				+ "\n\t\tsetQuery(query.toString());");
		}

		if (args.size()>0) {
			out.println("\n\t\t// Set the parameters.");
			for (Iterator i=args.iterator(); i.hasNext(); ) {
				Parameter p=(Parameter)i.next();
				if (p.isRequired()) {
					if (!p.isOutput()) {
						out.println("\t\tsetRequired(\"" + p.getName() + "\", "
							+ p.getType() + ");");
					} else {
						out.println("\t\tsetOutput(\"" + p.getName() + "\", "
							+ p.getType() + ");");
					}
				} else {
					out.println("\t\tsetOptional(\"" + p.getName() + "\", "
						+ p.getType() + ");");
				}
			}
		}

		// Set the cachetime, if there is one
		if(cachetime>0) {
			out.println("\n\t\t// Set the default cache time.");
			out.println("\t\tsetCacheTime(" + cachetime + ");");
		}

		out.println("\t}\n");

		// Create set methods for all the individual parameters
		for(Iterator i=args.iterator(); i.hasNext(); ) {
			Parameter p=(Parameter)i.next();

			out.println(createSetMethod(p));
		}

		out.println("}");

	}

	private String getDocQuery() {
		StringBuffer sb=new StringBuffer(1024);

		sb.append(" * <pre>\n");
		for(Iterator i=sqlquery.iterator(); i.hasNext(); ) {
			String part=(String)i.next();
			sb.append(" * ");
			sb.append(part);
			sb.append("\n");
		}
		sb.append(" * </pre>\n");

		return(sb.toString().trim());
	}

	private String getJavaQuery() {
		StringBuffer sb=new StringBuffer(1024);

		sb.append("\n\t\tStringBuffer query=new StringBuffer(1024);");

		for(Iterator i=sqlquery.iterator(); i.hasNext(); ) {
			String part=(String)i.next();
			sb.append("\n\t\tquery.append(\"");

			for(StringTokenizer st=new StringTokenizer(part, "\"", true);
				st.hasMoreTokens(); ) {

				String tmp=st.nextToken();
				if(tmp.equals("\"")) {
					tmp="\\\"";
				}
				sb.append(tmp);
			}

			sb.append("\\n\");");
		}

		return(sb.toString().trim());
	}

	private void parse() throws Exception {

		System.out.println("Parsing " + classname + ".spt");

		String tmp=in.readLine();
		while(tmp!=null) {

			// Don't do anything if the line is empty
			if(tmp.length() > 0) {
				if(tmp.charAt(0) == '@') {
					section=tmp.substring(1);
					// System.out.println("Working on section " + section);

					// Handlers for things that occur when a section is begun
					if(section.equals("debug")) {
						debug=true;
					} else if (section.startsWith("loosetyp")) {
						looseTypes=true;
					}
				} else if(tmp.charAt(0) == '#') {
					// Comment, ignore
				} else {

					if(section.equals("description")) {
						description+=tmp;
					} else if(section.equals("debug")) {
						System.err.println("Warning, stuff in debug section:  "
							+ tmp);
					} else if(section.equals("sql")) {
						sqlquery.add(tmp);
						superclass="DBSQL";
					} else if(section.equals("procname")) {
						procname+=tmp;
						superclass="DBSP";
					} else if(section.equals("callable")) {
						procname+=tmp;
						superclass="DBCP";
					} else if(section.equals("params")) {
						Parameter param=new Parameter(tmp);
						args.add(param);
						if(param.isRequired()) {
							if (!param.isOutput()) {
								required.add(param);
							} else {
								output.add(param);
							}
						} else {
							optional.add(param);
						}
					} else if(section.equals("results")) {
						try {
							results.add(new Result(tmp));
						} catch(IllegalArgumentException e) {
							System.err.println("Warning in " + classname
								+ ":  " + e.getMessage());
						}
					} else if(section.equals("package")) {
						pkg+=tmp;
					} else if(section.equals("cachetime")) {
						cachetime=Long.parseLong(tmp);
					} else {
						throw new Exception("Unknown section:  " + section);
					}

				}
			}
			
			tmp=in.readLine();
		}
	}

	// Private class for results

	private class Result extends Object {
		private String name=null;
		private String type=null;
		private String description=null;

		public Result(String line) {
			super();

			StringTokenizer st=new StringTokenizer(line, " \t");
			try {
				name=st.nextToken();
			} catch(NoSuchElementException e) {
				throw new IllegalArgumentException("No name given for result");
			}

			try {
				type=st.nextToken();

				if(!isValidJDBCType(type)) {
					System.err.println("Warning! Invalid JDBC type found:  "
						+ type);
				}
			} catch(NoSuchElementException e) {
				throw new IllegalArgumentException(
					"No type given for result ``" + name + "''");
			}

			try {
				description=st.nextToken("\n");
			} catch(NoSuchElementException e) {
				throw new IllegalArgumentException(
					"No description given for result ``" + name + "''");
			}
		}

		public String getName() {
			return(name);
		}

		public String getType() {
			return(type);
		}

		public String getDescription() {
			return(description);
		}

	}

	// Private class for parameters

	private class Parameter extends Object {
		private String name=null;
		private boolean required=false;
		private String type=null;
		private String description=null;
		private boolean output=false;

		public Parameter(String line) {
			super();

			StringTokenizer st=new StringTokenizer(line, " \t");
			try {
				name=st.nextToken();
			} catch (NoSuchElementException ex) {
				// ASSERT: this theoretically should never happen,
				// otherwise how did we end up here in the first place?
				throw new IllegalArgumentException("Missing parameter name! "
					+ex.toString());
			}

			String tmp=null;
			try {
				tmp=st.nextToken();
			} catch (NoSuchElementException ex) {
				// at this point you have forgotten to add in the parameter
				// of whether or not the parameter is required/optional.  I
				// guess a default could be applied here, but I'm just
				// gonna throw an Exception.
				throw new IllegalArgumentException(
					"Missing parameter requirement! " +ex.toString());
			}

			if(tmp.equals("required")) {
				required=true;
				output=false;
			} else if(tmp.equals("optional")) {
				required=false;
				output=false;
			} else if(tmp.equals("output")) {
				required=true;
				output=true;
			} else {
				throw new IllegalArgumentException(
					"Parameter must be required or optional, not "
					+ tmp + " like in " + line);
			}

			try {
				type=st.nextToken();

				if(isValidJDBCType(type)) {
					type="java.sql.Types."+type;
				} else {
					if (!looseTypes) {
						throw new IllegalArgumentException("Invalid JDBC type: "
							+ type);
					}
				}
			} catch (NoSuchElementException ex) {
				// now the variable type is missing  That's no good, you
				// need a speficic type ya know.
				throw new IllegalArgumentException("Missing parameter type! "
					+ex.toString());
			}

			try {
				// This character pretty much can't be in the line.
				description=st.nextToken("\n");
			} catch (NoSuchElementException ex) {
				// I don't think we cre if it's documented or not!  But
				// honestly I don't think this should ever happen cause you
				// need a newline.  Well, I guess if you ended the file odd
				// enough, and without a EOL before the EOF...very odd case
				description="";
			}
		}

		public String getName() {
			return(name);
		}

		public String getType() {
			return(type);
		}

		public String getDescription() {
			return(description);
		}

		public boolean isRequired() {
			return(required);
		}

		public boolean isOutput() {
			return(output);
		}
	}

	/**
	 * Usage:  SPGen filename
	 */
	public static void main(String args[]) throws Exception {

		String infile=args[0];
		// Get rid of the .spt
		int lastslash=infile.lastIndexOf(File.separatorChar);
		String basename=infile.substring(0, infile.indexOf(".spt"));
		// If it matches, start at the next character, if it didn't, it's
		// -1 and start at 0
		String classname=basename.substring(lastslash+1);
		String outfile=basename + ".java";

		BufferedReader in=new BufferedReader(new FileReader(infile));
		PrintWriter out=new PrintWriter(new FileWriter(outfile));
		SPGen spg=new SPGen(classname, in, out);

		spg.generate();

		in.close();
		out.close();
	}

}
