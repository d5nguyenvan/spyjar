//
// arch-tag: 7A781A74-1110-11D9-BE70-000A957659CC

package net.spy.pool;

import java.sql.Connection;
import java.sql.DriverManager;

import java.util.Enumeration;
import java.util.Properties;

import net.spy.SpyConfig;

/**
 * PoolFiller object to fill a pool with JDBC PoolAbles
 */

public class JDBCPoolFiller extends PoolFiller {

	public JDBCPoolFiller(String name, SpyConfig conf) {
		super(name, conf);
	}

	/**
	 * get a new object for the pool.
	 *
	 * The following config entries are required:
	 * <ul>
	 *  <li>dbDriverName - Name of the JDBC driver to use</li>
	 *  <li>dbSource - JDBC url for the database</li>
	 *  <li>dbUser - Database username</li>
	 *  <li>dbPass - Database password</li>
	 * </ul>
	 *
	 * The following config entries are optional:
	 * <ul>
	 *  <li>maxAge - The maximum amount of time (in milliseconds) that the
	 *      connection can live.  Default is forever</li>
	 *  <li>dboptions.* - Any JDBC driver specific options you want to
	 *      pass.</li>
	 * </ul>
	 *
	 * @exception PoolException if a new connection could not be made.
	 */
	public PoolAble getObject() throws PoolException {
		JDBCPoolAble p = null;
		try {
			String classname=null, source=null, user=null, pass=null;

			// Load the JDBC driver
			classname=getProperty("dbDriverName");
			if(classname==null) {
				throw new Exception("No dbDriverName property given");
			}
			Class.forName(classname);

			source=getProperty("dbSource");
			if(source==null) {
				throw new Exception("No dbSource property given");
			}

			Properties prop=new Properties();
			prop.put("user", getProperty("dbUser", ""));
			prop.put("password", getProperty("dbPass", ""));

			// Set the system-wide and local DB properties here.
			Properties sysprop=System.getProperties();
			setDBOptions(sysprop, prop, "dboption.");
			setDBOptions(getConfig(), prop, getName()+".dboption.");

			long maxAge=(long)getPropertyInt("max_age", 0);

			// Grab a connection.
			Connection db = DriverManager.getConnection(source, prop);
			// Create the PoolAble object
			p=new JDBCPoolAble(db, maxAge, getPoolHash());
		} catch(Exception e) {
			throw new PoolException(
				"Error getting new DB object for the "
					+ debugName() + " pool:  " + e
				);
		}

		return(p);
	}

	// Extract db options from various properties.
	private void setDBOptions(Properties from, Properties tmpconf,
		String base) {
		for(Enumeration e=from.propertyNames(); e.hasMoreElements(); ) {
			String pname=(String)e.nextElement();
			if(pname.startsWith(base)) {
				String oname=pname.substring(base.length());
				String ovalue=from.getProperty(pname);
				tmpconf.put(oname, ovalue);
			}
		}
	}
}
