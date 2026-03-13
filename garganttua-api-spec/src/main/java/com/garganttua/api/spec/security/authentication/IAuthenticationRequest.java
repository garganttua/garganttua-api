package com.garganttua.api.spec.security.authentication;

import com.garganttua.api.spec.service.IOperationResponse;

public interface IAuthenticationRequest {

	String getId();

	byte[] getCredentials();

	/**
	 * Optional when multi-tenancy is disabled or when the authentication
	 * identifier has system-wide unique scope.
	 */
	String getTenantId();

	void setTenantId(String tenantId);

	IOperationResponse execute();

}
