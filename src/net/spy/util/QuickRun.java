// Copyright (c) 2000  Dustin Sallings <dustin@spy.net>
//
// arch-tag: 865FE6EB-1110-11D9-9156-000A957659CC

package net.spy.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.net.ServerSocket;
import java.net.Socket;

import net.spy.SpyThread;

/**
 * Listens on a socket and lets you run stuff without firing up a new JVM.
 */
public class QuickRun extends SpyThread {

	// Server stuff
	private ServerSocket server=null;
	private int port=11011;

	// Client stuff
	private Socket client=null;
	private BufferedReader in=null;
	private PrintWriter out=null;
	private OutputStream ostream=null;

	public QuickRun(Socket s) throws Exception {
		super();
		this.client=s;
	}

	public QuickRun(int port) throws Exception {
		super();
		this.port=port;
	}

	public void run() {
		try {
			in = new BufferedReader(
				new InputStreamReader(client.getInputStream()));
			ostream=client.getOutputStream();
			out = new PrintWriter(new OutputStreamWriter(ostream));

			// Greet the client
			out.println("QuickRun ready.");
			out.flush();

			// Get the command.
			String cmd=in.readLine();

			processCmd(cmd);
		} catch(Exception e) {
			getLogger().error("QuickRun exception", e);
		} finally {
			try {
				ostream.close();
			} catch(Exception e) {
				getLogger().warn("Problem closing client output stream.", e);
			}
			try {
				out.close();
			} catch(Exception e) {
				getLogger().warn("Problem closing output stream.", e);
			}
			try {
				in.close();
			} catch(Exception e) {
				getLogger().warn("Problem closing input output stream.", e);
			}
		}
	}

	private void processCmd(String line) throws Exception {
		String cmd=null, args=null;
		int colon=line.indexOf(":");
		cmd=line.substring(0, colon);
		args=line.substring(colon+1);

		getLogger().info("Got command:  " + cmd + " args:  " + args);

		if(cmd.equals("RUN")) {
			runClass(SpyUtil.split(" ", args));
		/*
		} else if(cmd.equals("COMPILE")) {
			// Grab the Sun compiler
			sun.tools.javac.Main compiler=
				new sun.tools.javac.Main(ostream, "javac");
			compiler.compile(SpyUtil.split(" ", args));
		*/
		} else if(cmd.equalsIgnoreCase("QUIT")) {
			out.close();
			ostream.close();
			in.close();
		} else {
			out.println("Invalid command:  " + cmd);
		}
	}

	private void runClass(String args[]) throws Exception {
		String cn=args[0];
		String cargs[]=new String[args.length-1];
		// Copy the args over.
		System.arraycopy(args, 1, cargs, 0, cargs.length);

		SpyUtil.runClass(cn, cargs);
	}

	/**
	 * Start up the server.
	 */
	public void goServer() throws Exception {
		server=new ServerSocket(port);
		getLogger().info("QuickRun server listening on port " + port);

		while(true) {
			Socket client=server.accept();
			client.setSoTimeout(15000);
			QuickRun c=new QuickRun(client);
			c.start();
		}
	}

	/**
	 * Testing.
	 */
	public static void main(String args[]) throws Exception {
		QuickRun qr=new QuickRun(Integer.parseInt(args[0]));
		qr.goServer();
	}
}
