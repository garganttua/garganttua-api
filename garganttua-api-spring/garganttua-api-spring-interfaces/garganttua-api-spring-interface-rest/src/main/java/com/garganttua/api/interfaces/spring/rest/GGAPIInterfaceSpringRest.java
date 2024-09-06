package com.garganttua.api.interfaces.spring.rest;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.interfasse.IGGAPIInterface;
import com.garganttua.api.spec.service.GGAPIReadOutputMode;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;
import com.garganttua.reflection.beans.annotation.GGBean;
import com.garganttua.reflection.beans.annotation.GGBeanLoadingStrategy;

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@GGBean(name = "SpringRestInterface", strategy = GGBeanLoadingStrategy.newInstance)
public class GGAPIInterfaceSpringRest implements IGGAPIInterface {

	@Setter
	private IGGAPIDomain domain;
	private List<IGGAPIServiceInfos> servicesInfos;
	private IGGAPIService service;

	@Inject
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Setter
	private IGGAPIEngine engine;

	private GGAPIDomainInterface domainInterface;

	@PostConstruct
	public void printLimitations() {
		log.info("============ WARNING ============");
		log.info("= Custom methods are not        =");
		log.info("= supported by this interface   =");
		log.info("=================================");
	}

	@Override
	public void start() throws GGAPIException {
		try {
			this.domainInterface = new GGAPIDomainInterface(service, this.domain.getEntity().getValue0());
			this.createRequestMappings();
		} catch (NoSuchMethodException e) {
			throw new GGAPIEngineException(e);
		}
	}

	@Override
	public void setService(IGGAPIService service, List<IGGAPIServiceInfos> servicesInfos) {
		this.service = service;
		this.servicesInfos = servicesInfos;
	}

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
//		RequestMappingInfo requestMappingInfoCount = RequestMappingInfo.paths(baseUrl + "/count")
//				.methods(RequestMethod.GET).options(options).build();
		RequestMappingInfo requestMappingInfoGetOne = RequestMappingInfo.paths(baseUrl + "/{uuid}")
				.methods(RequestMethod.GET).options(options).build();
		RequestMappingInfo requestMappingInfoUpdate = RequestMappingInfo.paths(baseUrl + "/{uuid}")
				.methods(RequestMethod.PATCH).options(options).build();
		RequestMappingInfo requestMappingInfoDeleteOne = RequestMappingInfo.paths(baseUrl + "/{uuid}")
				.methods(RequestMethod.DELETE).options(options).build();

		if (this.domain.isAllowReadAll()) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoGetAll, this.domainInterface,
					this.domainInterface.getClass().getMethod("getEntities", IGGAPICaller.class, Map.class));
		}
		if (this.domain.isAllowDeleteAll()) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoDeleteAll, this.domainInterface,
					this.domainInterface.getClass().getMethod("deleteAll", IGGAPICaller.class, Map.class));
		}
		if (this.domain.isAllowCreation()) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoCreate, this.domainInterface,
					this.domainInterface.getClass().getMethod("createEntity", IGGAPICaller.class, String.class,
							Map.class));
		}
		if (this.domain.isAllowReadOne()) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoGetOne, this.domainInterface,
					this.domainInterface.getClass().getMethod("getEntity", IGGAPICaller.class, String.class,
							Map.class));
		}
		if (this.domain.isAllowUpdateOne()) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoUpdate, this.domainInterface,
					this.domainInterface.getClass().getMethod("updateEntity", IGGAPICaller.class, String.class,
							String.class, Map.class));
		}
		if (this.domain.isAllowDeleteOne()) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoDeleteOne, this.domainInterface,
					this.domainInterface.getClass().getMethod("deleteEntity", IGGAPICaller.class, String.class,
							Map.class));
		}

//		for (IGGAPIServiceInfos serviceInfos : servicesInfos) {
//			HttpMethod method = GGAPIServiceMethodToHttpMethodBinder.fromServiceMethod(serviceInfos.getMethod());
//			this.requestMappingHandlerMapping.registerMapping(
//					RequestMappingInfo.paths(serviceInfos.getPath())
//							.methods(RequestMethod.resolve(method)).options(options).build(),
//					service, service.getClass().getMethod(serviceInfos.getMethodName(), serviceInfos.getParameters()));
//		}
	}
}
