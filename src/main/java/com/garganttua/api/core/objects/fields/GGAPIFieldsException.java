package com.garganttua.api.core.objects.fields;

import com.garganttua.api.core.exceptions.GGAPICoreException;
import com.garganttua.api.core.exceptions.GGAPICoreExceptionCode;

public class GGAPIFieldsException extends GGAPICoreException {

	public GGAPIFieldsException(Exception e) {
		super(e);
	}

	public GGAPIFieldsException(GGAPICoreExceptionCode code, String string, Exception e) {
		super(code, string, e);
	}

	public GGAPIFieldsException(GGAPICoreExceptionCode code, String string) {
		super(code, string);
	}

	private static final long serialVersionUID = -709424867520967955L;

}
