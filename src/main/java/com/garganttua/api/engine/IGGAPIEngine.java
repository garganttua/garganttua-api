package com.garganttua.api.engine;

import java.util.Optional;

import com.garganttua.api.engine.accessors.IGGAPIAuthenticatorAccessor;
import com.garganttua.api.engine.accessors.IGGAPIOwnersControllerAccessor;
import com.garganttua.api.engine.accessors.IGGAPITenantsControllerAccessor;
import com.garganttua.api.engine.registries.IGGAPIAccessRulesRegistry;
import com.garganttua.api.engine.registries.IGGAPIControllersRegistry;
import com.garganttua.api.engine.registries.IGGAPIDaosRegistry;
import com.garganttua.api.engine.registries.IGGAPIDynamicDomainsRegistry;
import com.garganttua.api.engine.registries.IGGAPIRepositoriesRegistry;
import com.garganttua.api.engine.registries.IGGAPIServicesRegistry;
import com.garganttua.api.security.IGGAPISecurity;

public interface IGGAPIEngine {
    IGGAPIDynamicDomainsRegistry getDynamicDomainsRegistry();
    
    IGGAPIDaosRegistry getDaosRegistry();
    
    IGGAPIRepositoriesRegistry getRepositoriesRegistry();
    
    IGGAPIControllersRegistry getControllersRegistry();
    
    IGGAPIServicesRegistry getServicesRegistry();
    
    IGGAPIAuthenticatorAccessor getAuthenticatorAccessor();
    
    IGGAPITenantsControllerAccessor getTenantsControllerAccessor();
    
    IGGAPIOwnersControllerAccessor getOwnerControllerAccessor();
    
    Optional<IGGAPISecurity> getSecurity();

	IGGAPIAccessRulesRegistry getAccessRulesRegistry();
}
