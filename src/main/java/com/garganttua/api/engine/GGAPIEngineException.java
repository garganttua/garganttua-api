package com.garganttua.api.engine;

public class GGAPIEngineException extends Exception {

	public GGAPIEngineException(String string) {
		super(string);
	}

	public GGAPIEngineException(Exception e) {
		super(e);
	}

	public GGAPIEngineException(String string, Exception e) {
		super(string, e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3026591383888353678L;

}
