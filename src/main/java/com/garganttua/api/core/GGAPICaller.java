package com.garganttua.api.core;

import java.util.List;

import com.garganttua.api.engine.GGAPIDomain;
import com.garganttua.api.security.authorization.IGGAPIAccessRule;

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
	private GGAPIDomain domain;
	
	@Getter
	@Setter
	private boolean anonymous;

	@Override
	public String toString() {
		return String.format(
				"[tenantId [%s], requestedTenantId [%s], ownerId [%s], superTenant [%s], superOwner [%s], accessRule [%s], anonymous [%s]]",
				tenantId, requestedTenantId, ownerId, superTenant, superOwner, accessRule, anonymous);
	}

	@Override
	public List<String> getAuthorities() {
		// TODO Auto-generated method stub
		return null;
	}

}
