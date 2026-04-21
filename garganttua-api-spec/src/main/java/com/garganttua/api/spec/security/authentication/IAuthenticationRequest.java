package com.garganttua.api.spec.security.authentication;

public interface IAuthenticationRequest {

	String login();

	/**
	 * Credentials payload. Shape depends on the authentication flow:
	 * <ul>
	 *   <li><b>Login+password flow</b> — {@code byte[]} holding the password
	 *       (populated by the client on {@code /authenticate}).</li>
	 *   <li><b>Token verification flow</b> — the decoded
	 *       {@link com.garganttua.api.spec.security.authorization.IAuthorization}
	 *       instance (populated by {@code VERIFY_AUTHORIZATION.gs} after the
	 *       {@code IAuthorizationProtocol} decodes the raw header).</li>
	 * </ul>
	 * {@link com.garganttua.api.spec.security.authentication.IAuthentication}
	 * strategies pattern-match on the runtime type and yield to the next
	 * strategy in the {@code tryAuthenticate} cascade when they do not
	 * recognise the credentials shape.
	 */
	Object credentials();

	/**
	 * Optional when multi-tenancy is disabled or when the authentication
	 * identifier has system-wide unique scope.
	 */
	String tenantId();

}
