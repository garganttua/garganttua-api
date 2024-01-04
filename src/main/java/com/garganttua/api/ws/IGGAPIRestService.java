package com.garganttua.api.ws;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.engine.IGGAPIEngineObject;
import com.garganttua.api.events.IGGAPIEventPublisher;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.authorization.IGGAPIAuthorization;
import com.garganttua.api.security.tenants.GGAPIEngineTenantIdHeaderManager;
import com.garganttua.api.spec.GGAPICrudAccess;
import com.garganttua.api.spec.GGAPIReadOutputMode;
import com.garganttua.api.spec.IGGAPIDomainable;
import com.garganttua.api.spec.IGGAPIEntity;

public interface IGGAPIRestService<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> extends IGGAPIDomainable<Entity, Dto>, IGGAPIEngineObject {
	
	List<IGGAPIAuthorization> createAuthorizations();

	@RequestMapping(value = "", method = RequestMethod.POST)
	ResponseEntity<?> createEntity(@RequestBody(required = true) String entity, @RequestHeader(name = GGAPIEngineTenantIdHeaderManager.tenantIdHeaderName) String tenantId, @RequestAttribute(name="ownerId", required = false) String ownerId);
	@RequestMapping(value = "", method = RequestMethod.POST)
	ResponseEntity<?> createEntity(@RequestBody(required = true) String entity, @RequestAttribute(name="ownerId", required = false) String ownerId);

	@RequestMapping(value = "", method = RequestMethod.GET)
	ResponseEntity<?> getEntities(
			@RequestHeader(name = GGAPIEngineTenantIdHeaderManager.tenantIdHeaderName) String tenantId,
			@RequestParam(name = "mode", defaultValue = "full") GGAPIReadOutputMode mode,
			@RequestParam(name = "pageSize", defaultValue = "0") Integer pageSize,
			@RequestParam(name = "pageIndex", defaultValue = "0") Integer pageIndex,
			@RequestParam(name = "filter", defaultValue = "") String filterString,
			@RequestParam(name = "sort", defaultValue = "") String sortString, 
			@RequestParam(name = "geoloc", defaultValue = "") String geolocString,
			@RequestAttribute(name="ownerId", required = false) String ownerId);
	@RequestMapping(value = "", method = RequestMethod.GET)
	ResponseEntity<?> getEntities(
			@RequestParam(name = "mode", defaultValue = "full") GGAPIReadOutputMode mode,
			@RequestParam(name = "pageSize", defaultValue = "0") Integer pageSize,
			@RequestParam(name = "pageIndex", defaultValue = "0") Integer pageIndex,
			@RequestParam(name = "filter", defaultValue = "") String filterString,
			@RequestParam(name = "sort", defaultValue = "") String sortString, 
			@RequestParam(name = "geoloc", defaultValue = "") String geolocString,
			@RequestAttribute(name="ownerId", required = false) String ownerId);

	@RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
	ResponseEntity<?> getEntity(@RequestHeader(name = GGAPIEngineTenantIdHeaderManager.tenantIdHeaderName) String tenantId, @PathVariable(name = "uuid") String uuid, @RequestAttribute(name="ownerId", required = false) String ownerId);
	@RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
	ResponseEntity<?> getEntity(@PathVariable(name = "uuid") String uuid, @RequestAttribute(name="ownerId", required = false) String ownerId);

	@RequestMapping(value = "/{uuid}", method = RequestMethod.PATCH)
	ResponseEntity<?> updateEntity(@PathVariable(name = "uuid") String uuid, @RequestBody(required = true) String entity, @RequestHeader String tenantId, @RequestAttribute(name="ownerId", required = false) String ownerId);
	@RequestMapping(value = "/{uuid}", method = RequestMethod.PATCH)
	ResponseEntity<?> updateEntity(@PathVariable(name = "uuid") String uuid, @RequestBody(required = true) String entity, @RequestAttribute(name="ownerId", required = false) String ownerId);

	@RequestMapping(value = "/{uuid}", method = RequestMethod.DELETE)
	ResponseEntity<?> deleteEntity(@PathVariable(name = "uuid") String uuid, @RequestHeader(name = GGAPIEngineTenantIdHeaderManager.tenantIdHeaderName) String tenantId, @RequestAttribute(name="ownerId", required = false) String ownerId);
	@RequestMapping(value = "/{uuid}", method = RequestMethod.DELETE)
	ResponseEntity<?> deleteEntity(@PathVariable(name = "uuid") String uuid, @RequestAttribute(name="ownerId", required = false) String ownerId);

	@RequestMapping(value = "", method = RequestMethod.DELETE)
	ResponseEntity<?> deleteAll(@RequestHeader(name = GGAPIEngineTenantIdHeaderManager.tenantIdHeaderName) String tenantId, @RequestAttribute(name="ownerId", required = false) String ownerId );
	@RequestMapping(value = "", method = RequestMethod.DELETE)
	ResponseEntity<?> deleteAll(@RequestAttribute(name="ownerId", required = false) String ownerId );

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	ResponseEntity<?> getCount(@RequestHeader(name = GGAPIEngineTenantIdHeaderManager.tenantIdHeaderName) String tenantId, @RequestAttribute(name="ownerId", required = false) String ownerId );
	@RequestMapping(value = "/count", method = RequestMethod.GET)
	ResponseEntity<?> getCount(@RequestAttribute(name="ownerId", required = false) String ownerId );

	void allow(boolean allow_creation, boolean allow_read_all, boolean allow_read_one,
			boolean allow_update_one, boolean allow_delete_one, boolean allow_delete_all,
			boolean allow_count);
	
	void setAccesses(GGAPICrudAccess creation_access, GGAPICrudAccess read_all_access, GGAPICrudAccess read_one_access, GGAPICrudAccess update_one_access, GGAPICrudAccess delete_one_access, GGAPICrudAccess delete_all_access, GGAPICrudAccess count_access);

	void setController(Optional<IGGAPIController<Entity, Dto>> controller);

	void setEventPublisher(Optional<IGGAPIEventPublisher<Entity>> eventObj);

	void setMagicTenantId(String magicTenantId);

	void setAuthorities(boolean creation_authority, boolean read_all_authority, boolean read_one_authority,
			boolean update_one_authority, boolean delete_one_authority, boolean delete_all_authority,
			boolean count_authority);

	List<IGGAPICustomService> getCustomServices();

	Optional<IGGAPIEventPublisher<Entity>> getEventPublisher();


}
