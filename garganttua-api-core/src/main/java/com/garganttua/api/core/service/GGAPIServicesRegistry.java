package com.garganttua.api.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.spec.engine.IGGAPIServicesRegistry;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;

public class GGAPIServicesRegistry implements IGGAPIServicesRegistry {

	private Map<String, Pair<IGGAPIService, List<IGGAPIServiceInfos>>> services = new HashMap<String, Pair<IGGAPIService, List<IGGAPIServiceInfos>>>();

	public GGAPIServicesRegistry(Map<String, Pair<IGGAPIService, List<IGGAPIServiceInfos>>> services) {
		this.services = services;
	}

	@Override
	public IGGAPIService getService(String domain) {
		return this.services.get(domain).getValue0();
	}
	
	@Override
	public List<IGGAPIServiceInfos> getServiceInfos(String domain){
		return this.services.get(domain).getValue1();
	}

	@Override
	public List<IGGAPIService> getServices() {
		ArrayList<IGGAPIService> servicesList = new ArrayList<IGGAPIService>();
		this.services.values().parallelStream().forEach(pair -> {
			servicesList.add(pair.getValue0());
		});

		return servicesList;
	}
	
//	private Optional<IGGAPIEventPublisher> getEventPublisher(GGAPIDomain domain) throws GGAPIEngineException {
//		if( domain.event != null && !domain.event.isEmpty() ) {
//			return Optional.ofNullable(this.helper.getObjectFromConfiguration(domain.event, IGGAPIEventPublisher.class));
//		}
//		return Optional.ofNullable(null);
//	}

//	private void setOpenApiDocumentation(IGGAPIService service, GGAPIDomain domain, String baseUrl, List<IGGAPICustomService> customServices) throws IOException {
//		if( this.openApi.isPresent() ) {
//		
//			Class<?> entityClass = domain.entity.getValue0();
//		
//			Tag tag = new Tag().name("Domain " + domain.entity.getValue1().domain().toLowerCase()).description("Public Entity ["+domain.entity.getValue1().publicEntity()+"] Shared Entity ["+(domain.entity.getValue1().sharedEntity()?"false":domain.entity.getValue1().shareFieldAddress())+"] Hiddenable Entity ["+domain.entity.getValue1().hiddenableEntity()+"] Geolocalized ["+(domain.entity.getValue1().geolocalizedEntity()?"false":domain.entity.getValue1().locationFieldAddress())+"]");
//			this.openApi.get().addTagsItem(tag);
//
//			GGAPIEntity entityAnnotation = entityClass.getAnnotation(GGAPIEntity.class);
//
//			OpenAPI templateOpenApi = this.openApiHelper.getOpenApi(domain.entity.getValue1().domain().toLowerCase(), entityClass.getSimpleName(), entityAnnotation.openApiSchemas());
//			PathItem pathItemBase = new PathItem();
//			PathItem pathItemCount = new PathItem();
//			PathItem pathItemUuid = new PathItem();
//
//			this.openApi.get().getComponents().addSchemas(entityClass.getSimpleName(), templateOpenApi.getComponents().getSchemas().get(entityClass.getSimpleName()));
//			this.openApi.get().getComponents().addSchemas("ErrorObject", templateOpenApi.getComponents().getSchemas().get("ErrorObject"));
//			this.openApi.get().getComponents().addSchemas("SortQuery", templateOpenApi.getComponents().getSchemas().get("SortQuery"));
//			this.openApi.get().getComponents().addSchemas("FilterQuery", templateOpenApi.getComponents().getSchemas().get("FilterQuery"));
//		
//			if (domain.allow_read_all) {
//				this.openApi.get().path(baseUrl, pathItemBase.get(templateOpenApi.getPaths().get(baseUrl).getGet().description("Access : ["+domain.read_all_access+"] - Authority ["+(domain.read_all_authority==false?"none":BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(), GGAPICrudOperation.read_all))+"]")));
//			}
//			if (domain.allow_delete_all) {
//				this.openApi.get().path(baseUrl, pathItemBase.delete(templateOpenApi.getPaths().get(baseUrl).getDelete().description("Access : ["+domain.delete_all_access+"] - Authority ["+(domain.delete_all_authority==false?"none":BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(), GGAPICrudOperation.delete_all))+"]")));
//			}
//			if (domain.allow_creation) {
//				this.openApi.get().path(baseUrl, pathItemBase.post(templateOpenApi.getPaths().get(baseUrl).getPost().description("Access : ["+domain.creation_access+"] - Authority ["+(domain.creation_authority==false?"none":BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(), GGAPICrudOperation.create_one))+"]")));
//			}
//			if (domain.allow_count) {
//				this.openApi.get().path(baseUrl + "/count", pathItemCount.get(templateOpenApi.getPaths().get(baseUrl + "/count").getGet().description("Access : ["+domain.count_access+"] - Authority ["+(domain.count_authority==false?"none":BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(), GGAPICrudOperation.count))+"]")));
//			}
//			if (domain.allow_read_one) {
//				this.openApi.get().path(baseUrl + "/{uuid}", pathItemUuid.get(templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getGet().description("Access : ["+domain.read_one_access+"] - Authority ["+(domain.read_one_authority==false?"none":BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(), GGAPICrudOperation.read_one))+"]")));
//			}
//			if (domain.allow_update_one) {
//				this.openApi.get().path(baseUrl + "/{uuid}", pathItemUuid.patch(templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getPatch().description("Access : ["+domain.update_one_access+"] - Authority ["+(domain.update_one_authority==false?"none":BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(), GGAPICrudOperation.update_one))+"]")));
//			}
//			if (domain.allow_delete_one) {
//				this.openApi.get().path(baseUrl + "/{uuid}", pathItemUuid.delete(templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getDelete().description("Access : ["+domain.delete_one_access+"] - Authority ["+(domain.delete_one_authority==false?"none":BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(), GGAPICrudOperation.delete_one))+"]")));
//			}
//		
//			for( IGGAPICustomService cservice: customServices ) {
//				PathItem pathItem = new PathItem();
//				Operation operation = new Operation();
//				pathItem.post(operation).getPost().addTagsItem("Domain "+domain.entity.getValue1().domain()).description(cservice.getDescription());
//
//				this.openApi.get().path(cservice.getPath(), pathItem);
//			}
//	
//			Info infos = this.openApi.get().getInfo();
//			String description = infos.getDescription() + "       The configured Magic Tenant ID is : 0";
//			infos.description(description);
//		}
//	}
//


}
