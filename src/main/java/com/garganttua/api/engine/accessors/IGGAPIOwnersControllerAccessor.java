package com.garganttua.api.engine.accessors;

import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.core.IGGAPIOwner;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;

@FunctionalInterface
public interface IGGAPIOwnersControllerAccessor {

	IGGAPIController<IGGAPIOwner, IGGAPIDTOObject<IGGAPIOwner>> getOwnersController();
	
}
