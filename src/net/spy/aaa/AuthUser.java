// Copyright (c) 2001  Dustin Sallings <dustin@spy.net>
//
// $Id: AuthUser.java,v 1.1 2002/08/28 00:34:55 dustin Exp $

package net.spy.aaa;

/**
 * A user capable of checking its own password.  Used internally by the
 * Authenticator.
 */
public interface AuthUser {

	/**
	 * Check a password for this user.
	 */
	void checkPassword(String password) throws AuthException;

}

