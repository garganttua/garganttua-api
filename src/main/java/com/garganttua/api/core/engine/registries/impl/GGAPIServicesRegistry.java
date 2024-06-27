package com.garganttua.api.core.engine.registries.impl;

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

import com.garganttua.api.core.domain.GGAPIDomain;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.engine.GGAPIObjectsHelper;
import com.garganttua.api.core.security.authorization.BasicGGAPIAccessRule;
import com.garganttua.api.core.service.GGAPICustomServiceBuilder;
import com.garganttua.api.services.rest.GGAPIOpenAPIHelper;
import com.garganttua.api.services.rest.GGAPIRestService;
import com.garganttua.api.services.rest.GGAPIServiceMethodToHttpMethodBinder;
import com.garganttua.api.spec.GGAPICrudOperation;
import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.engine.IGGAPIDomainsRegistry;
import com.garganttua.api.spec.engine.IGGAPIFactoriesRegistry;
import com.garganttua.api.spec.engine.IGGAPIServicesRegistry;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;
import com.garganttua.api.spec.event.IGGAPIEventPublisher;
import com.garganttua.api.spec.service.GGAPIReadOutputMode;
import com.garganttua.api.spec.service.IGGAPICustomService;
import com.garganttua.api.spec.service.IGGAPIService;

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

	private Map<String, IGGAPIService> services = new HashMap<String, IGGAPIService>();

	@Autowired
	private IGGAPIDomainsRegistry domains;
	
	@Autowired
	private IGGAPIFactoriesRegistry factories;

	@Override
	public IGGAPIService getService( String name) {
		return this.services.get(name);
	}

	@Override
	public List<IGGAPIService> getServices() {
		return new ArrayList<IGGAPIService>(this.services.values());
	}

	@PostConstruct
	private void init() throws GGAPIEngineException {

		log.info("Creating Rest Services ...");
		for (GGAPIDomain domain : this.domains.getDomains()) {

			String ws__ = domain.ws;

			IGGAPIService service;
			if (ws__ != null && !ws__.isEmpty()) {
				service = helper.getObjectFromConfiguration(ws__, IGGAPIService.class);
			} else {
				service = new GGAPIRestService();
			}
			
			List<IGGAPICustomService> customServices = GGAPICustomServiceBuilder.buildGGAPIServices(service.getClass());
			
			service.setDomain(domain);
			service.setEventPublisher(this.getEventPublisher(domain));
			service.setFactory(this.factories.getFactory(domain.entity.getValue1().domain()));
			
			String baseUrl = "/" + domain.entity.getValue1().domain().toLowerCase();
			
			try {
				this.createRequestMappings(domain, service, baseUrl, customServices);
				this.setOpenApiDocumentation(service, domain, baseUrl, customServices);
			} catch (NoSuchMethodException | IOException e) {
				throw new GGAPIEngineException(e);
			}

			this.services.put(domain.entity.getValue1().domain(), service);
			
			log.info("	Service added [domain {}, service {}]", domain.entity.getValue1().domain(), service);
		}
	}

	private Optional<IGGAPIEventPublisher> getEventPublisher(GGAPIDomain domain) throws GGAPIEngineException {
		if( domain.event != null && !domain.event.isEmpty() ) {
			return Optional.ofNullable(this.helper.getObjectFromConfiguration(domain.event, IGGAPIEventPublisher.class));
		}
		return Optional.ofNullable(null);
	}

	private void setOpenApiDocumentation(IGGAPIService service, GGAPIDomain domain, String baseUrl, List<IGGAPICustomService> customServices) throws IOException {
		if( this.openApi.isPresent() ) {
		
			Class<?> entityClass = domain.entity.getValue0();
		
			Tag tag = new Tag().name("Domain " + domain.entity.getValue1().domain().toLowerCase()).description("Public Entity ["+domain.entity.getValue1().publicEntity()+"] Shared Entity ["+(domain.entity.getValue1().sharedEntity()?"false":domain.entity.getValue1().shareFieldAddress())+"] Hiddenable Entity ["+domain.entity.getValue1().hiddenableEntity()+"] Geolocalized ["+(domain.entity.getValue1().geolocalizedEntity()?"false":domain.entity.getValue1().locationFieldAddress())+"]");
			this.openApi.get().addTagsItem(tag);

			GGAPIEntity entityAnnotation = entityClass.getAnnotation(GGAPIEntity.class);

			OpenAPI templateOpenApi = this.openApiHelper.getOpenApi(domain.entity.getValue1().domain().toLowerCase(), entityClass.getSimpleName(), entityAnnotation.openApiSchemas());
			PathItem pathItemBase = new PathItem();
			PathItem pathItemCount = new PathItem();
			PathItem pathItemUuid = new PathItem();

			this.openApi.get().getComponents().addSchemas(entityClass.getSimpleName(), templateOpenApi.getComponents().getSchemas().get(entityClass.getSimpleName()));
			this.openApi.get().getComponents().addSchemas("ErrorObject", templateOpenApi.getComponents().getSchemas().get("ErrorObject"));
			this.openApi.get().getComponents().addSchemas("SortQuery", templateOpenApi.getComponents().getSchemas().get("SortQuery"));
			this.openApi.get().getComponents().addSchemas("FilterQuery", templateOpenApi.getComponents().getSchemas().get("FilterQuery"));
		
			if (domain.allow_read_all) {
				this.openApi.get().path(baseUrl, pathItemBase.get(templateOpenApi.getPaths().get(baseUrl).getGet().description("Access : ["+domain.read_all_access+"] - Authority ["+(domain.read_all_authority==false?"none":BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(), GGAPICrudOperation.read_all))+"]")));
			}
			if (domain.allow_delete_all) {
				this.openApi.get().path(baseUrl, pathItemBase.delete(templateOpenApi.getPaths().get(baseUrl).getDelete().description("Access : ["+domain.delete_all_access+"] - Authority ["+(domain.delete_all_authority==false?"none":BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(), GGAPICrudOperation.delete_all))+"]")));
			}
			if (domain.allow_creation) {
				this.openApi.get().path(baseUrl, pathItemBase.post(templateOpenApi.getPaths().get(baseUrl).getPost().description("Access : ["+domain.creation_access+"] - Authority ["+(domain.creation_authority==false?"none":BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(), GGAPICrudOperation.create_one))+"]")));
			}
			if (domain.allow_count) {
				this.openApi.get().path(baseUrl + "/count", pathItemCount.get(templateOpenApi.getPaths().get(baseUrl + "/count").getGet().description("Access : ["+domain.count_access+"] - Authority ["+(domain.count_authority==false?"none":BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(), GGAPICrudOperation.count))+"]")));
			}
			if (domain.allow_read_one) {
				this.openApi.get().path(baseUrl + "/{uuid}", pathItemUuid.get(templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getGet().description("Access : ["+domain.read_one_access+"] - Authority ["+(domain.read_one_authority==false?"none":BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(), GGAPICrudOperation.read_one))+"]")));
			}
			if (domain.allow_update_one) {
				this.openApi.get().path(baseUrl + "/{uuid}", pathItemUuid.patch(templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getPatch().description("Access : ["+domain.update_one_access+"] - Authority ["+(domain.update_one_authority==false?"none":BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(), GGAPICrudOperation.update_one))+"]")));
			}
			if (domain.allow_delete_one) {
				this.openApi.get().path(baseUrl + "/{uuid}", pathItemUuid.delete(templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getDelete().description("Access : ["+domain.delete_one_access+"] - Authority ["+(domain.delete_one_authority==false?"none":BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(), GGAPICrudOperation.delete_one))+"]")));
			}
		
			for( IGGAPICustomService cservice: customServices ) {
				PathItem pathItem = new PathItem();
				Operation operation = new Operation();
				pathItem.post(operation).getPost().addTagsItem("Domain "+domain.entity.getValue1().domain()).description(cservice.getDescription());

				this.openApi.get().path(cservice.getPath(), pathItem);
			}
	
			Info infos = this.openApi.get().getInfo();
			String description = infos.getDescription() + "       The configured Magic Tenant ID is : 0";
			infos.description(description);
		}
	}

	private void createRequestMappings(GGAPIDomain ddomain,
			IGGAPIService service, String baseUrl, List<IGGAPICustomService> customServices)
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
							Integer.class, String.class, String.class, String.class));
		}
		if (ddomain.allow_delete_all) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoDeleteAll, service,
					service.getClass().getMethod("deleteAll", IGGAPICaller.class,  String.class, String.class));
		}
		if (ddomain.allow_creation) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoCreate, service,
					service.getClass().getMethod("createEntity", IGGAPICaller.class, String.class, String.class));
		}
		if (ddomain.allow_count) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoCount, service,
					service.getClass().getMethod("getCount", IGGAPICaller.class, String.class, String.class));
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
