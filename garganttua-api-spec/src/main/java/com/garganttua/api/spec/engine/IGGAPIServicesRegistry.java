package com.garganttua.api.spec.engine;

import java.util.List;

import com.garganttua.api.spec.service.IGGAPIService;

public interface IGGAPIServicesRegistry {
	
	IGGAPIService getService(String name);

	List<IGGAPIService> getServices();

}
