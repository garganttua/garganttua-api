package com.garganttua.api.core.accessRules;

import java.util.Set;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIAccessRulesRegistry;

public class GGAPIAccessRulesFactory {

	private Set<IGGAPIDomain> domains;
	private IGGAPIAccessRulesRegistry registry;

	public GGAPIAccessRulesFactory(Set<IGGAPIDomain> domains) {
		this.domains = domains;
		this.init();
	}

	private void init() {
		this.registry = new GGAPIAccessRulesRegistry(domains);
	}

	public IGGAPIAccessRulesRegistry getRegistry() {
		return this.registry;
	}

}