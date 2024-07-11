package com.garganttua.api.spec.engine;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.dao.IGGAPIDaosRegistry;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.domain.IGGAPIDomainsRegistry;
import com.garganttua.api.spec.factory.IGGAPIFactoriesRegistry;
import com.garganttua.api.spec.interfasse.IGGAPIInterfacesRegistry;
import com.garganttua.api.spec.repository.IGGAPIRepositoriesRegistry;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;
import com.garganttua.api.spec.service.IGGAPIServicesRegistry;

public interface IGGAPIEngine {

    IGGAPIDomainsRegistry getDomainsRegistry();
    
    IGGAPIDaosRegistry getDaosRegistry();
    
    IGGAPIRepositoriesRegistry getRepositoriesRegistry();
    
    IGGAPIFactoriesRegistry getFactoriesRegistry();

    IGGAPIServicesRegistry getServicesRegistry();

	IGGAPIDomain getOwnerDomain();
	
	IGGAPIDomain getTenantDomain();

	IGGAPISecurityEngine getSecurity();
	
	IGGAPIEngine start() throws GGAPIException;

	IGGAPIEngine stop() throws GGAPIException;

	IGGAPIEngine reload() throws GGAPIException;

	IGGAPIEngine flush() throws GGAPIException;

	IGGAPIEngine init() throws GGAPIException;

	IGGAPIInterfacesRegistry getInterfacesRegistry();
	
}
