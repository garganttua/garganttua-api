package com.garganttua.api.interfaces.spring.rest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.core.pageable.GGAPIPageable;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.filter.GGAPILiteral;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.pageable.IGGAPIPageable;
import com.garganttua.api.spec.service.GGAPIReadOutputMode;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;
import com.garganttua.api.spec.sort.GGAPISort;
import com.garganttua.api.spec.sort.IGGAPISort;

public class GGAPIDomainInterface {
	
	protected IGGAPIService service;
	private ObjectMapper mapper;
	private Class<?> entityClass;
	
	public GGAPIDomainInterface(IGGAPIService service, Class<?> entityClass) {
		this.service = service;
		this.entityClass = entityClass;
		this.mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<?> createEntity(@RequestAttribute(name=GGAPICallerFilter.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller, @RequestBody(required = true) String entity,
			@RequestParam(name = "params", defaultValue = "") String customParameters) {

		Object entityObject = null;
		Map<String, String> customParametersMap = null;
		IGGAPIServiceResponse response = null;
		
		try {
			entityObject = this.mapper.readValue(entity, this.entityClass);
			if( customParameters != null && !customParameters.isEmpty() )
				customParametersMap = this.mapper.readValue(customParameters, Map.class);
		} catch(Exception e) {
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.BAD_REQUEST), HttpStatus.BAD_REQUEST);
		}
		
		try {
			response = this.service.createEntity(caller, entityObject, customParametersMap);
		} catch(Exception e) {
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.UNEXPECTED_ERROR), HttpStatus.BAD_REQUEST);
		}
		
		return GGAPIServiceResponseUtils.toResponseEntity(response);
	}
	
	@SuppressWarnings("unchecked")
	public ResponseEntity<?> getEntities(
			@RequestAttribute(name=GGAPICallerFilter.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller, 
			@RequestParam(name = "mode", defaultValue = "full") GGAPIReadOutputMode mode,
			@RequestParam(name = "pageSize", defaultValue = "0") Integer pageSize,
			@RequestParam(name = "pageIndex", defaultValue = "0") Integer pageIndex,
			@RequestParam(name = "filter", defaultValue = "") String filterString,
			@RequestParam(name = "sort", defaultValue = "") String sortString, 
			@RequestParam(name = "params", defaultValue = "") String customParameters
			) {
		IGGAPISort sort = null;
		Map<String, String> customParametersMap = new HashMap<String, String>();
		IGGAPIServiceResponse response = null;
		IGGAPIPageable pageable = null;
		IGGAPIFilter filter = null;
		
		try {
			if( customParameters != null && !customParameters.isEmpty() )
				customParametersMap = this.mapper.readValue(customParameters, Map.class);
			if( sortString != null && !sortString.isEmpty() )
				sort = (IGGAPISort) this.mapper.readValue(sortString, GGAPISort.class);
			if( filterString != null && !filterString.isEmpty() )
				filter = (IGGAPIFilter) this.mapper.readValue(filterString, GGAPILiteral.class);
		} catch(Exception e) {
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.BAD_REQUEST), HttpStatus.BAD_REQUEST);
		}
		
		if( pageSize != null && pageIndex != null ) {
			pageable = GGAPIPageable.getPage(pageSize, pageIndex);
		}
		
		try {
			response = this.service.getEntities(caller, mode, pageable, filter, sort, customParametersMap);
		} catch( Exception e ) {
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.UNEXPECTED_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return GGAPIServiceResponseUtils.toResponseEntity(response);
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<?> getEntity(@RequestAttribute(name=GGAPICallerFilter.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller, @PathVariable(name = "uuid") String uuid,
			@RequestParam(name = "params", defaultValue = "") String customParameters) {
		IGGAPIServiceResponse response = null;
		Map<String, String> customParametersMap = new HashMap<String, String>();
		try {
			if( customParameters != null && !customParameters.isEmpty() )
				customParametersMap = this.mapper.readValue(customParameters, Map.class);
		} catch(Exception e) {
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.BAD_REQUEST), HttpStatus.BAD_REQUEST);
		}
		
		try {
			response = this.service.getEntity(caller, uuid, customParametersMap);
		} catch( Exception e ) {
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.UNEXPECTED_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return GGAPIServiceResponseUtils.toResponseEntity(response);
	}
	
	@SuppressWarnings("unchecked")
	public ResponseEntity<?> updateEntity(@RequestAttribute(name=GGAPICallerFilter.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller, @PathVariable(name = "uuid") String uuid, @RequestBody(required = true) String entity,
			@RequestParam(name = "params", defaultValue = "") String customParameters) {
		IGGAPIServiceResponse response = null;
		Map<String, String> customParametersMap = new HashMap<String, String>();
		Object entityObject = null;
		try {
			entityObject = this.mapper.readValue(entity, this.entityClass);
			if( customParameters != null && !customParameters.isEmpty() )
				customParametersMap = this.mapper.readValue(customParameters, Map.class);
		} catch(Exception e) {
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.BAD_REQUEST), HttpStatus.BAD_REQUEST);
		}
		
		try {
			response = this.service.updateEntity(caller, uuid, entityObject, customParametersMap);
		} catch( Exception e ) {
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.UNEXPECTED_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return GGAPIServiceResponseUtils.toResponseEntity(response);
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<?> deleteEntity(@RequestAttribute(name=GGAPICallerFilter.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller, @PathVariable(name = "uuid") String uuid,
			@RequestParam(name = "params", defaultValue = "") String customParameters) {
		IGGAPIServiceResponse response = null;
		Map<String, String> customParametersMap = new HashMap<String, String>();
		try {
			if( customParameters != null && !customParameters.isEmpty() )
				customParametersMap = this.mapper.readValue(customParameters, Map.class);
		} catch(Exception e) {
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.BAD_REQUEST), HttpStatus.BAD_REQUEST);
		}
		
		try {
			response = this.service.deleteEntity(caller, uuid, customParametersMap);
		} catch( Exception e ) {
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.UNEXPECTED_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return GGAPIServiceResponseUtils.toResponseEntity(response);
	}
	
	@SuppressWarnings("unchecked")
	public ResponseEntity<?> deleteAll(@RequestAttribute(name=GGAPICallerFilter.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller,
			@RequestParam(name = "filter", defaultValue = "") String filterString,
			@RequestParam(name = "params", defaultValue = "") String customParameters) {
		IGGAPIServiceResponse response = null;
		Map<String, String> customParametersMap = new HashMap<String, String>();
		IGGAPIFilter filter = null;
		try {
			if( customParameters != null && !customParameters.isEmpty() )
				customParametersMap = this.mapper.readValue(customParameters, Map.class);
			if( filterString != null && !filterString.isEmpty() )
				filter = (IGGAPIFilter) this.mapper.readValue(filterString, GGAPILiteral.class);
		} catch(Exception e) {
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.BAD_REQUEST), HttpStatus.BAD_REQUEST);
		}
		
		try {
			response = this.service.deleteAll(caller, filter, customParametersMap);
		} catch( Exception e ) {
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.UNEXPECTED_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return GGAPIServiceResponseUtils.toResponseEntity(response);
	}

}
