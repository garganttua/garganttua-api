package com.garganttua.api.engine.accessors;

import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.IGGAPIOwner;

@FunctionalInterface
public interface IGGAPIOwnersControllerAccessor {

	IGGAPIController<IGGAPIOwner, IGGAPIDTOObject<IGGAPIOwner>> getOwnersController();
	
}
