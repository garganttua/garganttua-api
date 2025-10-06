package com.garganttua.api.core.security.exceptions;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;

import lombok.Getter;

@Getter
public class SecurityException extends CoreException {

	private static final long serialVersionUID = 2528143588504398416L;

	public SecurityException(CoreExceptionCode code, String message, Exception exception) {
		super(code, message, exception);
	}

	public SecurityException(CoreExceptionCode code, String message) {
		super(code, message);
	}

	public SecurityException(Exception exception) {
		super(exception);
	}

	public SecurityException(CoreExceptionCode code, Exception exception) {
		super(code, exception.getMessage(), exception);
	}

}
