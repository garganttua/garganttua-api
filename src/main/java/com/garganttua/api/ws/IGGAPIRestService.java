package com.garganttua.api.ws;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.garganttua.api.core.GGAPIReadOutputMode;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.IGGAPIEngineObject;
import com.garganttua.api.events.IGGAPIEventPublisher;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.IGGAPISecurity;
import com.garganttua.api.ws.filters.GGAPICallerManager;

public interface IGGAPIRestService<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> extends IGGAPIEngineObject {

	@RequestMapping(value = "", method = RequestMethod.POST)
	ResponseEntity<?> createEntity(@RequestAttribute(name=GGAPICallerManager.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller, @RequestBody(required = true) String entity,
			@RequestParam(name = "params", defaultValue = "") String customParameters);
	
	@RequestMapping(value = "", method = RequestMethod.GET)
	ResponseEntity<?> getEntities(
			@RequestAttribute(name=GGAPICallerManager.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller, 
			@RequestParam(name = "mode", defaultValue = "full") GGAPIReadOutputMode mode,
			@RequestParam(name = "pageSize", defaultValue = "0") Integer pageSize,
			@RequestParam(name = "pageIndex", defaultValue = "0") Integer pageIndex,
			@RequestParam(name = "filter", defaultValue = "") String filterString,
			@RequestParam(name = "sort", defaultValue = "") String sortString, 
			@RequestParam(name = "geoloc", defaultValue = "") String geolocString,
			@RequestParam(name = "params", defaultValue = "") String customParameters);

	@RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
	ResponseEntity<?> getEntity(@RequestAttribute(name=GGAPICallerManager.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller, @PathVariable(name = "uuid") String uuid,
			@RequestParam(name = "params", defaultValue = "") String customParameters);
	
	@RequestMapping(value = "/{uuid}", method = RequestMethod.PATCH)
	ResponseEntity<?> updateEntity(@RequestAttribute(name=GGAPICallerManager.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller, @PathVariable(name = "uuid") String uuid, @RequestBody(required = true) String entity,
			@RequestParam(name = "params", defaultValue = "") String customParameters);

	@RequestMapping(value = "/{uuid}", method = RequestMethod.DELETE)
	ResponseEntity<?> deleteEntity(@RequestAttribute(name=GGAPICallerManager.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller, @PathVariable(name = "uuid") String uuid,
			@RequestParam(name = "params", defaultValue = "") String customParameters);
	
	@RequestMapping(value = "", method = RequestMethod.DELETE)
	ResponseEntity<?> deleteAll(
			@RequestAttribute(name=GGAPICallerManager.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller,
			@RequestParam(name = "filter", defaultValue = "") String filterString,
			@RequestParam(name = "geoloc", defaultValue = "") String geolocString,
			@RequestParam(name = "params", defaultValue = "") String customParameters);
	
	@RequestMapping(value = "/count", method = RequestMethod.GET)
	ResponseEntity<?> getCount(
			@RequestAttribute(name=GGAPICallerManager.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller,
			@RequestParam(name = "filter", defaultValue = "") String filterString,
			@RequestParam(name = "geoloc", defaultValue = "") String geolocString,
			@RequestParam(name = "params", defaultValue = "") String customParameters);

	void setEventPublisher(Optional<IGGAPIEventPublisher<Entity>> eventObj);

	Optional<IGGAPIEventPublisher<Entity>> getEventPublisher();

	void setDynamicDomain(GGAPIDynamicDomain ddomain);
	
	GGAPIDynamicDomain getDynamicDomain();
	
	void setSecurity(Optional<IGGAPISecurity> security);

}
