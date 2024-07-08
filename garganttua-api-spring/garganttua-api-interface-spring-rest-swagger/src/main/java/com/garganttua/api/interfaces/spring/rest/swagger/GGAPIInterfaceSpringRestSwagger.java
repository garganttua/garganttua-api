package com.garganttua.api.interfaces.spring.rest.swagger;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityMandatory;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GGAPIInterfaceSpringRestSwagger {
	
	@Value(value = "${com.garganttua.api.spring.rest.superOnwerId:0}")
	private String superOwnerId = "0";
	
	@Value(value = "${com.garganttua.api.spring.rest.superTenantId:0}")
	private String superTenantId = "0";
	
	@Autowired
	private IGGAPIEngine engine;
	
	@Autowired
	private OpenAPI openApi;
	
	@Autowired
	private GGAPIOpenAPIHelper openApiHelper;
	
	@PostConstruct
	private void init() {
		
		Info infos = this.openApi.getInfo();
		String description = infos.getDescription() 
				+ "<br>"
				+ "<br>The configured <b>Super Tenant ID</b> is : "+superTenantId
				+ "<br>The configured <b>Super Owner ID</b> is : "+superOwnerId;
		infos.description(description);
		
		this.engine.getDomainsRegistry().getDomains().stream().forEach(domain -> {
			IGGAPIService service = this.engine.getServicesRegistry().getService(domain.getDomain());

			try {
				this.setOpenApiDocumentation(service, domain, "/"+domain.getDomain(), List.of());
			} catch (Exception e) {
				log.warn("Error during openapi initialisation :", e);
			}
		});
	}
	
	private void setOpenApiDocumentation(IGGAPIService service, IGGAPIDomain domain, String baseUrl, List<IGGAPIServiceInfos> customServices) throws Exception {
		
		Class<?> entityClass = domain.getEntity().getValue0();
	
		Tag tag = new Tag().name("Domain " + domain.getEntity().getValue1().domain().toLowerCase()).description(
				  "<b>Public Entity</b> ["+domain.getEntity().getValue1().publicEntity()+"] <br> "
				+ "<b>Shared Entity</b> ["+(domain.getEntity().getValue1().sharedEntity()?"false":domain.getEntity().getValue1().shareFieldAddress())+"] <br> "
				+ "<b>Hiddenable Entity</b> ["+domain.getEntity().getValue1().hiddenableEntity()+"] <br> "
				+ "<b>Geolocalized</b> ["+(domain.getEntity().getValue1().geolocalizedEntity()?"false":domain.getEntity().getValue1().locationFieldAddress())+"]");
		this.openApi.addTagsItem(tag);

		GGAPIEntity entityAnnotation = entityClass.getAnnotation(GGAPIEntity.class);

		String entityClassSchema = this.test(entityClass);
		
		OpenAPI templateOpenApi = this.openApiHelper.getOpenApi(domain.getEntity().getValue1().domain().toLowerCase(), entityClass.getSimpleName(), entityClassSchema);
		PathItem pathItemBase = new PathItem();
		PathItem pathItemCount = new PathItem();
		PathItem pathItemUuid = new PathItem();

		this.openApi.getComponents().addSchemas(entityClass.getSimpleName(), templateOpenApi.getComponents().getSchemas().get(entityClass.getSimpleName()));
		this.openApi.getComponents().addSchemas("ResponseObject", templateOpenApi.getComponents().getSchemas().get("ResponseObject"));
		this.openApi.getComponents().addSchemas("SortQuery", templateOpenApi.getComponents().getSchemas().get("SortQuery"));
		this.openApi.getComponents().addSchemas("FilterQuery", templateOpenApi.getComponents().getSchemas().get("FilterQuery"));
	
//		if (domain.isAllowReadAll()) {
//			this.openApi.path(baseUrl, pathItemBase.get(templateOpenApi.getPaths().get(baseUrl).getGet().description("Access : ["+domain.read_all_access+"] - Authority ["+(domain.read_all_authority==false?"none":BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(), GGAPICrudOperation.read_all))+"]")));
//		}
//		if (domain.isAllowDeleteAll()) {
//			this.openApi.path(baseUrl, pathItemBase.delete(templateOpenApi.getPaths().get(baseUrl).getDelete().description("Access : ["+domain.delete_all_access+"] - Authority ["+(domain.delete_all_authority==false?"none":BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(), GGAPICrudOperation.delete_all))+"]")));
//		}
//		if (domain.isAllowCreation()) {
//			this.openApi.path(baseUrl, pathItemBase.post(templateOpenApi.getPaths().get(baseUrl).getPost().description("Access : ["+domain.creation_access+"] - Authority ["+(domain.creation_authority==false?"none":BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(), GGAPICrudOperation.create_one))+"]")));
//		}
//		if (domain.isAllowCount()) {
//			this.openApi.path(baseUrl + "/count", pathItemCount.get(templateOpenApi.getPaths().get(baseUrl + "/count").getGet().description("Access : ["+domain.count_access+"] - Authority ["+(domain.count_authority==false?"none":BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(), GGAPICrudOperation.count))+"]")));
//		}
//		if (domain.isAllowReadOne()) {
//			this.openApi.path(baseUrl + "/{uuid}", pathItemUuid.get(templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getGet().description("Access : ["+domain.read_one_access+"] - Authority ["+(domain.read_one_authority==false?"none":BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(), GGAPICrudOperation.read_one))+"]")));
//		}
//		if (domain.isAllowUpdateOne()) {
//			this.openApi.path(baseUrl + "/{uuid}", pathItemUuid.patch(templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getPatch().description("Access : ["+domain.update_one_access+"] - Authority ["+(domain.update_one_authority==false?"none":BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(), GGAPICrudOperation.update_one))+"]")));
//		}
//		if (domain.isAllowDeleteOne()) {
//			this.openApi.path(baseUrl + "/{uuid}", pathItemUuid.delete(templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getDelete().description("Access : ["+domain.delete_one_access+"] - Authority ["+(domain.delete_one_authority==false?"none":BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(), GGAPICrudOperation.delete_one))+"]")));
//		}
		
		if (domain.isAllowReadAll()) {
			this.openApi.path(baseUrl, pathItemBase.get(templateOpenApi.getPaths().get(baseUrl).getGet()));
		}
		if (domain.isAllowDeleteAll()) {
			this.openApi.path(baseUrl, pathItemBase.delete(templateOpenApi.getPaths().get(baseUrl).getDelete()));
		}
		if (domain.isAllowCreation()) {
			this.openApi.path(baseUrl, pathItemBase.post(templateOpenApi.getPaths().get(baseUrl).getPost()));
		}
		if (domain.isAllowCount()) {
			this.openApi.path(baseUrl + "/count", pathItemCount.get(templateOpenApi.getPaths().get(baseUrl + "/count").getGet()));
		}
		if (domain.isAllowReadOne()) {
			this.openApi.path(baseUrl + "/{uuid}", pathItemUuid.get(templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getGet()));
		}
		if (domain.isAllowUpdateOne()) {
			this.openApi.path(baseUrl + "/{uuid}", pathItemUuid.patch(templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getPatch()));
		}
		if (domain.isAllowDeleteOne()) {
			this.openApi.path(baseUrl + "/{uuid}", pathItemUuid.delete(templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getDelete()));
		}
	
//			for( IGGAPICustomService cservice: customServices ) {
//				PathItem pathItem = new PathItem();
//				Operation operation = new Operation();
//				pathItem.post(operation).getPost().addTagsItem("Domain "+domain.entity.getValue1().domain()).description(cservice.getDescription());
//
//				this.openApi.get().path(cservice.getPath(), pathItem);
//			}
	}
	
	private String test(Class<?> clazz) {
		JacksonModule module = new JacksonModule(JacksonOption.RESPECT_JSONPROPERTY_REQUIRED);
		SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON);
		configBuilder
			.forMethods()
			.withRequiredCheck(method -> method.getAnnotationConsideringFieldAndGetter(NotNull.class) != null);
		configBuilder.forFields()
//		  	.withRequiredCheck(field -> field.getAnnotationConsideringFieldAndGetter(Nullable.class) == null)
		  	.withRequiredCheck(field -> field.getAnnotationConsideringFieldAndGetter(GGAPIEntityMandatory.class) != null)
		  	.withArrayUniqueItemsResolver(scope -> scope.getType().getErasedType() == (List.class) ? true : null);
		SchemaGeneratorConfig config = configBuilder
			.with(Option.EXTRA_OPEN_API_FORMAT_VALUES)
			.with(module)
			.without(Option.FLATTENED_ENUMS_FROM_TOSTRING)
			.build();

		SchemaGenerator generator = new SchemaGenerator(config);
		JsonNode jsonSchema = generator.generateSchema(clazz);
		return jsonSchema.toString();
	}

}
