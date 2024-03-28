package com.garganttua.api.core.objects.utils;

public class GGAPIObjectReflectionHelperExcpetion extends Exception {

	public GGAPIObjectReflectionHelperExcpetion(String string) {
		super(string);
	}

	public GGAPIObjectReflectionHelperExcpetion(Exception e) {
		super(e);
	}

	public GGAPIObjectReflectionHelperExcpetion(String string, Exception e) {
		super(string, e);
	}

	private static final long serialVersionUID = -4059467497613214724L;

}
