package com.garganttua.api.spec.service;

import java.util.List;

import com.garganttua.api.spec.engine.IGGAPIEngineObject;

public interface IGGAPIServicesRegistry extends IGGAPIEngineObject {
	
	IGGAPIService getService(String name);

	List<IGGAPIService> getServices();

	List<IGGAPIServiceInfos> getServiceInfos(String domain);

}
