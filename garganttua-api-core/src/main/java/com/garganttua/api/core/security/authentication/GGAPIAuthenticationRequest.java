package com.garganttua.api.core.security.authentication;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationRequest;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GGAPIAuthenticationRequest implements IGGAPIAuthenticationRequest {

	private IGGAPIDomain domain;
	private String tenantId;
	private String principal;
	private Object credentials;
	private Class<?> authenticationType;
	
}
