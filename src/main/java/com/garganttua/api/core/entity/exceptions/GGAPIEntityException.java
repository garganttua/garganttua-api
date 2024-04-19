/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.core.entity.exceptions;

import com.garganttua.api.core.exceptions.GGAPICoreException;
import com.garganttua.api.core.exceptions.GGAPICoreExceptionCode;

import lombok.Getter;

@Getter
public class GGAPIEntityException extends GGAPICoreException {
	
	private static final long serialVersionUID = 2528143588504398416L;

	public GGAPIEntityException(GGAPICoreExceptionCode code, String message, Exception exception) {
		super(code, message, exception);
	}
	
	public GGAPIEntityException(GGAPICoreExceptionCode code, String message) {
		super(code, message);
	}
	
	public GGAPIEntityException(Exception exception) {
		super(exception);
	}

}
