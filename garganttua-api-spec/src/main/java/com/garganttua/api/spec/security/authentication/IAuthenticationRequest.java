package com.garganttua.api.spec.security.authentication;

public interface IAuthenticationRequest {

	String login();

	byte[] credentials();

	/**
	 * Optional when multi-tenancy is disabled or when the authentication
	 * identifier has system-wide unique scope.
	 */
	String tenantId();

}
