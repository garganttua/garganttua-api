package com.garganttua.api.core.objects.fields;

public class GGAPIFieldsException extends Exception {

	public GGAPIFieldsException(Exception e) {
		super(e);
	}

	public GGAPIFieldsException(String string, Exception e) {
		super(string, e);
	}

	public GGAPIFieldsException(String string) {
		super(string);
	}

	private static final long serialVersionUID = -709424867520967955L;

}
