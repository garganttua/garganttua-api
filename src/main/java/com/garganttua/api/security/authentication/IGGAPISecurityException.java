package com.garganttua.api.security.authentication;

public class IGGAPISecurityException extends Exception {

	public IGGAPISecurityException(String string) {
		super(string);
	}

	public IGGAPISecurityException(Exception e) {
		super(e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3409319315638884145L;

}
