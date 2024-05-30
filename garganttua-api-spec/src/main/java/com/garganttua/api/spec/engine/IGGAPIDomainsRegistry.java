package com.garganttua.api.spec.engine;

import java.util.List;

import com.garganttua.api.spec.domain.IGGAPIDomain;

public interface IGGAPIDomainsRegistry {

	List<IGGAPIDomain> getDomains();

//	GGAPIDynamicDomain getDomain(HttpServletRequest request);

	IGGAPIDomain getDomain(String string);
	
	IGGAPIDomain getOwnerDomain();
	
	IGGAPIDomain getTenantDomain();
}
