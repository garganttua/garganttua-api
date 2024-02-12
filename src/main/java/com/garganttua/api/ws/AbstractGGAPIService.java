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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.core.GGAPICrudAccess;
import com.garganttua.api.core.GGAPICrudOperation;
import com.garganttua.api.core.GGAPIDomainable;
import com.garganttua.api.core.GGAPIEntityException;
import com.garganttua.api.core.GGAPIReadOutputMode;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.core.filter.GGAPIGeolocFilter;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.sort.GGAPISort;
import com.garganttua.api.engine.IGGAPIEngine;
import com.garganttua.api.events.GGAPIEvent;
import com.garganttua.api.events.IGGAPIEventPublisher;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.authorization.BasicGGAPIAccessRule;
import com.garganttua.api.security.authorization.IGGAPIAccessRule;

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

	protected abstract List<IGGAPIAccessRule> createCustomAuthorizations();

	private ArrayList<IGGAPIAccessRule> accessRules;

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
	public List<IGGAPIAccessRule> createAccessRules() {
		if (this.accessRules == null) {
			this.accessRules = new ArrayList<IGGAPIAccessRule>();

			if( this.ALLOW_GET_ALL )
				this.accessRules.add(new BasicGGAPIAccessRule("/" + this.domain.toLowerCase(),
						this.GET_ALL_AUTHORITY == true
								? BasicGGAPIAccessRule.getAuthority(this.domain.toLowerCase(),
										GGAPICrudOperation.read_all)
								: null,
						HttpMethod.GET, this.GET_ALL_ACCESS));
			
			if( this.ALLOW_CREATION )
				this.accessRules.add(new BasicGGAPIAccessRule("/" + this.domain.toLowerCase(),
						this.CREATION_AUTHORITY == true
								? BasicGGAPIAccessRule.getAuthority(this.domain.toLowerCase(),
										GGAPICrudOperation.create_one)
								: null,
						HttpMethod.POST, this.CREATION_ACCESS));
			
			if( this.ALLOW_DELETE_ALL )
				this.accessRules.add(new BasicGGAPIAccessRule("/" + this.domain.toLowerCase(),
						this.DELETE_ALL_AUTHORITY == true
								? BasicGGAPIAccessRule.getAuthority(this.domain.toLowerCase(),
										GGAPICrudOperation.delete_all)
								: null,
						HttpMethod.DELETE, this.DELETE_ALL_ACCESS));
			
			if( this.ALLOW_COUNT )
				this.accessRules.add(new BasicGGAPIAccessRule("/" + this.domain.toLowerCase() + "/count",
						this.COUNT_AUTHORITY == true
								? BasicGGAPIAccessRule.getAuthority(this.domain.toLowerCase(),
										GGAPICrudOperation.count)
								: null,
						HttpMethod.GET, this.COUNT_ACCESS));
			
			if( this.ALLOW_GET_ONE )
				this.accessRules.add(new BasicGGAPIAccessRule("/" + this.domain.toLowerCase() + "/*",
						this.GET_ONE_AUTHORITY == true
								? BasicGGAPIAccessRule.getAuthority(this.domain.toLowerCase(),
										GGAPICrudOperation.read_one)
								: null,
						HttpMethod.GET, this.GET_ONE_ACCESS));
			
			if( this.ALLOW_UPDATE )
				this.accessRules.add(new BasicGGAPIAccessRule("/" + this.domain.toLowerCase() + "/*",
						this.UPDATE_AUTHORITY == true
								? BasicGGAPIAccessRule.getAuthority(this.domain.toLowerCase(),
										GGAPICrudOperation.update_one)
								: null,
						HttpMethod.PATCH, this.UPDATE_ACCESS));
			
			if( this.ALLOW_DELETE_ONE )
				this.accessRules.add(new BasicGGAPIAccessRule("/" + this.domain.toLowerCase() + "/*",
						this.DELETE_ONE_AUTHORITY == true
								? BasicGGAPIAccessRule.getAuthority(this.domain.toLowerCase(),
										GGAPICrudOperation.delete_one)
								: null,
						HttpMethod.DELETE, this.DELETE_ONE_ACCESS));

			if (this.createCustomAuthorizations() != null) {
				this.accessRules.addAll(this.createCustomAuthorizations());
			}
		}
		return accessRules;
	}

	/**
	 * Creates an entity.
	 * 
	 * @param
	 * @return
	 */
	@Override
	public ResponseEntity<?> createEntity(IGGAPICaller caller, String entity__, String customParameters__) {
		ResponseEntity<?> response = null;
		GGAPIEvent<Entity> event = new GGAPIEvent<Entity>();
		event.setTenantId(caller.getTenantId());
		event.setOwnerId(caller.getOwnerId());
		event.setOperation(GGAPICrudOperation.create_one);
		event.setEntityClass(this.entityClass.getName());
		try {
			if (this.ALLOW_CREATION) {
				try {
					Map<String, String> customParameters = null;
					if( customParameters__ != null  && ! customParameters__.isEmpty()) {
						TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String,String>>() {};
						customParameters = new ObjectMapper().readValue(customParameters__, typeRef);
					}
					
					Entity entity = (Entity) new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(entity__.getBytes(), this.entityClass);

					event.setIn(entity);
					entity = this.controller.get().createEntity(caller, entity, customParameters);
					response = new ResponseEntity<>(entity, HttpStatus.CREATED);
					event.setOut(entity);
					event.setHttpReturnedCode(HttpStatus.NOT_IMPLEMENTED.value());
				} catch (GGAPIEntityException e) {
					response = new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
							e.getHttpErrorCode());
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
	public ResponseEntity<?> getEntities(IGGAPICaller caller, GGAPIReadOutputMode mode, Integer pageSize, Integer pageIndex,
			String filterString, String sortString, String geolocString, String customParameters__) {
		GGAPIEvent<Entity> event = new GGAPIEvent<Entity>();
		event.setTenantId(caller.getTenantId());
		event.setOwnerId(caller.getOwnerId());
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
				Map<String, String> customParameters = null;
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
					if( customParameters__ != null  && ! customParameters__.isEmpty()) {
						TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String,String>>() {};
						customParameters = new ObjectMapper().readValue(customParameters__, typeRef);
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
					entities = this.controller.get().getEntityList(caller, pageSize, pageIndex, filter, sort, geoloc, mode, customParameters);
				} catch (GGAPIEntityException e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setExceptionCode(e.getCode());
					event.setHttpReturnedCode(e.getHttpErrorCode().value());
					return new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
							e.getHttpErrorCode());
				} catch (Exception e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					return new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
							HttpStatus.INTERNAL_SERVER_ERROR);
				}

				if (pageSize > 0) {
					long totalCount = 0;
					try {
						totalCount = this.controller.get().getEntityTotalCount(caller, filter, geoloc, customParameters);
					} catch (GGAPIEntityException e) {
//						event.setException(e);
						event.setExceptionMessage(e.getMessage());
						event.setExceptionCode(e.getCode());
						event.setHttpReturnedCode(e.getHttpErrorCode().value());
						return new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
								e.getHttpErrorCode());
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
	public ResponseEntity<?> getEntity(IGGAPICaller caller, String uuid, String customParameters__) {
		GGAPIEvent<Entity> event = new GGAPIEvent<Entity>();
		event.setTenantId(caller.getTenantId());
		event.setOwnerId(caller.getOwnerId());
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
					Map<String, String> customParameters = null;
					if( customParameters__ != null  && ! customParameters__.isEmpty()) {
						TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String,String>>() {};
						customParameters = new ObjectMapper().readValue(customParameters__, typeRef);
					}
					entity = this.controller.get().getEntity(caller, uuid, customParameters);
					response = new ResponseEntity<>(entity, HttpStatus.OK);
					event.setOut(entity);
				} catch (GGAPIEntityException e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setExceptionCode(e.getCode());
					event.setHttpReturnedCode(e.getHttpErrorCode().value());
					response = new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
							e.getHttpErrorCode());
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
	public ResponseEntity<?> updateEntity(IGGAPICaller caller, String uuid, String entity__, String customParameters__) {
		GGAPIEvent<Entity> event = new GGAPIEvent<Entity>();
		event.setTenantId(caller.getTenantId());
		event.setOwnerId(caller.getOwnerId());
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
					Map<String, String> customParameters = null;
					if( customParameters__ != null  && ! customParameters__.isEmpty()) {
						TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String,String>>() {};
						customParameters = new ObjectMapper().readValue(customParameters__, typeRef);
					}
					entity.setUuid(uuid);
					event.setIn(entity);
					Entity updatedEntity = this.controller.get().updateEntity(caller, entity, customParameters);
					response = new ResponseEntity<>(updatedEntity, HttpStatus.OK);
					event.setOut(entity);
					event.setHttpReturnedCode(HttpStatus.OK.value());
				} catch (GGAPIEntityException e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setExceptionCode(e.getCode());
					event.setHttpReturnedCode(e.getHttpErrorCode().value());
					response = new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
							e.getHttpErrorCode());
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
	public ResponseEntity<?> deleteEntity(IGGAPICaller caller, String uuid, String customParameters__) {
		GGAPIEvent<Entity> event = new GGAPIEvent<Entity>();
		event.setTenantId(caller.getTenantId());
		event.setOwnerId(caller.getOwnerId());
		event.setOperation(GGAPICrudOperation.delete_one);
		event.setEntityClass(this.entityClass.getName());
		Map<String, String> params = new HashMap<String, String>();
		params.put("uuid", uuid);
		event.setInParams(params);
		try {
			if (this.ALLOW_DELETE_ONE) {
				ResponseEntity<?> response = null;

				try {
					Map<String, String> customParameters = null;
					if( customParameters__ != null  && ! customParameters__.isEmpty()) {
						TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String,String>>() {};
						customParameters = new ObjectMapper().readValue(customParameters__, typeRef);
					}
					this.controller.get().deleteEntity(caller, uuid, customParameters);
					response = new ResponseEntity<>(new GGAPIErrorObject(SUCCESSFULLY_DELETED), HttpStatus.OK);
					event.setHttpReturnedCode(HttpStatus.OK.value());
				} catch (GGAPIEntityException e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setExceptionCode(e.getCode());
					event.setHttpReturnedCode(e.getHttpErrorCode().value());
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
	public ResponseEntity<?> deleteAll(IGGAPICaller caller, String customParameters__) {
		GGAPIEvent<Entity> event = new GGAPIEvent<Entity>();
		event.setTenantId(caller.getTenantId());
		event.setOwnerId(caller.getOwnerId());
		event.setOperation(GGAPICrudOperation.delete_all);
		event.setEntityClass(this.entityClass.getName());
		try {
			if (this.ALLOW_DELETE_ALL) {
				ResponseEntity<?> response = null;

				try {
					Map<String, String> customParameters = null;
					if( customParameters__ != null  && ! customParameters__.isEmpty()) {
						TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String,String>>() {};
						customParameters = new ObjectMapper().readValue(customParameters__, typeRef);
					}
					this.controller.get().deleteEntities(caller, customParameters);
					response = new ResponseEntity<>(new GGAPIErrorObject(SUCCESSFULLY_DELETED), HttpStatus.OK);
					event.setHttpReturnedCode(HttpStatus.OK.value());
				} catch (GGAPIEntityException e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setExceptionCode(e.getCode());
					event.setHttpReturnedCode(e.getHttpErrorCode().value());
					response = new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
							e.getHttpErrorCode());
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
	public ResponseEntity<?> getCount(IGGAPICaller caller, String customParameters__) {
		GGAPIEvent<Entity> event = new GGAPIEvent<Entity>();
		event.setTenantId(caller.getTenantId());
		event.setOwnerId(caller.getOwnerId());
		event.setOperation(GGAPICrudOperation.count);
		event.setEntityClass(this.entityClass.getName());
		try {
			if (this.ALLOW_COUNT) {
				ResponseEntity<?> response = null;

				try {
					Map<String, String> customParameters = null;
					if( customParameters__ != null  && ! customParameters__.isEmpty()) {
						TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String,String>>() {};
						customParameters = new ObjectMapper().readValue(customParameters__, typeRef);
					}
					long count = this.controller.get().getEntityTotalCount(caller, null, null, customParameters);
					response = new ResponseEntity<>(count, HttpStatus.OK);
					event.setHttpReturnedCode(HttpStatus.OK.value());
					event.setOutCount(count);
				} catch (GGAPIEntityException e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setExceptionCode(e.getCode());
					event.setHttpReturnedCode(e.getHttpErrorCode().value());
					response = new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
							e.getHttpErrorCode());
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
