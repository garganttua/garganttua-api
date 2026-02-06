package com.garganttua.api.spec.caller;

import java.util.List;

public interface ICaller {

	String tenantId();

	String requestedTenantId();

	String ownerId();

	String callerId();

	boolean superTenant();

	boolean superOwner();

	List<String> authorities();

	default boolean anonymous() {
		return callerId() == null;
	}

}
