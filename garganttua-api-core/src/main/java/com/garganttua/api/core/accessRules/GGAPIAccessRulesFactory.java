package com.garganttua.api.core.accessRules;

import java.util.Set;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIAccessRulesRegistry;
import com.garganttua.api.spec.service.IGGAPIServicesInfosRegistry;

public class GGAPIAccessRulesFactory {

	private IGGAPIServicesInfosRegistry servicesInfosRegistry;
	private IGGAPIAccessRulesRegistry registry;

	public GGAPIAccessRulesFactory(IGGAPIServicesInfosRegistry servicesInfosRegistry) {
		this.servicesInfosRegistry = servicesInfosRegistry;
		this.init();
	}

	private void init() {
		this.registry = new GGAPIAccessRulesRegistry(servicesInfosRegistry);
	}

	public IGGAPIAccessRulesRegistry getRegistry() {
		return this.registry;
	}

}
