// Copyright (c) 2001  Dustin Sallings <dustin@spy.net>

package net.spy.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import net.spy.SpyObject;
import net.spy.db.QuerySelector;

/**
 * Generator for .spt-&gt;.java.
 */
public class SPGen extends SpyObject {

	private BufferedReader in=null;
	private PrintWriter out=null;
	private String classname=null;
	private boolean isInterface=true;
	private boolean wantsResultSet=false;
	private boolean wantsCursor=false;

	private String section="";
	private String description="";
	private String procname="";
	private String pkg="";

	private String superclass=null;
	private String dbcpSuperclass=null;
	private String dbspSuperclass=null;

	private String superinterface=null;
	private long cachetime=0;
	private Map<String, List<String>> queries=null;
	private String currentQuery=QuerySelector.DEFAULT_QUERY;
	private List<Result> results=null;
	List<Parameter> args=null;
	private Set<String> interfaces=null;
	private Set<String> imports=null;
	private int timeout=0;

	private static Set<String> types=null;
	static Map<String, String> javaTypes=null;
	static Map<String, String> javaResultTypes=null;

	boolean looseTypes=false;

	private boolean typeDbsp=false;
	private boolean typeDbcp=false;

	private boolean verbose=true;

	/**
	 * Get a new SPGen from the given BufferedReader.
	 *
	 * @param cn the name of the class to generate
	 * @param i the stream containing the spt source
	 * @param o the stream to which the java code will be written
	 */
	public SPGen(String cn, BufferedReader i, PrintWriter o) {
		super();
		this.in=i;
		this.out=o;
		this.classname=cn;
		queries=new TreeMap<String, List<String>>();
		results=new ArrayList<Result>();
		args=new ArrayList<Parameter>();
		interfaces=new HashSet<String>();
		imports=new TreeSet<String>();

		if(types==null) {
			initTypes();
		}
	}

	/**
	 * Set the verbosity flag.
	 */
	public void setVerbose(boolean to) {
		this.verbose=to;
	}

	/**
	 * Set the superclass of the generated java class.
	 */
	public void setSuperclass(String sc) {
		if (sc!=null) {
			this.superclass=sc;
		}
	}

	/**
	 * Add an interface to the implements line.
	 * @param intf the fully qualified name of the interface to implement
	 */
	public void addInterface(String intf) {
		interfaces.add(intf);
	}

	/**
	 * Add a collection of interfaces.
	 * @param set the set of interfaces
	 */
	public void addInterfaces(Collection<String> set) {
		interfaces.addAll(set);
	}

	/**
	 * Set the DBCP superclass of the generated java class.
	 */
	public void setDbcpSuperclass(String sc) {
		if (sc!=null) {
			this.dbcpSuperclass=sc;
		}
	}

	/**
	 * Set the DBSP superclass of the generated java class.
	 */
	public void setDbspSuperclass(String sc) {
		if (sc!=null) {
			this.dbspSuperclass=sc;
		}
	}

	private static synchronized void initTypes() {
		if(types==null) {
			javaTypes=new HashMap<String, String>();
			javaResultTypes=new HashMap<String, String>();

			String jstypes="java.sql.Types.";
			int jstypeslen=jstypes.length();

			// Map the jdbc types to useful java types
			String typ="java.sql.Types.BIT";
			javaTypes.put(typ, "Boolean");
			javaResultTypes.put(typ, "boolean");
			typ="java.sql.Types.DATE";
			javaTypes.put(typ, "Date");
			javaResultTypes.put(typ, "java.sql.Date");
			typ="java.sql.Types.DOUBLE";
			javaTypes.put(typ, "Double");
			javaResultTypes.put(typ, "double");
			typ="java.sql.Types.FLOAT";
			javaTypes.put(typ, "Float");
			javaResultTypes.put(typ, "float");
			typ="java.sql.Types.INTEGER";
			javaTypes.put(typ, "Int");
			javaResultTypes.put(typ, "int");
			typ="java.sql.Types.BIGINT";
			javaTypes.put(typ, "BigDecimal");
			javaResultTypes.put(typ, "java.math.BigDecimal");
			typ="java.sql.Types.NUMERIC";
			javaTypes.put(typ, "BigDecimal");
			javaResultTypes.put(typ, "java.math.BigDecimal");
			typ="java.sql.Types.DECIMAL";
			javaTypes.put(typ, "BigDecimal");
			javaResultTypes.put(typ, "java.math.BigDecimal");
			typ="java.sql.Types.SMALLINT";
			javaTypes.put(typ, "Int");
			javaResultTypes.put(typ, "int");
			typ="java.sql.Types.TINYINT";
			javaTypes.put(typ, "Int");
			javaResultTypes.put(typ, "int");
			typ="java.sql.Types.OTHER";
			javaTypes.put(typ, "Object");
			javaResultTypes.put(typ, "Object");
			typ="java.sql.Types.VARCHAR";
			javaTypes.put(typ, "String");
			javaResultTypes.put(typ, "String");
			typ="java.sql.Types.CLOB";
			javaTypes.put(typ, "String");
			javaResultTypes.put(typ, "String");
			typ="java.sql.Types.TIME";
			javaTypes.put(typ, "Time");
			javaResultTypes.put(typ, "java.sql.Time");
			typ="java.sql.Types.TIMESTAMP";
			javaTypes.put(typ, "Timestamp");
			javaResultTypes.put(typ, "java.sql.Timestamp");

			// Same as above, without the java.sql. part
			Map<String, String> tmp=new HashMap<String, String>();
			for(Map.Entry<String, String> me : javaTypes.entrySet()) {
				String k=me.getKey();
				if(k.startsWith(jstypes)) {
					tmp.put(k.substring(jstypeslen), me.getValue());
				}
			}
			javaTypes.putAll(tmp);
			tmp.clear();
			for(Map.Entry<String, String> me : javaResultTypes.entrySet()) {
				if(me.getKey().startsWith(jstypes)) {
					tmp.put(me.getKey().substring(jstypeslen), me.getValue());
				}
			}
			javaResultTypes.putAll(tmp);

			Field[] fields=java.sql.Types.class.getDeclaredFields();
			types=new HashSet<String>();

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
		StringBuilder sb=new StringBuilder(word.length());

		StringTokenizer st=new StringTokenizer(word, "_");
		while(st.hasMoreTokens()) {
			String part=st.nextToken();
			StringBuilder mntmp=new StringBuilder(part);
			char c=Character.toUpperCase(mntmp.charAt(0));
			mntmp.setCharAt(0, c);

			sb.append(mntmp.toString());
		}

		return(sb.toString());
	}

	// Create a specific set method for a given parameter.
	private String createSetMethod(Parameter p) throws Exception {
		String rv=null;
		String[] atypes=null;

		// Get the type map entry for this parameter
		try {
			ResourceBundle typeMap=
				ResourceBundle.getBundle("net.spy.db.typemap");
			String typeString=typeMap.getString(p.getType());
			atypes=SpyUtil.split(" ", typeString);
		} catch(MissingResourceException e) {
			getLogger().warn("Can't set all types for " + p, e);
			String[] typesTmp={"java.lang.Object"};
			atypes=typesTmp;
		}

		String methodName=methodify(p.getName());

		rv="";
		for(int i=0; i<atypes.length; i++) {
			String type=atypes[i];
			// Too verbose, need some way to configure this kind of stuff
			getLogger().debug("Generating " + p + " for " + type);
			rv+="\t/**\n"
				+ "\t * Set the ``" + p.getName() + "'' parameter.\n"
				+ "\t * " + p.getDescription() + "\n"
				+ "\t *\n"
				+ "\t * @param to the value to which to set the parameter\n"
				+ "\t */\n"
				+ "\tpublic void set" + methodName + "(" + type + " to)\n"
				+ "\t\tthrows SQLException";
			if(isInterface) {
				rv+=";\n";
			} else {
				rv+=" {\n\n"
					+ "\t\tsetArg(\"" + p.getName() + "\", to, "
					+ p.getType() +");\n\t}\n";
			}
		}

		return(rv);
	}

	private void write() throws Exception {
		if(verbose) {
			System.out.println("Writing out " + pkg + "." + classname);
		}
		// Copyright info
		out.println(
			"// Copyright (c) 2001  SPY internetworking <dustin@spy.net>\n"
		    + "// Written by Dustin's SQL generator\n"
			+ "//\n"
			+ "// $" + "Id" + "$\n");
		out.flush();

		// Package info
		out.println("package " + pkg + ";\n");

		// Imports
		imports.add("java.sql.SQLException");
		if(!isInterface) {
			imports.add("java.sql.Connection");
			imports.add("java.util.Map");
			imports.add("java.util.HashMap");
			imports.add("net.spy.util.SpyConfig");
		}
		if(wantsResultSet && results.size() > 0) {
			imports.add("java.sql.ResultSet");
		}

		// output imports
		for (String tmpimp : imports) {
			out.print("import ");
			out.print(tmpimp);
			out.println(";");
		}
		out.println("\n");

		// Generate the documentation.
		out.println("/**\n"
			+ " * \n"
			+ " * " + description + "\n"
			+ " *\n"
			+ " * <p>\n"
			+ " *\n"
			+ " * Generated by SPGen on " + new java.util.Date() + ".\n"
			+ " *\n"
			+ " * </p>\n"
			+ " * <p>\n"
			+ " *");

		// cursor requested mode.
		if(wantsCursor) {
			out.println(" * <b>This query requests a cursor.</b>\n"
				+ " *\n"
				+ " * </p>\n"
				+ " *\n"
				+ " * <p>\n"
				+ " *");
		}

		// Different stuff for different classes
		if(typeDbsp) {
			out.println(" * <b>Procedure Name</b>\n"
				+ " *\n"
				+ " * <ul>\n"
				+ " *  <li>" + procname + "</li>\n"
				+ " * </ul>");
		} else if (typeDbcp) {
			out.println(" * <b>Callable Name</b>\n"
				+ " *\n"
				+ " * <ul>\n"
				+ " *  <li>" + procname + "</li>\n"
				+ " * </ul>");
		} else {
			// If it's not a stored procedure or callable, and it's not an
			// interface, show the query.
			if(!isInterface) {
				out.println(" * <b>SQL Query</b>\n"
					+ " *\n"
					+ " * <ul>\n"
					+ " " + getDocQuery() + "\n"
					+ " * </ul>");
			}
		}

		// Required parameters
		out.println(" *\n"
			+ " * <b>Required Parameters</b>\n"
			+ " * <ul>");
		if(getRequiredArgs(false).size()==0) {
			out.println(" *  <li><i>none</i></li>");
		} else {
			for(Parameter p : getRequiredArgs(false)) {
				out.print(" * <li>" + p.getName() + " - "
					+ "{@link java.sql.Types#" + p.getShortType() + " "
						+ p.getType() + "}\n * "
					+ " - " + p.getDescription());
				Default d=p.getDefaultValue();
				if(d!=null) {
					out.print(" (default:  <i>" + d.getPrintValue() + "</i>)");
				}
				out.println("</li>");
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
		if(getOptionalArgs().size()==0) {
			out.println(" *  <li><i>none</i></li>");
		} else {
			for(Parameter p : getOptionalArgs()) {
				out.println(" * <li>" + p.getName() + " - "
					+ "{@link java.sql.Types#" + p.getShortType() + " "
						+ p.getType() + "}\n * "
					+ " - " + p.getDescription() + "</li>");
			}
		}
		out.println(" * </ul>\n"
			+ " *\n"
			+ " * </p>\n"
			+ " * <p>\n"
			+ " *");

		if (typeDbcp) {
			// Output parameters
			out.println(" *\n"
				+ " * <b>Output Parameters</b>\n"
				+ " * <ul>");
			if(getOutputParameters().size()==0) {
				out.println(" *  <li><i>none</i></li>");
			} else {
				for(Parameter p : getOutputParameters()) {
					out.println(" * <li>" + p.getName() + " - "
						+ "{@link java.sql.Types#" + p.getShortType() + " "
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

			for(Result r : results) {
				out.print(" *  <li>"
					+ r.getName() + " - ");
				if(isValidJDBCType(r.getType())) {
					out.print("{@link java.sql.Types#" + r.getShortType() + " "
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
		out.print("public " + (isInterface?"interface ":"class ")
			+ classname + " extends "
				+ (isInterface?superinterface:superclass));
		if(interfaces.size() > 0) {
			// If this is an interface, extra interfaces are just appended
			if(isInterface) {
				out.print(", ");
			} else {
				out.print("\n\timplements ");
			}
			out.print(SpyUtil.join(interfaces, ", "));
		}
		out.println(" {\n");

		// The map (staticially initialized)

		if(!isInterface) {
			if(!(typeDbsp || typeDbcp)) {
				out.println("\tprivate static final Map<String, String> "
						+ "queries=getQueries();\n");
			}

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

			// If a cursor was requested, build one
			if(wantsCursor) {
				out.println("\t\t// Generate a cursor for this query\n"
					+ "\t\tgenerateCursorName();\n");
			}

			// set the timeout variable
			out.println("\t\tsetQueryTimeout("+timeout+");\n");

			// Figure out whether we're a DBSP or a DBSQL
			if(typeDbsp || typeDbcp) {
				out.println("\t\t// Set the stored procedure name\n"
					+ "\t\tsetSPName(\"" + procname + "\");");
			} else {
				out.println("\t\t// Register the SQL queries");
				out.println("\t\tsetRegisteredQueryMap(queries);");
			}

			// parameters
			if (args.size()>0) {
				out.println("\n\t\t// Set the parameters.");
				for (Parameter p : args) {
					if (p.isRequired()) {
						if (p.isOutput()) {
							out.println("\t\tsetOutput(\"" + p.getName()
									+ "\", " + p.getType() + ");");
						} else {
							out.println("\t\tsetRequired(\"" + p.getName()
									+ "\", " + p.getType() + ");");
						}
					} else {
						out.println("\t\tsetOptional(\"" + p.getName()
							+ "\", " + p.getType() + ");");
					}

					Default d=p.getDefaultValue();
					if(d!=null) {
						out.println("\t\t// Default for " + d.getName());
						out.println("\t\tset(\"" + d.getName() + "\", "
							+ d.getPrintValue() + ");");
					}
				}
			}

			// Set the cachetime, if there is one
			if(cachetime>0) {
				out.println("\n\t\t// Set the default cache time.");
				out.println("\t\tsetCacheTime(" + cachetime + ");");
			}

			// End of spinit
			out.println("\t}\n");

			// Create the static initializers
			if(!(typeDbsp || typeDbcp)) {
				out.println("\t// Static initializer for query map.");
				out.println(
						"\tprivate static Map<String, String> getQueries() {");
				out.println("\t\t" + getJavaQueries());
				out.println("\n\t\treturn(rv);");
				out.println("\t}\n");
			}
		}

		// Create set methods for all the individual parameters
		int count=1;
		for(Parameter p : args) {
			if (p.isOutput()) {
				// output param
				out.println(createGetOutputMethod(p, count));
			} else {
				out.println(createSetMethod(p));
			}
			count++;
		}

		// If we want result sets, add them.
		if(wantsResultSet) {
			if(results.size() > 0) {
				out.println(createExecuteMethods());
				out.println(createResultClass());
			}
		}

		out.println("}");
	}

	private String createExecuteMethods() {
		String rv="\t/**\n"
			+ "\t * Execute this query and get a Result object.\n"
			+ "\t */\n"
			+ "\tpublic Result getResult() throws SQLException {\n"
			+ "\t\treturn(new Result(executeQuery()));\n"
			+ "\t}\n";
		return(rv);
	}

	private String createGetMethod(Result r) {
		String rv="\t\t/**\n"
			+ "\t\t * Get the " + r.getName() + " value.\n"
			+ "\t\t */\n"
			+ "\t\tpublic " + r.getJavaResultType()
			+ " get" + methodify(r.getName()) + "() throws SQLException {\n"
			+ "\t\t\treturn(get" + r.getJavaType()
				+ "(\"" + r.getName() + "\"));\n"
			+ "\t\t}\n\n";

		return(rv);
	}

	private String createGetOutputMethod(Parameter p, int index) {
		String rv="\tpublic Object get"+methodify(p.getName())
			+"() throws SQLException {\n"
			+"\t\treturn(getCallableStatement().getObject("+index
				+"));\n"
			+"\t}\n\n";

		return(rv);
	}

	private String createResultClass() {
		String rv="";

		// Class header
		rv+="\t/**\n"
			+ "\t * ResultSet object representing the results of this query.\n"
			+ "\t */\n"
			+ "\tpublic class Result extends net.spy.db.DBSPResult {\n"
			+ "\n\t\tprivate Result(ResultSet rs) {\n"
			+ "\t\t\tsuper(rs);\n"
			+ "\t\t}\n\n";

		for(Result r : results) {
			rv+=createGetMethod(r);
		}

		// End of class
		rv+="\n\t}\n";

		return(rv);
	}

	// Fix > and < characters, and & characters if there are any
	private String docifySQL(String sql) {
		StringBuilder sb=new StringBuilder(sql.length());

		char[] acters=sql.toCharArray();
		for(int i=0; i<acters.length; i++) {
			switch(acters[i]) {
				case '>':
					sb.append("&gt;");
					break;
				case '<':
					sb.append("&lt;");
					break;
				case '&':
					sb.append("&amp;");
					break;
				default:
					sb.append(acters[i]);
			}
		}

		return (sb.toString());
	}

	private String getDocQuery() {
		StringBuilder sb=new StringBuilder(1024);

		for(Map.Entry<String, List<String>> me : queries.entrySet()) {

			List<String> sqlquery=me.getValue();

			sb.append(" * <li>\n");
			sb.append(" *  <b>\n");
			sb.append(" *   ");
			sb.append(me.getKey());
			sb.append("\n");
			sb.append(" *  </b>\n");
			sb.append(" *  <pre>\n");
			for(String part : sqlquery) {
				sb.append(" * ");
				sb.append(docifySQL(part));
				sb.append("\n");
			}
			sb.append(" *  </pre>\n * </li>\n");

		}

		return(sb.toString().trim());
	}

	private String getJavaQueries() {
		StringBuilder sb=new StringBuilder(1024);

		sb.append("StringBuilder query=null;\n");
		sb.append("\t\tMap<String, String> rv"
			+ "=new HashMap<String, String>();\n");

		for(Map.Entry<String, List<String>> me : queries.entrySet()) {

			List<String> sqlquery=me.getValue();

			sb.append("\n\t\tquery=new StringBuilder(1024);");

			for(String part : sqlquery) {
				sb.append("\n\t\tquery.append(\"");

				for(StringTokenizer st=new StringTokenizer(part, "\"", true);
					st.hasMoreTokens();) {

					String tmp=st.nextToken();
					if(tmp.equals("\"")) {
						tmp="\\\"";
					}
					sb.append(tmp);
				}

				sb.append("\\n\");");
			}

			sb.append("\n\t\trv.put(\"");
			sb.append(me.getKey());
			sb.append("\", ");
			sb.append("query.toString());\n");

		}

		return(sb.toString().trim());
	}

	private void parse() throws Exception {

		// this is for when a user overrides the superclass
		StringBuilder userSuperclass=null;

		if(verbose) {
			System.out.println("Parsing " + classname + ".spt");
		}

		String tmp=in.readLine();
		while(tmp!=null) {

			// Don't do anything if the line is empty
			if(tmp.length() > 0) {
				if(tmp.charAt(0) == '@') {
					// lower case and trim before we begin...RAP WITH ME!!
					section=tmp.substring(1).trim().toLowerCase();
					// System.out.println("Working on section " + section);

					// Handlers for things that occur when a section is begun
					if (section.equals("genresults")) {
						wantsResultSet=true;
					} else if (section.equals("cursor")) {
						wantsCursor=true;
					} else if (section.startsWith("loosetyp")) {
						looseTypes=true;
					} else if (section.startsWith("sql.")) {
						currentQuery=section.substring(4);
						section="sql";
					} else if (section.equals("sql")) {
						currentQuery=QuerySelector.DEFAULT_QUERY;
					}
				} else if(tmp.charAt(0) == '#') {
					// Comment, ignore
				} else {

					if(section.equals("description")) {
						description+=tmp;
					} else if(section.equals("sql")) {
						isInterface=false;
						if (superclass==null) {
							superclass="net.spy.db.DBSQL";
						}

						List<String> sqlquery=queries.get(currentQuery);
						if(sqlquery == null) {
							sqlquery=new ArrayList<String>();
							queries.put(currentQuery, sqlquery);
						}
						sqlquery.add(tmp);
					} else if(section.equals("procname")) {
						isInterface=false;
						procname+=tmp;
						if (dbspSuperclass==null) {
							superclass="net.spy.db.DBSP";
						} else {
							superclass=dbspSuperclass;
						}
						typeDbsp=true;
					} else if(section.equals("callable")) {
						isInterface=false;
						procname+=tmp;
						if (dbcpSuperclass==null) {
							superclass="net.spy.db.DBCP";
						} else {
							superclass=dbcpSuperclass;
						}
						typeDbcp=true;
					} else if(section.equals("defaults")) {
						Default d=new Default(tmp);
						registerDefault(d);
					} else if(section.equals("params")) {
						Parameter param=new Parameter(tmp);
						args.add(param);
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
					} else if(section.equals("timeout")) {
						timeout=Integer.parseInt(tmp);
					} else if(section.equals("superclass")) {
						userSuperclass=new StringBuilder(96);
						userSuperclass.append(tmp);
					} else if(section.equals("import")) {
						imports.add(tmp);
					} else if(section.equals("implements")) {
						interfaces.add(tmp);
					} else {
						throw new Exception("Unknown section: ``"+section+"''");
					}

				}
			}

			tmp=in.readLine();
		}

		// Make sure a superinterface got defined
		if(superinterface == null) {
			superinterface="net.spy.db.DBSPLike";
		}

		// if the user over-rode (like your mom) the superclass, use it!!
		if (userSuperclass!=null) {
			if(isInterface) {
				superinterface=userSuperclass.toString();
			} else {
				superclass=userSuperclass.toString();
			}
		}

	}

	private void registerDefault(Default d) {
		boolean done=false;
		for(Parameter p : args) {
			if(p.getName().equals(d.getName())) {
				p.setDefaultValue(d);
				done=true;
				break;
			}
		}
		if(!done) {
			throw new IllegalArgumentException("Didn't find parameter "
				+ d.getName() + " when registering");
		}
	}

	// get all of the required arguments
	// If evenOutput is true, then we even get the output parameters
	private Collection<Parameter> getRequiredArgs(boolean evenOutput) {
		Collection<Parameter> rv=new ArrayList<Parameter>(args.size());
		for(Parameter p : args) {
			if(p.isRequired()) {
				// Deal with output parameters
				if(p.isOutput()) {
					if(evenOutput) {
						rv.add(p);
					}
				} else {
					rv.add(p);
				}
			}
		}
		return(rv);
	}

	// get all of the required arguments
	private Collection<Parameter> getOptionalArgs() {
		Collection<Parameter> rv=new ArrayList<Parameter>(args.size());
		for(Parameter p : args) {
			if(!p.isRequired()) {
				rv.add(p);
			}
		}
		return(rv);
	}

	// Get the output parameters
	private Collection<Parameter> getOutputParameters() {
		Collection<Parameter> rv=new ArrayList<Parameter>(args.size());
		for(Parameter p : args) {
			if(p.isOutput()) {
				rv.add(p);
			}
		}
		return(rv);
	}

	// Private class for results

	static class Result extends Object {
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
					throw new IllegalArgumentException("Invalid JDBC type: "
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

		public String getShortType() {
			String rv=type;
			int i=type.lastIndexOf('.');
			if(i>0) {
				rv=type.substring(i+1);
			}
			return(rv);
		}

		public String getJavaType() {
			String rv=javaTypes.get(type);
			if(rv==null) {
				throw new RuntimeException("Whoops!  " + type
					+ " must have been overlooked");
			}
			return(rv);
		}

		public String getJavaResultType() {
			String rv=javaResultTypes.get(type);
			if(rv==null) {
				throw new RuntimeException("Whoops!  " + type
					+ " must have been overlooked");
			}
			return(rv);
		}

		public String getDescription() {
			return(description);
		}

	}

	// Private class for parameters

	class Parameter extends Object {
		private String name=null;
		private boolean required=false;
		private String type=null;
		private String paramDescr=null;
		private boolean output=false;
		private Default defaultValue=null;

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
					type="java.sql.Types." + getParamTypeAlias(type);
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
				paramDescr=st.nextToken("\n");
			} catch (NoSuchElementException ex) {
				// I don't think we cre if it's documented or not!  But
				// honestly I don't think this should ever happen cause you
				// need a newline.  Well, I guess if you ended the file odd
				// enough, and without a EOL before the EOF...very odd case
				paramDescr="";
			}
		}

		// Need to alias NUMERIC to DECIMAL, since I can't otherwise tell them
		// apart.
		private String getParamTypeAlias(String pt) {
			String rv=pt;
			if(pt.equals("NUMERIC")) {
				rv="DECIMAL";
			}
			return(rv);
		}

		@Override
		public String toString() {
			return("{Parameter " + name + "}");
		}

		/**
		 * Get the hash code of the name of this parameter.
		 */
		@Override
		public int hashCode() {
			return(name.hashCode());
		}

		/**
		 * True if the given objet is an instance of Parameter with the
		 * same name.
		 */
		@Override
		public boolean equals(Object o) {
			boolean rv=false;
			if(o instanceof Parameter) {
				Parameter p=(Parameter)o;
				rv=name.equals(p.name);
			}

			return(rv);
		}

		public String getName() {
			return(name);
		}

		public String getType() {
			return(type);
		}

		public String getShortType() {
			String rv=type;
			int i=type.lastIndexOf('.');
			if(i>0) {
				rv=type.substring(i+1);
			}
			return(rv);
		}

		public String getDescription() {
			return(paramDescr);
		}

		public boolean isRequired() {
			return(required);
		}

		public boolean isOutput() {
			return(output);
		}

		public Default getDefaultValue() {
			return(defaultValue);
		}

		public void setDefaultValue(Default d) {
			defaultValue=d;
		}
	}

	// Default values for parameters

	class Default extends Object {
		private String name=null;
		// Class of this parameter
		private String type=null;
		private Object value=null;

		public Default(String line) {
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

			// Figure out the type of this parameter
			type=findType();

			// Get the rest of the line
			String rest=st.nextToken("\n");

			parse(rest);
		}

		public String getName() {
			return(name);
		}

		// Get the value to be used for printing in the source
		public String getPrintValue() {
			String rv=null;
			boolean needsCast=false;

			// Figure out if we need a cast
			if(value == null) {
				// If the value was null, cast it
				needsCast=true;
			}

			if(needsCast) {
				rv="(" + javaResultTypes.get(type) + ")" + value;
			} else {
				rv=value.toString();
			}
			return(rv);
		}

		private void parse(String input) {
			if(input==null) {
				throw new NullPointerException("Can't parse a null string");
			}
			// Get rid of whitespace from the ends
			input=input.trim();

			// make sure there's something left
			if(input.length() == 0) {
				throw new IllegalArgumentException(
					"Can't parse nothin'");
			}

			if(input.equals("NULL")) {
				value=null;
			} else {

				if(type.equals("java.sql.Types.INTEGER")) {
					value=new Integer(input);
				} else if(type.equals("java.sql.Types.SMALLINT")) {
					value=new Short(input);
				} else if(type.equals("java.sql.Types.TINYINT")) {
					value=new Short(input);
				} else if(type.equals("java.sql.Types.BIGINT")) {
					value=new BigDecimal(input);
				} else if(type.equals("java.sql.Types.Decimal")) {
					value=new BigDecimal(input);
				} else if(type.equals("java.sql.Types.BIT")) {
					value=SpyUtil.getBoolean(input);
				} else if(type.equals("java.sql.Types.DOUBLE")) {
					value=new Double(input);
				} else if(type.equals("java.sql.Types.FLOAT")) {
					value=new Float(input);
				} else if(type.equals("java.sql.Types.VARCHAR")) {
					value=input;
				} else {
					throw new IllegalArgumentException(
						"I don't know how to parse a default for " + type);
				}
			}
		}

		// Lookup the type for this spt.
		private String findType() {
			String rv=null;

			// Look for the parameter
			for(Parameter p : args) {
				if(p.getName().equals(name)) {
					rv=p.getType();
					break;
				}
			}

			// Make sure we got some
			if(rv==null) {
				throw new IllegalArgumentException(
					"No parameter for this default:  " + name);
			}
			return(rv);
		}

		// String me
		@Override
		public String toString() {
			String rv="{Default " + name + " (" + type + ") " + value + "}";
			return(rv);
		}
	}

	/**
	 * Usage:  SPGen filename
	 */
	public static void main(String argv[]) throws Exception {

		String infile=argv[0];
		// Get rid of the .spt
		int lastslash=infile.lastIndexOf(File.separatorChar);
		String basename=infile.substring(0, infile.indexOf(".spt"));
		// If it matches, start at the next character, if it didn't, it's
		// -1 and start at 0
		String cname=basename.substring(lastslash+1);
		String outfile=basename + ".java";

		BufferedReader ireader=new BufferedReader(new FileReader(infile));
		PrintWriter owriter=new PrintWriter(new FileWriter(outfile));
		SPGen spg=new SPGen(cname, ireader, owriter);

		spg.generate();

		CloseUtil.close(ireader);
		CloseUtil.close(owriter);
	}

}
