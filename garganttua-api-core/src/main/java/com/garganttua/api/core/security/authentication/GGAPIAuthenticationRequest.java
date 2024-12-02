package com.garganttua.api.core.security.authentication;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
public class GGAPIAuthenticationRequest implements IGGAPIAuthenticationRequest {

	private IGGAPIDomain domain;
	private String tenantId;
	private String principal;
	private Object credentials;
	private Class<?> authenticationType;
	@Setter
	private Object authentication;

	public GGAPIAuthenticationRequest(IGGAPIDomain domain, String tenantId, String principal, Object credentials,
			Class<?> authenticationType) {
		this.domain = domain;
		this.tenantId = tenantId;
		this.principal = principal;
		this.credentials = credentials;
		this.authenticationType = authenticationType;
	}

}
