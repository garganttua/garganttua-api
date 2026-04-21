package com.garganttua.api.core.filter;

import com.garganttua.api.commons.ApiException;

public class FilterException extends ApiException {

	private static final int FILTER_ERROR_CODE = 300;

	public FilterException(String message) {
		super(FILTER_ERROR_CODE, message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1526902068954824368L;

}