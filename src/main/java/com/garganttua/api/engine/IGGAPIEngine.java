package com.garganttua.api.engine;

import java.util.Optional;

import com.garganttua.api.engine.registries.IGGAPIAccessRulesRegistry;
import com.garganttua.api.engine.registries.IGGAPIDaosRegistry;
import com.garganttua.api.engine.registries.IGGAPIDomainsRegistry;
import com.garganttua.api.engine.registries.IGGAPIFactoriesRegistry;
import com.garganttua.api.engine.registries.IGGAPIRepositoriesRegistry;
import com.garganttua.api.engine.registries.IGGAPIServicesRegistry;
import com.garganttua.api.security.IGGAPISecurity;

public interface IGGAPIEngine {

    IGGAPIDomainsRegistry getDomainsRegistry();
    
    IGGAPIDaosRegistry getDaosRegistry();
    
    IGGAPIRepositoriesRegistry getRepositoriesRegistry();
    
    IGGAPIFactoriesRegistry getFactoriesRegistry();

    IGGAPIServicesRegistry getServicesRegistry();
    
	IGGAPIAccessRulesRegistry getAccessRulesRegistry();
	
	GGAPIDomain getOwnerDomain();
	
	GGAPIDomain getTenantDomain();

	Optional<IGGAPISecurity> getSecurity();
	
}
