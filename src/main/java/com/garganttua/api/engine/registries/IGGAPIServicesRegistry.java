package com.garganttua.api.engine.registries;

import java.util.List;

import com.garganttua.api.service.IGGAPIService;

public interface IGGAPIServicesRegistry {
	
	IGGAPIService getService(String name);

	List<IGGAPIService> getServices();

}
