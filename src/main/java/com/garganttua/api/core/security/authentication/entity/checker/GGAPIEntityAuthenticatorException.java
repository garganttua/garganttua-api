package com.garganttua.api.core.security.authentication.entity.checker;

import com.garganttua.api.core.objects.query.GGAPIObjectQueryException;
import com.garganttua.api.spec.GGAPICoreException;
import com.garganttua.api.spec.GGAPICoreExceptionCode;

public class GGAPIEntityAuthenticatorException extends GGAPICoreException {

	
	public GGAPIEntityAuthenticatorException(GGAPICoreExceptionCode code, String string) {
		super(code, string);
	}
	public GGAPIEntityAuthenticatorException(GGAPIObjectQueryException e) {
		super(e);
	}
	private static final long serialVersionUID = 7582285443985460141L;
	public static final int ENTITY_DEFINITION_ERROR = 0;

}
