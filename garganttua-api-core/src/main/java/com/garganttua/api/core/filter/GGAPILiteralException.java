package com.garganttua.api.core.filter;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;

public class GGAPILiteralException extends GGAPIException {

	public GGAPILiteralException(GGAPIExceptionCode code, String message) {
		super(code, message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1526902068954824368L;

}
