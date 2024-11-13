package com.garganttua.api.spec.engine;

import java.util.List;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICallerFactory;
import com.garganttua.api.spec.dao.IGGAPIDaosRegistry;
import com.garganttua.api.spec.domain.IGGAPIDomainsRegistry;
import com.garganttua.api.spec.factory.IGGAPIFactoriesRegistry;
import com.garganttua.api.spec.interfasse.IGGAPIInterfacesRegistry;
import com.garganttua.api.spec.repository.IGGAPIRepositoriesRegistry;
import com.garganttua.api.spec.service.IGGAPIServicesInfosRegistry;
import com.garganttua.api.spec.service.IGGAPIServicesRegistry;

public interface IGGAPIEngine {

    IGGAPIDomainsRegistry getDomainsRegistry();
    
    IGGAPIDaosRegistry getDaosRegistry();
    
    IGGAPIRepositoriesRegistry getRepositoriesRegistry();
    
    IGGAPIFactoriesRegistry getFactoriesRegistry();

    IGGAPIServicesRegistry getServicesRegistry();
    
    IGGAPIServicesInfosRegistry getServicesInfosRegistry();
	
    IGGAPIAccessRulesRegistry getAccessRulesRegistry();

    IGGAPIEngine start() throws GGAPIException;

	IGGAPIEngine stop() throws GGAPIException;

	IGGAPIEngine reload() throws GGAPIException;

	IGGAPIEngine flush() throws GGAPIException;

	IGGAPIEngine init() throws GGAPIException;

	IGGAPIInterfacesRegistry getInterfacesRegistry();
	
	IGGAPICallerFactory getCallerFactory(String domainName);

	List<String> getAuthorities();
}
