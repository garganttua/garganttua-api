package com.garganttua.api.spec.security.authentication;

import com.garganttua.api.spec.definition.IDomainDefinition;

public interface IAuthenticationRequest {

	IDomainDefinition<?> getDomainDefinition();

	String getTenantId();

	String getPrincipal();

	Object getCredentials();

	Class<?> getAuthenticationType();

	Object getAuthentication();

	void setAuthentication(Object authentication);

	void setTenantId(String tenantId);

}
