package com.garganttua.api.core.legacy.accessRules;

import java.util.Set;

import com.garganttua.api.spec.security.context.IAccessRulesRegistry;
import com.garganttua.api.spec.domain.IDomain;

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
