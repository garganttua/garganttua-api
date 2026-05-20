package com.garganttua.api.commons.security.authorization;

import com.garganttua.api.commons.ApiException;

/**
 * Thrown when the raw {@code Authorization} header cannot be parsed because its
 * format is malformed (missing scheme/value separator, empty value, etc.).
 *
 * <p>Distinguished from generic {@link ApiException} so the verify-authorization
 * pipeline can map malformed headers to HTTP 400 (client should fix the request)
 * while keeping other authorization failures on 401 (token rejected). The
 * {@code VERIFY_AUTHORIZATION.gs} script catches this type explicitly via
 * {@code ! AuthorizationFormatException.Class -> 400}.
 */
public class AuthorizationFormatException extends ApiException {

	public AuthorizationFormatException(String message) {
		super(message);
	}

	public AuthorizationFormatException(String message, Throwable cause) {
		super(message, cause);
	}
}
