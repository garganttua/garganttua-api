package com.garganttua.api.commons.service;

import java.util.List;

import com.garganttua.api.commons.caller.ICaller;

/**
 * The {@link ICaller} view over a {@link MapBackedOperationRequest}'s args.
 * Named (not anonymous) for the same native-image reason as
 * {@link MapBackedOperationRequest}; its native descriptor is shipped alongside
 * it in this package's {@code reflect-config.json}.
 */
final class MapBackedCaller implements ICaller {

	private final IOperationRequest request;

	MapBackedCaller(IOperationRequest request) {
		this.request = request;
	}

	@Override
	public String tenantId() {
		return this.request.arg(IOperationRequest.TENANT_ID).orElse(null);
	}

	@Override
	public String requestedTenantId() {
		return this.request.arg(IOperationRequest.REQUESTED_TENANT_ID).orElse(null);
	}

	@Override
	public String callerId() {
		return this.request.arg(IOperationRequest.CALLER_ID).orElse(null);
	}

	@Override
	public String ownerId() {
		return this.request.arg(IOperationRequest.OWNER_ID).orElse(null);
	}

	@Override
	public boolean superTenant() {
		return Boolean.TRUE.equals(this.request.arg(IOperationRequest.SUPER_TENANT).orElse(null));
	}

	@Override
	public boolean superOwner() {
		return Boolean.TRUE.equals(this.request.arg(IOperationRequest.SUPER_OWNER).orElse(null));
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> authorities() {
		return (List<String>) this.request.arg(IOperationRequest.AUTHORITIES).orElse(null);
	}
}
