package com.garganttua.api.engine.accessors;

import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.IGGAPIEntity;

@FunctionalInterface
public interface IGGAPITenantsControllerAccessor {
	
	IGGAPIController<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> getTenantsController();

}
