package com.garganttua.api.core.engine;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;

public class GGAPIEngineException extends GGAPIException {

	public GGAPIEngineException(GGAPIExceptionCode code, String message, Exception exception) {
		super(code, message, exception);
	}
	
	public GGAPIEngineException(GGAPIExceptionCode code, String message) {
		super(code, message);
	}
	
	public GGAPIEngineException(Exception exception) {
		super(exception);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 3026591383888353678L;

}
