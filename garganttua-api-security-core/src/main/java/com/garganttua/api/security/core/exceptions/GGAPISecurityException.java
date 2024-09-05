package com.garganttua.api.security.core.exceptions;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;

import lombok.Getter;

@Getter
public class GGAPISecurityException extends GGAPIException {

	private static final long serialVersionUID = 2528143588504398416L;

	public GGAPISecurityException(GGAPIExceptionCode code, String message, Exception exception) {
		super(code, message, exception);
	}

	public GGAPISecurityException(GGAPIExceptionCode code, String message) {
		super(code, message);
	}

	public GGAPISecurityException(Exception exception) {
		super(exception);
	}

}
