package com.garganttua.api.core.mapper;

import com.garganttua.api.core.exceptions.GGAPICoreException;
import com.garganttua.api.core.exceptions.GGAPICoreExceptionCode;

public class GGAPIMapperException extends GGAPICoreException {

	private static final long serialVersionUID = 3629256996026750672L;

	public GGAPIMapperException(GGAPICoreExceptionCode code, String message, Exception exception) {
		super(code, message, exception);
	}
	
	public GGAPIMapperException(GGAPICoreExceptionCode code, String message) {
		super(code, message);
	}
	
	public GGAPIMapperException(Exception exception) {
		super(exception);
	}

}
