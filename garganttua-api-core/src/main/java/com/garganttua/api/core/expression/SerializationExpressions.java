package com.garganttua.api.core.expression;
import com.garganttua.core.reflection.annotations.Reflected;

import java.util.Locale;
import java.util.Optional;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.MimeType;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.definition.IDtoDefinition;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.operation.OperationType;
import com.garganttua.api.commons.operation.TechnicalOperation;
import com.garganttua.api.commons.serialization.ISerializer;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.expression.annotations.Expression;
import com.garganttua.core.reflection.IClass;

import jakarta.annotation.Nullable;

import static com.garganttua.api.core.expression.ExpressionUtils.unwrapOptional;

/**
 * Expressions for the data pipeline stages (deserialize on request, serialize on response).
 * Serializers are looked up on the API-level pool configured via
 * {@code IApiBuilder.serializer(...)}.
 */
@Reflected(queryAllPublicMethods = true)
public class SerializationExpressions {

	@Expression(name = "operationExpectsBody",
			description = "Returns true if the operation carries a request body (create/update)")
	public static boolean operationExpectsBody(@Nullable Object operation) {
		OperationDefinition opDef = (OperationDefinition) unwrapOptional(operation);
		if (opDef == null) return false;
		TechnicalOperation tech = opDef.technicalOperation();
		return tech == TechnicalOperation.create || tech == TechnicalOperation.update;
	}

	@Expression(name = "resolveBodyType",
			description = "Resolves the target entity class for the operation body (the same class CRUD stages operate on)")
	public static IClass<?> resolveBodyType(@Nullable Object operation, @Nullable Object apiContext) {
		OperationDefinition opDef = (OperationDefinition) unwrapOptional(operation);
		if (opDef == null) {
			throw new ApiException("Cannot resolve body type: operation is null");
		}
		// Authentication carries credentials, not the authenticator entity: the body is
		// an AuthenticationRequest(login, credentials, tenantId), the shape AUTHENTICATE.gs
		// reads under "entity". Without this the body would deserialize to the User entity
		// and the login would fail over HTTP.
		if (opDef.type() == OperationType.authentication) {
			return IClass.getClass(com.garganttua.api.core.security.authentication.AuthenticationRequest.class);
		}
		IClass<?> entityClass = opDef.entity();
		if (entityClass != null) {
			return entityClass;
		}
		// Fallback: pick the first DTO registered on the domain (rare path when the
		// operation carries no entity class — e.g. custom use cases).
		IApi api = (IApi) unwrapOptional(apiContext);
		if (api == null) {
			throw new ApiException("Cannot resolve body type: operation has no entity class and apiContext is null");
		}
		IDomain<?> domain = api.getDomain(opDef.domainName())
				.orElseThrow(() -> new ApiException("Unknown domain: " + opDef.domainName()));
		IDomainDefinition<?> def = domain.getDomainDefinition();
		if (def.dtoDefinitions().isEmpty()) {
			throw new ApiException("No DTO registered for domain: " + opDef.domainName());
		}
		IDtoDefinition<?> dto = def.dtoDefinitions().get(0);
		return dto.dtoClass();
	}

	@Expression(name = "resolveSerializer",
			description = "Looks up a serializer by Content-Type, stripping media type parameters. Throws 415 if none match.")
	public static ISerializer resolveSerializer(@Nullable Object apiContext, @Nullable Object contentType) {
		IApi api = (IApi) unwrapOptional(apiContext);
		String raw = asString(contentType);
		if (api == null) {
			throw new ApiException("API context is null");
		}
		MimeType requested = raw == null
				? MimeType.APPLICATION_JSON
				: MimeType.find(raw).orElseThrow(() ->
						new ApiException("Unsupported Content-Type: " + raw));
		return api.getSerializers().stream()
				.filter(s -> s.mimeType() == requested)
				.findFirst()
				.orElseThrow(() ->
						new ApiException("No serializer registered for Content-Type: " + requested));
	}

	@Expression(name = "negotiateSerializer",
			description = "Picks the best serializer matching the Accept header. Falls back to JSON for */* or missing header. Throws 406 if nothing matches.")
	public static ISerializer negotiateSerializer(@Nullable Object apiContext, @Nullable Object acceptHeader) {
		IApi api = (IApi) unwrapOptional(apiContext);
		if (api == null) {
			throw new ApiException("API context is null");
		}
		String raw = asString(acceptHeader);
		if (raw == null || raw.isBlank() || raw.contains("*/*")) {
			return api.getSerializers().stream()
					.filter(s -> s.mimeType() == MimeType.APPLICATION_JSON)
					.findFirst()
					.orElseGet(() -> api.getSerializers().stream()
							.findFirst()
							.orElseThrow(() -> new ApiException("No serializer registered")));
		}
		for (String token : raw.split(",")) {
			String trimmed = token.trim();
			if (trimmed.isEmpty()) continue;
			int semicolon = trimmed.indexOf(';');
			String candidate = (semicolon < 0 ? trimmed : trimmed.substring(0, semicolon))
					.trim().toLowerCase(Locale.ROOT);
			Optional<MimeType> mime = MimeType.find(candidate);
			if (mime.isEmpty()) continue;
			Optional<ISerializer> match = api.getSerializers().stream()
					.filter(s -> s.mimeType() == mime.get())
					.findFirst();
			if (match.isPresent()) return match.get();
		}
		throw new ApiException("No acceptable serializer for: " + raw);
	}

	@Expression(name = "deserialize",
			description = "Deserializes raw bytes into the target DTO using the given serializer")
	public static Object deserialize(@Nullable Object serializer, @Nullable Object bytes, @Nullable Object type)
			throws ApiException {
		ISerializer ser = (ISerializer) unwrapOptional(serializer);
		byte[] data = toByteArray(unwrapOptional(bytes));
		@SuppressWarnings({"unchecked", "rawtypes"})
		IClass<Object> targetType = (IClass) unwrapOptional(type);
		if (ser == null || data == null || targetType == null) {
			throw new ApiException("deserialize: missing serializer, bytes, or target type");
		}
		return ser.deserialize(data, targetType);
	}

	@Expression(name = "serialize",
			description = "Serializes an object to a byte array using the given serializer")
	public static byte[] serialize(@Nullable Object serializer, @Nullable Object object) throws ApiException {
		ISerializer ser = (ISerializer) unwrapOptional(serializer);
		if (ser == null) {
			throw new ApiException("serialize: serializer is null");
		}
		return ser.serialize(unwrapOptional(object));
	}

	@Expression(name = "serializerContentType",
			description = "Returns the wire MIME string of the given serializer (e.g. \"application/json\"), so the response can be labelled with the media type actually used.")
	public static String serializerContentType(@Nullable Object serializer) throws ApiException {
		ISerializer ser = (ISerializer) unwrapOptional(serializer);
		if (ser == null) {
			throw new ApiException("serializerContentType: serializer is null");
		}
		MimeType mime = ser.mimeType();
		return mime == null ? null : mime.toString();
	}

	@Expression(name = "setRequestArg",
			description = "Writes a value into the operation request's arg map and returns the value unchanged")
	public static Object setRequestArg(@Nullable Object request, @Nullable Object key, @Nullable Object value) {
		IOperationRequest req = (IOperationRequest) unwrapOptional(request);
		String keyStr = asString(key);
		Object unwrappedValue = unwrapOptional(value);
		if (req == null || keyStr == null) {
			throw new ApiException("setRequestArg: request or key is null");
		}
		req.arg(keyStr, unwrappedValue);
		return unwrappedValue;
	}

	private static String asString(Object value) {
		Object unwrapped = unwrapOptional(value);
		return unwrapped == null ? null : unwrapped.toString();
	}

	private static byte[] toByteArray(Object value) {
		if (value == null) return null;
		if (value instanceof byte[] arr) return arr;
		if (value instanceof Byte[] boxed) {
			byte[] out = new byte[boxed.length];
			for (int i = 0; i < boxed.length; i++) out[i] = boxed[i];
			return out;
		}
		throw new ApiException("Expected byte[] or Byte[] but got " + value.getClass().getName());
	}
}
