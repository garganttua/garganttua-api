package com.garganttua.api.engine.registries;

import java.util.List;

import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.ws.IGGAPIRestService;

public interface IGGAPIServicesRegistry {
	
	IGGAPIRestService<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> getService(String name);

	List<IGGAPIRestService<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>> getServices();

}
