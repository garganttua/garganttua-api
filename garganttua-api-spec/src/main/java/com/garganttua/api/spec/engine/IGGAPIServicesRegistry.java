package com.garganttua.api.spec.engine;

import java.util.List;

import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;

public interface IGGAPIServicesRegistry {
	
	IGGAPIService getService(String name);

	List<IGGAPIService> getServices();

	List<IGGAPIServiceInfos> getServiceInfos(String domain);

}
