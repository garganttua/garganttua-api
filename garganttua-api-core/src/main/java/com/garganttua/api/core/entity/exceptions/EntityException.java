/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.core.entity.exceptions;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;

import lombok.Getter;

@Getter
public class EntityException extends CoreException {
	
	private static final long serialVersionUID = 2528143588504398416L;

	public EntityException(CoreExceptionCode code, String message, Exception exception) {
		super(code, message, exception);
	}
	
	public EntityException(CoreExceptionCode code, String message) {
		super(code, message);
	}
	
	public EntityException(Exception exception) {
		super(exception);
	}

}
