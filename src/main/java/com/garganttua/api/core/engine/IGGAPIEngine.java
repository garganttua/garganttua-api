package com.garganttua.api.core.engine;

import java.util.Optional;

import com.garganttua.api.core.engine.registries.IGGAPIAccessRulesRegistry;
import com.garganttua.api.core.engine.registries.IGGAPIDaosRegistry;
import com.garganttua.api.core.engine.registries.IGGAPIDomainsRegistry;
import com.garganttua.api.core.engine.registries.IGGAPIFactoriesRegistry;
import com.garganttua.api.core.engine.registries.IGGAPIRepositoriesRegistry;
import com.garganttua.api.core.engine.registries.IGGAPIServicesRegistry;
import com.garganttua.api.core.security.IGGAPISecurity;

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
