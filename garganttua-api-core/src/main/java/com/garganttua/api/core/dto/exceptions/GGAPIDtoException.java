package com.garganttua.api.core.dto.exceptions;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;

public class GGAPIDtoException extends GGAPIException {

	public GGAPIDtoException(GGAPIExceptionCode code, String message, Exception exception) {
		super(code, message, exception);
	}
	
	public GGAPIDtoException(GGAPIExceptionCode code, String message) {
		super(code, message);
	}
	
	public GGAPIDtoException(Exception exception) {
		super(exception);
	}

	private static final long serialVersionUID = 4914275250821928797L;
	
}
