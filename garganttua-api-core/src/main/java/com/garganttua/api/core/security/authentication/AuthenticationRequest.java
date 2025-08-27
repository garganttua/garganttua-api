package com.garganttua.api.core.security.authentication;

import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.security.authentication.IAuthenticationRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
public class AuthenticationRequest implements IAuthenticationRequest {

	private IDomain domain;
	@Setter
	private String tenantId;
	private String principal;
	private Object credentials;
	private Class<?> authenticationType;
	@Setter
	private Object authentication;

	public AuthenticationRequest(IDomain domain, String tenantId, String principal, Object credentials,
			Class<?> authenticationType) {
		this.domain = domain;
		this.tenantId = tenantId;
		this.principal = principal;
		this.credentials = credentials;
		this.authenticationType = authenticationType;
	}

}
