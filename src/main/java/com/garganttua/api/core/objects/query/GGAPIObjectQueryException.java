package com.garganttua.api.core.objects.query;

import com.garganttua.api.core.exceptions.GGAPICoreException;
import com.garganttua.api.core.exceptions.GGAPICoreExceptionCode;

public class GGAPIObjectQueryException extends GGAPICoreException {

	public GGAPIObjectQueryException(GGAPICoreExceptionCode code, String string) {
		super(code, string);
	}

	public GGAPIObjectQueryException(Exception e) {
		super(e);
	}

	private static final long serialVersionUID = 6029849216646775106L;

}
