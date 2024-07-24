package com.garganttua.api.spec.service;

import java.util.Map;
import java.util.Optional;

import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.engine.IGGAPIEngineObject;
import com.garganttua.api.spec.event.IGGAPIEventPublisher;
import com.garganttua.api.spec.factory.IGGAPIEntityFactory;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.pageable.IGGAPIPageable;
import com.garganttua.api.spec.security.IGGAPISecurity;
import com.garganttua.api.spec.sort.IGGAPISort;

public interface IGGAPIService extends IGGAPIEngineObject {

	IGGAPIServiceResponse createEntity(IGGAPICaller caller, Object entity,
			Map<String, String> customParameters);
		
	IGGAPIServiceResponse getEntities(
			IGGAPICaller caller, 
			GGAPIReadOutputMode mode,
			IGGAPIPageable pageable,
			IGGAPIFilter filterString,
			IGGAPISort sortString, 
			Map<String, String> customParameters);

	IGGAPIServiceResponse getEntity(IGGAPICaller caller, String uuid,
			Map<String, String> customParameters);
	
	IGGAPIServiceResponse updateEntity(IGGAPICaller caller, String uuid, Object entity,
			Map<String, String> customParameters);

	IGGAPIServiceResponse deleteEntity(IGGAPICaller caller, String uuid, Map<String, String> customParameters);
	
	IGGAPIServiceResponse deleteAll(
			IGGAPICaller caller,
			IGGAPIFilter filter,
			Map<String, String> customParameters);
	
//	IGGAPIServiceResponse getCount(
//			IGGAPICaller caller,
//			IGGAPIFilter filter,
//			Map<String, String> customParameters);

	void setEventPublisher(Optional<IGGAPIEventPublisher> eventObj);
	
	void setFactory(IGGAPIEntityFactory<Object> factory);
	
	void setSecurity(Optional<IGGAPISecurity> security);

}
