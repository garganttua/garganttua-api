package com.garganttua.api.interfaces.spring.rest;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.pageable.GGAPIPageable;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.pageable.IGGAPIPageable;
import com.garganttua.api.spec.service.GGAPIReadOutputMode;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;
import com.garganttua.api.spec.sort.GGAPISort;
import com.garganttua.api.spec.sort.IGGAPISort;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIDomainInterface {
	
	private static final String REQUEST_PARAM_MODE = "mode";
	private static final String REQUEST_PARAM_PAGE_SIZE = "pageSize";
	private static final String REQUEST_PARAM_PAGE_INDEX = "pageIndex";
	private static final String REQUEST_PARAM_SORT = "sort";
	private static final String REQUEST_PARAM_FILTER = "filter";
	
	
	protected IGGAPIService service;
	private ObjectMapper mapper;
	private Class<?> entityClass;
	
	public GGAPIDomainInterface(IGGAPIService service, Class<?> entityClass) {
		this.service = service;
		this.entityClass = entityClass;
		this.mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public ResponseEntity<?> createEntity(@RequestAttribute(name=GGAPICallerFilter.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller, @RequestBody(required = true) String entity,
			@RequestParam Map<String, String> customParameters) {

		Object entityObject = null;
		IGGAPIServiceResponse response = null;

		try {
			entityObject = this.mapper.readValue(entity, this.entityClass);
		} catch(Exception e) {
			log.atDebug().log("error", e);
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.BAD_REQUEST), HttpStatus.BAD_REQUEST);
		}
		
		try {
			response = this.service.createEntity(caller, entityObject, customParameters);
		} catch(Exception e) {
			log.atDebug().log("error", e);
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.UNEXPECTED_ERROR), HttpStatus.BAD_REQUEST);
		}
		
		return GGAPIServiceResponseUtils.toResponseEntity(response);
	}
	
	public ResponseEntity<?> getEntities (
			@RequestAttribute(name=GGAPICallerFilter.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller, 
			@RequestParam Map<String, String> customParameters
			) {
		IGGAPISort sort = null;
		IGGAPIServiceResponse response = null;
		IGGAPIPageable pageable = null;
		IGGAPIFilter filter = null;

		String sortString = this.getAndRemoveRequestParameter(REQUEST_PARAM_SORT, customParameters);
		String filterString = this.getAndRemoveRequestParameter(REQUEST_PARAM_FILTER, customParameters);
		String pageSize = this.getAndRemoveRequestParameter(REQUEST_PARAM_PAGE_SIZE, customParameters);
		String pageIndex = this.getAndRemoveRequestParameter(REQUEST_PARAM_PAGE_INDEX, customParameters);
		String modeString = this.getAndRemoveRequestParameter(REQUEST_PARAM_MODE, customParameters);
		try {
			
			if( sortString  != null && !sortString.isEmpty() )
				sort = (IGGAPISort) this.mapper.readValue(sortString, GGAPISort.class);
			if( filterString != null && !filterString.isEmpty() )
				filter = (IGGAPIFilter) this.mapper.readValue(filterString, GGAPILiteral.class);
		} catch(Exception e) {
			log.atDebug().log("error", e);
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.BAD_REQUEST), HttpStatus.BAD_REQUEST);
		}
		
		if( pageSize != null && pageIndex != null ) {
			pageable = GGAPIPageable.getPage(Integer.valueOf(pageSize), Integer.valueOf(pageIndex));
		}
		
		try {
			GGAPIReadOutputMode mode = modeString==null?GGAPIReadOutputMode.full:GGAPIReadOutputMode.valueOf(modeString);
			response = this.service.getEntities(caller, mode, pageable, filter, sort, customParameters);
		} catch( Exception e ) {
			log.atDebug().log("error", e);
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.UNEXPECTED_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return GGAPIServiceResponseUtils.toResponseEntity(response);
	}

	private String getAndRemoveRequestParameter(String parameterName, Map<String, String> customParameters) {
		String parameterValue = customParameters.get(parameterName);
		if( parameterValue != null ) {
			customParameters.remove(parameterName);
		}
		return parameterValue;
	}

	public ResponseEntity<?> getEntity(@RequestAttribute(name=GGAPICallerFilter.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller, @PathVariable(name = "uuid") String uuid,
			@RequestParam Map<String, String> customParameters) {
		IGGAPIServiceResponse response = null;

		try {
			response = this.service.getEntity(caller, uuid, customParameters);
		} catch( Exception e ) {
			log.atDebug().log("error", e);
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.UNEXPECTED_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return GGAPIServiceResponseUtils.toResponseEntity(response);
	}
	
	public ResponseEntity<?> updateEntity(@RequestAttribute(name=GGAPICallerFilter.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller, @PathVariable(name = "uuid") String uuid, @RequestBody(required = true) String entity,
			@RequestParam Map<String, String> customParameters) {
		IGGAPIServiceResponse response = null;
		
		Object entityObject = null;
		try {
			entityObject = this.mapper.readValue(entity, this.entityClass);
		} catch(Exception e) {
			log.atDebug().log("error", e);
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.BAD_REQUEST), HttpStatus.BAD_REQUEST);
		}
		
		try {
			response = this.service.updateEntity(caller, uuid, entityObject, customParameters);
		} catch( Exception e ) {
			log.atDebug().log("error", e);
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.UNEXPECTED_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return GGAPIServiceResponseUtils.toResponseEntity(response);
	}

	public ResponseEntity<?> deleteEntity(@RequestAttribute(name=GGAPICallerFilter.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller, @PathVariable(name = "uuid") String uuid,
			@RequestParam Map<String, String> customParameters) {
		IGGAPIServiceResponse response = null;
		
		try {
			response = this.service.deleteEntity(caller, uuid, customParameters);
		} catch( Exception e ) {
			log.atDebug().log("error", e);
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.UNEXPECTED_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return GGAPIServiceResponseUtils.toResponseEntity(response);
	}
	
	public ResponseEntity<?> deleteAll(@RequestAttribute(name=GGAPICallerFilter.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller,
			@RequestParam Map<String, String> customParameters) {
		IGGAPIServiceResponse response = null;
		IGGAPIFilter filter = null;
		
		String filterString = this.getAndRemoveRequestParameter(REQUEST_PARAM_FILTER, customParameters);
		try {
			if( filterString != null && !filterString.isEmpty() )
				filter = (IGGAPIFilter) this.mapper.readValue(filterString, GGAPILiteral.class);
		} catch(Exception e) {
			log.atDebug().log("error", e);
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.BAD_REQUEST), HttpStatus.BAD_REQUEST);
		}
		
		try {
			response = this.service.deleteAll(caller, filter, customParameters);
		} catch( Exception e ) {
			log.atDebug().log("error", e);
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.UNEXPECTED_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return GGAPIServiceResponseUtils.toResponseEntity(response);
	}

}
