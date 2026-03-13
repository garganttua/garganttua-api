package com.garganttua.api.core.security.authentication;

import java.util.List;

import com.garganttua.api.spec.security.IApiSecurityContext;
import com.garganttua.api.spec.security.authentication.IAuthenticationRequest;
import com.garganttua.api.spec.security.context.IAuthenticationContext;
import com.garganttua.api.spec.service.IOperationResponse;

public class AuthenticationRequest implements IAuthenticationRequest {

	private final IApiSecurityContext apiSecurityContext;
	private final List<IAuthenticationContext> authenticationContexts;
	private final String id;
	private final byte[] credentials;
	private String tenantId;

	AuthenticationRequest(IApiSecurityContext apiSecurityContext,
			List<IAuthenticationContext> authenticationContexts,
			String id, byte[] credentials, String tenantId) {
		this.apiSecurityContext = apiSecurityContext;
		this.authenticationContexts = authenticationContexts;
		this.id = id;
		this.credentials = credentials;
		this.tenantId = tenantId;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public byte[] getCredentials() {
		return this.credentials;
	}

	@Override
	public String getTenantId() {
		return this.tenantId;
	}

	@Override
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	@Override
	public IOperationResponse execute() {
		return this.apiSecurityContext.authenticate(this);
	}

}
