package com.garganttua.api.ws;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.authorization.IGGAPIAuthorization;
import com.garganttua.api.spec.IGGAPIDomainable;
import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.spec.GGAPIReadOutputMode;

public interface IGGAPIRestService<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> extends IGGAPIDomainable<Entity, Dto> {

	List<IGGAPIAuthorization> createAuthorizations();

	@RequestMapping(value = "", method = RequestMethod.POST)
	ResponseEntity<?> createEntity(@RequestBody(required = true) String entity, @RequestHeader(name = "tenantId") String tenantId, @RequestAttribute(name="userId", required = false) String userId);

	@RequestMapping(value = "", method = RequestMethod.GET)
	ResponseEntity<?> getEntities(
			@RequestHeader(name = "tenantId") String tenantId,
			@RequestParam(name = "mode", defaultValue = "full") GGAPIReadOutputMode mode,
			@RequestParam(name = "pageSize", defaultValue = "0") Integer pageSize,
			@RequestParam(name = "pageIndex", defaultValue = "0") Integer pageIndex,
			@RequestParam(name = "filter", defaultValue = "") String filterString,
			@RequestParam(name = "sort", defaultValue = "") String sortString, 
			@RequestAttribute(name="userId", required = false) String userId);

	@RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
	ResponseEntity<?> getEntity(@RequestHeader(name = "tenantId") String tenantId, @PathVariable(name = "uuid") String uuid, @RequestAttribute(name="userId", required = false) String userId);

	@RequestMapping(value = "/{uuid}", method = RequestMethod.PATCH)
	ResponseEntity<?> updateEntity(@PathVariable(name = "uuid") String uuid, @RequestBody(required = true) String entity, @RequestHeader String tenantId, @RequestAttribute(name="userId", required = false) String userId);

	@RequestMapping(value = "/{uuid}", method = RequestMethod.DELETE)
	ResponseEntity<?> deleteEntity(@PathVariable(name = "uuid") String uuid, @RequestHeader(name = "tenantId") String tenantId, @RequestAttribute(name="userId", required = false) String userId);

	@RequestMapping(value = "", method = RequestMethod.DELETE)
	ResponseEntity<?> deleteAll(@RequestHeader(name = "tenantId") String tenantId, @RequestAttribute(name="userId", required = false) String userId );

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	ResponseEntity<?> getCount(@RequestHeader(name = "tenantId") String tenantId, @RequestAttribute(name="userId", required = false) String userId );

	void authorize(boolean authorize_creation, boolean authorize_read_all, boolean authorize_read_one,
			boolean authorize_update_one, boolean authorize_delete_one, boolean authorize_delete_all,
			boolean authorize_count);

	void setController(IGGAPIController<Entity, Dto> controller);

}
