// SNPP implementation
//
// Copyright (c) 1999 Dustin Sallings
//
// arch-tag: 78144148-1110-11D9-8C3B-000A957659CC

package net.spy.net;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import java.net.Socket;
import java.net.UnknownHostException;

import net.spy.SpyObject;
import net.spy.util.SpyUtil;

/**
 * SNPP client.
 */
public class SNPP extends SpyObject {
	private Socket s=null;
	private InputStream in=null;
	private OutputStream out=null;
	private BufferedReader din=null;
	private PrintWriter prout=null;

	// 2way support
	private boolean goesBothWays=false;
	private String msgTag=null;

	/**
	 * Current full line received from the SNPP server.
	 */
	private String currentline=null;
	/**
	 * Current message received from the SNPP server.
	 */
	private String currentmessage=null;
	/**
	 * Current status received from SNPP server.
	 */
	private int currentstatus=-1;

	/**
	 * Get a new SNPP object connected to host:port
	 *
	 * @param host SNPP host to connect to
	 * @param port SNPP port number
	 * @param timeout SO_TIMEOUT in milliseconds
	 *
	 * @exception IOException Thrown if the various input and output
	 * streams cannot be established.
	 *
	 * @exception UnknownHostException Thrown if the SNPP server hostname
	 * cannot be resolved.
	 */
	public SNPP(String host, int port, int timeout)
		throws IOException, UnknownHostException {
		s = new Socket(host, port);

		if(timeout>0) {
			s.setSoTimeout(timeout);
		}

		in=s.getInputStream();
		din = new BufferedReader(new InputStreamReader(in));
		out=s.getOutputStream();
		prout=new PrintWriter(out);

		getaline();
	}

	/**
	 * Get a new SNPP object connected to host:port
	 *
	 * @param host SNPP host to connect to
	 * @param port SNPP port number
	 *
	 * @exception IOException Thrown if the various input and output
	 * streams cannot be established.
	 *
	 * @exception UnknownHostException Thrown if the SNPP server hostname
	 * cannot be resolved.
	 */
	public SNPP(String host, int port)
		throws IOException, UnknownHostException {
		this(host, port, 0);
	}

	/**
	 * Current full line received from the SNPP server.
	 */
	public String getCurrentline() {
		return(currentline);
	}

	/**
	 * Current message received from the SNPP server.
	 */
	public String getCurrentmessage() {
		return(currentmessage);
	}

	/**
	 * Current status received from SNPP server.
	 */
	public int getCurrentstatus() {
		return(currentstatus);
	}

	/**
	 * Put this into 2way mode.
	 *
	 * @exception Exception when the 2way command fails
	 */
	public void twoWay() throws Exception {
		cmd("2way");
		goesBothWays=true;
	}

	/**
	 * sets the pager ID
	 *
	 * @param id snpp pager id
	 *
	 * @exception Exception when the page command fails
	 */
	public void pagerID(String id) throws Exception {
		cmd("page " + id);
	}

	/**
	 * sets the message to send
	 *
	 * @param msg snpp message
	 *
	 * @exception Exception when the command fails
	 */
	public void message(String msg) throws Exception {
		String tmp="";
		String atmp[]=SpyUtil.split("\r\n", msg);
		for(int i=0; i<atmp.length; i++) {
			tmp+=atmp[i] + " ";
		}
		cmd("mess " + tmp);
	}

	/**
	 * sets the message to send, keeps newlines and all that
	 *
	 * @param msg snpp message
	 *
	 * @exception Exception when the command fails, possibly because DATA
	 * is not supported
	 */
	public void data(String msg) throws Exception {
		try {
			cmd("data");
		} catch(Exception e) {
			if(currentstatus != 354) {
				throw e;
			}
		}
		cmd(msg + "\r\n.");
	}

	/**
	 * gets the message tag on a 2way page
	 *
	 * @return the tag, or null if there is no tag
	 */
	public String getTag() {
		return(msgTag);
	}

	/**
	 * Send a simple page.
	 *
	 * @param id SNPP recipient ID.
	 * @param msg msg to send.
	 *
	 * @exception Exception Thrown if any of the commands required to send
	 * the page threw an exception.
	 */
	public void sendpage(String id, String msg) throws Exception {
		// Reset so this thing can be called more than once.
		cmd("rese");
		if(goesBothWays) {
			twoWay();
		}
		pagerID(id);
		message(msg);
		// My pager server supports priority, so we'll ignore any errors
		// with this one.
		try {
			cmd("priority high");
		} catch(Exception e) {
			// This is a nonstandard command and is likely to throw an
			// exception.
		}
		send();
	}

	/**
	 * send is handled separately in case it's a two-way transaction.
	 *
	 * @exception Exception Thrown if the send command fails.
	 */
	public void send() throws Exception {
		cmd("send");
		if(goesBothWays) {
			// If it looks 2way, we get the stuff
			if(currentstatus >= 860) {
				String a[]=SpyUtil.split(" ", currentmessage);
				msgTag=a[0] + " " + a[1];
			}
		}
	}

	/**
	 * Check for a response from a 2way message.
	 *
	 * @param msgTag the message tag to look up.
	 *
	 * @return the response message, or NULL if it's not ready
	 *
	 * @exception Exception when the msta command fails, or we're not doing
	 * 2way.
	 */
	public String getResponse(String tag) throws Exception {
		String ret=null;
		if(goesBothWays) {
			cmd("msta " + tag);
			if(currentstatus == 889) {
				String tmp=currentmessage;
				tmp=tmp.substring(tmp.indexOf(" ")).trim();
				tmp=tmp.substring(tmp.indexOf(" ")).trim();
				tmp=tmp.substring(tmp.indexOf(" ")).trim();
				ret=tmp;
			}
		} else {
			throw new Exception("I don't go both ways.");
		}
		return(ret);
	}

	/**
	 * Check for a response from a 2way message.
	 *
	 * @return the response message, or NULL if it's not ready
	 *
	 * @exception Exception when the msta command fails, or we're not doing
	 * 2way.
	 */
	public String getResponse() throws Exception {
		if(msgTag == null) {
			throw new Exception("No msg tag received, have you done a "
				+ "2way page yet?");
		}
		return(getResponse(msgTag));
	}

	/**
	 * adds a response to the SNPP message.
	 *
	 * @param response the canned response to add
	 *
	 * @exception Exception when we're not in a 2way transaction, or the
	 * command fails.
	 */
	public void addResponse(String response) throws Exception {
		if(!goesBothWays) {
			throw new Exception("I don't go both ways.");
		}
		cmd("mcre " + response);
	}

	/**
	 * Send an SNPP command.
	 *
	 * @param command command to send.  It's sent literally to the SNPP
	 * server.
	 *
	 * @exception Exception Thrown if the command does not return an ``OK''
	 * status from the SNPP server.
	 */
	public void cmd(String command) throws Exception {
		if(getLogger().isDebugEnabled()) {
			getLogger().debug(">> " + command);
		}
		prout.print(command + "\r\n");
		prout.flush();
		getaline();
		if(!ok()) {
			throw new Exception(currentmessage + " (" + command + ")");
		}
	}

	/**
	 * Close the connection to the SNPP server.
	 */
	public void close() {
		if(s!=null) {
			try {
				cmd("quit");
			} catch(Exception e) {
				// Don't care, we tried...
			} finally {
				try {
					s.close();
				} catch(IOException e) {
					// Don't care anymore
				}
			}
			// Go ahead and set s to null anyway.
			s=null;
		}
	}

	protected void finalize() throws Throwable {
		if(getLogger().isDebugEnabled()) {
			getLogger().debug("Finalizing...");
		}
		close();
		super.finalize();
	}

	// Return whether the current status number is within an OK range.
	private boolean ok() {
		boolean r = false;
		if(currentstatus < 300 ) {
			if(currentstatus >= 200) {
				r = true;
			}
		}
		// Specific stuff for two-way
		if(goesBothWays && r == false) {
			if(currentstatus < 890 && currentstatus >= 860) {
				// delivered, processing or final
				r=true;
			} else if(currentstatus < 970 && currentstatus >= 960) {
				// Queued transaction
				r=true;
			}
		}
		return(r);
	}

	// Return a line from the SNPP server.
	private void getaline() throws IOException {
		String stmp;
		Integer itmp;

		// Get the line
		currentline = din.readLine();

		// make sure we read something
		if(currentline==null) {
			throw new IOException("Read returned null, disconnected?");
		}

		if(getLogger().isDebugEnabled()) {
			getLogger().debug("<< " + currentline);
		}

		// Extract the message
		currentmessage = currentline.substring(4);

		// Calculate the status number
		stmp = currentline.substring(0, 3);
		itmp = Integer.valueOf(stmp);
		currentstatus = itmp.intValue();
	}

	/**
	 * Test page routine, send pages from the commandline.
	 *
	 * <p>
	 *
	 * Usage:<br>
	 * SNPP hostname port username password
	 */
	public static void main(String args[]) throws Exception {
		SNPP snpp=new SNPP(args[0], Integer.parseInt(args[1]));
		snpp.sendpage(args[2], args[3]);
		snpp.close();
	}
}
