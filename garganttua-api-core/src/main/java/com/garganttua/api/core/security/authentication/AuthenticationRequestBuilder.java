package com.garganttua.api.core.security.authentication;

import java.util.List;

import com.garganttua.api.spec.security.authentication.IAuthenticationRequest;
import com.garganttua.api.spec.security.authentication.IAuthenticationRequestBuilder;
import com.garganttua.api.spec.security.context.IAuthenticationContext;

public class AuthenticationRequestBuilder implements IAuthenticationRequestBuilder {

	private final List<IAuthenticationContext> authenticationContexts;

	private String id;
	private byte[] credentials;
	private String tenantId;

	public AuthenticationRequestBuilder(List<IAuthenticationContext> authenticationContexts) {
		this.authenticationContexts = authenticationContexts;
	}

	@Override
	public IAuthenticationRequestBuilder id(String id) {
		this.id = id;
		return this;
	}

	@Override
	public IAuthenticationRequestBuilder credentials(byte[] credentials) {
		this.credentials = credentials;
		return this;
	}

	@Override
	public IAuthenticationRequestBuilder tenantId(String tenantId) {
		this.tenantId = tenantId;
		return this;
	}

	@Override
	public IAuthenticationRequest build() {
		return new AuthenticationRequest(authenticationContexts, id, credentials, tenantId);
	}

}
