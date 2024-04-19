package com.garganttua.api.core.security.authorization.tokens.jwt;

import com.garganttua.api.core.exceptions.GGAPICoreException;
import com.garganttua.api.core.exceptions.GGAPICoreExceptionCode;

public class GGAPITokenNotFoundException extends GGAPICoreException {

	protected GGAPITokenNotFoundException() {
		super(GGAPICoreExceptionCode.TOKEN_NOT_FOUND, "Token not found");
	}

	private static final long serialVersionUID = -954338843682420388L;

}
