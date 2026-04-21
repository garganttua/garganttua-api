package com.garganttua.api.core.security.authentication;

import com.garganttua.api.spec.security.authentication.IAuthenticationRequest;

public record AuthenticationRequest(
	String login,
	Object credentials,
	String tenantId) implements IAuthenticationRequest {

}
