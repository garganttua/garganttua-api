package com.garganttua.api.spec.security.authentication;

import com.garganttua.api.spec.service.IOperationResponse;

public interface IAuthenticationRequestBuilder {

	IAuthenticationRequestBuilder id(String id);

	IAuthenticationRequestBuilder credentials(byte[] credentials);

	/**
	 * Optional when multi-tenancy is disabled or when the authentication
	 * identifier has system-wide unique scope.
	 */
	IAuthenticationRequestBuilder tenantId(String tenantId);

	IAuthenticationRequest build();

	default IOperationResponse execute() {
		return build().execute();
	}

}
