package com.garganttua.api.spec.security;

import java.util.List;

public interface IGGAPIAuthenticator {

	String getTenantId();
	
	String getUuid();

	List<String> getAuthoritiesList();

	String getId();

}
