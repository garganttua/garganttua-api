package com.garganttua.api.commons.security.authorization;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.core.reflection.IClass;

/**
 * Scheme-level decoder for authorization headers.
 * <p>
 * An {@code IAuthorizationProtocol} handles exactly one authentication scheme
 * (as defined by RFC 7235 § 2.1 — {@code Bearer}, {@code Basic}, {@code ApiKey},
 * or custom). It converts the raw header value into the user-defined POJO whose
 * class is the protocol's {@link #targetDomain()} entity. No interface contract
 * is imposed on the returned entity: signature, expiration, and revocation are
 * derived from the DSL field declarations on
 * {@code IDomainAuthorizationDefinition} (`.signable().signature(...)`,
 * `.expirable(...)`, `.revokable(...)`).
 * <p>
 * The framework discriminates protocols by comparing {@link #scheme()} with the
 * first whitespace-delimited token of the raw {@code Authorization} header,
 * case-insensitively. Registration order acts as priority: register more specific
 * protocols before generic ones.
 * <p>
 * Invoked from {@code VERIFY_AUTHORIZATION.gs} only when the target operation
 * requires authorization and no authorization entity has been pre-populated on
 * the request (Mode B pass-through). Anonymous operations never hit a protocol.
 * <p>
 * Implementations are registered either:
 * <ul>
 *   <li>manually via {@code IApiBuilder.authorizationProtocol(IAuthorizationProtocol)}
 *       or {@code IApiBuilder.authorizationProtocol(ISupplierBuilder)}, or</li>
 *   <li>automatically via the {@link AuthorizationProtocol} annotation combined with
 *       {@code IApiBuilder.withPackage(...)} + {@code autoDetect(true)}.</li>
 * </ul>
 */
public interface IAuthorizationProtocol {

	/**
	 * The scheme token this protocol handles (first whitespace-delimited word of
	 * the {@code Authorization} header). Matched case-insensitively.
	 * Examples: {@code "Bearer"}, {@code "Basic"}, {@code "ApiKey"}.
	 */
	String scheme();

	/**
	 * Entity class of the domain on which {@code VERIFY_AUTHORIZATION.gs} should
	 * invoke the {@code authenticate} pipeline to validate the decoded token.
	 * <p>
	 * Typical values:
	 * <ul>
	 *   <li>Bearer JWT → the token entity class (dual-role domain: both
	 *       {@code .security().authorization()} and {@code .security().authenticator()}).</li>
	 *   <li>Basic → the authenticator entity class (e.g. {@code User.class}).</li>
	 *   <li>ApiKey → the ApiKey entity class (also dual-role).</li>
	 * </ul>
	 * Looked up at runtime via {@code IApi.getDomains()} matching on
	 * {@code IDomain.getEntityClass()}.
	 */
	IClass<?> targetDomain();

	/**
	 * Decode the portion of the {@code Authorization} header AFTER the scheme
	 * token. For a header {@code "Bearer eyJhbGc…"}, the value passed here is
	 * {@code "eyJhbGc…"}.
	 *
	 * @param rawAuthorizationValue the header value after the scheme token
	 * @param api                   the enclosing API context (access to registered
	 *                              beans, decoders, domains, etc.)
	 * @return the decoded authorization entity — same runtime class as
	 *         {@link #targetDomain()}, no interface contract required
	 * @throws ApiException if the value cannot be decoded into a valid authorization
	 */
	Object decode(String rawAuthorizationValue, IApi api) throws ApiException;
}
