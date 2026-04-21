package com.garganttua.api.core.caller;

import java.util.List;

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

	public static ICaller createSuperCaller() {
		return new Caller(null, null, null, null, true, true, null);
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
