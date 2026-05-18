package com.garganttua.api.core.caller;

import java.util.List;
import java.util.Objects;

import com.garganttua.api.commons.caller.ICaller;

public record Caller(
		String tenantId,
		String requestedTenantId,
		String callerId,
		String ownerId,
		boolean superTenant,
		boolean superOwner,
		List<String> authorities
) implements ICaller {

	/**
	 * @deprecated Produces a caller with {@code tenantId=null}, which
	 *             {@code Domain.invoke()} rejects with
	 *             {@code "No caller provided"}. Use
	 *             {@link #createSuperCaller(String)} instead, passing
	 *             {@code IApi.getSuperTenantId()} so the super caller is bound
	 *             to the configured super-tenant — matching the convention used
	 *             internally by {@code Api.autoCreateMasterTenant()}.
	 */
	@Deprecated
	public static ICaller createSuperCaller() {
		return new Caller(null, null, null, null, true, true, null);
	}

	/**
	 * Builds a super caller pinned to the configured super-tenant id.
	 *
	 * <p>The {@code superTenant=true} / {@code superOwner=true} flags carried
	 * by the returned caller grant a <strong>cross-tenancy capability</strong>:
	 * the repository tenant filter is bypassed (see
	 * {@code RepositoryFilterTools.isSuperTenantWithoutTenant}) so the holder
	 * can read entities across tenant boundaries.
	 *
	 * <p><strong>Important — what this is NOT:</strong> the flag is not a
	 * substitute for an authorization. Any non-anonymous operation invoked
	 * through {@code Domain.invoke()} still runs {@code VERIFY_AUTHORIZATION}
	 * and requires either a pre-populated {@code authorization} arg (Mode B)
	 * or a {@code rawAuthorization} header that decodes successfully (Mode A).
	 * Constructing this caller alone will NOT let you bypass the pipeline.
	 *
	 * <p>Framework-internal operations that need to write/read outside the
	 * pipeline (master tenant bootstrap, internal authz lookups) should use
	 * {@code domain.getRepository()} directly rather than building a super
	 * caller — the repository write/query path is the documented escape hatch.
	 */
	public static ICaller createSuperCaller(String superTenantId) {
		Objects.requireNonNull(superTenantId,
				"superTenantId is required — a super caller must be bound to the configured superTenantId");
		return new Caller(superTenantId, superTenantId, null, null, true, true, null);
	}

	public static ICaller createTenantCaller(String tenantId) {
		return new Caller(tenantId, tenantId, null, null, false, false, null);
	}

	public static ICaller createTenantCallerWithOwnerId(String tenantId, String ownerId) {
		return new Caller(tenantId, tenantId, null, ownerId, false, false, null);
	}

	public Caller withCallerId(String callerId) {
		return new Caller(tenantId, requestedTenantId, callerId, ownerId, superTenant, superOwner, authorities);
	}

	public Caller withOwnerId(String ownerId) {
		return new Caller(tenantId, requestedTenantId, callerId, ownerId, superTenant, superOwner, authorities);
	}

	public Caller withAuthorities(List<String> authorities) {
		return new Caller(tenantId, requestedTenantId, callerId, ownerId, superTenant, superOwner, authorities);
	}

}
