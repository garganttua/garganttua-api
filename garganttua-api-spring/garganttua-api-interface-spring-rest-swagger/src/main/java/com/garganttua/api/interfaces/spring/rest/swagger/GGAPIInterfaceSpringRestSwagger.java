package com.garganttua.api.interfaces.spring.rest.swagger;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.garganttua.api.security.core.accessRules.BasicGGAPIAccessRule;
import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityMandatory;
import com.garganttua.api.spec.service.GGAPIServiceAccess;
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
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GGAPIInterfaceSpringRestSwagger {
	
	@Value(value = "${com.garganttua.api.spring.superOwnerId:0}")
	private String superOwnerId = "0";
	
	@Value(value = "${com.garganttua.api.spring.superTenantId:0}")
	private String superTenantId = "0";
	
	@Value(value = "${com.garganttua.api.interface.spring.rest.ownerIdHeaderName:tenantId}")
	private String tenantIdHeaderName = "tenantId";

	@Value(value = "${com.garganttua.api.interface.spring.rest.requestedTenantIdHeaderName:requestedTenantId}")
	private String requestedTenantIdHeaderName = "requestedTenantId";

	@Value(value = "${com.garganttua.api.interface.spring.rest.ownerIdHeaderName:ownerId}")
	private String ownerIdHeaderName = "ownerId";
	
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

		if (domain.isAllowReadAll()) {
			Operation operation = templateOpenApi.getPaths().get(baseUrl).getGet();
			this.openApi.path(baseUrl, pathItemBase.get(operation.description("<b>Access </b>: ["+domain.getSecurity().readAllAccess()+"] <br> <b>Authority</b>: ["+(domain.getSecurity().readAllAuthority()==false?"none":BasicGGAPIAccessRule.getAuthority(domain.getEntity().getValue1().domain().toLowerCase(), GGAPIEntityOperation.read_all))+"]")));
			this.addTenantIdHeader(operation, domain.isTenantIdMandatoryForOperation(GGAPIEntityOperation.read_all) );
			this.addRequestedTenantIdHeader(operation);
			this.addOwnerIdHeader(operation, domain.isOwnerIdMandatoryForOperation(GGAPIEntityOperation.read_all) );
		}
		if (domain.isAllowDeleteAll()) {
			Operation operation = templateOpenApi.getPaths().get(baseUrl).getDelete();
			this.openApi.path(baseUrl, pathItemBase.delete(operation.description("<b>Access</b> : ["+domain.getSecurity().deleteAllAccess()+"] <br> <b>Authority</b>: ["+(domain.getSecurity().deleteAllAuthority()==false?"none":BasicGGAPIAccessRule.getAuthority(domain.getEntity().getValue1().domain().toLowerCase(), GGAPIEntityOperation.delete_all))+"]")));
			this.addTenantIdHeader(operation, domain.isTenantIdMandatoryForOperation(GGAPIEntityOperation.delete_all) );
			this.addRequestedTenantIdHeader(operation);
			this.addOwnerIdHeader(operation, domain.isOwnerIdMandatoryForOperation(GGAPIEntityOperation.delete_all) );
		}
		if (domain.isAllowCreation()) {
			Operation operation = templateOpenApi.getPaths().get(baseUrl).getPost();
			this.openApi.path(baseUrl, pathItemBase.post(operation.description("<b>Access</b> : ["+domain.getSecurity().creationAccess()+"] <br> <b>Authority</b>: ["+(domain.getSecurity().creationAuthority()==false?"none":BasicGGAPIAccessRule.getAuthority(domain.getEntity().getValue1().domain().toLowerCase(), GGAPIEntityOperation.create_one))+"]")));
			if( !domain.getEntity().getValue1().tenantEntity() ) {
				this.addTenantIdHeader(operation, domain.isTenantIdMandatoryForOperation(GGAPIEntityOperation.create_one) );
				this.addRequestedTenantIdHeader(operation);
				this.addOwnerIdHeader(operation, domain.isOwnerIdMandatoryForOperation(GGAPIEntityOperation.create_one) );
			}
		}
//		if (domain.isAllowCount()) {
//			Operation operation = templateOpenApi.getPaths().get(baseUrl + "/count").getGet();
//			this.openApi.path(baseUrl + "/count", pathItemCount.get(operation.description("<b>Access</b> : ["+domain.getSecurity().countAccess()+"] <br> <b>Authority</b>: ["+(domain.getSecurity().countAuthority()==false?"none":BasicGGAPIAccessRule.getAuthority(domain.getEntity().getValue1().domain().toLowerCase(), GGAPIEntityOperation.count))+"]")));
//			this.addTenantIdHeader(operation, domain.isTenantIdMandatoryForOperation(GGAPIEntityOperation.count) );
//			this.addRequestedTenantIdHeader(operation);
//			this.addOwnerIdHeader(operation, domain.isOwnerIdMandatoryForOperation(GGAPIEntityOperation.count) );
//		}
		if (domain.isAllowReadOne()) {
			Operation operation = templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getGet();
			this.openApi.path(baseUrl + "/{uuid}", pathItemUuid.get(operation.description("<b>Access</b> : ["+domain.getSecurity().readOneAccess()+"] <br> <b>Authority</b>: ["+(domain.getSecurity().readOneAuthority()==false?"none":BasicGGAPIAccessRule.getAuthority(domain.getEntity().getValue1().domain().toLowerCase(), GGAPIEntityOperation.read_one))+"]")));
			this.addTenantIdHeader(operation, domain.isTenantIdMandatoryForOperation(GGAPIEntityOperation.read_one) );
			this.addRequestedTenantIdHeader(operation);
			this.addOwnerIdHeader(operation, domain.isOwnerIdMandatoryForOperation(GGAPIEntityOperation.read_one) );
		}
		if (domain.isAllowUpdateOne()) {
			Operation operation = templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getPatch();
			this.openApi.path(baseUrl + "/{uuid}", pathItemUuid.patch(operation.description("<b>Access</b> : ["+domain.getSecurity().updateOneAccess()+"] <br> <b>Authority</b>: ["+(domain.getSecurity().updateOneAuthority()==false?"none":BasicGGAPIAccessRule.getAuthority(domain.getEntity().getValue1().domain().toLowerCase(), GGAPIEntityOperation.update_one))+"]")));
			this.addTenantIdHeader(operation, domain.isTenantIdMandatoryForOperation(GGAPIEntityOperation.update_one) );
			this.addRequestedTenantIdHeader(operation);
			this.addOwnerIdHeader(operation, domain.isOwnerIdMandatoryForOperation(GGAPIEntityOperation.update_one) );
		}
		if (domain.isAllowDeleteOne()) {
			Operation operation = templateOpenApi.getPaths().get(baseUrl + "/{uuid}").getDelete();
			this.openApi.path(baseUrl + "/{uuid}", pathItemUuid.delete(operation.description("<b>Access</b> : ["+domain.getSecurity().deleteOneAccess()+"] <br> <b>Authority</b>: ["+(domain.getSecurity().deleteOneAuthority()==false?"none":BasicGGAPIAccessRule.getAuthority(domain.getEntity().getValue1().domain().toLowerCase(), GGAPIEntityOperation.delete_one))+"]")));
			this.addTenantIdHeader(operation, domain.isTenantIdMandatoryForOperation(GGAPIEntityOperation.delete_one) );
			this.addRequestedTenantIdHeader(operation);
			this.addOwnerIdHeader(operation, domain.isOwnerIdMandatoryForOperation(GGAPIEntityOperation.delete_one) );
		}

//			for( IGGAPICustomService cservice: customServices ) {
//				PathItem pathItem = new PathItem();
//				Operation operation = new Operation();
//				pathItem.post(operation).getPost().addTagsItem("Domain "+domain.entity.getValue1().domain()).description(cservice.getDescription());
//
//				this.openApi.get().path(cservice.getPath(), pathItem);
//			}
	}

	private void addOwnerIdHeader(Operation operation, boolean tenantIdMandatoryForOperation) {
		if( tenantIdMandatoryForOperation ) {
			List<Parameter> params = operation.getParameters();
			HeaderParameter param = new HeaderParameter();
			param.setName(this.ownerIdHeaderName);
			param.setRequired(tenantIdMandatoryForOperation);
			param.setSchema(new StringSchema());
			params.add(param);
			operation.setParameters(params);
		}
	}

	private void addRequestedTenantIdHeader(Operation operation) {
		List<Parameter> params = operation.getParameters();
		HeaderParameter param = new HeaderParameter();
		param.setName(this.requestedTenantIdHeaderName);
		param.setRequired(false);
		param.setSchema(new StringSchema());
		params.add(param);
		operation.setParameters(params);
	}

	private void addTenantIdHeader(Operation operation, boolean mandatory) {
		List<Parameter> params = operation.getParameters();
		HeaderParameter param = new HeaderParameter();
		param.setName(this.tenantIdHeaderName);
		param.setRequired(mandatory);
		param.setSchema(new StringSchema());
		params.add(param);
		operation.setParameters(params);
	}
	
	private String test(Class<?> clazz) {
		JacksonModule module = new JacksonModule(JacksonOption.RESPECT_JSONPROPERTY_REQUIRED);
		SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON);
		configBuilder
			.forMethods()
			.withRequiredCheck(method -> method.getAnnotationConsideringFieldAndGetter(NotNull.class) != null);
		configBuilder.forFields()
		  	.withRequiredCheck(field -> field.getAnnotationConsideringFieldAndGetter(Nullable.class) == null)
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
