package com.garganttua.api.commons.context;

import com.garganttua.api.commons.operation.Access;

/**
 * Configuration for the framework-provided "list system authorities" endpoint.
 *
 * <p>Returned by {@link IApi#getAuthoritiesEndpoint()} — {@code null} when
 * the endpoint has not been exposed via the DSL. When non-null, the values
 * carry the user-chosen access level and (optionally) the authority required
 * to call the endpoint. Modules that publish the API over a transport (HTTP,
 * RPC, …) read this descriptor to wire the route with the right security
 * filter.
 *
 * <p>This is a pure descriptor: the actual enforcement happens inside
 * {@link IApi#getAuthoritiesForCaller(com.garganttua.api.commons.caller.ICaller)}
 * so the security policy stays in one place and tests can exercise it
 * without a transport layer.
 */
public interface IAuthoritiesEndpoint {

	/**
	 * The access level enforced by {@link IApi#getAuthoritiesForCaller(com.garganttua.api.commons.caller.ICaller)}.
	 * Never {@code null} — defaults to {@link Access#authenticated} when the
	 * user did not call {@code .access(...)} on the DSL sub-builder.
	 */
	Access access();

	/**
	 * Optional authority name required on the caller. {@code null} when the
	 * user did not call {@code .authority(...)} on the DSL — in that case
	 * any caller that passes the {@link #access()} check may call the
	 * endpoint.
	 *
	 * <p>Super-tenant and super-owner callers bypass this check (mirrors
	 * {@code callerHasAuthority}).
	 */
	String authority();

}
