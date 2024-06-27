package com.garganttua.api.core;

import java.util.List;

import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.IGGAPIAccessRule;

import lombok.Getter;
import lombok.Setter;

public class GGAPICaller implements IGGAPICaller {

	@Getter
	@Setter
	private String tenantId;

	@Getter
	@Setter
	private String requestedTenantId;

	@Getter
	@Setter
	private String ownerId;

	@Getter
	@Setter
	private boolean superTenant;

	@Getter
	@Setter
	private boolean superOwner;

	@Getter
	@Setter
	private IGGAPIAccessRule accessRule;

	@Getter
	@Setter
	private IGGAPIDomain domain;
	
	@Getter
	@Setter
	private boolean anonymous;

	@Getter
	@Setter
	private List<String> authorities;

	@Override
	public String toString() {
		return String.format(
				"[tenantId [%s], requestedTenantId [%s], ownerId [%s], superTenant [%s], superOwner [%s], accessRule [%s], anonymous [%s]]",
				tenantId, requestedTenantId, ownerId, superTenant, superOwner, accessRule, anonymous);
	}

}
