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

		String resolvedTenantId = tenantId();
		String resolvedRequestedTenantId = reconcileScope(headerTenant, tenantId(), isSuperTenant(), "tenant");

		String resolvedOwnerId = ownerId();
		String resolvedRequestedOwnerId = reconcileScope(headerOwner, ownerId(), isSuperOwner(), "owner");

		return ICaller.of(resolvedTenantId, resolvedRequestedTenantId, callerId,
				resolvedOwnerId, resolvedRequestedOwnerId,
				isSuperTenant(), isSuperOwner(), authorities());
	}

	/**
	 * Computes the "requested" scope (tenant or owner) from the header value against
	 * the token's value: no header → token's own scope ({@code null} for a super
	 * principal, meaning "all"); same → the shared value; different → the header is a
	 * cross-target, allowed only when {@code superCapability}, else rejected.
	 */
	private static String reconcileScope(String headerValue, String tokenValue, boolean superCapability,
			String dimension) {
		if (headerValue == null || headerValue.isBlank()) {
			return superCapability ? null : tokenValue;
		}
		if (headerValue.equals(tokenValue)) {
			return tokenValue;
		}
		if (superCapability) {
			return headerValue; // cross-target: the super principal operates on the requested scope
		}
		throw new ApiException("Authenticated principal's " + dimension + " '" + tokenValue
				+ "' is not super; it cannot operate on " + dimension + " '" + headerValue + "'.");
	}

}
