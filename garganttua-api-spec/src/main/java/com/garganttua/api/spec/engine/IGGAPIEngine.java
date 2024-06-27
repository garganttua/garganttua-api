package com.garganttua.api.spec.engine;

import java.util.Optional;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;

public interface IGGAPIEngine {

    IGGAPIDomainsRegistry getDomainsRegistry();
    
    IGGAPIDaosRegistry getDaosRegistry();
    
    IGGAPIRepositoriesRegistry getRepositoriesRegistry();
    
    IGGAPIFactoriesRegistry getFactoriesRegistry();

    IGGAPIServicesRegistry getServicesRegistry();
    
	IGGAPIAccessRulesRegistry getAccessRulesRegistry();
	
	IGGAPIDomain getOwnerDomain();
	
	IGGAPIDomain getTenantDomain();

	Optional<IGGAPISecurityEngine> getSecurity();
	
	IGGAPIEngine start() throws GGAPIException;

	IGGAPIEngine stop() throws GGAPIException;

	IGGAPIEngine reload() throws GGAPIException;

	IGGAPIEngine flush() throws GGAPIException;

	IGGAPIEngine init() throws GGAPIException;
	
}
