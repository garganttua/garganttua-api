package com.garganttua.api.core.legacy.dto.exceptions;

import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;

public class DtoException extends CoreException {

	public DtoException(CoreExceptionCode code, String message, Exception exception) {
		super(code, message, exception);
	}
	
	public DtoException(CoreExceptionCode code, String message) {
		super(code, message);
	}
	
	public DtoException(Exception exception) {
		super(exception);
	}

	private static final long serialVersionUID = 4914275250821928797L;
	
}
