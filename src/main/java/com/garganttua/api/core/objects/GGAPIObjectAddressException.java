package com.garganttua.api.core.objects;

import com.garganttua.api.core.exceptions.GGAPICoreException;
import com.garganttua.api.core.exceptions.GGAPICoreExceptionCode;

public class GGAPIObjectAddressException extends GGAPICoreException {

	public GGAPIObjectAddressException(GGAPICoreExceptionCode code, String string) {
		super(code, string);
	}

	private static final long serialVersionUID = 2732095843634378815L;

}
