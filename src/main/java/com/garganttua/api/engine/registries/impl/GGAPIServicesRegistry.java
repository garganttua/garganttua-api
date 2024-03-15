package com.garganttua.api.engine.registries.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

import com.garganttua.api.core.GGAPICrudOperation;
import com.garganttua.api.core.GGAPIReadOutputMode;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.entity.annotations.GGAPIEntity;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntity;
import com.garganttua.api.engine.GGAPIDomain;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.engine.GGAPIObjectsHelper;
import com.garganttua.api.engine.GGAPIOpenAPIHelper;
import com.garganttua.api.engine.registries.IGGAPIDomainsRegistry;
import com.garganttua.api.engine.registries.IGGAPIServicesRegistry;
import com.garganttua.api.events.IGGAPIEventPublisher;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.authorization.BasicGGAPIAccessRule;
import com.garganttua.api.service.GGAPICustomServiceBuilder;
import com.garganttua.api.service.IGGAPICustomService;
import com.garganttua.api.service.IGGAPIService;
import com.garganttua.api.service.rest.GGAPIRestService;
import com.garganttua.api.service.rest.GGAPIServiceMethodToHttpMethodBinder;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service(value = "servicesRegistry")
public class GGAPIServicesRegistry implements IGGAPIServicesRegistry {

	@Autowired
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Autowired
	private GGAPIObjectsHelper helper;

	@Value("${com.garganttua.api.superTenantId}")
	private String magicTenantId;
	
	@Autowired
	private Optional<OpenAPI> openApi;
	
	private GGAPIOpenAPIHelper openApiHelper = new GGAPIOpenAPIHelper();

	private Map<String, IGGAPIService<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>> restServices = new HashMap<String, IGGAPIService<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>>();

	@Autowired
	private IGGAPIDomainsRegistry dynamicDomains;

	@Override
	public IGGAPIService<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> getService(
			String name) {
		return this.restServices.get(name);
	}

	@Override
	public List<IGGAPIService<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>> getServices() {
		List<IGGAPIService<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>> services = new ArrayList<IGGAPIService<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>>();
		this.restServices.forEach((k, v) -> {
			services.add(v);
		});
		return services;
	}

	@SuppressWarnings("unchecked")
	@PostConstruct
	private void init() throws GGAPIEngineException {

		log.info("Creating Rest Services ...");
		for (GGAPIDomain ddomain : this.dynamicDomains.getDynamicDomains()) {

			String ws__ = ddomain.ws;

			IGGAPIService<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> service;
			if (ws__ != null && !ws__.isEmpty()) {
				service = helper.getObjectFromConfiguration(ws__, IGGAPIService.class);
			} else {
				service = new GGAPIRestService<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>();
			}
			
			List<IGGAPICustomService> customServices = GGAPICustomServiceBuilder.buildGGAPIServices(service.getClass());
			
			Optional<IGGAPIEventPublisher<IGGAPIEntity>> eventObj = this.getEventPublisher(ddomain);
			service.setDynamicDomain(ddomain);
			service.setEventPublisher(eventObj);

			String baseUrl = "/" + ddomain.domain.toLowerCase();
			
			try {
				this.createRequestMappings(ddomain, service, baseUrl, customServices);
				this.setOpenApiDocumentation(service, ddomain, baseUrl, customServices);
			} catch (NoSuchMethodException | IOException e) {
				throw new GGAPIEngineException(e);
			}

			this.restServices.put(ddomain.domain, service);
			
			log.info("	Rest Service added [domain {}]", ddomain.domain);
		}
	}

	@SuppressWarnings("unchecked")
	private Optional<IGGAPIEventPublisher<IGGAPIEntity>> getEventPublisher(GGAPIDomain ddomain) throws GGAPIEngineException {
		if( ddomain.event != null && !ddomain.event.isEmpty() ) {
			return Optional.ofNullable(this.helper.getObjectFromConfiguration(ddomain.event, IGGAPIEventPublisher.class));
		}
		return Optional.ofNullable(null);
	}

	@SuppressWarnings("unchecked")
	private void setOpenApiDocumentation(IGGAPIService<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> service, GGAPIDomain ddomain, String baseUrl, List<IGGAPICustomService> customServices) throws IOException {
		if( this.openApi.isPresent() ) {
		
			Class<? extends IGGAPIEntity> entityClass = ddomain.entityClass;
		
			Tag tag = new Tag().name("Domain " + ddomain.domain.toLowerCase()).description("Public Entity ["+ddomain.publicEntity+"] Shared Entity ["+(ddomain.shared.isEmpty()?"false":ddomain.shared)+"] Hiddenable Entity ["+ddomain.hiddenable+"] Geolocalized ["+(ddomain.geolocalized.isEmpty()?"false":ddomain.geolocalized)+"]");
			this.openApi.get().addTagsItem(tag);

			GGAPIEntity entityAnnotation = ((Class<IGGAPIEntity>) entityClass).getAnnotation(GGAPIEntity.class);

			OpenAPI templateOpenApi = this.openApiHelper.getOpenApi(ddomain.domain.toLowerCase(), entityClass.getSimpleName(), entityAnnotation.openApiSchemas());
			PathItem pathItemBase = new PathItem();
			PathItem pathItemCount = new PathItem();
			PathItem pathItemUuid = new PathItem();

			this.openApi.get().getComponents().addSchemas(entityClass.getSimpleName(), templateOpenApi.getComponents().getSchemas().get(entityClass.getSimpleName()));
			this.openApi.get().getComponents().addSchemas("ErrorObject", templateOpenApi.getComponents().getSchemas().get("ErrorObject"));
			this.openApi.get().getComponents().addSchemas("SortQuery", templateOpenApi.getComponents().getSchemas().get("SortQuery"));
			this.openApi.get().getComponents().addSchemas("FilterQuery", templateOpenApi.getComponents().getSchemas().get("FilterQuery"));
		
			if (ddomain.allow_read_all) {
				this.openApi.get().path(baseUrl, pathItemBase.get(templateOpenApi.getPaths().get(baseUrl).getGet().description("Access : ["+ddomain.read_all_access+"] - Authority ["+(ddomain.read_all_authority==false?"none":BasicGGAPIAccessRule.getAuthority(ddomain.domain.toLowerCase(), GGAPICrudOperation.read_all))+"]")));
			}
			if (ddomain.allow_delete_all) {
				this.openApi.get().path(baseUrl, pathItemBase.delete(templateOpenApi.getPaths().get(baseUrl).getDelete().description("Access : ["+ddomain.delete_all_access+"] - Authority ["+(ddomain.delete_all_authority==false?"none":BasicGGAPIAccessRule.getAuthority(ddomain.domain.toLowerCase(), GGAPICrudOperation.delete_all))+"]")));
			}
			if (ddomain.allow_creation) {
				this.openApi.get().path(baseUrl, pathItemBase.post(templateOpenApi.getPaths().get(baseUrl).getPost().description("Access : ["+ddomain.creation_access+"] - Authority ["+(ddomain.creation_authority==false?"none":BasicGGAPIAccessRule.getAuthority(ddomain.domain.toLowerCase(), GGAPICrudOperation.create_one))+"]")));
			}
			if (ddomain.allow_count) {
				this.openApi.get().path(baseUrl + "/count", pathItemCount.get(templateOpenApi.getPaths().get(baseUrl + "/count").getGet().description("Access : ["+ddomain.count_access+"] - Authority ["+(ddomain.count_authority==false?"none":BasicGGAPIAccessRule.getAuthority(ddomain.domain.toLowerCase(), GGAPICrudOperation.count))+"]")));
			}
			if (ddomain.allow_read_one) {
				this.openApi.get().path(baseUrl + "/{uuid}", pathItemUuid.get(templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getGet().description("Access : ["+ddomain.read_one_access+"] - Authority ["+(ddomain.read_one_authority==false?"none":BasicGGAPIAccessRule.getAuthority(ddomain.domain.toLowerCase(), GGAPICrudOperation.read_one))+"]")));
			}
			if (ddomain.allow_update_one) {
				this.openApi.get().path(baseUrl + "/{uuid}", pathItemUuid.patch(templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getPatch().description("Access : ["+ddomain.update_one_access+"] - Authority ["+(ddomain.update_one_authority==false?"none":BasicGGAPIAccessRule.getAuthority(ddomain.domain.toLowerCase(), GGAPICrudOperation.update_one))+"]")));
			}
			if (ddomain.allow_delete_one) {
				this.openApi.get().path(baseUrl + "/{uuid}", pathItemUuid.delete(templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getDelete().description("Access : ["+ddomain.delete_one_access+"] - Authority ["+(ddomain.delete_one_authority==false?"none":BasicGGAPIAccessRule.getAuthority(ddomain.domain.toLowerCase(), GGAPICrudOperation.delete_one))+"]")));
			}
		
			for( IGGAPICustomService cservice: customServices ) {
				PathItem pathItem = new PathItem();
				Operation operation = new Operation();
				pathItem.post(operation).getPost().addTagsItem("Domain "+ddomain.domain).description(cservice.getDescription());

				this.openApi.get().path(cservice.getPath(), pathItem);
			}
	
			Info infos = this.openApi.get().getInfo();
			String description = infos.getDescription() + "       The configured Magic Tenant ID is : 0";
			infos.description(description);
		}
	}

	private void createRequestMappings(GGAPIDomain ddomain,
			IGGAPIService<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> service, String baseUrl, List<IGGAPICustomService> customServices)
			throws NoSuchMethodException {
		RequestMappingInfo.BuilderConfiguration options = new RequestMappingInfo.BuilderConfiguration();
		options.setPatternParser(new PathPatternParser());

		RequestMappingInfo requestMappingInfoGetAll = RequestMappingInfo.paths(baseUrl).methods(RequestMethod.GET)
				.options(options).build();
		RequestMappingInfo requestMappingInfoDeleteAll = RequestMappingInfo.paths(baseUrl)
				.methods(RequestMethod.DELETE).options(options).build();
		RequestMappingInfo requestMappingInfoCreate = RequestMappingInfo.paths(baseUrl).methods(RequestMethod.POST)
				.options(options).build();
		RequestMappingInfo requestMappingInfoCount = RequestMappingInfo.paths(baseUrl + "/count")
				.methods(RequestMethod.GET).options(options).build();
		RequestMappingInfo requestMappingInfoGetOne = RequestMappingInfo.paths(baseUrl + "/{uuid}")
				.methods(RequestMethod.GET).options(options).build();
		RequestMappingInfo requestMappingInfoUpdate = RequestMappingInfo.paths(baseUrl + "/{uuid}")
				.methods(RequestMethod.PATCH).options(options).build();
		RequestMappingInfo requestMappingInfoDeleteOne = RequestMappingInfo.paths(baseUrl + "/{uuid}")
				.methods(RequestMethod.DELETE).options(options).build();

		if (ddomain.allow_read_all) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoGetAll, service,
					service.getClass().getMethod("getEntities",IGGAPICaller.class, GGAPIReadOutputMode.class, Integer.class,
							Integer.class, String.class, String.class, String.class, String.class));
		}
		if (ddomain.allow_delete_all) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoDeleteAll, service,
					service.getClass().getMethod("deleteAll", IGGAPICaller.class,  String.class, String.class, String.class));
		}
		if (ddomain.allow_creation) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoCreate, service,
					service.getClass().getMethod("createEntity", IGGAPICaller.class, String.class, String.class));
		}
		if (ddomain.allow_count) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoCount, service,
					service.getClass().getMethod("getCount", IGGAPICaller.class, String.class, String.class, String.class));
		}
		if (ddomain.allow_read_one) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoGetOne, service,
					service.getClass().getMethod("getEntity", IGGAPICaller.class, String.class, String.class));
		}
		if (ddomain.allow_update_one) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoUpdate, service,
					service.getClass().getMethod("updateEntity", IGGAPICaller.class, String.class, String.class, String.class));
		}
		if (ddomain.allow_delete_one) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoDeleteOne, service,
					service.getClass().getMethod("deleteEntity", IGGAPICaller.class, String.class, String.class));
		}

		for (IGGAPICustomService cservice : customServices) {
			HttpMethod method = GGAPIServiceMethodToHttpMethodBinder.fromServiceMethod(cservice.getAccessRule().getMethod());
			this.requestMappingHandlerMapping.registerMapping(	
				RequestMappingInfo.paths(cservice.getAccessRule().getEndpoint()).methods(RequestMethod.resolve(method)).options(options).build(), 
				service, 
				service.getClass().getMethod(cservice.getMethodName(), cservice.getParameters())
			);
		}
	}

}
