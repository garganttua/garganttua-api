package com.garganttua.api.service;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.garganttua.api.core.GGAPIReadOutputMode;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntity;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.IGGAPIEngineObject;
import com.garganttua.api.events.IGGAPIEventPublisher;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.IGGAPISecurity;

public interface IGGAPIService<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> extends IGGAPIEngineObject {

	ResponseEntity<?> createEntity(IGGAPICaller caller, String entity,
			String customParameters);
		
	ResponseEntity<?> getEntities(
			IGGAPICaller caller, 
			GGAPIReadOutputMode mode,
			Integer pageSize,
			Integer pageIndex,
			String filterString,
			String sortString, 
			String geolocString,
			String customParameters);

	ResponseEntity<?> getEntity(IGGAPICaller caller, String uuid,
			String customParameters);
	
	ResponseEntity<?> updateEntity(IGGAPICaller caller, String uuid, @RequestBody(required = true) String entity,
			String customParameters);

	ResponseEntity<?> deleteEntity(IGGAPICaller caller, String uuid, String customParameters);
	
	ResponseEntity<?> deleteAll(
			IGGAPICaller caller,
			String filterString,
			String geolocString,
			String customParameters);
	
	ResponseEntity<?> getCount(
			IGGAPICaller caller,
			String filterString,
			String geolocString,
			String customParameters);

	void setEventPublisher(Optional<IGGAPIEventPublisher<Entity>> eventObj);

	Optional<IGGAPIEventPublisher<Entity>> getEventPublisher();

	void setDynamicDomain(GGAPIDynamicDomain ddomain);
	
	GGAPIDynamicDomain getDynamicDomain();
	
	void setSecurity(Optional<IGGAPISecurity> security);

}
