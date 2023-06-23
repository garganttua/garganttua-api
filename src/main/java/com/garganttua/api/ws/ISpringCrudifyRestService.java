package com.garganttua.api.ws;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.garganttua.api.controller.ISpringCrudifyController;
import com.garganttua.api.repository.dto.ISpringCrudifyDTOObject;
import com.garganttua.api.security.authorization.ISpringCrudifyAuthorization;
import com.garganttua.api.spec.ISpringCrudifyDomainable;
import com.garganttua.api.spec.ISpringCrudifyEntity;
import com.garganttua.api.spec.SpringCrudifyReadOutputMode;

public interface ISpringCrudifyRestService<Entity extends ISpringCrudifyEntity, Dto extends ISpringCrudifyDTOObject<Entity>> extends ISpringCrudifyDomainable<Entity, Dto> {

	List<ISpringCrudifyAuthorization> createAuthorizations();

	@RequestMapping(value = "", method = RequestMethod.POST)
	ResponseEntity<?> createEntity(@RequestBody(required = true) String entity, @RequestHeader(name = "tenantId") String tenantId);

	@RequestMapping(value = "", method = RequestMethod.GET)
	ResponseEntity<?> getEntities(
			@RequestHeader(name = "tenantId") String tenantId,
			@RequestParam(name = "mode", defaultValue = "full") SpringCrudifyReadOutputMode mode,
			@RequestParam(name = "pageSize", defaultValue = "0") Integer pageSize,
			@RequestParam(name = "pageIndex", defaultValue = "0") Integer pageIndex,
			@RequestParam(name = "filter", defaultValue = "") String filterString,
			@RequestParam(name = "sort", defaultValue = "") String sortString);

	@RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
	ResponseEntity<?> getEntity(@RequestHeader(name = "tenantId") String tenantId, @PathVariable(name = "uuid") String uuid);

	@RequestMapping(value = "/{uuid}", method = RequestMethod.PATCH)
	ResponseEntity<?> updateEntity(@PathVariable(name = "uuid") String uuid, @RequestBody(required = true) String entity, @RequestHeader String tenantId);

	@RequestMapping(value = "/{uuid}", method = RequestMethod.DELETE)
	ResponseEntity<?> deleteEntity(String uuid, String tenantId);

	@RequestMapping(value = "", method = RequestMethod.DELETE)
	ResponseEntity<?> deleteAll(@RequestHeader(name = "tenantId") String tenantId);

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	ResponseEntity<?> getCount(String tenantId);

	void authorize(boolean authorize_creation, boolean authorize_read_all, boolean authorize_read_one,
			boolean authorize_update_one, boolean authorize_delete_one, boolean authorize_delete_all,
			boolean authorize_count);

	void setController(ISpringCrudifyController<Entity, Dto> controller);

}
