package com.garganttua.api.core.runtime.filter;

import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;

public class LiteralException extends CoreException {

	public LiteralException(String message) {
		super(CoreExceptionCode.FILTER_ERROR, message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1526902068954824368L;

}
