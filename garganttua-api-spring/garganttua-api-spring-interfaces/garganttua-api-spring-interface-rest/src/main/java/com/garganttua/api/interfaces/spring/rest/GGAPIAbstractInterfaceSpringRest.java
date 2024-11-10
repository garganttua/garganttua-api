package com.garganttua.api.interfaces.spring.rest;

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
import com.garganttua.api.spec.service.IGGAPIService;

import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public abstract class GGAPIAbstractInterfaceSpringRest implements IGGAPIInterface {

	@Setter
	protected IGGAPIDomain domain;
	protected IGGAPIService service;

	@Inject
	protected RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Setter
	protected IGGAPIEngine engine;

	protected GGAPIDomainInterface domainInterface;

	@Override
	public void start() throws GGAPIException {
		try {
			this.domainInterface = new GGAPIDomainInterface(this.service, this.domain.getEntity().getValue0());
			this.createRequestMappings();
			this.createCustomMappings(this.requestMappingHandlerMapping);
		} catch (NoSuchMethodException e) {
			throw new GGAPIEngineException(e);
		}
	}

	protected abstract void createCustomMappings(RequestMappingHandlerMapping requestMappingHandlerMapping);

	@Override
	public void setService(IGGAPIService service) {
		this.service = service;
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
	}

	@Override
	public String getName() {
		return "SpringRestInterface-"+this.domain.getDomain();
	}
}
