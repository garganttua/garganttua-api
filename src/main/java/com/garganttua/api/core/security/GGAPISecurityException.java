package com.garganttua.api.core.security;

import com.garganttua.api.core.exceptions.GGAPICoreException;
import com.garganttua.api.core.exceptions.GGAPICoreExceptionCode;

public class GGAPISecurityException extends GGAPICoreException {

	public GGAPISecurityException(String string) {
		super(GGAPICoreExceptionCode.GENERIC_SECURITY_ERROR, string);
	}

	public GGAPISecurityException(Exception e) {
		super(e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 3409319315638884145L;

}
