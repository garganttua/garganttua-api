package com.garganttua.api.spec.security.authentication;

import com.garganttua.api.spec.definition.IDomainDefinition;
import com.garganttua.core.reflection.IClass;

public interface IAuthenticationRequest {

	IDomainDefinition<?> getDomainDefinition();

	String getTenantId();

	String getPrincipal();

	Object getCredentials();

	IClass<?> getAuthenticationType();

	Object getAuthentication();

	void setAuthentication(Object authentication);

	void setTenantId(String tenantId);

}
