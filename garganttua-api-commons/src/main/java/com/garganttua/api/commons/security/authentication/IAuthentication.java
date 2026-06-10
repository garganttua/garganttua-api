package com.garganttua.api.commons.security.authentication;

import java.util.List;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;

/**
 * Result of an authentication — and, since "verifying an authorization" ≡ "an
 * authentication", also the result of token verification. It carries the full
 * security context of the authenticated principal: identity (tenant / owner),
 * privileges (super-tenant / super-owner), granted authorities, and the
 * authorization (token) itself.
 *
 * <p>Once produced by a successful {@code authenticate}, an {@code IAuthentication}
 * is <strong>trusted</strong>: the pipeline calls {@link #reconcile(ICaller)} to
 * fold the (untrusted) protocol-layer caller into this verified identity.
 */
public interface IAuthentication {

	boolean authenticated();

	Object principal();

	Object credentials();

	Object authorization();

	List<String> authorities();

	/** The authenticated principal's tenant. */
	String tenantId();

	/** The authenticated principal's (qualified) owner id. */
	String ownerId();

	/** Whether the principal belongs to a super tenant (cross-tenant capability). */
	boolean isSuperTenant();

	/** Whether the principal is a super owner (cross-owner capability). */
	boolean isSuperOwner();

	boolean credentialsNonExpired();

	boolean enabled();

	boolean accountNonLocked();

	boolean accountNonExpired();

	/**
	 * Reconciles the (untrusted) protocol-layer caller with THIS verified, trusted
	 * authentication, returning the caller the pipeline must use. The default
	 * implements the framework rules (R1-R3, see docs/repository-filters.md §1.2):
	 *
	 * <ul>
	 *   <li>identity comes from the token: {@code tenantId}/{@code ownerId} are the
	 *       authenticated principal's, super flags are this authentication's — the
	 *       protocol headers cannot spoof them;</li>
	 *   <li>a header tenant/owner that DIFFERS from the token's is a <em>cross-target</em>
	 *       request: allowed only for a super tenant/owner (→ {@code requestedTenantId}/
	 *       {@code requestedOwnerId} carry the target), otherwise <strong>rejected</strong>;</li>
	 *   <li>no header → operate on the token's own tenant/owner (a super principal with
	 *       no target sees all tenants/owners — {@code requested* = null}).</li>
	 * </ul>
	 *
	 * Overridable per-domain via the DSL (a custom reconcile method binder).
	 *
	 * @throws ApiException when a non-super caller's header contradicts the token
	 *         (cross-target without the capability) — mapped to 403 by the pipeline.
	 */
	default ICaller reconcile(ICaller protocolCaller) {
		String headerTenant = protocolCaller == null ? null : protocolCaller.tenantId();
		String headerOwner = protocolCaller == null ? null : protocolCaller.ownerId();
		String callerId = protocolCaller == null ? null : protocolCaller.callerId();

		String[] tenant = reconcileScope(headerTenant, tenantId(), isSuperTenant(), "tenant");
		String[] owner = reconcileScope(headerOwner, ownerId(), isSuperOwner(), "owner");

		// Authorities come from the verified authentication. A null (NOT empty) list means
		// the authentication did not resolve them (e.g. a Mode-B trusted in-process token) —
		// fall back to the protocol caller's. An empty list is authoritative (no roles).
		List<String> resolvedAuthorities = authorities() != null
				? authorities()
				: (protocolCaller == null ? null : protocolCaller.authorities());

		return ICaller.of(tenant[0], tenant[1], callerId, owner[0], owner[1],
				isSuperTenant(), isSuperOwner(), resolvedAuthorities);
	}

	/**
	 * Resolves a dimension (tenant or owner) into {@code {home, requested}} from the
	 * header value against the token's value:
	 * <ul>
	 *   <li>the token does NOT carry this dimension (null — e.g. a non-tenant entity, or a
	 *       Mode-B trusted token without a registered domain) → it imposes no constraint:
	 *       keep the header for both (the server super-recompute downstream still applies);</li>
	 *   <li>no header → the token's own scope ({@code requested=null} for a super principal
	 *       = "all");</li>
	 *   <li>header == token → the shared value;</li>
	 *   <li>header ≠ token → a cross-target: {@code home=token, requested=header}, allowed only
	 *       when {@code superCapability}, else rejected.</li>
	 * </ul>
	 */
	private static String[] reconcileScope(String header, String token, boolean superCapability, String dimension) {
		boolean noToken = token == null || token.isBlank();
		boolean noHeader = header == null || header.isBlank();
		if (noToken) {
			return new String[] { noHeader ? null : header, noHeader ? null : header };
		}
		if (noHeader) {
			return new String[] { token, superCapability ? null : token };
		}
		if (token.equals(header)) {
			return new String[] { token, token };
		}
		if (superCapability) {
			return new String[] { token, header };
		}
		throw new ApiException("Authenticated principal's " + dimension + " '" + token
				+ "' is not super; it cannot operate on " + dimension + " '" + header + "'.");
	}

}
