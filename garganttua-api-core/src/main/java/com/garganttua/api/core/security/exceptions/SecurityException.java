package com.garganttua.api.core.security.exceptions;

import com.garganttua.api.spec.ApiException;

public class SecurityException extends ApiException {

	public static final int SECURITY_ERROR_CODE = 200;

	public SecurityException(String message) {
		super(SECURITY_ERROR_CODE, message);
	}

	public SecurityException(String message, Throwable cause) {
		super(SECURITY_ERROR_CODE, message, cause);
	}

	public SecurityException(Throwable cause) {
		super(cause);
	}

	public SecurityException(int code, String message) {
		super(code, message);
	}

	public SecurityException(int code, String message, Throwable cause) {
		super(code, message, cause);
	}

}
