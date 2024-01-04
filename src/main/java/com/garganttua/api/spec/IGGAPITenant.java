package com.garganttua.api.spec;

public interface IGGAPITenant extends IGGAPIEntity {
	
	String getTenantId();
	
	boolean isSuperTenant();

}
