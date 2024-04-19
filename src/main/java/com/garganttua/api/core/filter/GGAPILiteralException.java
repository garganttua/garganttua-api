package com.garganttua.api.core.filter;

import com.garganttua.api.core.exceptions.GGAPICoreException;
import com.garganttua.api.core.exceptions.GGAPICoreExceptionCode;

public class GGAPILiteralException extends GGAPICoreException {

	public GGAPILiteralException(GGAPICoreExceptionCode code, String message) {
		super(code, message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1526902068954824368L;

}
