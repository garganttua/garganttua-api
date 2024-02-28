package com.garganttua.api.engine;

import java.util.Optional;

import com.garganttua.api.core.entity.factory.IGGAPIEntityFactory;
import com.garganttua.api.engine.registries.IGGAPIAccessRulesRegistry;
import com.garganttua.api.engine.registries.IGGAPIDaosRegistry;
import com.garganttua.api.engine.registries.IGGAPIDynamicDomainsRegistry;
import com.garganttua.api.engine.registries.IGGAPIRepositoriesRegistry;
import com.garganttua.api.engine.registries.IGGAPIServicesRegistry;
import com.garganttua.api.security.IGGAPISecurity;

public interface IGGAPIEngine {
	
	IGGAPIEntityFactory getEntityFactory();
	
    IGGAPIDynamicDomainsRegistry getDynamicDomainsRegistry();
    
    IGGAPIDaosRegistry getDaosRegistry();
    
    IGGAPIRepositoriesRegistry getRepositoriesRegistry();

    IGGAPIServicesRegistry getServicesRegistry();
    
	IGGAPIAccessRulesRegistry getAccessRulesRegistry();
	
	GGAPIDynamicDomain getAuthenticatorDomain();
	
	GGAPIDynamicDomain getOwnerDomain();
	
	GGAPIDynamicDomain getTenantDomain();
	
}
