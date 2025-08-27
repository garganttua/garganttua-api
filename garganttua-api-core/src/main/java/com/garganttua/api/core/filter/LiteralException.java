package com.garganttua.api.core.filter;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;

public class LiteralException extends CoreException {

	public LiteralException(CoreExceptionCode code, String message) {
		super(code, message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1526902068954824368L;

}
