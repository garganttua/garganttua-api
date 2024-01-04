/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.ws;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.engine.IGGAPIEngine;
import com.garganttua.api.events.GGAPIEvent;
import com.garganttua.api.events.IGGAPIEventPublisher;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.authorization.BasicGGAPIAuthorization;
import com.garganttua.api.security.authorization.IGGAPIAuthorization;
import com.garganttua.api.spec.GGAPICrudAccess;
import com.garganttua.api.spec.GGAPICrudOperation;
import com.garganttua.api.spec.GGAPIDomainable;
import com.garganttua.api.spec.GGAPIEntityException;
import com.garganttua.api.spec.GGAPIReadOutputMode;
import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.spec.filter.GGAPIGeolocFilter;
import com.garganttua.api.spec.filter.GGAPILiteral;
import com.garganttua.api.spec.sort.GGAPISort;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author J.Colombet
 *
 * @param <Entity>
 */
public abstract class AbstractGGAPIService<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>>
		extends GGAPIDomainable<Entity, Dto> implements IGGAPIRestService<Entity, Dto> {

	protected static final String SUCCESSFULLY_DELETED = "Ressource has been successfully deleted";

	protected static final String NOT_IMPLEMENTED = "This function is not implemented";
	protected static final String FILTER_ERROR = "The filter has error";

	protected boolean ALLOW_CREATION = false;
	protected boolean ALLOW_GET_ALL = false;
	protected boolean ALLOW_GET_ONE = false;
	protected boolean ALLOW_UPDATE = false;
	protected boolean ALLOW_DELETE_ONE = false;
	protected boolean ALLOW_DELETE_ALL = false;
	protected boolean ALLOW_COUNT = false;

	protected GGAPICrudAccess CREATION_ACCESS = GGAPICrudAccess.tenant;
	protected GGAPICrudAccess GET_ALL_ACCESS = GGAPICrudAccess.tenant;
	protected GGAPICrudAccess GET_ONE_ACCESS = GGAPICrudAccess.tenant;
	protected GGAPICrudAccess UPDATE_ACCESS = GGAPICrudAccess.tenant;
	protected GGAPICrudAccess DELETE_ONE_ACCESS = GGAPICrudAccess.tenant;
	protected GGAPICrudAccess DELETE_ALL_ACCESS = GGAPICrudAccess.tenant;
	protected GGAPICrudAccess COUNT_ACCESS = GGAPICrudAccess.tenant;

	protected boolean CREATION_AUTHORITY = false;
	protected boolean GET_ALL_AUTHORITY = false;
	protected boolean GET_ONE_AUTHORITY = false;
	protected boolean UPDATE_AUTHORITY = false;
	protected boolean DELETE_ONE_AUTHORITY = false;
	protected boolean DELETE_ALL_AUTHORITY = false;
	protected boolean COUNT_AUTHORITY = false;

	protected abstract List<IGGAPIAuthorization> createCustomAuthorizations();

	private ArrayList<IGGAPIAuthorization> authorizations;

	@Autowired
	@Setter
	@Getter
	protected Optional<IGGAPIEventPublisher<Entity>> eventPublisher;
	
	@Setter
	protected String magicTenantId;

	@Setter
	protected IGGAPIEngine engine;

	@PostConstruct
	protected void init() {
		this.allow(this.ALLOW_CREATION, this.ALLOW_COUNT, this.ALLOW_COUNT, this.ALLOW_UPDATE, this.ALLOW_DELETE_ONE,
				this.ALLOW_DELETE_ALL, this.ALLOW_COUNT);
	}
	
	@Autowired
	@Setter
	protected Optional<IGGAPIController<Entity, Dto>> controller;

	@Override
	public List<IGGAPIAuthorization> createAuthorizations() {
		if (this.authorizations == null) {
			this.authorizations = new ArrayList<IGGAPIAuthorization>();

			this.authorizations.add(new BasicGGAPIAuthorization("/" + this.domain.toLowerCase(),
					this.GET_ALL_AUTHORITY == true
							? BasicGGAPIAuthorization.getAuthorization(this.domain.toLowerCase(),
									GGAPICrudOperation.read_all)
							: null,
					HttpMethod.GET, this.GET_ALL_ACCESS));
			this.authorizations.add(new BasicGGAPIAuthorization("/" + this.domain.toLowerCase(),
					this.CREATION_AUTHORITY == true
							? BasicGGAPIAuthorization.getAuthorization(this.domain.toLowerCase(),
									GGAPICrudOperation.create_one)
							: null,
					HttpMethod.POST, this.CREATION_ACCESS));
			this.authorizations.add(new BasicGGAPIAuthorization("/" + this.domain.toLowerCase(),
					this.DELETE_ALL_AUTHORITY == true
							? BasicGGAPIAuthorization.getAuthorization(this.domain.toLowerCase(),
									GGAPICrudOperation.delete_all)
							: null,
					HttpMethod.DELETE, this.DELETE_ALL_ACCESS));
			this.authorizations.add(new BasicGGAPIAuthorization("/" + this.domain.toLowerCase() + "/count",
					this.COUNT_AUTHORITY == true
							? BasicGGAPIAuthorization.getAuthorization(this.domain.toLowerCase(),
									GGAPICrudOperation.count)
							: null,
					HttpMethod.GET, this.COUNT_ACCESS));
			this.authorizations.add(new BasicGGAPIAuthorization("/" + this.domain.toLowerCase() + "/*",
					this.GET_ONE_AUTHORITY == true
							? BasicGGAPIAuthorization.getAuthorization(this.domain.toLowerCase(),
									GGAPICrudOperation.read_one)
							: null,
					HttpMethod.GET, this.GET_ONE_ACCESS));
			this.authorizations.add(new BasicGGAPIAuthorization("/" + this.domain.toLowerCase() + "/*",
					this.UPDATE_AUTHORITY == true
							? BasicGGAPIAuthorization.getAuthorization(this.domain.toLowerCase(),
									GGAPICrudOperation.update_one)
							: null,
					HttpMethod.PATCH, this.UPDATE_ACCESS));
			this.authorizations.add(new BasicGGAPIAuthorization("/" + this.domain.toLowerCase() + "/*",
					this.DELETE_ONE_AUTHORITY == true
							? BasicGGAPIAuthorization.getAuthorization(this.domain.toLowerCase(),
									GGAPICrudOperation.delete_one)
							: null,
					HttpMethod.DELETE, this.DELETE_ONE_ACCESS));

			if (this.createCustomAuthorizations() != null) {
				this.authorizations.addAll(this.createCustomAuthorizations());
			}
		}
		return authorizations;
	}

	/**
	 * Creates an entity.
	 * 
	 * @param
	 * @return
	 */
	@Override
	public ResponseEntity<?> createEntity(String entity__, String tenantId, String ownerId) {
		ResponseEntity<?> response = null;
		GGAPIEvent<Entity> event = new GGAPIEvent<Entity>();
		event.setTenantId(tenantId);
		event.setOwnerId(ownerId);
		event.setOperation(GGAPICrudOperation.create_one);
		event.setEntityClass(this.entityClass.getName());
		try {
			if (this.ALLOW_CREATION) {
				try {

					Entity entity = (Entity) new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(entity__.getBytes(), this.entityClass);

					event.setIn(entity);
					entity = this.controller.get().createEntity(tenantId, ownerId, entity);
					response = new ResponseEntity<>(entity, HttpStatus.CREATED);
					event.setOut(entity);
					event.setHttpReturnedCode(HttpStatus.NOT_IMPLEMENTED.value());
				} catch (GGAPIEntityException e) {
					response = new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
							this.getHttpErrorCodeFromEntityExceptionCode(e));
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setExceptionCode(e.getCode());
					event.setHttpReturnedCode(HttpStatus.NOT_IMPLEMENTED.value());
				} catch (Exception e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setHttpReturnedCode(HttpStatus.NOT_IMPLEMENTED.value());
					return new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				event.setHttpReturnedCode(HttpStatus.NOT_IMPLEMENTED.value());
				response = new ResponseEntity<>(new GGAPIErrorObject(NOT_IMPLEMENTED), HttpStatus.NOT_IMPLEMENTED);
			}

			return response;

		} finally {
			if (this.eventPublisher.isPresent()) {
				event.setOutDate(new Date());
				this.eventPublisher.get().publishEvent(event);
			}
		}
	}

	/**
	 * Get a list of entities.
	 * 
	 * @return
	 */
	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<?> getEntities(String tenantId, GGAPIReadOutputMode mode, Integer pageSize, Integer pageIndex,
			String filterString, String sortString, String geolocString, String ownerId) {
		GGAPIEvent<Entity> event = new GGAPIEvent<Entity>();
		event.setTenantId(tenantId);
		event.setOwnerId(ownerId);
		event.setOperation(GGAPICrudOperation.read_all);
		event.setEntityClass(this.entityClass.getName());
		Map<String, String> params = new HashMap<String, String>();
		params.put("mode", mode.toString());
		params.put("pageSize", pageSize.toString());
		params.put("pageIndex", pageIndex.toString());
		params.put("filterString", filterString);
		params.put("sortString", sortString);
		params.put("geolocString", geolocString);
		event.setInParams(params);
		try {
			if (this.ALLOW_GET_ALL) {

				Object entities = null;

				ObjectMapper mapper = new ObjectMapper();
				GGAPILiteral filter = null;
				GGAPISort sort = null;
				GGAPIGeolocFilter geoloc = null;
				try {
					if (filterString != null && !filterString.isEmpty()) {
						filter = mapper.readValue(filterString, GGAPILiteral.class);
					}
					if (sortString != null && !sortString.isEmpty()) {
						sort = mapper.readValue(sortString, GGAPISort.class);
					}
					if (geolocString != null && !geolocString.isEmpty()) {
						geoloc = mapper.readValue(geolocString, GGAPIGeolocFilter.class);
					}
				} catch (JsonProcessingException e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setHttpReturnedCode(HttpStatus.BAD_REQUEST.value());
					return new ResponseEntity<>(
							new GGAPIErrorObject("Error parsing request param : " + e.getMessage()),
							HttpStatus.BAD_REQUEST);
				}

				try {
					entities = this.controller.get().getEntityList(tenantId, ownerId, pageSize, pageIndex, filter, sort, geoloc, mode);
				} catch (GGAPIEntityException e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setExceptionCode(e.getCode());
					event.setHttpReturnedCode(this.getHttpErrorCodeFromEntityExceptionCode(e).value());
					return new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
							this.getHttpErrorCodeFromEntityExceptionCode(e));
				} catch (Exception e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					return new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
							HttpStatus.INTERNAL_SERVER_ERROR);
				}

				if (pageSize > 0) {
					long totalCount = 0;
					try {
						totalCount = this.controller.get().getEntityTotalCount(tenantId, ownerId, filter);
					} catch (GGAPIEntityException e) {
//						event.setException(e);
						event.setExceptionMessage(e.getMessage());
						event.setExceptionCode(e.getCode());
						event.setHttpReturnedCode(this.getHttpErrorCodeFromEntityExceptionCode(e).value());
						return new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
								this.getHttpErrorCodeFromEntityExceptionCode(e));
					} catch (Exception e) {
//						event.setException(e);
						event.setExceptionMessage(e.getMessage());
						return new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
								HttpStatus.INTERNAL_SERVER_ERROR);
					}

					GGAPIWsPage page = new GGAPIWsPage(totalCount, ((List<Object>) entities));
					event.setOutList((List<Entity>) entities);
					event.setHttpReturnedCode(HttpStatus.OK.value());
					return new ResponseEntity<>(page, HttpStatus.OK);
				} else {
					event.setOutList((List<Entity>) entities);
					event.setHttpReturnedCode(HttpStatus.OK.value());
					return new ResponseEntity<>(entities, HttpStatus.OK);
				}

			} else {
				return new ResponseEntity<>(new GGAPIErrorObject(NOT_IMPLEMENTED), HttpStatus.NOT_IMPLEMENTED);
			}

		} finally {
			if (this.eventPublisher.isPresent()) {
				event.setOutDate(new Date());
				this.eventPublisher.get().publishEvent(event);
			}
		}

	}

	/**
	 * Get one entity.
	 * 
	 * @return
	 */
	@Override
	public ResponseEntity<?> getEntity(String tenantId, String uuid, String ownerId) {
		GGAPIEvent<Entity> event = new GGAPIEvent<Entity>();
		event.setTenantId(tenantId);
		event.setOwnerId(ownerId);
		event.setOperation(GGAPICrudOperation.read_one);
		event.setEntityClass(this.entityClass.getName());
		Map<String, String> params = new HashMap<String, String>();
		params.put("uuid", uuid);
		event.setInParams(params);
		try {
			ResponseEntity<?> response = null;

			if (this.ALLOW_GET_ONE) {
				Entity entity;
				try {
					entity = this.controller.get().getEntity(tenantId, ownerId, uuid);
					response = new ResponseEntity<>(entity, HttpStatus.OK);
					event.setOut(entity);
				} catch (GGAPIEntityException e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setExceptionCode(e.getCode());
					event.setHttpReturnedCode(this.getHttpErrorCodeFromEntityExceptionCode(e).value());
					response = new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
							this.getHttpErrorCodeFromEntityExceptionCode(e));
				} catch (Exception e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setHttpReturnedCode(HttpStatus.NOT_IMPLEMENTED.value());
					return new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				event.setHttpReturnedCode(HttpStatus.NOT_IMPLEMENTED.value());
				return new ResponseEntity<>(new GGAPIErrorObject(NOT_IMPLEMENTED), HttpStatus.NOT_IMPLEMENTED);
			}
			return response;
		} finally {
			if (this.eventPublisher.isPresent()) {
				event.setOutDate(new Date());
				this.eventPublisher.get().publishEvent(event);
			}
		}
	}

	/**
	 * Update an entity.
	 * 
	 * @return
	 */
	@Override
	public ResponseEntity<?> updateEntity(String uuid, String entity__, String tenantId, String ownerId) {
		GGAPIEvent<Entity> event = new GGAPIEvent<Entity>();
		event.setTenantId(tenantId);
		event.setOwnerId(ownerId);
		event.setOperation(GGAPICrudOperation.update_one);
		event.setEntityClass(this.entityClass.getName());
		Map<String, String> params = new HashMap<String, String>();
		params.put("uuid", uuid);
		event.setInParams(params);
		try {
			ResponseEntity<?> response = null;

			if (this.ALLOW_UPDATE) {
				try {

					Entity entity = (Entity) new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(entity__.getBytes(), this.entityClass);
					entity.setUuid(uuid);
					event.setIn(entity);
					Entity updatedEntity = this.controller.get().updateEntity(tenantId, ownerId, entity);
					response = new ResponseEntity<>(updatedEntity, HttpStatus.OK);
					event.setOut(entity);
					event.setHttpReturnedCode(HttpStatus.OK.value());
				} catch (GGAPIEntityException e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setExceptionCode(e.getCode());
					event.setHttpReturnedCode(this.getHttpErrorCodeFromEntityExceptionCode(e).value());
					response = new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
							this.getHttpErrorCodeFromEntityExceptionCode(e));
				} catch (Exception e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setHttpReturnedCode(HttpStatus.NOT_IMPLEMENTED.value());
					return new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
							HttpStatus.INTERNAL_SERVER_ERROR);
				}

			} else {
				event.setHttpReturnedCode(HttpStatus.NOT_IMPLEMENTED.value());
				response = new ResponseEntity<>(new GGAPIErrorObject(NOT_IMPLEMENTED), HttpStatus.NOT_IMPLEMENTED);
			}

			return response;
		} finally {
			if (this.eventPublisher.isPresent()) {
				event.setOutDate(new Date());
				this.eventPublisher.get().publishEvent(event);
			}
		}
	}

	/**
	 * Delete an entity.
	 * 
	 * @return
	 */
	@Override
	public ResponseEntity<?> deleteEntity(String uuid, String tenantId, String ownerId) {
		GGAPIEvent<Entity> event = new GGAPIEvent<Entity>();
		event.setTenantId(tenantId);
		event.setOwnerId(ownerId);
		event.setOperation(GGAPICrudOperation.delete_one);
		event.setEntityClass(this.entityClass.getName());
		Map<String, String> params = new HashMap<String, String>();
		params.put("uuid", uuid);
		event.setInParams(params);
		try {
			if (this.ALLOW_DELETE_ONE) {
				ResponseEntity<?> response = null;

				try {
					this.controller.get().deleteEntity(tenantId, ownerId, uuid);
					response = new ResponseEntity<>(new GGAPIErrorObject(SUCCESSFULLY_DELETED), HttpStatus.OK);
					event.setHttpReturnedCode(HttpStatus.OK.value());
				} catch (GGAPIEntityException e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setExceptionCode(e.getCode());
					event.setHttpReturnedCode(this.getHttpErrorCodeFromEntityExceptionCode(e).value());
					response = new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()), HttpStatus.NOT_ACCEPTABLE);
				} catch (Exception e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setHttpReturnedCode(HttpStatus.NOT_IMPLEMENTED.value());
					return new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
							HttpStatus.INTERNAL_SERVER_ERROR);
				}

				return response;

			} else {
				event.setHttpReturnedCode(HttpStatus.NOT_IMPLEMENTED.value());
				return new ResponseEntity<>(new GGAPIErrorObject(NOT_IMPLEMENTED), HttpStatus.NOT_IMPLEMENTED);
			}
		} finally {
			if (this.eventPublisher.isPresent()) {
				event.setOutDate(new Date());
				this.eventPublisher.get().publishEvent(event);
			}
		}
	}

	/**
	 * Delete all the entities.
	 * 
	 * @return
	 */
	@Override
	public ResponseEntity<?> deleteAll(String tenantId, String ownerId) {
		GGAPIEvent<Entity> event = new GGAPIEvent<Entity>();
		event.setTenantId(tenantId);
		event.setOwnerId(ownerId);
		event.setOperation(GGAPICrudOperation.delete_all);
		event.setEntityClass(this.entityClass.getName());
		try {
			if (this.ALLOW_DELETE_ALL) {
				ResponseEntity<?> response = null;

				try {
					this.controller.get().deleteEntities(tenantId, ownerId);
					response = new ResponseEntity<>(new GGAPIErrorObject(SUCCESSFULLY_DELETED), HttpStatus.OK);
					event.setHttpReturnedCode(HttpStatus.OK.value());
				} catch (GGAPIEntityException e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setExceptionCode(e.getCode());
					event.setHttpReturnedCode(this.getHttpErrorCodeFromEntityExceptionCode(e).value());
					response = new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
							this.getHttpErrorCodeFromEntityExceptionCode(e));
				} catch (Exception e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setHttpReturnedCode(HttpStatus.NOT_IMPLEMENTED.value());
					return new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
							HttpStatus.INTERNAL_SERVER_ERROR);
				}

				return response;

			} else {
				event.setHttpReturnedCode(HttpStatus.NOT_IMPLEMENTED.value());
				return new ResponseEntity<>(new GGAPIErrorObject(NOT_IMPLEMENTED), HttpStatus.NOT_IMPLEMENTED);
			}
		} finally {
			if (this.eventPublisher.isPresent()) {
				event.setOutDate(new Date());
				this.eventPublisher.get().publishEvent(event);
			}
		}
	}

	/**
	 * Get count of entities
	 * 
	 * @return
	 */
	@Override
	public ResponseEntity<?> getCount(String tenantId, String ownerId) {
		GGAPIEvent<Entity> event = new GGAPIEvent<Entity>();
		event.setTenantId(tenantId);
		event.setOwnerId(ownerId);
		event.setOperation(GGAPICrudOperation.count);
		event.setEntityClass(this.entityClass.getName());
		try {
			if (this.ALLOW_COUNT) {
				ResponseEntity<?> response = null;

				try {
					long count = this.controller.get().getEntityTotalCount(tenantId, ownerId, null);
					response = new ResponseEntity<>(count, HttpStatus.OK);
					event.setHttpReturnedCode(HttpStatus.OK.value());
					event.setOutCount(count);
				} catch (GGAPIEntityException e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setExceptionCode(e.getCode());
					event.setHttpReturnedCode(this.getHttpErrorCodeFromEntityExceptionCode(e).value());
					response = new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
							this.getHttpErrorCodeFromEntityExceptionCode(e));
				} catch (Exception e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setHttpReturnedCode(HttpStatus.NOT_IMPLEMENTED.value());
					return new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
							HttpStatus.INTERNAL_SERVER_ERROR);
				}

				return response;

			} else {
				event.setHttpReturnedCode(HttpStatus.NOT_IMPLEMENTED.value());
				return new ResponseEntity<>(new GGAPIErrorObject(NOT_IMPLEMENTED), HttpStatus.NOT_IMPLEMENTED);
			}
		} finally {
			if (this.eventPublisher.isPresent()) {
				event.setOutDate(new Date());
				this.eventPublisher.get().publishEvent(event);
			}
		}
	}

	@Override
	public ResponseEntity<?> createEntity(String entity, String userId) {
		return this.createEntity(entity, this.magicTenantId, userId);
	}

	@Override
	public ResponseEntity<?> getEntities(GGAPIReadOutputMode mode, Integer pageSize, Integer pageIndex,
			String filterString, String sortString, String geolocString, String userId) {
		return this.getEntities(this.magicTenantId, mode, pageSize, pageIndex, filterString, sortString, geolocString, userId);
	}

	@Override
	public ResponseEntity<?> getEntity(String uuid, String userId) {
		return this.getEntity(this.magicTenantId, uuid, userId);
	}

	@Override
	public ResponseEntity<?> updateEntity(String uuid, String entity, String userId) {
		return this.updateEntity(uuid, entity, this.magicTenantId, userId);
	}

	@Override
	public ResponseEntity<?> deleteEntity(String uuid, String userId) {
		return this.deleteEntity(uuid, this.magicTenantId, userId);
	}

	@Override
	public ResponseEntity<?> deleteAll(String userId) {
		return this.deleteAll(this.magicTenantId, userId);
	}

	@Override
	public ResponseEntity<?> getCount(String userId) {
		return this.getCount(this.magicTenantId, userId);
	}

	/**
	 * 
	 * @param e
	 * @return
	 */
	protected HttpStatus getHttpErrorCodeFromEntityExceptionCode(GGAPIEntityException e) {
		switch (e.getCode()) {
		default:
		case GGAPIEntityException.BAD_REQUEST:
			return HttpStatus.BAD_REQUEST;
		case GGAPIEntityException.ENTITY_NOT_FOUND:
			return HttpStatus.NOT_FOUND;
		}
	}
	
	@Override
	public void allow(boolean allow_creation, boolean allow_read_all, boolean allow_read_one,
			boolean allow_update_one, boolean allow_delete_one, boolean allow_delete_all,
			boolean allow_count) {
		this.ALLOW_CREATION = allow_creation;
		this.ALLOW_GET_ALL = allow_read_all;
		this.ALLOW_GET_ONE = allow_read_one;
		this.ALLOW_UPDATE = allow_update_one;
		this.ALLOW_DELETE_ONE = allow_delete_one;
		this.ALLOW_DELETE_ALL = allow_delete_all;
		this.ALLOW_COUNT = allow_count;
	}

	@Override
	public void setAccesses(GGAPICrudAccess creation_access, GGAPICrudAccess read_all_access,
			GGAPICrudAccess read_one_access, GGAPICrudAccess update_one_access, GGAPICrudAccess delete_one_access,
			GGAPICrudAccess delete_all_access, GGAPICrudAccess count_access) {
		this.CREATION_ACCESS = creation_access;
		this.GET_ALL_ACCESS = read_all_access;
		this.GET_ONE_ACCESS = read_one_access;
		this.UPDATE_ACCESS = update_one_access;
		this.DELETE_ONE_ACCESS = delete_one_access;
		this.DELETE_ALL_ACCESS = delete_all_access;
		this.COUNT_ACCESS = count_access;
		
	}

	@Override
	public void setAuthorities(boolean creation_authority, boolean read_all_authority, boolean read_one_authority,
			boolean update_one_authority, boolean delete_one_authority, boolean delete_all_authority,
			boolean count_authority) {
		this.CREATION_AUTHORITY = creation_authority;
		this.GET_ALL_AUTHORITY = read_all_authority;
		this.GET_ONE_AUTHORITY = read_one_authority;
		this.UPDATE_AUTHORITY = update_one_authority;
		this.DELETE_ONE_AUTHORITY = delete_one_authority;
		this.DELETE_ALL_AUTHORITY = delete_all_authority;
		this.COUNT_AUTHORITY = count_authority;
	}

}
