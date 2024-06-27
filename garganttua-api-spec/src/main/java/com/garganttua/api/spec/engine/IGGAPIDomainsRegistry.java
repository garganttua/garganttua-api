package com.garganttua.api.spec.engine;

import java.util.Set;

import com.garganttua.api.spec.domain.IGGAPIDomain;

public interface IGGAPIDomainsRegistry {

	Set<IGGAPIDomain> getDomains();

	IGGAPIDomain getDomain(String string);
	
	IGGAPIDomain getOwnerDomain();
	
	IGGAPIDomain getTenantDomain();
}
