package com.garganttua.api.core;

public interface IGGAPITenant extends IGGAPIEntity {
	
	String getTenantId();
	
	boolean isSuperTenant();

}
