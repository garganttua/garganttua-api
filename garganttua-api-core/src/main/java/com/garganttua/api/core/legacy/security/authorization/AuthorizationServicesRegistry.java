package com.garganttua.api.core.legacy.security.authorization;

import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.security.authorization.IAuthorizationServicesRegistry;
import com.garganttua.api.spec.service.IService;

public class AuthorizationServicesRegistry implements IAuthorizationServicesRegistry {

	private Map<IDomain, Pair<Class<?>, IService>> services;

	public AuthorizationServicesRegistry(Map<IDomain, Pair<Class<?>, IService>> services) {
		this.services = services;
	}

}
