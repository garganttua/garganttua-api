/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.service.rest;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.core.GGAPIReadOutputMode;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.factory.GGAPIEntityIdentifier;
import com.garganttua.api.core.entity.factory.IGGAPIEntityFactory;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntity;
import com.garganttua.api.core.filter.GGAPIGeolocFilter;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.sort.GGAPISort;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.engine.IGGAPIEngine;
import com.garganttua.api.events.GGAPIEvent;
import com.garganttua.api.events.IGGAPIEventPublisher;
import com.garganttua.api.security.IGGAPISecurity;
import com.garganttua.api.service.GGAPIErrorObject;
import com.garganttua.api.service.GGAPIServiceEntityPage;
import com.garganttua.api.service.GGAPIServiceMethod;
import com.garganttua.api.service.IGGAPIService;
import com.garganttua.api.service.rest.filters.GGAPICallerManager;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author J.Colombet
 *
 * @param <Entity>
 */
public class GGAPIRestService<Entity, Dto> implements IGGAPIService<Entity, Dto> {

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

	protected IGGAPIEntityFactory factory;

	@Setter
	protected Optional<IGGAPISecurity> security = Optional.empty();

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
	@RequestMapping(value = "", method = RequestMethod.POST)
	public ResponseEntity<?> createEntity(@RequestAttribute(name=GGAPICallerManager.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller, @RequestBody(required = true) String entity__, @RequestParam(name = "params", defaultValue = "") String customParameters__) {
		ResponseEntity<?> response = null;
		Map<String, String> customParameters = null;
		try {
			customParameters = getCustomParametersFromString(customParameters__);
		} catch (JsonProcessingException e) {
			Map<String, String> customs = new HashMap<String, String>();
			customs.put("body",entity__);
			GGAPIEvent<Entity> event = this.prepareEvent(caller, GGAPIServiceMethod.CREATE, customs);
			this.eventPublisher.get().publishEvent(event);
			event.setHttpReturnedCode(HttpStatus.BAD_REQUEST.value());
			event.setOutDate(new Date());
			return new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
					HttpStatus.BAD_REQUEST);
		}
		GGAPIEvent<Entity> event = this.prepareEvent(caller, GGAPIServiceMethod.CREATE, customParameters);
		
		try {
			if (this.ALLOW_CREATION) {
				try {
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

	protected GGAPIEvent<Entity> prepareEvent(IGGAPICaller caller, GGAPIServiceMethod method, Map<String, String> params) {
		GGAPIEvent<Entity> event = new GGAPIEvent<Entity>();
		event.setTenantId(caller.getTenantId());
		event.setOwnerId(caller.getOwnerId());
		event.setCaller(caller);
		event.setMethod(method);
		event.setEndPoint(caller.getAccessRule().getEndpoint());
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
	@RequestMapping(value = "", method = RequestMethod.GET)
	public ResponseEntity<?> getEntities(@RequestAttribute(name=GGAPICallerManager.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller, 
			@RequestParam(name = "mode", defaultValue = "full") GGAPIReadOutputMode mode,
			@RequestParam(name = "pageSize", defaultValue = "0") Integer pageSize,
			@RequestParam(name = "pageIndex", defaultValue = "0") Integer pageIndex,
			@RequestParam(name = "filter", defaultValue = "") String filterString,
			@RequestParam(name = "sort", defaultValue = "") String sortString, 
			@RequestParam(name = "geoloc", defaultValue = "") String geolocString,
			@RequestParam(name = "params", defaultValue = "") String customParameters__) {

		
		Map<String, String> customParameters = null;
		try {
			customParameters = getCustomParametersFromString(customParameters__);
		} catch (JsonProcessingException e) {
			Map<String, String> customs = new HashMap<String, String>();
			customs.put("mode", mode.toString());
			customs.put("pageSize", pageSize.toString());
			customs.put("pageIndex", pageIndex.toString());
			customs.put("filterString", filterString);
			customs.put("sortString", sortString);
			customs.put("geolocString", geolocString);
			GGAPIEvent<Entity> event = this.prepareEvent(caller, GGAPIServiceMethod.READ, customs);
			this.eventPublisher.get().publishEvent(event);
			event.setHttpReturnedCode(HttpStatus.BAD_REQUEST.value());
			event.setOutDate(new Date());
			return new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
					HttpStatus.BAD_REQUEST);
		}
		customParameters.put("mode", mode.toString());
		customParameters.put("pageSize", pageSize.toString());
		customParameters.put("pageIndex", pageIndex.toString());
		customParameters.put("filterString", filterString);
		customParameters.put("sortString", sortString);
		customParameters.put("geolocString", geolocString);
		GGAPIEvent<Entity> event = this.prepareEvent(caller, GGAPIServiceMethod.READ, customParameters);
		
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

					GGAPIServiceEntityPage page = new GGAPIServiceEntityPage(totalCount, ((List<Object>) entities));
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
	@RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
	public ResponseEntity<?> getEntity(@RequestAttribute(name=GGAPICallerManager.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller, @PathVariable(name = "uuid") String uuid,
			@RequestParam(name = "params", defaultValue = "") String customParameters__) {
		Map<String, String> customParameters = null;
		try {
			customParameters = getCustomParametersFromString(customParameters__);
		} catch (JsonProcessingException e) {
			Map<String, String> customs = new HashMap<String, String>();
			customs.put("uuid", uuid);
			GGAPIEvent<Entity> event = this.prepareEvent(caller, GGAPIServiceMethod.READ, customs);
			this.eventPublisher.get().publishEvent(event);
			event.setHttpReturnedCode(HttpStatus.BAD_REQUEST.value());
			event.setOutDate(new Date());
			return new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
					HttpStatus.BAD_REQUEST);
		}
		customParameters.put("uuid", uuid);
		GGAPIEvent<Entity> event = this.prepareEvent(caller, GGAPIServiceMethod.READ, customParameters);
		try {
			ResponseEntity<?> response = null;

			if (this.ALLOW_GET_ONE) {
				Entity entity;
				try {
					entity = this.factory.getEntityFromRepository(dynamicDomain, caller, customParameters, GGAPIEntityIdentifier.UUID, uuid);
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
	@RequestMapping(value = "/{uuid}", method = RequestMethod.PATCH)
	public ResponseEntity<?> updateEntity(@RequestAttribute(name=GGAPICallerManager.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller, @PathVariable(name = "uuid") String uuid, @RequestBody(required = true) String entity__,
			@RequestParam(name = "params", defaultValue = "") String customParameters__) {
		Map<String, String> customParameters = null;
		try {
			customParameters = getCustomParametersFromString(customParameters__);
		} catch (JsonProcessingException e) {
			Map<String, String> customs = new HashMap<String, String>();
			customs.put("uuid", uuid);
			customs.put("body",entity__);
			GGAPIEvent<Entity> event = this.prepareEvent(caller, GGAPIServiceMethod.PARTIAL_UPDATE, customs);
			this.eventPublisher.get().publishEvent(event);
			event.setHttpReturnedCode(HttpStatus.BAD_REQUEST.value());
			event.setOutDate(new Date());
			return new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
					HttpStatus.BAD_REQUEST);
		}
		customParameters.put("uuid", uuid);
		GGAPIEvent<Entity> event = this.prepareEvent(caller, GGAPIServiceMethod.PARTIAL_UPDATE, customParameters);
		try {
			ResponseEntity<?> response = null;

			if (this.ALLOW_UPDATE) {
				try {

					Entity entity = this.factory.getEntityFromJson(this.dynamicDomain, customParameters, entity__.getBytes());
					
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

	protected Map<String, String> getCustomParametersFromString(String customParameters__)
			throws JsonProcessingException, JsonMappingException {
		Map<String, String> customParameters = null;
		if( customParameters__ != null  && ! customParameters__.isEmpty()) {
			TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String,String>>() {};
			customParameters = new ObjectMapper().readValue(customParameters__, typeRef);
		} else {
			customParameters = new HashMap<String, String>();
		}
		return customParameters;
	}

	/**
	 * Delete an entity.
	 * 
	 * @return
	 */
	@Override
	@RequestMapping(value = "/{uuid}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteEntity(@RequestAttribute(name=GGAPICallerManager.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller, @PathVariable(name = "uuid") String uuid,
			@RequestParam(name = "params", defaultValue = "") String customParameters__) {

		Map<String, String> customParameters = null;
		try {
			customParameters = getCustomParametersFromString(customParameters__);
		} catch (JsonProcessingException e) {
			Map<String, String> customs = new HashMap<String, String>();
			customs.put("uuid", uuid);
			GGAPIEvent<Entity> event = this.prepareEvent(caller, GGAPIServiceMethod.DELETE, customs);
			this.eventPublisher.get().publishEvent(event);
			event.setHttpReturnedCode(HttpStatus.BAD_REQUEST.value());
			event.setOutDate(new Date());
			return new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
					HttpStatus.BAD_REQUEST);
		}
		customParameters.put("uuid", uuid);
		GGAPIEvent<Entity> event = this.prepareEvent(caller, GGAPIServiceMethod.DELETE, customParameters);
		
		try {
			if (this.ALLOW_DELETE_ONE) {
				ResponseEntity<?> response = null;

				try {					
					IGGAPIEntity entity = this.factory.getEntityFromRepository(this.dynamicDomain, caller, customParameters, GGAPIEntityIdentifier.UUID, uuid);
					
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
	@RequestMapping(value = "", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteAll(@RequestAttribute(name=GGAPICallerManager.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller,
			@RequestParam(name = "filter", defaultValue = "") String filterString,
			@RequestParam(name = "geoloc", defaultValue = "") String geolocString,
			@RequestParam(name = "params", defaultValue = "") String customParameters__) {
		
		Map<String, String> customParameters = null;
		try {
			customParameters = getCustomParametersFromString(customParameters__);
		} catch (JsonProcessingException e) {
			Map<String, String> customs = new HashMap<String, String>();
			customs.put("filter", filterString);
			customs.put("geoloc", geolocString);
			customs.put("customs", customParameters__);
			GGAPIEvent<Entity> event = this.prepareEvent(caller, GGAPIServiceMethod.DELETE, customs);
			event.setHttpReturnedCode(HttpStatus.BAD_REQUEST.value());
			event.setOutDate(new Date());
			this.eventPublisher.get().publishEvent(event);
			return new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
					HttpStatus.BAD_REQUEST);
		}
		customParameters.put("filter", filterString);
		customParameters.put("geoloc", geolocString);
		GGAPIEvent<Entity> event = this.prepareEvent(caller, GGAPIServiceMethod.DELETE, customParameters);
		
		try {
			if (this.ALLOW_DELETE_ALL) {
				ResponseEntity<?> response = null;
				
				ObjectMapper mapper = new ObjectMapper();
				GGAPILiteral filter = null;
				GGAPISort sort = null;
				GGAPIGeolocFilter geoloc = null;
				try {
					if (filterString != null && !filterString.isEmpty()) {
						filter = mapper.readValue(filterString, GGAPILiteral.class);
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
	@RequestMapping(value = "/count", method = RequestMethod.GET)
	public ResponseEntity<?> getCount(
			@RequestAttribute(name=GGAPICallerManager.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller,
			@RequestParam(name = "filter", defaultValue = "") String filterString,
			@RequestParam(name = "geoloc", defaultValue = "") String geolocString,
			@RequestParam(name = "params", defaultValue = "") String customParameters__) {
		
		Map<String, String> customParameters = null;
		try {
			customParameters = getCustomParametersFromString(customParameters__);
		} catch (JsonProcessingException e) {
			Map<String, String> customs = new HashMap<String, String>();
			customs.put("filter", filterString);
			customs.put("geoloc", geolocString);
			customs.put("customs", customParameters__);
			GGAPIEvent<Entity> event = this.prepareEvent(caller, GGAPIServiceMethod.READ, customs);
			event.setHttpReturnedCode(HttpStatus.BAD_REQUEST.value());
			event.setOutDate(new Date());
			this.eventPublisher.get().publishEvent(event);
			return new ResponseEntity<>(new GGAPIErrorObject(e.getMessage()),
					HttpStatus.BAD_REQUEST);
		}
		customParameters.put("filter", filterString);
		customParameters.put("geoloc", geolocString);
		GGAPIEvent<Entity> event = this.prepareEvent(caller, GGAPIServiceMethod.READ, customParameters);
		
		try {
			if (this.ALLOW_COUNT) {
				ResponseEntity<?> response = null;
				
				ObjectMapper mapper = new ObjectMapper();
				GGAPILiteral filter = null;
				GGAPISort sort = null;
				GGAPIGeolocFilter geoloc = null;
				try {
					if (filterString != null && !filterString.isEmpty()) {
						filter = mapper.readValue(filterString, GGAPILiteral.class);
					}
					if (geolocString != null && !geolocString.isEmpty()) {
						geoloc = mapper.readValue(geolocString, GGAPIGeolocFilter.class);
					}
					customParameters = getCustomParametersFromString(customParameters__);
					event = this.prepareEvent(caller, GGAPIServiceMethod.READ, customParameters);
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
