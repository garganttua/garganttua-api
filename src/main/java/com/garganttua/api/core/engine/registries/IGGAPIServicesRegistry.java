package com.garganttua.api.core.engine.registries;

import java.util.List;

import com.garganttua.api.core.service.IGGAPIService;

public interface IGGAPIServicesRegistry {
	
	IGGAPIService getService(String name);

	List<IGGAPIService> getServices();

}
