/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.core.entity.exceptions;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;

import lombok.Getter;

@Getter
public class GGAPIEntityException extends GGAPIException {
	
	private static final long serialVersionUID = 2528143588504398416L;

	public GGAPIEntityException(GGAPIExceptionCode code, String message, Exception exception) {
		super(code, message, exception);
	}
	
	public GGAPIEntityException(GGAPIExceptionCode code, String message) {
		super(code, message);
	}
	
	public GGAPIEntityException(Exception exception) {
		super(exception);
	}

}
