package com.garganttua.api.core.security.keys;

import com.garganttua.api.spec.GGAPICoreException;
import com.garganttua.api.spec.GGAPICoreExceptionCode;

public class GGAPIKeyExpiredException extends GGAPICoreException {

	public GGAPIKeyExpiredException(String string) {
		super(GGAPICoreExceptionCode.TOKEN_EXPIRED, string);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1665224263075512986L;

}