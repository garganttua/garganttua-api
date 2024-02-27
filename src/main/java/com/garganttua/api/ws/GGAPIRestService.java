/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.ws;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.core.GGAPICrudOperation;
import com.garganttua.api.core.GGAPIEntityException;
import com.garganttua.api.core.GGAPIReadOutputMode;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.core.IGGAPIEntityFactory;
import com.garganttua.api.core.filter.GGAPIGeolocFilter;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.sort.GGAPISort;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.engine.IGGAPIEngine;
import com.garganttua.api.events.GGAPIEvent;
import com.garganttua.api.events.IGGAPIEventPublisher;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.IGGAPISecurity;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author J.Colombet
 *
 * @param <Entity>
 */
public class GGAPIRestService<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>>
		implements IGGAPIRestService<Entity, Dto> {

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

	@Autowired
	@Setter
	@Getter
	protected Optional<IGGAPIEventPublisher<Entity>> eventPublisher;
	
	@Setter
	protected String magicTenantId;

	protected IGGAPIEngine engine;

	@Getter
	private GGAPIDynamicDomain dynamicDomain;

	private IGGAPIEntityFactory factory;

	@Setter
	private Optional<IGGAPISecurity> security = Optional.empty();

	public void setEngine(IGGAPIEngine engine) {
		this.engine = engine;
		this.factory = this.engine.getEntityFactory();
	}
	
	@Override
	public void setDynamicDomain(GGAPIDynamicDomain ddomain) {
		this.dynamicDomain = ddomain;
		this.ALLOW_CREATION = ddomain.allow_creation;
		this.ALLOW_GET_ALL = ddomain.allow_read_all;
		this.ALLOW_GET_ONE = ddomain.allow_read_one;
		this.ALLOW_UPDATE = ddomain.allow_update_one;
		this.ALLOW_DELETE_ONE = ddomain.allow_delete_one;
		this.ALLOW_DELETE_ALL = ddomain.allow_delete_all;
		this.ALLOW_COUNT = ddomain.allow_count;
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
		GGAPIEvent<Entity> event = this.prepareEvent(caller, GGAPICrudOperation.create_one, null);
		
		try {
			if (this.ALLOW_CREATION) {
				try {
					Map<String, String> customParameters = null;
					if( customParameters__ != null  && ! customParameters__.isEmpty()) {
						TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String,String>>() {};
						customParameters = new ObjectMapper().readValue(customParameters__, typeRef);
					}
					Entity entity = this.factory.getEntityFromJson(this.dynamicDomain, customParameters, entity__.getBytes());
					event.setIn(entity);
					entity.save(caller, customParameters, this.security);
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

	private GGAPIEvent<Entity> prepareEvent(IGGAPICaller caller, GGAPICrudOperation operation, Map<String, String> params) {
		GGAPIEvent<Entity> event = new GGAPIEvent<Entity>();
		event.setTenantId(caller.getTenantId());
		event.setOwnerId(caller.getOwnerId());
		event.setOperation(operation);
		event.setEntityClass(this.dynamicDomain.entityClass.getName());
		event.setInParams(params);
		return event;
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
		Map<String, String> params = new HashMap<String, String>();
		params.put("mode", mode.toString());
		params.put("pageSize", pageSize.toString());
		params.put("pageIndex", pageIndex.toString());
		params.put("filterString", filterString);
		params.put("sortString", sortString);
		params.put("geolocString", geolocString);
		GGAPIEvent<Entity> event = this.prepareEvent(caller, GGAPICrudOperation.read_all, params);
		
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
					customParameters = getCustomParametersFromString(customParameters__);
				} catch (JsonProcessingException e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setHttpReturnedCode(HttpStatus.BAD_REQUEST.value());
					return new ResponseEntity<>(
							new GGAPIErrorObject("Error parsing request param : " + e.getMessage()),
							HttpStatus.BAD_REQUEST);
				}

				try {
					entities = this.factory.getEntitiesFromRepository(this.dynamicDomain, caller, pageSize, pageIndex, filter, sort, geoloc, customParameters);
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
					long totalCount = this.factory.countEntities(this.dynamicDomain, caller, filter, geoloc, customParameters);

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
		Map<String, String> params = new HashMap<String, String>();
		params.put("uuid", uuid);
		GGAPIEvent<Entity> event = this.prepareEvent(caller, GGAPICrudOperation.read_one, params);
		try {
			ResponseEntity<?> response = null;

			if (this.ALLOW_GET_ONE) {
				Entity entity;
				try {
					Map<String, String> customParameters = getCustomParametersFromString(customParameters__);
					entity = this.factory.getEntityFromRepository(dynamicDomain, caller, customParameters, uuid);
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
		Map<String, String> params = new HashMap<String, String>();
		params.put("uuid", uuid);
		GGAPIEvent<Entity> event = this.prepareEvent(caller, GGAPICrudOperation.update_one, params);
		try {
			ResponseEntity<?> response = null;

			if (this.ALLOW_UPDATE) {
				try {

					Entity entity = this.factory.getEntityFromJson(this.dynamicDomain, params, entity__.getBytes());
					Map<String, String> customParameters = getCustomParametersFromString(customParameters__);
					entity.setUuid(uuid);
					event.setIn(entity);
					entity.save(caller, customParameters, this.security);
					
					response = new ResponseEntity<>(entity, HttpStatus.OK);
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

	private Map<String, String> getCustomParametersFromString(String customParameters__)
			throws JsonProcessingException, JsonMappingException {
		Map<String, String> customParameters = null;
		if( customParameters__ != null  && ! customParameters__.isEmpty()) {
			TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String,String>>() {};
			customParameters = new ObjectMapper().readValue(customParameters__, typeRef);
		}
		return customParameters;
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
		event.setEntityClass(this.dynamicDomain.entityClass.getName());
		Map<String, String> params = new HashMap<String, String>();
		params.put("uuid", uuid);
		event.setInParams(params);
		try {
			if (this.ALLOW_DELETE_ONE) {
				ResponseEntity<?> response = null;

				try {
					Map<String, String> customParameters = getCustomParametersFromString(customParameters__);
					IGGAPIEntity entity = this.factory.getEntityFromRepository(this.dynamicDomain, caller, customParameters, uuid);
					
					entity.delete(caller, customParameters);
					
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
	public ResponseEntity<?> deleteAll(IGGAPICaller caller, String filterString, String geolocString, String customParameters__) {
		GGAPIEvent<Entity> event = new GGAPIEvent<Entity>();
		event.setTenantId(caller.getTenantId());
		event.setOwnerId(caller.getOwnerId());
		event.setOperation(GGAPICrudOperation.delete_all);
		event.setEntityClass(this.dynamicDomain.entityClass.getName());
		try {
			if (this.ALLOW_DELETE_ALL) {
				ResponseEntity<?> response = null;
				
				ObjectMapper mapper = new ObjectMapper();
				GGAPILiteral filter = null;
				GGAPISort sort = null;
				GGAPIGeolocFilter geoloc = null;
				Map<String, String> customParameters = null;
				try {
					if (filterString != null && !filterString.isEmpty()) {
						filter = mapper.readValue(filterString, GGAPILiteral.class);
					}
					if (geolocString != null && !geolocString.isEmpty()) {
						geoloc = mapper.readValue(geolocString, GGAPIGeolocFilter.class);
					}
					customParameters = getCustomParametersFromString(customParameters__);
				} catch (JsonProcessingException e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setHttpReturnedCode(HttpStatus.BAD_REQUEST.value());
					return new ResponseEntity<>(
							new GGAPIErrorObject("Error parsing request param : " + e.getMessage()),
							HttpStatus.BAD_REQUEST);
				}

				try {
					final Map<String, String> cp = customParameters;
					this.factory.getEntitiesFromRepository(this.dynamicDomain, caller, 0, 0, filter, sort, geoloc, customParameters).forEach(entity ->{
						try {
							entity.delete(caller, cp);
						} catch (GGAPIEntityException | GGAPIEngineException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});
					
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
	public ResponseEntity<?> getCount(IGGAPICaller caller, String filterString, String geolocString, String customParameters__) {
		GGAPIEvent<Entity> event = new GGAPIEvent<Entity>();
		event.setTenantId(caller.getTenantId());
		event.setOwnerId(caller.getOwnerId());
		event.setOperation(GGAPICrudOperation.count);
		event.setEntityClass(this.dynamicDomain.entityClass.getName());
		try {
			if (this.ALLOW_COUNT) {
				ResponseEntity<?> response = null;
				
				ObjectMapper mapper = new ObjectMapper();
				GGAPILiteral filter = null;
				GGAPISort sort = null;
				GGAPIGeolocFilter geoloc = null;
				Map<String, String> customParameters = null;
				try {
					if (filterString != null && !filterString.isEmpty()) {
						filter = mapper.readValue(filterString, GGAPILiteral.class);
					}
					if (geolocString != null && !geolocString.isEmpty()) {
						geoloc = mapper.readValue(geolocString, GGAPIGeolocFilter.class);
					}
					customParameters = getCustomParametersFromString(customParameters__);
				} catch (JsonProcessingException e) {
//					event.setException(e);
					event.setExceptionMessage(e.getMessage());
					event.setHttpReturnedCode(HttpStatus.BAD_REQUEST.value());
					return new ResponseEntity<>(
							new GGAPIErrorObject("Error parsing request param : " + e.getMessage()),
							HttpStatus.BAD_REQUEST);
				}

				long count = this.factory.countEntities(this.dynamicDomain, caller, filter, geoloc, customParameters);
				response = new ResponseEntity<>(count, HttpStatus.OK);
				event.setHttpReturnedCode(HttpStatus.OK.value());
				event.setOutCount(count);

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
}
