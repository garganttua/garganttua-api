package com.garganttua.api.engine.registries;

import java.util.List;

import com.garganttua.api.core.entity.interfaces.IGGAPIEntity;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.service.IGGAPIService;

public interface IGGAPIServicesRegistry {
	
	IGGAPIService<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> getService(String name);

	List<IGGAPIService<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>> getServices();

}
