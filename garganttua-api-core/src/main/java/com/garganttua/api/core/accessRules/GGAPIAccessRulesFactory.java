package com.garganttua.api.core.accessRules;

import java.util.Set;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIAccessRulesRegistry;
import com.garganttua.api.spec.service.IGGAPIServicesInfosRegistry;

public class GGAPIAccessRulesFactory {

	private IGGAPIServicesInfosRegistry servicesInfosRegistry;
	private IGGAPIAccessRulesRegistry registry;
	private Set<IGGAPIDomain> domains;

	public GGAPIAccessRulesFactory(Set<IGGAPIDomain> domains, IGGAPIServicesInfosRegistry servicesInfosRegistry) {
		this.domains = domains;
		this.servicesInfosRegistry = servicesInfosRegistry;
		this.init();
	}

	private void init() {
		this.registry = new GGAPIAccessRulesRegistry(domains, servicesInfosRegistry);
	}

	public IGGAPIAccessRulesRegistry getRegistry() {
		return this.registry;
	}

}
