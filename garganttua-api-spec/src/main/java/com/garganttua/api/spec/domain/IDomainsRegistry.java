package com.garganttua.api.spec.domain;

import java.util.Set;

public interface IDomainsRegistry {

	Set<IDomain> getDomains();

	IDomain getDomain(String string);
	
	IDomain getOwnerDomain();
	
	IDomain getTenantDomain();
}
