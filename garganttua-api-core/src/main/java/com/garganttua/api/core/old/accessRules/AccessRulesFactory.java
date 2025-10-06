package com.garganttua.api.core.accessRules;

import java.util.Set;

import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.engine.IAccessRulesRegistry;

public class AccessRulesFactory {

	private IAccessRulesRegistry registry;
	private Set<IDomain> domains;

	public AccessRulesFactory(Set<IDomain> domains) {
		this.domains = domains;
		this.init();
	}

	private void init() {
		this.registry = new AccessRulesRegistry(domains);
	}

	public IAccessRulesRegistry getRegistry() {
		return this.registry;
	}

}
