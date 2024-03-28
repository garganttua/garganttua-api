package com.garganttua.api.core.objects.query;

public class GGAPIObjectQueryException extends Exception {

	public GGAPIObjectQueryException(String string) {
		super(string);
	}

	public GGAPIObjectQueryException(Exception e) {
		super(e);
	}

	private static final long serialVersionUID = 6029849216646775106L;

}
