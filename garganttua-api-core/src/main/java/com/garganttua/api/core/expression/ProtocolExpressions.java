package com.garganttua.api.core.expression;
import com.garganttua.core.reflection.annotations.Reflected;

import java.util.Map;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.protocol.IProtocol;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.expression.annotations.Expression;

import jakarta.annotation.Nullable;

import static com.garganttua.api.core.expression.ExpressionUtils.unwrapOptional;

/**
 * Expressions for the data pipeline stages 1 (extract) and 10 (response).
 * Protocols are looked up on the API-level pool configured via
 * {@code IApiBuilder.protocol(...)} or auto-detected via {@code @Protocol}.
 */
@Reflected(queryAllPublicMethods = true)
public class ProtocolExpressions {

	@Expression(name = "resolveProtocol",
			description = "Picks the first registered IProtocol whose requestType matches rawRequest. Throws 415 if none match.")
	public static IProtocol<?, ?> resolveProtocol(@Nullable Object apiContext, @Nullable Object rawRequest) {
		IApi api = (IApi) unwrapOptional(apiContext);
		Object request = unwrapOptional(rawRequest);
		if (api == null) {
			throw new ApiException("API context is null");
		}
		if (request == null) {
			throw new ApiException("Raw request is null");
		}
		for (IProtocol<?, ?> p : api.getProtocols()) {
			if (p.requestType().isInstance(request)) {
				return p;
			}
		}
		throw new ApiException("No protocol registered for request type: " + request.getClass().getName());
	}

	@Expression(name = "extractCaller",
			description = "Delegates to IProtocol.getCaller(rawRequest)")
	public static ICaller extractCaller(@Nullable Object protocol, @Nullable Object rawRequest) {
		return invokeExtraction(protocol, rawRequest, (p, r) -> cast(p).getCaller(r));
	}

	@Expression(name = "extractRawBody",
			description = "Delegates to IProtocol.getRawBody(rawRequest)")
	public static byte[] extractRawBody(@Nullable Object protocol, @Nullable Object rawRequest) {
		return invokeExtraction(protocol, rawRequest, (p, r) -> cast(p).getRawBody(r));
	}

	@Expression(name = "extractAuthorization",
			description = "Delegates to IProtocol.getAuthorization(rawRequest)")
	public static String extractAuthorization(@Nullable Object protocol, @Nullable Object rawRequest) {
		return invokeExtraction(protocol, rawRequest, (p, r) -> cast(p).getAuthorization(r));
	}

	@Expression(name = "extractContentType",
			description = "Delegates to IProtocol.getContentType(rawRequest)")
	public static String extractContentType(@Nullable Object protocol, @Nullable Object rawRequest) {
		return invokeExtraction(protocol, rawRequest, (p, r) -> cast(p).getContentType(r));
	}

	@Expression(name = "extractAccept",
			description = "Delegates to IProtocol.getAccept(rawRequest)")
	public static String extractAccept(@Nullable Object protocol, @Nullable Object rawRequest) {
		return invokeExtraction(protocol, rawRequest, (p, r) -> cast(p).getAccept(r));
	}

	@Expression(name = "extractPath",
			description = "Delegates to IProtocol.getPath(rawRequest)")
	public static String extractPath(@Nullable Object protocol, @Nullable Object rawRequest) {
		return invokeExtraction(protocol, rawRequest, (p, r) -> cast(p).getPath(r));
	}

	@Expression(name = "extractMethod",
			description = "Delegates to IProtocol.getMethod(rawRequest)")
	public static String extractMethod(@Nullable Object protocol, @Nullable Object rawRequest) {
		return invokeExtraction(protocol, rawRequest, (p, r) -> cast(p).getMethod(r));
	}

	@Expression(name = "extractQueryParameters",
			description = "Delegates to IProtocol.getQueryParameters(rawRequest)")
	public static Map<String, String> extractQueryParameters(@Nullable Object protocol, @Nullable Object rawRequest) {
		return invokeExtraction(protocol, rawRequest, (p, r) -> cast(p).getQueryParameters(r));
	}

	@Expression(name = "buildProtocolResponse",
			description = "Delegates to IProtocol.buildResponse(rawRequest, output, statusCode, contentType). Handles byte[] and raw object outputs; contentType is the negotiated serializer's MIME (null when serialization was skipped).")
	public static Object buildProtocolResponse(@Nullable Object protocol, @Nullable Object rawRequest,
			@Nullable Object output, @Nullable Object statusCode, @Nullable Object contentType) {
		IProtocol<Object, Object> p = cast(unwrapOptional(protocol));
		Object request = unwrapOptional(rawRequest);
		Object payload = unwrapOptional(output);
		int code = toInt(unwrapOptional(statusCode), 200);
		Object ctRaw = unwrapOptional(contentType);
		String ct = ctRaw == null ? null : String.valueOf(ctRaw);
		if (p == null || request == null) {
			throw new ApiException("buildProtocolResponse: protocol or rawRequest is null");
		}
		return p.buildResponse(request, payload, code, ct);
	}

	@Expression(name = "setCallerArgs",
			description = "Writes ICaller fields (tenantId, callerId, ownerId, authorities, superTenant, superOwner) into the operation request's arg map. Returns the caller unchanged.")
	public static ICaller setCallerArgs(@Nullable Object request, @Nullable Object caller) {
		IOperationRequest req = (IOperationRequest) unwrapOptional(request);
		ICaller c = (ICaller) unwrapOptional(caller);
		if (req == null) {
			throw new ApiException("setCallerArgs: request is null");
		}
		if (c == null) {
			return null;
		}
		req.arg(IOperationRequest.TENANT_ID.name(), c.tenantId());
		req.arg(IOperationRequest.REQUESTED_TENANT_ID.name(), c.requestedTenantId());
		req.arg(IOperationRequest.CALLER_ID.name(), c.callerId());
		req.arg(IOperationRequest.OWNER_ID.name(), c.ownerId());
		req.arg(IOperationRequest.AUTHORITIES.name(), c.authorities());
		req.arg(IOperationRequest.SUPER_TENANT.name(), c.superTenant());
		req.arg(IOperationRequest.SUPER_OWNER.name(), c.superOwner());
		return c;
	}

	// ----- helpers -----

	@FunctionalInterface
	private interface Extractor<R> {
		R apply(Object protocol, Object request) throws ApiException;
	}

	private static <R> R invokeExtraction(Object protocol, Object rawRequest, Extractor<R> fn) {
		Object p = unwrapOptional(protocol);
		Object r = unwrapOptional(rawRequest);
		if (p == null || r == null) {
			throw new ApiException("extraction: protocol or rawRequest is null");
		}
		return fn.apply(p, r);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static IProtocol<Object, Object> cast(Object protocol) {
		return (IProtocol) protocol;
	}

	private static int toInt(Object value, int fallback) {
		if (value == null) return fallback;
		if (value instanceof Integer i) return i;
		if (value instanceof Number n) return n.intValue();
		try {
			return Integer.parseInt(value.toString());
		} catch (NumberFormatException e) {
			return fallback;
		}
	}
}
