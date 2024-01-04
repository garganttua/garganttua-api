package com.garganttua.api.security;

public class GGAPISecurityException extends Exception {

	public GGAPISecurityException(String string) {
		super(string);
	}

	public GGAPISecurityException(Exception e) {
		super(e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3409319315638884145L;

}
