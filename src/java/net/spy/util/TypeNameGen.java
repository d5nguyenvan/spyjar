// Copyright (c) 2000  Dustin Sallings <dustin@spy.net>

package net.spy.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.sql.Types;

/**
 * Used at compile time to generate {@link net.spy.db.TypeNames}.
 */
public class TypeNameGen extends Object {

	/**
	 * Get the TypeNameGen instance.
	 */
	public TypeNameGen() {
		super();
	}

	/**
	 * Write the TypeNames class to the given PrintStream.
	 *
	 * @param ps the printstream to which to write the source
	 * @throws IOException if there's a problem writing
	 */
	public void writeTo(PrintStream ps) throws IOException {
		ps.println("package net.spy.db;\n");
		ps.println("import java.sql.Types;\n");
		ps.println("/**");
		ps.println(" * Get names for SQL data types (autogenerated).");
		ps.println(" */");
		ps.println("public class TypeNames extends Object {\n");

		// getTypeName method
		ps.println("\t/**");
		ps.println("\t * Get the name of the given java.sql.Types type.");
		ps.println("\t */");
		ps.println("\tpublic final static String getTypeName(int type) {");
		ps.println("\t\tString rv=null;\n");
		ps.println("\t\tswitch(type) {");

		// Fill in the switch statement with all the known types.
		Field[] fields=Types.class.getDeclaredFields();
		for(int i=0; i<fields.length; i++) {
			ps.println("\t\t\tcase Types." + fields[i].getName() + ":");
			ps.println("\t\t\t\trv=\"" + fields[i].getName() + "\";");
			ps.println("\t\t\t\tbreak;");
		}

		ps.println("\t\t\tdefault:");
		ps.println("\t\t\t\trv=\"Unknown#\" + type;");
		ps.println("\t\t}\n");
		ps.println("\t\treturn(rv);");
		ps.println("\t}\n");

		ps.println("}");
	}

	/**
	 * Write out the TypeNames implementation.
	 *
	 * @param args (0) = path to which to write the TypeNames
	 * @throws IOException if there's a problem writing the file
	 */
	public static void main(String args[]) throws IOException {
		TypeNameGen tng=new TypeNameGen();
		FileOutputStream fos=new FileOutputStream(args[0]);
		PrintStream ps=new PrintStream(fos);

		tng.writeTo(ps);

		ps.close();
		fos.close();
	}
}
