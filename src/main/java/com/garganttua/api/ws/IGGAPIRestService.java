package com.garganttua.api.ws;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.core.GGAPICrudAccess;
import com.garganttua.api.core.GGAPIReadOutputMode;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.IGGAPIDomainable;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.engine.IGGAPIEngineObject;
import com.garganttua.api.events.IGGAPIEventPublisher;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.authorization.IGGAPIAccessRule;
import com.garganttua.api.ws.filters.GGAPICallerManager;

public interface IGGAPIRestService<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> extends IGGAPIDomainable<Entity, Dto>, IGGAPIEngineObject {
	
	List<IGGAPIAccessRule> createAccessRules();

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
	ResponseEntity<?> deleteAll(@RequestAttribute(name=GGAPICallerManager.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller,
			@RequestParam(name = "params", defaultValue = "") String customParameters);
	
	@RequestMapping(value = "/count", method = RequestMethod.GET)
	ResponseEntity<?> getCount(@RequestAttribute(name=GGAPICallerManager.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller,
			@RequestParam(name = "params", defaultValue = "") String customParameters);
	
	void allow(boolean allow_creation, boolean allow_read_all, boolean allow_read_one,
			boolean allow_update_one, boolean allow_delete_one, boolean allow_delete_all,
			boolean allow_count);
	
	void setAccesses(GGAPICrudAccess creation_access, GGAPICrudAccess read_all_access, GGAPICrudAccess read_one_access, GGAPICrudAccess update_one_access, GGAPICrudAccess delete_one_access, GGAPICrudAccess delete_all_access, GGAPICrudAccess count_access);

	void setController(Optional<IGGAPIController<Entity, Dto>> controller);

	void setEventPublisher(Optional<IGGAPIEventPublisher<Entity>> eventObj);

	void setAuthorities(boolean creation_authority, boolean read_all_authority, boolean read_one_authority,
			boolean update_one_authority, boolean delete_one_authority, boolean delete_all_authority,
			boolean count_authority);

	List<IGGAPICustomService> getCustomServices();

	Optional<IGGAPIEventPublisher<Entity>> getEventPublisher();


}
