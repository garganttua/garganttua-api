package com.garganttua.api.service.rest;

import org.springframework.http.HttpStatus;

import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.factory.GGAPIFactoryException;
import com.garganttua.api.core.tools.GGAPIObjectReflectionHelperExcpetion;

public class GGAPIHttpErrorCodeTranslator {

	public static HttpStatus getHttpErrorCode(Exception e) {
		
		if( e instanceof GGAPIEntityException ) {
			return GGAPIHttpErrorCodeTranslator.getHttpErrorCodeFromEntityException((GGAPIEntityException) e);
		} else if( e instanceof GGAPIFactoryException ) {
			return GGAPIHttpErrorCodeTranslator.getHttpErrorCodeFromFactoryException((GGAPIFactoryException) e);
		} else if( e instanceof GGAPIObjectReflectionHelperExcpetion ) {
			return GGAPIHttpErrorCodeTranslator.getHttpErrorCodeFromObjectReflectionHelper((GGAPIObjectReflectionHelperExcpetion) e);
		}

		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	private static HttpStatus getHttpErrorCodeFromObjectReflectionHelper(GGAPIObjectReflectionHelperExcpetion e) {
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	private static HttpStatus getHttpErrorCodeFromFactoryException(GGAPIFactoryException e) {
		switch(e.getCode()) {
		case GGAPIFactoryException.BAD_ENTITY:
			return HttpStatus.INTERNAL_SERVER_ERROR;
		case GGAPIFactoryException.ENTITY_INJECTION_ERROR:
			return HttpStatus.INTERNAL_SERVER_ERROR;
		default:
		case GGAPIEntityException.UNKNOWN_ERROR:
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
	}

	private static HttpStatus getHttpErrorCodeFromEntityException(GGAPIEntityException e) {
		switch(e.getCode()) {
		case GGAPIEntityException.BAD_REQUEST:
			return HttpStatus.BAD_REQUEST;
		case GGAPIEntityException.DELETION_ERROR:
			return HttpStatus.INTERNAL_SERVER_ERROR;
		case GGAPIEntityException.ENTITY_ALREADY_EXISTS:
			return HttpStatus.BAD_REQUEST;
		case GGAPIEntityException.ENTITY_DEFINITION_ERROR:
			return HttpStatus.INTERNAL_SERVER_ERROR;
		case GGAPIEntityException.SET_FIELD_VALUE:
			return HttpStatus.INTERNAL_SERVER_ERROR;
		default:
		case GGAPIEntityException.UNKNOWN_ERROR:
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
	}

}
