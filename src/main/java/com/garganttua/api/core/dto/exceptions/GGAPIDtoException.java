package com.garganttua.api.core.dto.exceptions;

import com.garganttua.api.spec.GGAPICoreException;
import com.garganttua.api.spec.GGAPICoreExceptionCode;

public class GGAPIDtoException extends GGAPICoreException {

	public GGAPIDtoException(GGAPICoreExceptionCode code, String message, Exception exception) {
		super(code, message, exception);
	}
	
	public GGAPIDtoException(GGAPICoreExceptionCode code, String message) {
		super(code, message);
	}
	
	public GGAPIDtoException(Exception exception) {
		super(exception);
	}

	private static final long serialVersionUID = 4914275250821928797L;
	
}
