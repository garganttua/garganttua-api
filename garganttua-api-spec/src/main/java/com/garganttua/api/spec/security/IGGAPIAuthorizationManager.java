package com.garganttua.api.spec.security;

import com.garganttua.api.spec.GGAPIException;

public interface IGGAPIAuthorizationManager {

//	IGGAPISecurity configureSecurity(IGGAPISecurity http) throws GGAPIException;

	IGGAPIAuthorizationProvider getAuthorizationProvider();
	
}
