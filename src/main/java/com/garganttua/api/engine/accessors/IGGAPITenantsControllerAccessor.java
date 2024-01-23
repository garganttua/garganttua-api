package com.garganttua.api.engine.accessors;

import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.core.IGGAPITenant;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;

@FunctionalInterface
public interface IGGAPITenantsControllerAccessor {
	
	IGGAPIController<IGGAPITenant, IGGAPIDTOObject<IGGAPITenant>> getTenantsController();

}
