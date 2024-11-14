package com.garganttua.api.interfaces.spring.rest.swagger;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.GGAPIMethod;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.entity.GGAPIEntityDocumentationInfos;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityMandatory;
import com.garganttua.api.spec.service.GGAPIServiceAccess;
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
import io.swagger.v3.oas.models.parameters.PathParameter;
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
			try {
				this.setOpenApiDocumentation(domain);
			} catch (Exception e) {
				log.warn("Error during openapi initialisation :", e);
			}
		});
	}

	private void setOpenApiDocumentation(IGGAPIDomain domain) throws Exception {
		String domainName = domain.getDomain().toLowerCase();
		Class<?> entityClass = domain.getEntity().getValue0();
		GGAPIEntityDocumentationInfos documentation = domain.getDocumentation();

		String tagName = "Domain " + domain.getEntity().getValue1().domain().toLowerCase();
		Tag tag = new Tag().name(tagName)
				.description(this.getDocumentation(domain));
		this.openApi.addTagsItem(tag);

		String entityClassSchema = this.test(entityClass);

		OpenAPI templateOpenApi = this.openApiHelper.getOpenApi(domain.getEntity().getValue1().domain().toLowerCase(),
				entityClass.getSimpleName(), entityClassSchema);
		
		Map<String, PathItem> pathItems= new HashMap<String, PathItem>();

		this.openApi.getComponents().addSchemas(entityClass.getSimpleName(),
				templateOpenApi.getComponents().getSchemas().get(entityClass.getSimpleName()));
		this.openApi.getComponents().addSchemas("ResponseObject",
				templateOpenApi.getComponents().getSchemas().get("ResponseObject"));
		this.openApi.getComponents().addSchemas("SortQuery",
				templateOpenApi.getComponents().getSchemas().get("SortQuery"));
		this.openApi.getComponents().addSchemas("FilterQuery",
				templateOpenApi.getComponents().getSchemas().get("FilterQuery"));
		
		Map<GGAPIEntityOperation, IGGAPIServiceInfos> infos = domain.getServiceInfos();
		infos.forEach((operation, info) -> {
			PathItem pathItem = pathItems.get(info.getPath());
			if( pathItem == null ) {
				pathItem = new PathItem();
				pathItems.put(info.getPath(), pathItem);
			}

			if( !info.getOperation().isCustom() ) {
				this.ceateStandardDocumentation(domain, domainName, documentation, templateOpenApi, pathItem, operation, info);
			} else {
				this.createCustomDocumentation(domain, domainName, documentation, pathItem, operation, info, tagName);
			}
		});
	}

	private void createCustomDocumentation(IGGAPIDomain domain, String domainName,
			GGAPIEntityDocumentationInfos documentation, PathItem pathItemBase,
			GGAPIEntityOperation operation, IGGAPIServiceInfos info, String tagName) {
		GGAPIServiceAccess access = domain.getSecurity().getAccess(info);
		String authority = domain.getSecurity().getAuthority(info);
		String description = this.getOperationDescription(domainName,
				access, !(authority==null||authority.isEmpty()), operation, documentation == null ? null : documentation.readAll(), authority);
		
		String entityClassSchema = this.test(info.getOperation().getEntity());
		
		Operation httpOperation = new Operation();
		httpOperation.addTagsItem(tagName);
		Operation httpOperationDescription = httpOperation.description(description);
		
		PathItem pathItem = this.getPathItem(pathItemBase, httpOperationDescription, info.getOperation().getMethod());
		
		this.openApi.path(info.getPath(), pathItem);
		
		this.setAdditionalInfos(domain, operation, httpOperation);
				
		List<String> pathParameters = this.extractSubstringsBetweenBraces(info.getPath());
		pathParameters.forEach( param -> {
			PathParameter parameter = new PathParameter().required(true);
			parameter.setName(param);
			pathItem.addParametersItem(parameter);
		});
	}

	private void ceateStandardDocumentation(IGGAPIDomain domain, String domainName, GGAPIEntityDocumentationInfos documentation,
			OpenAPI templateOpenApi, PathItem pathItemBase, GGAPIEntityOperation operation, IGGAPIServiceInfos info) {
		GGAPIServiceAccess access = domain.getSecurity().getAccess(info);
		String authority = domain.getSecurity().getAuthority(info);
		String description = this.getOperationDescription(domainName,
				access, !(authority==null||authority.isEmpty()), operation, documentation == null ? null : documentation.readAll(), authority);

		Operation httpOperation = this.getHttpOperationFromTemplate(templateOpenApi, info.getPath(), info.getOperation().getMethod());

		Operation httpOperationDescription = httpOperation.description(description);
		
		PathItem pathItem = this.getPathItem(pathItemBase, httpOperationDescription, info.getOperation().getMethod());
		
		this.openApi.path(info.getPath(), pathItem);

		if (!(domain.getEntity().getValue1().tenantEntity() && info.getOperation().getMethod() == GGAPIMethod.create)) {
			this.setAdditionalInfos(domain, operation, httpOperation);
		}
		this.addCustomParamsPathParameter(httpOperation);
	}

	private PathItem getPathItem(PathItem pathItem, Operation operationDescription, GGAPIMethod method) {
		switch (method) {
		case create:
			return pathItem.post(operationDescription);
		case delete:
			return pathItem.delete(operationDescription);
		case read:
			return pathItem.get(operationDescription);
		case update:
			return pathItem.patch(operationDescription);
		default:
			throw new IllegalArgumentException("Unexpected value: " + method);
		}
	}

	private Operation getHttpOperationFromTemplate(OpenAPI templateOpenApi, String path, GGAPIMethod method) {
		switch (method) {
		case create:
			return templateOpenApi.getPaths().get(path).getPost();
		case delete:
			return templateOpenApi.getPaths().get(path).getDelete();
		case read:
			return templateOpenApi.getPaths().get(path).getGet();
		case update:
			return templateOpenApi.getPaths().get(path).getPatch();
		default:
			throw new IllegalArgumentException("Unexpected value: " + method);
		}
	}

	private void setAdditionalInfos(IGGAPIDomain domain, GGAPIEntityOperation entityOperation, Operation operation) {
		this.addTenantIdHeader(operation, domain.isTenantIdMandatoryForOperation(entityOperation));
		this.addRequestedTenantIdHeader(operation);
		this.addOwnerIdHeader(operation, domain.isOwnerIdMandatoryForOperation(entityOperation));
	}

	private String getOperationDescription(String domainName, GGAPIServiceAccess access, boolean hasAuthority,
			GGAPIEntityOperation operation, String documentation, String authority) {
		String description = "<b>Access </b>: [" + access + "] <br> <b>Authority</b>: ["
				+ (hasAuthority == false ? "none"
						: authority)
				+ "]";
		if (documentation != null && !documentation.isEmpty()) {
			description += "<br><h3><strong><u><b>Documentation</b></u></strong></h3><br>" + documentation + "<br>";
		}
		return description;
	}

	private String getDocumentation(IGGAPIDomain domain) {
		String authenticatorEntity = domain.getSecurity().getAuthenticatorInfos()==null?"false":"true";
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
				+ "<b>Tenant Entity</b> [" + domain.getEntity().getValue1().tenantEntity() + "] <br> "
				+ "<b>Authenticator Entity</b> [" + authenticatorEntity + "] <br> ";

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
			if( params == null ) {
				params = new ArrayList<Parameter>();
			}
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
		if( params == null ) {
			params = new ArrayList<Parameter>();
		}
		QueryParameter param = new QueryParameter();
		param.setName("params");
		param.setRequired(false);
		param.setSchema(new MapSchema());
		params.add(param);
		operation.setParameters(params);
	}

	private void addRequestedTenantIdHeader(Operation operation) {
		List<Parameter> params = operation.getParameters();
		if( params == null ) {
			params = new ArrayList<Parameter>();
		}
		HeaderParameter param = new HeaderParameter();
		param.setName(this.requestedTenantIdHeaderName);
		param.setRequired(false);
		param.setSchema(new StringSchema());
		params.add(param);
		operation.setParameters(params);
	}

	private void addTenantIdHeader(Operation operation, boolean mandatory) {
		List<Parameter> params = operation.getParameters();
		if( params == null ) {
			params = new ArrayList<Parameter>();
		}
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
						field -> field.getAnnotation(GGAPIEntityMandatory.class) != null)
				.withArrayUniqueItemsResolver(scope -> scope.getType().getErasedType() == (List.class) ? true : null);
		SchemaGeneratorConfig config = configBuilder.with(Option.EXTRA_OPEN_API_FORMAT_VALUES).with(module)
				.without(Option.FLATTENED_ENUMS_FROM_TOSTRING).build();

		SchemaGenerator generator = new SchemaGenerator(config);
		JsonNode jsonSchema = generator.generateSchema(clazz);
		return jsonSchema.toString();
	}
	
	public List<String> extractSubstringsBetweenBraces(String input) {
        List<String> substrings = new ArrayList<>();
        if (input == null || input.isEmpty()) {
            return substrings;
        }

        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            substrings.add(matcher.group(1));
        }
        return substrings;
    }
}
