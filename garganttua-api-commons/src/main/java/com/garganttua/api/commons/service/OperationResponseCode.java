package com.garganttua.api.commons.service;

import com.garganttua.api.commons.ApiException;

public enum OperationResponseCode {
	NOT_AVAILABLE, SERVER_ERROR, CLIENT_ERROR, CREATED, NOT_FOUND, OK, UPDATED, DELETED, UNAUTHORIZED, FORBIDDEN,
	CONFLICT, NOT_ACCEPTABLE, UNSUPPORTED_MEDIA_TYPE;

	public static OperationResponseCode fromExceptionCode(ApiException e) {
		OperationResponseCode code = SERVER_ERROR;
		
		switch(e.getCode()) {
		default:
			code = SERVER_ERROR;
			break;
		/* case ENTITY_ALREADY_EXISTS:
		case BAD_REQUEST: 
			code = CLIENT_ERROR;
			break;
		case ENTITY_NOT_FOUND:
		case OBJECT_NOT_FOUND:
			code = NOT_FOUND;
			break;
		case TOKEN_EXPIRED:
		case TOKEN_NOT_FOUND:
		case TOKEN_REVOKED:
		case GENERIC_SECURITY_ERROR:
		case KEY_ERROR:
		case KEY_EXPIRED:
		case KEY_REVOKED: 
			code = UNAUTHORIZED;
			break;*/
		}

		return code;
	}
}
