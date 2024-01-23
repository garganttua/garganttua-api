package com.garganttua.api.engine.registries;

import java.util.List;

import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;

public interface IGGAPIControllersRegistry {

	IGGAPIController<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> getController(String name);

	List<IGGAPIController<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>> getControllers();
	
}
