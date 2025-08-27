package com.garganttua.api.spec.security.authentication;

import com.garganttua.api.spec.domain.IDomain;

public interface IAuthenticationRequest {

	IDomain getDomain();

	String getTenantId();

	String getPrincipal();

	Object getCredentials();
	
	Class<?> getAuthenticationType();
	
	Object getAuthentication();

	void setAuthentication(Object authentication);

  void setTenantId(String tenantId);

}
