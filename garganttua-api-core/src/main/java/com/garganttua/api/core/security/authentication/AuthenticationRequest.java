package com.garganttua.api.core.security.authentication;

import com.garganttua.api.commons.security.authentication.IAuthenticationRequest;

public record AuthenticationRequest(
	String login,
	Object credentials) implements IAuthenticationRequest {

}
