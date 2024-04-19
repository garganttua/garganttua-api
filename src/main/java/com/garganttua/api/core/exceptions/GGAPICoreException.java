package com.garganttua.api.core.exceptions;

import lombok.Getter;

public abstract class GGAPICoreException extends Exception {

	private static final long serialVersionUID = 7855765591949705798L;
	
	@Getter
	protected GGAPICoreExceptionCode code = GGAPICoreExceptionCode.UNKNOWN_ERROR;
	
	protected GGAPICoreException(GGAPICoreExceptionCode code, String message) {
		super(message);
		this.code = code;
	}
	
	protected GGAPICoreException(GGAPICoreExceptionCode code, String message, Exception exception) {
		super(message, exception);
		this.code = code;
	}
	
	protected GGAPICoreException(Exception exception) {
		super(exception.getMessage(), exception);
		if( GGAPICoreException.class.isAssignableFrom(exception.getClass()) ){
			this.code = ((GGAPICoreException) exception).getCode();
		} else {
			this.code = GGAPICoreExceptionCode.UNKNOWN_ERROR;
		}
	}

}
