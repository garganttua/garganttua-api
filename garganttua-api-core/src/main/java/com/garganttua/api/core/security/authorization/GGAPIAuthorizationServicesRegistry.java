package com.garganttua.api.core.security.authorization;

import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.authorization.IGGAPIAuthorizationServicesRegistry;
import com.garganttua.api.spec.service.IGGAPIService;

public class GGAPIAuthorizationServicesRegistry implements IGGAPIAuthorizationServicesRegistry {

	private Map<IGGAPIDomain, Pair<Class<?>, IGGAPIService>> services;

	public GGAPIAuthorizationServicesRegistry(Map<IGGAPIDomain, Pair<Class<?>, IGGAPIService>> services) {
		this.services = services;
	}

}
