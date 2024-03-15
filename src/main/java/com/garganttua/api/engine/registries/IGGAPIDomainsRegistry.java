package com.garganttua.api.engine.registries;

import java.util.List;

import com.garganttua.api.engine.GGAPIDomain;

public interface IGGAPIDomainsRegistry {

	List<GGAPIDomain> getDomains();

//	GGAPIDynamicDomain getDomain(HttpServletRequest request);

	GGAPIDomain getDomain(String string);
	
	GGAPIDomain getOwnerDomain();
	
	GGAPIDomain getTenantDomain();
}
