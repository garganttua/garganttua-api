package com.garganttua.api.spec.domain;

import java.util.Set;

import com.garganttua.api.spec.engine.IGGAPIEngineObject;

public interface IGGAPIDomainsRegistry extends IGGAPIEngineObject {

	Set<IGGAPIDomain> getDomains();

	IGGAPIDomain getDomain(String string);
	
	IGGAPIDomain getOwnerDomain();
	
	IGGAPIDomain getTenantDomain();
}
