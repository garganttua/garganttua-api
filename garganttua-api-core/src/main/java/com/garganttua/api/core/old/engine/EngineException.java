package com.garganttua.api.core.engine;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;

public class EngineException extends CoreException {

	public EngineException(CoreExceptionCode code, String message, Exception exception) {
		super(code, message, exception);
	}
	
	public EngineException(CoreExceptionCode code, String message) {
		super(code, message);
	}
	
	public EngineException(Exception exception) {
		super(exception);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 3026591383888353678L;

}
