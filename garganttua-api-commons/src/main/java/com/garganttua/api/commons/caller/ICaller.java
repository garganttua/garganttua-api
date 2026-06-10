package com.garganttua.api.commons.caller;

import java.util.List;

public interface ICaller {

	String tenantId();

	String requestedTenantId();

	String ownerId();

	/**
	 * The owner the caller wants to OPERATE ON — the dual of {@link #requestedTenantId()}
	 * for ownership. For a regular caller it equals {@link #ownerId()}; a super owner may
	 * target another owner via this field (cross-owner). Defaults to {@code ownerId()} so
	 * existing {@link ICaller} implementations need no change.
	 */
	default String requestedOwnerId() {
		return ownerId();
	}

	String callerId();

	boolean superTenant();

	boolean superOwner();

	List<String> authorities();

	default boolean anonymous() {
		return callerId() == null;
	}

	/**
	 * Builds an {@link ICaller} from explicit values — usable from {@code commons}
	 * (where the runtime {@code Caller} of {@code core} is not visible), e.g. by
	 * {@code IAuthentication.reconcile(...)}.
	 */
	static ICaller of(String tenantId, String requestedTenantId, String callerId, String ownerId,
			String requestedOwnerId, boolean superTenant, boolean superOwner, List<String> authorities) {
		return new ResolvedCaller(tenantId, requestedTenantId, callerId, ownerId, requestedOwnerId,
				superTenant, superOwner, authorities);
	}

	/** Plain {@link ICaller} carrier for {@link #of}. */
	record ResolvedCaller(String tenantId, String requestedTenantId, String callerId, String ownerId,
			String requestedOwnerId, boolean superTenant, boolean superOwner, List<String> authorities)
			implements ICaller {
	}

}
