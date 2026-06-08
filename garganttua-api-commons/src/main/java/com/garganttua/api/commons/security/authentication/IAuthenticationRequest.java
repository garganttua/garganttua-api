package com.garganttua.api.commons.security.authentication;

public interface IAuthenticationRequest {

	String login();

	/**
	 * Credentials payload. Shape depends on the authentication flow:
	 * <ul>
	 *   <li><b>Login+password flow</b> — {@code byte[]} holding the password
	 *       (populated by the client on {@code /authenticate}).</li>
	 *   <li><b>Token verification flow</b> — the decoded authorization entity
	 *       (populated by {@code VERIFY_AUTHORIZATION.gs} after the matching
	 *       {@code IAuthorizationProtocol} produces it from the raw header,
	 *       or supplied directly by an in-process caller as Mode B). No
	 *       interface contract on the entity — the framework treats it as a
	 *       POJO and consults the DSL-declared fields for signature /
	 *       expiration / revocation.</li>
	 * </ul>
	 * {@link com.garganttua.api.commons.security.authentication.IAuthentication}
	 * strategies pattern-match on the runtime type and yield to the next
	 * strategy in the {@code tryAuthenticate} cascade when they do not
	 * recognise the credentials shape.
	 */
	Object credentials();

}
