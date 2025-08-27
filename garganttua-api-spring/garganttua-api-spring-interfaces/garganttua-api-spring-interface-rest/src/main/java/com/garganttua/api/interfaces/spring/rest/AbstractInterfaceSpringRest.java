package com.garganttua.api.interfaces.spring.rest;

import java.lang.reflect.Method;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.core.filter.Literal;
import com.garganttua.api.core.pageable.Pageable;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.interfasse.InterfaceMethod;
import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.service.ReadOutputMode;
import com.garganttua.api.spec.service.IService;
import com.garganttua.api.spec.service.IServiceResponse;
import com.garganttua.api.spec.sort.Sort;
import com.garganttua.api.spec.sort.ISort;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@Slf4j
public abstract class AbstractInterfaceSpringRest implements IInterface {

	private static final String REQUEST_PARAM_MODE = "mode";
	private static final String REQUEST_PARAM_PAGE_SIZE = "pageSize";
	private static final String REQUEST_PARAM_PAGE_INDEX = "pageIndex";
	private static final String REQUEST_PARAM_SORT = "sort";
	private static final String REQUEST_PARAM_FILTER = "filter";

	@Inject
	private ObjectMapper mapper;

	private Class<?> entityClass;

	@Setter
	protected IDomain domain;

	@Inject
	protected RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Setter
	protected IEngine engine;

	@Setter
	protected IService service;

	@Override
	public void start() throws CoreException {
		try {
			this.entityClass = this.domain.getEntityClass();
			this.createRequestMappings();
			this.createCustomMappings(this.requestMappingHandlerMapping);
		} catch (NoSuchMethodException e) {
			throw new EngineException(e);
		}
	}

	protected abstract void createCustomMappings(RequestMappingHandlerMapping requestMappingHandlerMapping)
			throws NoSuchMethodException;

	private void createRequestMappings() throws NoSuchMethodException {
		RequestMappingInfo.BuilderConfiguration options = new RequestMappingInfo.BuilderConfiguration();
		options.setPatternParser(new PathPatternParser());

		String baseUrl = "/api/" + this.domain.getDomain();

		RequestMappingInfo requestMappingInfoGetAll = RequestMappingInfo.paths(baseUrl).methods(RequestMethod.GET)
				.options(options).build();
		RequestMappingInfo requestMappingInfoDeleteAll = RequestMappingInfo.paths(baseUrl).methods(RequestMethod.DELETE)
				.options(options).build();
		RequestMappingInfo requestMappingInfoCreate = RequestMappingInfo.paths(baseUrl).methods(RequestMethod.POST)
				.options(options).build();
		RequestMappingInfo requestMappingInfoGetOne = RequestMappingInfo.paths(baseUrl + "/{uuid}")
				.methods(RequestMethod.GET).options(options).build();
		RequestMappingInfo requestMappingInfoUpdate = RequestMappingInfo.paths(baseUrl + "/{uuid}")
				.methods(RequestMethod.PATCH).options(options).build();
		RequestMappingInfo requestMappingInfoDeleteOne = RequestMappingInfo.paths(baseUrl + "/{uuid}")
				.methods(RequestMethod.DELETE).options(options).build();

		if (this.domain.isAllowReadAll()) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoGetAll, this,
					this.getClass().getMethod("getEntities", ICaller.class, Map.class));
		}
		if (this.domain.isAllowDeleteAll()) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoDeleteAll, this,
					this.getClass().getMethod("deleteAll", ICaller.class, Map.class));
		}
		if (this.domain.isAllowCreation()) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoCreate, this,
					this.getClass().getMethod("createEntity", ICaller.class, String.class, Map.class));
		}
		if (this.domain.isAllowReadOne()) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoGetOne, this,
					this.getClass().getMethod("getEntity", ICaller.class, String.class, Map.class));
		}
		if (this.domain.isAllowUpdateOne()) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoUpdate, this, this.getClass()
					.getMethod("updateEntity", ICaller.class, String.class, String.class, Map.class));
		}
		if (this.domain.isAllowDeleteOne()) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoDeleteOne, this,
					this.getClass().getMethod("deleteEntity", ICaller.class, String.class, Map.class));
		}
	}

	@Override
	public String getName() {
		return "SpringRestInterface-" + this.domain.getDomain();
	}

	@Override
	public Method getMethod(InterfaceMethod method) {
		Method method_ = null;
		switch (method) {
		case count -> {
			return null;
		}
		case createOne -> {
			try {
				method_ = this.getClass().getMethod("createEntity", ICaller.class, String.class, Map.class);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}
		case deleteAll -> {
			try {
				method_ = this.getClass().getMethod("deleteAll", ICaller.class, Map.class);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}
		case deleteOne -> {
			try {
				method_ = this.getClass().getMethod("deleteEntity", ICaller.class, String.class, Map.class);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}
		case readAll -> {
			try {
				method_ = this.getClass().getMethod("getEntities", ICaller.class, Map.class);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}
		case readOne -> {
			try {
				method_ = this.getClass().getMethod("getEntity", ICaller.class, String.class, Map.class);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}
		case updateOne -> {
			try {
				method_ = this.getClass().getMethod("updateEntity", ICaller.class, String.class, String.class,
						Map.class);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}
		}
		return method_;
	}

	// Interface handling methods

	public ResponseEntity<?> createEntity(
			@RequestAttribute(name = CallerFilter.CALLER_ATTRIBUTE_NAME) ICaller caller,
			@RequestBody(required = true) String entity, @RequestParam Map<String, String> customParameters) {

		Object entityObject = null;
		IServiceResponse response = null;

		try {
			entityObject = this.mapper.readValue(entity, this.entityClass);
		} catch (Exception e) {
			log.atDebug().log("error", e);
			return new ResponseEntity<>(new ResponseObject(e.getMessage(), ResponseObject.BAD_REQUEST),
					HttpStatus.BAD_REQUEST);
		}

		try {
			response = this.service.createEntity(caller, entityObject, customParameters);
		} catch (Exception e) {
			log.atDebug().log("error", e);
			return new ResponseEntity<>(new ResponseObject(e.getMessage(), ResponseObject.UNEXPECTED_ERROR),
					HttpStatus.BAD_REQUEST);
		}

		return ServiceResponseUtils.toResponseEntity(response);
	}

	public ResponseEntity<?> getEntities(
			@RequestAttribute(name = CallerFilter.CALLER_ATTRIBUTE_NAME) ICaller caller,
			@RequestParam Map<String, String> customParameters) {
		ISort sort = null;
		IServiceResponse response = null;
		IPageable pageable = null;
		IFilter filter = null;

		String sortString = this.getAndRemoveRequestParameter(REQUEST_PARAM_SORT, customParameters);
		String filterString = this.getAndRemoveRequestParameter(REQUEST_PARAM_FILTER, customParameters);
		String pageSize = this.getAndRemoveRequestParameter(REQUEST_PARAM_PAGE_SIZE, customParameters);
		String pageIndex = this.getAndRemoveRequestParameter(REQUEST_PARAM_PAGE_INDEX, customParameters);
		String modeString = this.getAndRemoveRequestParameter(REQUEST_PARAM_MODE, customParameters);
		try {

			if (sortString != null && !sortString.isEmpty())
				sort = (ISort) this.mapper.readValue(sortString, Sort.class);
			if (filterString != null && !filterString.isEmpty())
				filter = (IFilter) this.mapper.readValue(filterString, Literal.class);
		} catch (Exception e) {
			log.atDebug().log("error", e);
			return new ResponseEntity<>(new ResponseObject(e.getMessage(), ResponseObject.BAD_REQUEST),
					HttpStatus.BAD_REQUEST);
		}

		if (pageSize != null && pageIndex != null) {
			pageable = Pageable.getPage(Integer.valueOf(pageSize), Integer.valueOf(pageIndex));
		}

		try {
			ReadOutputMode mode = modeString == null ? ReadOutputMode.full
					: ReadOutputMode.valueOf(modeString);
			response = this.service.getEntities(caller, mode, pageable, filter, sort, customParameters);
		} catch (Exception e) {
			log.atDebug().log("error", e);
			return new ResponseEntity<>(new ResponseObject(e.getMessage(), ResponseObject.UNEXPECTED_ERROR),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return ServiceResponseUtils.toResponseEntity(response);
	}

	private String getAndRemoveRequestParameter(String parameterName, Map<String, String> customParameters) {
		String parameterValue = customParameters.get(parameterName);
		if (parameterValue != null) {
			customParameters.remove(parameterName);
		}
		return parameterValue;
	}

	public ResponseEntity<?> getEntity(
			@RequestAttribute(name = CallerFilter.CALLER_ATTRIBUTE_NAME) ICaller caller,
			@PathVariable(name = "uuid") String uuid, @RequestParam Map<String, String> customParameters) {
		IServiceResponse response = null;

		try {
			response = this.service.getEntity(caller, uuid, customParameters);
		} catch (Exception e) {
			log.atDebug().log("error", e);
			return new ResponseEntity<>(new ResponseObject(e.getMessage(), ResponseObject.UNEXPECTED_ERROR),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return ServiceResponseUtils.toResponseEntity(response);
	}

	public ResponseEntity<?> updateEntity(
			@RequestAttribute(name = CallerFilter.CALLER_ATTRIBUTE_NAME) ICaller caller,
			@PathVariable(name = "uuid") String uuid, @RequestBody(required = true) String entity,
			@RequestParam Map<String, String> customParameters) {
		IServiceResponse response = null;

		Object entityObject = null;
		try {
			entityObject = this.mapper.readValue(entity, this.entityClass);
		} catch (Exception e) {
			log.atDebug().log("error", e);
			return new ResponseEntity<>(new ResponseObject(e.getMessage(), ResponseObject.BAD_REQUEST),
					HttpStatus.BAD_REQUEST);
		}

		try {
			response = this.service.updateEntity(caller, uuid, entityObject, customParameters);
		} catch (Exception e) {
			log.atDebug().log("error", e);
			return new ResponseEntity<>(new ResponseObject(e.getMessage(), ResponseObject.UNEXPECTED_ERROR),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return ServiceResponseUtils.toResponseEntity(response);
	}

	public ResponseEntity<?> deleteEntity(
			@RequestAttribute(name = CallerFilter.CALLER_ATTRIBUTE_NAME) ICaller caller,
			@PathVariable(name = "uuid") String uuid, @RequestParam Map<String, String> customParameters) {
		IServiceResponse response = null;

		try {
			response = this.service.deleteEntity(caller, uuid, customParameters);
		} catch (Exception e) {
			log.atDebug().log("error", e);
			return new ResponseEntity<>(new ResponseObject(e.getMessage(), ResponseObject.UNEXPECTED_ERROR),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return ServiceResponseUtils.toResponseEntity(response);
	}

	public ResponseEntity<?> deleteAll(
			@RequestAttribute(name = CallerFilter.CALLER_ATTRIBUTE_NAME) ICaller caller,
			@RequestParam Map<String, String> customParameters) {
		IServiceResponse response = null;
		IFilter filter = null;

		String filterString = this.getAndRemoveRequestParameter(REQUEST_PARAM_FILTER, customParameters);
		try {
			if (filterString != null && !filterString.isEmpty())
				filter = (IFilter) this.mapper.readValue(filterString, Literal.class);
		} catch (Exception e) {
			log.atDebug().log("error", e);
			return new ResponseEntity<>(new ResponseObject(e.getMessage(), ResponseObject.BAD_REQUEST),
					HttpStatus.BAD_REQUEST);
		}

		try {
			response = this.service.deleteAll(caller, filter, customParameters);
		} catch (Exception e) {
			log.atDebug().log("error", e);
			return new ResponseEntity<>(new ResponseObject(e.getMessage(), ResponseObject.UNEXPECTED_ERROR),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return ServiceResponseUtils.toResponseEntity(response);
	}

}
