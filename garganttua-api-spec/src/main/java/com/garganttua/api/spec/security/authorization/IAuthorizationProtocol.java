package com.garganttua.api.spec.security.authorization;

import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.context.IApi;

/**
 * Scheme-level decoder for authorization headers.
 * <p>
 * An {@code IAuthorizationProtocol} handles exactly one authentication scheme
 * (as defined by RFC 7235 § 2.1 — {@code Bearer}, {@code Basic}, {@code ApiKey},
 * or custom). It converts the raw header value into a typed {@link IAuthorization}
 * that the rest of the pipeline can validate and reason about.
 * <p>
 * The framework discriminates protocols by comparing {@link #scheme()} with the
 * first whitespace-delimited token of the raw {@code Authorization} header,
 * case-insensitively. Registration order acts as priority: register more specific
 * protocols before generic ones.
 * <p>
 * Invoked from {@code VERIFY_AUTHORIZATION.gs} only when the target operation requires
 * authorization and no {@code IAuthorization} has been pre-populated on the
 * request (Mode B pass-through). Anonymous operations never hit a protocol.
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
	 * Decode the portion of the {@code Authorization} header AFTER the scheme
	 * token. For a header {@code "Bearer eyJhbGc…"}, the value passed here is
	 * {@code "eyJhbGc…"}.
	 *
	 * @param rawAuthorizationValue the header value after the scheme token
	 * @param api                   the enclosing API context (access to registered
	 *                              beans, decoders, domains, etc.)
	 * @return a typed {@link IAuthorization} representing the decoded token
	 * @throws ApiException if the value cannot be decoded into a valid authorization
	 */
	IAuthorization decode(String rawAuthorizationValue, IApi api) throws ApiException;
}
