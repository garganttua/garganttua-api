package com.garganttua.api.core.entity.factory;

import com.garganttua.api.spec.GGAPICoreException;
import com.garganttua.api.spec.GGAPICoreExceptionCode;

public class GGAPIFactoryException extends GGAPICoreException {

	public GGAPIFactoryException(Exception e) {
		super(e);
	}
	public GGAPIFactoryException(String string) {
		super(GGAPICoreExceptionCode.GENERIC_FACTORY_EXCEPTION, string);
	}
	public GGAPIFactoryException(GGAPICoreExceptionCode code, String string) {
		super(code, string);
	}
	public GGAPIFactoryException(GGAPICoreExceptionCode code, String string, Exception e) {
		super(code, string, e);
	}

	private static final long serialVersionUID = 2731911397218146961L;

}
