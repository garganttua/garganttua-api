package com.garganttua.api.spec;

import lombok.Getter;

public class GGAPIException extends Exception {

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

	public static GGAPIException findFirstInException(Exception exception) {
        Throwable cause = exception.getCause();
        while (cause != null) {
            if (cause instanceof GGAPIException) {
                return (GGAPIException) cause;
            }
            cause = cause.getCause();
        }
        return null;
	}
	
	public static void processException(Exception e) throws GGAPIException {
		GGAPIException apiException = GGAPIException.findFirstInException(e);
		if( apiException != null ) {
			throw apiException; 
		} else {
			throw new GGAPIException(GGAPIExceptionCode.UNKNOWN_ERROR, e.getMessage(), e);
		}
	}
}
