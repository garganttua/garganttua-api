package com.garganttua.api.core.objects.utils;

import com.garganttua.api.core.exceptions.GGAPICoreException;
import com.garganttua.api.core.exceptions.GGAPICoreExceptionCode;

public class GGAPIObjectReflectionHelperExcpetion extends GGAPICoreException {

	private static final long serialVersionUID = -4059467497613214724L;
	
	public GGAPIObjectReflectionHelperExcpetion(GGAPICoreExceptionCode code, String message, Exception exception) {
		super(code, message, exception);
	}
	
	public GGAPIObjectReflectionHelperExcpetion(GGAPICoreExceptionCode code, String message) {
		super(code, message);
	}
	
	public GGAPIObjectReflectionHelperExcpetion(Exception exception) {
		super(exception);
	}

}
