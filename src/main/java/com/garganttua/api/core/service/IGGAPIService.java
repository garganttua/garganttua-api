package com.garganttua.api.core.service;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.engine.GGAPIDomain;
import com.garganttua.api.core.engine.IGGAPIEngineObject;
import com.garganttua.api.core.entity.factory.IGGAPIEntityFactory;
import com.garganttua.api.core.events.IGGAPIEventPublisher;
import com.garganttua.api.core.security.IGGAPISecurity;

public interface IGGAPIService extends IGGAPIEngineObject {

	ResponseEntity<?> createEntity(IGGAPICaller caller, String entity,
			String customParameters);
		
	ResponseEntity<?> getEntities(
			IGGAPICaller caller, 
			GGAPIReadOutputMode mode,
			Integer pageSize,
			Integer pageIndex,
			String filterString,
			String sortString, 
			String customParameters);

	ResponseEntity<?> getEntity(IGGAPICaller caller, String uuid,
			String customParameters);
	
	ResponseEntity<?> updateEntity(IGGAPICaller caller, String uuid, @RequestBody(required = true) String entity,
			String customParameters);

	ResponseEntity<?> deleteEntity(IGGAPICaller caller, String uuid, String customParameters);
	
	ResponseEntity<?> deleteAll(
			IGGAPICaller caller,
			String filterString,
			String customParameters);
	
	ResponseEntity<?> getCount(
			IGGAPICaller caller,
			String filterString,
			String customParameters);

	void setEventPublisher(Optional<IGGAPIEventPublisher> eventObj);

	void setDomain(GGAPIDomain ddomain);
	
	void setFactory(IGGAPIEntityFactory<?> factory);
	
	void setSecurity(Optional<IGGAPISecurity> security);

}
