package com.garganttua.api.spec;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface IGGAPIEntityWithTenant {

	@JsonProperty(value = "tenantId")
	public String getTenantId();
	
	public void setTenantId(String tenantId);
	
}
