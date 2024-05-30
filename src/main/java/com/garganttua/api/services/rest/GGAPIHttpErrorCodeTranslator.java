package com.garganttua.api.services.rest;

import org.springframework.http.HttpStatus;

import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.spec.GGAPICoreException;

public class GGAPIHttpErrorCodeTranslator {

	public static HttpStatus getHttpErrorCode(Exception e) {
		
		if( e instanceof GGAPICoreException ) {
			return GGAPIHttpErrorCodeTranslator.getHttpErrorCodeFromCoreException((GGAPIEntityException) e);
		} 

		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	private static HttpStatus getHttpErrorCodeFromCoreException(GGAPIEntityException e) {
		switch(e.getCode()) {
		default:
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
	}

}
