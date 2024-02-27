package com.garganttua.api.engine;

import com.garganttua.api.core.IGGAPIEntityFactory;
import com.garganttua.api.engine.registries.IGGAPIAccessRulesRegistry;
import com.garganttua.api.engine.registries.IGGAPIDaosRegistry;
import com.garganttua.api.engine.registries.IGGAPIDynamicDomainsRegistry;
import com.garganttua.api.engine.registries.IGGAPIRepositoriesRegistry;
import com.garganttua.api.engine.registries.IGGAPIServicesRegistry;

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
