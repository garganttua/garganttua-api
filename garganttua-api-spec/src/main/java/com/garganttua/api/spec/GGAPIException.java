package com.garganttua.api.spec;

import lombok.Getter;

public abstract class GGAPIException extends Exception {

	private static final long serialVersionUID = 7855765591949705798L;
	
	@Getter
	protected GGAPIExceptionCode code = GGAPIExceptionCode.UNKNOWN_ERROR;
	
	protected GGAPIException(GGAPIExceptionCode code, String message) {
		super(message);
		this.code = code;
	}
	
	protected GGAPIException(GGAPIExceptionCode code, String message, Exception exception) {
		super(message, exception);
		this.code = code;
	}
	
	protected GGAPIException(Exception exception) {
		super(exception.getMessage(), exception);
		if( GGAPIException.class.isAssignableFrom(exception.getClass()) ){
			this.code = ((GGAPIException) exception).getCode();
		} else {
			this.code = GGAPIExceptionCode.UNKNOWN_ERROR;
		}
	}
}
