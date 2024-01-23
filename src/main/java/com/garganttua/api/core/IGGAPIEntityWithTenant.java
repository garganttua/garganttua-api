package com.garganttua.api.core;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface IGGAPIEntityWithTenant extends IGGAPIEntity {

	@JsonProperty(value = "tenantId")
	public String getTenantId();
	
	public void setTenantId(String tenantId);
	
}
