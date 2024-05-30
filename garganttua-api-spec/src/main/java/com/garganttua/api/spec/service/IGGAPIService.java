package com.garganttua.api.spec.service;

import java.util.Map;
import java.util.Optional;

import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngineObject;
import com.garganttua.api.spec.event.IGGAPIEventPublisher;
import com.garganttua.api.spec.factory.IGGAPIEntityFactory;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.pageable.IGGAPIPageable;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;
import com.garganttua.api.spec.sort.IGGAPISort;

public interface IGGAPIService extends IGGAPIEngineObject {

	Object createEntity(IGGAPICaller caller, String entityAsString,
			String customParameters);
		
	Object getEntities(
			IGGAPICaller caller, 
			GGAPIReadOutputMode mode,
			IGGAPIPageable pageable,
			IGGAPIFilter filterString,
			IGGAPISort sortString, 
			Map<String, String> customParameters);

	Object getEntity(IGGAPICaller caller, String uuid,
			Map<String, String> customParameters);
	
	Object updateEntity(IGGAPICaller caller, String uuid, Object entityAsString,
			Map<String, String> customParameters);

	Object deleteEntity(IGGAPICaller caller, String uuid, Map<String, String> customParameters);
	
	Object deleteAll(
			IGGAPICaller caller,
			IGGAPIFilter filter,
			Map<String, String> customParameters);
	
	Object getCount(
			IGGAPICaller caller,
			IGGAPIFilter filter,
			Map<String, String> customParameters);

	void setEventPublisher(Optional<IGGAPIEventPublisher> eventObj);

	void setDomain(IGGAPIDomain ddomain);
	
	void setFactory(IGGAPIEntityFactory<?> factory);
	
	void setSecurity(Optional<IGGAPISecurityEngine> security);

}
