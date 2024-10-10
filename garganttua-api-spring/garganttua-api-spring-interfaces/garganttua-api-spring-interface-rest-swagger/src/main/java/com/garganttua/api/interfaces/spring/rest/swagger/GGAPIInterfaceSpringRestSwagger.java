package com.garganttua.api.interfaces.spring.rest.swagger;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.garganttua.api.core.accessRules.BasicGGAPIAccessRule;
import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.entity.GGAPIEntityDocumentationInfos;
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
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GGAPIInterfaceSpringRestSwagger {

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
		String description = infos.getDescription() + "<br>";
		infos.description(description);

		this.engine.getDomainsRegistry().getDomains().stream().forEach(domain -> {
			IGGAPIService service = this.engine.getServicesRegistry().getService(domain.getDomain());
			try {
				this.setOpenApiDocumentation(service, domain, "/api/" + domain.getDomain(), List.of());
			} catch (Exception e) {
				log.warn("Error during openapi initialisation :", e);
			}
		});
	}

	private void setOpenApiDocumentation(IGGAPIService service, IGGAPIDomain domain, String baseUrl,
			List<IGGAPIServiceInfos> customServices) throws Exception {

		String domainName = domain.getDomain().toLowerCase();
		Class<?> entityClass = domain.getEntity().getValue0();
		GGAPIEntityDocumentationInfos documentation = domain.getDocumentation();

		Tag tag = new Tag().name("Domain " + domain.getEntity().getValue1().domain().toLowerCase())
				.description(this.getDocumentation(domain));
		this.openApi.addTagsItem(tag);

		String entityClassSchema = this.test(entityClass);

		OpenAPI templateOpenApi = this.openApiHelper.getOpenApi(domain.getEntity().getValue1().domain().toLowerCase(),
				entityClass.getSimpleName(), entityClassSchema);
		PathItem pathItemBase = new PathItem();
		PathItem pathItemUuid = new PathItem();

		this.openApi.getComponents().addSchemas(entityClass.getSimpleName(),
				templateOpenApi.getComponents().getSchemas().get(entityClass.getSimpleName()));
		this.openApi.getComponents().addSchemas("ResponseObject",
				templateOpenApi.getComponents().getSchemas().get("ResponseObject"));
		this.openApi.getComponents().addSchemas("SortQuery",
				templateOpenApi.getComponents().getSchemas().get("SortQuery"));
		this.openApi.getComponents().addSchemas("FilterQuery",
				templateOpenApi.getComponents().getSchemas().get("FilterQuery"));

		if (domain.isAllowReadAll()) {
			GGAPIEntityOperation entityOperation = GGAPIEntityOperation.read_all;
			boolean hasAuthority = domain.getSecurity().readAllAuthority();
			GGAPIServiceAccess access = domain.getSecurity().readAllAccess();
			Operation operation = templateOpenApi.getPaths().get(baseUrl).getGet();

			this.openApi.path(baseUrl, pathItemBase.get(operation.description(this.getOperationDescription(domainName,
					access, hasAuthority, entityOperation, documentation == null ? null : documentation.readAll()))));
			this.setAdditionalInfos(domain, entityOperation, operation);
			this.addCustomParamsPathParameter(operation);
		}
		if (domain.isAllowDeleteAll()) {
			GGAPIEntityOperation entityOperation = GGAPIEntityOperation.delete_all;
			boolean hasAuthority = domain.getSecurity().deleteAllAuthority();
			GGAPIServiceAccess access = domain.getSecurity().readAllAccess();
			Operation operation = templateOpenApi.getPaths().get(baseUrl).getDelete();

			this.openApi.path(baseUrl,
					pathItemBase.delete(operation.description(this.getOperationDescription(domainName, access,
							hasAuthority, entityOperation, documentation == null ? null : documentation.deleteAll()))));
			this.setAdditionalInfos(domain, entityOperation, operation);
			this.addCustomParamsPathParameter(operation);
		}
		if (domain.isAllowCreation()) {
			GGAPIEntityOperation entityOperation = GGAPIEntityOperation.create_one;
			boolean hasAuthority = domain.getSecurity().creationAuthority();
			GGAPIServiceAccess access = domain.getSecurity().creationAccess();
			Operation operation = templateOpenApi.getPaths().get(baseUrl).getPost();

			this.openApi.path(baseUrl, pathItemBase.post(operation.description(this.getOperationDescription(domainName,
					access, hasAuthority, entityOperation, documentation == null ? null : documentation.createOne()))));

			if (!domain.getEntity().getValue1().tenantEntity()) {
				this.setAdditionalInfos(domain, entityOperation, operation);
			}

			this.addCustomParamsPathParameter(operation);
		}
		if (domain.isAllowReadOne()) {
			String endpoint = baseUrl + "/{uuid}";
			GGAPIEntityOperation entityOperation = GGAPIEntityOperation.read_one;
			boolean hasAuthority = domain.getSecurity().readOneAuthority();
			GGAPIServiceAccess access = domain.getSecurity().readOneAccess();
			Operation operation = templateOpenApi.getPaths().get(endpoint).getGet();

			this.openApi.path(endpoint, pathItemUuid.get(operation.description(this.getOperationDescription(domainName,
					access, hasAuthority, entityOperation, documentation == null ? null : documentation.readOne()))));
			this.setAdditionalInfos(domain, entityOperation, operation);
			this.addCustomParamsPathParameter(operation);
		}
		if (domain.isAllowUpdateOne()) {
			String endpoint = baseUrl + "/{uuid}";
			GGAPIEntityOperation entityOperation = GGAPIEntityOperation.update_one;
			boolean hasAuthority = domain.getSecurity().updateOneAuthority();
			GGAPIServiceAccess access = domain.getSecurity().updateOneAccess();
			Operation operation = templateOpenApi.getPaths().get(endpoint).getPatch();

			this.openApi.path(endpoint,
					pathItemUuid.patch(operation.description(this.getOperationDescription(domainName, access,
							hasAuthority, entityOperation, documentation == null ? null : documentation.updateOne()))));
			this.setAdditionalInfos(domain, entityOperation, operation);
			this.addCustomParamsPathParameter(operation);
		}
		if (domain.isAllowDeleteOne()) {
			String endpoint = baseUrl + "/{uuid}";
			GGAPIEntityOperation entityOperation = GGAPIEntityOperation.delete_one;
			boolean hasAuthority = domain.getSecurity().deleteOneAuthority();
			GGAPIServiceAccess access = domain.getSecurity().deleteOneAccess();
			Operation operation = templateOpenApi.getPaths().get(endpoint).getDelete();

			this.openApi.path(endpoint,
					pathItemUuid.delete(operation.description(this.getOperationDescription(domainName, access,
							hasAuthority, entityOperation, documentation == null ? null : documentation.deleteOne()))));
			this.setAdditionalInfos(domain, entityOperation, operation);
			this.addCustomParamsPathParameter(operation);
		}
	}

	private void setAdditionalInfos(IGGAPIDomain domain, GGAPIEntityOperation entityOperation, Operation operation) {
		this.addTenantIdHeader(operation, domain.isTenantIdMandatoryForOperation(entityOperation));
		this.addRequestedTenantIdHeader(operation);
		this.addOwnerIdHeader(operation, domain.isOwnerIdMandatoryForOperation(entityOperation));
	}

	private String getOperationDescription(String domainName, GGAPIServiceAccess access, boolean hasAuthority,
			GGAPIEntityOperation operation, String documentation) {
		String description = "<b>Access </b>: [" + access + "] <br> <b>Authority</b>: ["
				+ (hasAuthority == false ? "none"
						: BasicGGAPIAccessRule.getAuthority(domainName.toLowerCase(), operation))
				+ "]";
		if (documentation != null && !documentation.isEmpty()) {
			description += "<br><h3><strong><u><b>Documentation</b></u></strong></h3><br>" + documentation + "<br>";
		}
		return description;
	}

	private String getDocumentation(IGGAPIDomain domain) {
		String description = "<b>Public Entity</b> [" + domain.getEntity().getValue1().publicEntity() + "] <br> "
				+ "<b>Shared Entity</b> ["
				+ (domain.getEntity().getValue1().sharedEntity() ? "false"
						: domain.getEntity().getValue1().shareFieldAddress())
				+ "] <br> " + "<b>Hiddenable Entity</b> [" + domain.getEntity().getValue1().hiddenableEntity()
				+ "] <br> " + "<b>Geolocalized</b> ["
				+ (!domain.getEntity().getValue1().geolocalizedEntity() ? "false"
						: domain.getEntity().getValue1().locationFieldAddress())
				+ "]<br>" + "<b>Onwed Entity</b> [" + domain.getEntity().getValue1().ownedEntity() + "] <br> "
				+ "<b>Owner Entity</b> [" + domain.getEntity().getValue1().ownerEntity() + "] <br> "
				+ "<b>Tenant Entity</b> [" + domain.getEntity().getValue1().tenantEntity() + "] <br> ";

		if (domain.getDocumentation() != null && domain.getDocumentation().general() != null
				&& !domain.getDocumentation().general().isEmpty()) {
			description += "<br><h2><strong><u><b>General documentation</b></u></strong></h2><br>"
					+ domain.getDocumentation().general() + "<br>";
		}
		return description;
	}

	private void addOwnerIdHeader(Operation operation, boolean tenantIdMandatoryForOperation) {
		if (tenantIdMandatoryForOperation) {
			List<Parameter> params = operation.getParameters();
			HeaderParameter param = new HeaderParameter();
			param.setName(this.ownerIdHeaderName);
			param.setRequired(tenantIdMandatoryForOperation);
			param.setSchema(new StringSchema());
			params.add(param);
			operation.setParameters(params);
		}
	}

	private void addCustomParamsPathParameter(Operation operation) {
		List<Parameter> params = operation.getParameters();
		QueryParameter param = new QueryParameter();
		param.setName("params");
		param.setRequired(false);
		param.setSchema(new MapSchema());
		params.add(param);
		operation.setParameters(params);
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
		SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12,
				OptionPreset.PLAIN_JSON);
		configBuilder.forMethods()
				.withRequiredCheck(method -> method.getAnnotationConsideringFieldAndGetter(NotNull.class) != null);
		configBuilder.forFields()
				.withRequiredCheck(field -> field.getAnnotationConsideringFieldAndGetter(Nullable.class) == null)
				.withRequiredCheck(
						field -> field.getAnnotationConsideringFieldAndGetter(GGAPIEntityMandatory.class) != null)
				.withArrayUniqueItemsResolver(scope -> scope.getType().getErasedType() == (List.class) ? true : null);
		SchemaGeneratorConfig config = configBuilder.with(Option.EXTRA_OPEN_API_FORMAT_VALUES).with(module)
				.without(Option.FLATTENED_ENUMS_FROM_TOSTRING).build();

		SchemaGenerator generator = new SchemaGenerator(config);
		JsonNode jsonSchema = generator.generateSchema(clazz);
		return jsonSchema.toString();
	}

}
