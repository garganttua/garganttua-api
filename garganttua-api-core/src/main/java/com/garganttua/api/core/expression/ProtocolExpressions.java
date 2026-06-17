package com.garganttua.api.core.expression;
import com.garganttua.core.reflection.annotations.Reflected;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.geojson.GeoJsonObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.pageable.Pageable;
import com.garganttua.api.commons.protocol.IProtocol;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.sort.Sort;
import com.garganttua.api.commons.sort.SortDirection;
import com.garganttua.api.core.filter.Filter;
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

	@Expression(name = "applyReadParamsFromQuery",
			description = "Translates HTTP query parameters into the typed readAll args so pagination / sort / "
					+ "output-mode / projection / filter work over the transport: page+size → IPageable (PAGE), "
					+ "sort=field[,asc|desc] → ISort (SORT), mode=full|uuid|id → MODE, fields=a,b → PROJECTION, "
					+ "filter=field:op:value[;…] OR filter=<JSON> → FILTER (AND-combined, then AND'd with the "
					+ "caller's access filter; JSON is a Mongo-like shape supporting the full operator set incl. "
					+ "geospatial). No-op for absent params; harmless for non-readAll ops.")
	public static Object applyReadParamsFromQuery(@Nullable Object request, @Nullable Object queryParameters) {
		IOperationRequest req = (IOperationRequest) unwrapOptional(request);
		Object qpObj = unwrapOptional(queryParameters);
		if (req == null || !(qpObj instanceof Map<?, ?> qp)) {
			return request;
		}
		Object sizeRaw = qp.get("size");
		if (sizeRaw != null) {
			int pageSize = toInt(sizeRaw, 0);
			if (pageSize > 0) {
				int pageIndex = Math.max(toInt(qp.get("page"), 0), 0);
				req.arg(IOperationRequest.PAGE.name(), new Pageable(pageIndex, pageSize));
			}
		}
		Object sortRaw = qp.get("sort");
		if (sortRaw != null && !String.valueOf(sortRaw).isBlank()) {
			String[] parts = String.valueOf(sortRaw).split(",", 2);
			String field = parts[0].trim();
			if (!field.isEmpty()) {
				SortDirection dir = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim()))
						? SortDirection.desc : SortDirection.asc;
				req.arg(IOperationRequest.SORT.name(), new Sort(field, dir));
			}
		}
		Object modeRaw = qp.get("mode");
		if (modeRaw != null && !String.valueOf(modeRaw).isBlank()) {
			req.arg(IOperationRequest.MODE.name(), String.valueOf(modeRaw).trim());
		}
		// fields=name,email → PROJECTION (a List<String> of entity field names). Trim + drop blanks;
		// an empty list is treated as "no projection" and not set.
		Object fieldsRaw = qp.get("fields");
		if (fieldsRaw != null && !String.valueOf(fieldsRaw).isBlank()) {
			List<String> fields = java.util.Arrays.stream(String.valueOf(fieldsRaw).split(","))
					.map(String::trim)
					.filter(s -> !s.isEmpty())
					.toList();
			if (!fields.isEmpty()) {
				req.arg(IOperationRequest.PROJECTION.name(), fields);
			}
		}
		// filter=field:op:value[;field:op:value…] → FILTER. Clauses are AND-combined; the framework
		// then ANDs the result with the caller's access filter (buildFilter). The transport sets only
		// the BASE business filter — tenant/owner/visibility isolation stays server-authoritative.
		Object filterRaw = qp.get("filter");
		if (filterRaw != null && !String.valueOf(filterRaw).isBlank()) {
			IFilter filter = parseQueryFilter(String.valueOf(filterRaw));
			if (filter != null) {
				req.arg(IOperationRequest.FILTER.name(), filter);
			}
		}
		return request;
	}

	/**
	 * Parses the {@code filter} query parameter into an {@link IFilter}, accepting TWO syntaxes:
	 * <ul>
	 *   <li>a <b>JSON</b> object/array (when the value starts with {@code &#123;} or {@code [}) — a
	 *       Mongo-like shape, see {@link #parseJsonFilter(String)};</li>
	 *   <li>otherwise the compact <b>{@code field:op:value}</b> mini-language, see
	 *       {@link #parseDelimitedFilter(String)}.</li>
	 * </ul>
	 * Returns {@code null} when nothing parses.
	 */
	static IFilter parseQueryFilter(String raw) {
		if (raw == null) {
			return null;
		}
		String trimmed = raw.trim();
		if (trimmed.isEmpty()) {
			return null;
		}
		if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
			return parseJsonFilter(trimmed);
		}
		return parseDelimitedFilter(trimmed);
	}

	/**
	 * Parses the compact {@code field:op:value} mini-language: clauses separated by {@code ;},
	 * AND-combined. Operators: {@code eq, ne, gt, gte, lt, lte, regex, empty} and {@code in, nin}
	 * (comma-separated values). A value splits on the FIRST two colons only, so the value itself may
	 * contain {@code :} (e.g. {@code url:eq:http://x}). Scalar values coerce to Boolean / Number /
	 * String. A malformed or unknown-operator clause is skipped. (Geospatial operators need the JSON
	 * syntax — they carry a GeoJSON geometry.)
	 */
	private static IFilter parseDelimitedFilter(String raw) {
		List<Filter> clauses = new ArrayList<>();
		for (String token : raw.split(";")) {
			String clause = token.trim();
			if (clause.isEmpty()) {
				continue;
			}
			String[] parts = clause.split(":", 3);
			if (parts.length < 2) {
				continue;
			}
			String field = parts[0].trim();
			String op = parts[1].trim().toLowerCase(Locale.ROOT);
			String value = parts.length > 2 ? parts[2] : null;
			if (field.isEmpty()) {
				continue;
			}
			Filter f = buildFilterClause(field, op, value);
			if (f != null) {
				clauses.add(f);
			}
		}
		if (clauses.isEmpty()) {
			return null;
		}
		return clauses.size() == 1 ? clauses.get(0) : Filter.and(clauses.toArray(new Filter[0]));
	}

	private static Filter buildFilterClause(String field, String op, String value) {
		switch (op) {
			case "eq":    return Filter.eq(field, coerceFilterValue(value));
			case "ne":    return Filter.ne(field, coerceFilterValue(value));
			case "gt":    return Filter.gt(field, coerceFilterValue(value));
			case "gte":   return Filter.gte(field, coerceFilterValue(value));
			case "lt":    return Filter.lt(field, coerceFilterValue(value));
			case "lte":   return Filter.lte(field, coerceFilterValue(value));
			case "regex": return value != null ? Filter.regex(field, value) : null;
			case "empty": return Filter.empty(field);
			case "in":    return Filter.in(field, coerceFilterList(value));
			case "nin":   return Filter.nin(field, coerceFilterList(value));
			default:      return null; // unknown operator — skip the clause
		}
	}

	/** Coerces a query value to Boolean / Long / Double when it parses cleanly, else keeps the String. */
	private static Object coerceFilterValue(String value) {
		if (value == null) {
			return null;
		}
		String v = value.trim();
		if ("true".equalsIgnoreCase(v)) return Boolean.TRUE;
		if ("false".equalsIgnoreCase(v)) return Boolean.FALSE;
		try { return Long.valueOf(v); } catch (NumberFormatException ignored) { /* not a long */ }
		try { return Double.valueOf(v); } catch (NumberFormatException ignored) { /* not a double */ }
		return value;
	}

	private static Object[] coerceFilterList(String value) {
		if (value == null || value.isBlank()) {
			return new Object[0];
		}
		return java.util.Arrays.stream(value.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.map(ProtocolExpressions::coerceFilterValue)
				.toArray();
	}

	// ----- JSON filter (Mongo-like) -----

	private static final ObjectMapper FILTER_JSON = new ObjectMapper();

	/**
	 * Parses a Mongo-like JSON filter into an {@link IFilter}, supporting the FULL operator set:
	 * <ul>
	 *   <li>a field with a scalar → {@code $eq}: {@code {"name":"Alice"}};</li>
	 *   <li>a field with an operator object → those operators (AND-combined):
	 *       {@code {"age":{"$gte":18,"$lt":65}}}. Operators: {@code $eq,$ne,$gt,$gte,$lt,$lte,
	 *       $in,$nin,$regex,$text,$empty,$geoWithin,$geoWithinSphere};</li>
	 *   <li>a field with an array → {@code $in} shorthand: {@code {"role":["admin","user"]}};</li>
	 *   <li>several fields at one level → AND; explicit logical groups {@code $and/$or/$nor}
	 *       take arrays of sub-filters: {@code {"$or":[{"a":1},{"b":2}]}};</li>
	 *   <li>geospatial: the operator value is a GeoJSON geometry:
	 *       {@code {"location":{"$geoWithin":{"type":"Polygon","coordinates":[…]}}}}.</li>
	 * </ul>
	 * Scalars keep their JSON type (boolean / long / double / string). A malformed JSON (or an
	 * invalid GeoJSON geometry) raises an {@link ApiException}; an unknown operator is skipped.
	 */
	private static IFilter parseJsonFilter(String json) {
		JsonNode root;
		try {
			root = FILTER_JSON.readTree(json);
		} catch (Exception e) {
			throw new ApiException("Invalid JSON filter: " + e.getMessage(), e);
		}
		return jsonNodeToFilter(root);
	}

	private static Filter jsonNodeToFilter(JsonNode node) {
		if (node == null || node.isNull()) {
			return null;
		}
		if (node.isArray()) {
			// A top-level array is an implicit AND of its sub-filters.
			List<Filter> subs = new ArrayList<>();
			for (JsonNode element : node) {
				Filter f = jsonNodeToFilter(element);
				if (f != null) subs.add(f);
			}
			return combine(subs);
		}
		if (!node.isObject()) {
			return null;
		}
		List<Filter> clauses = new ArrayList<>();
		Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
		while (fields.hasNext()) {
			Map.Entry<String, JsonNode> entry = fields.next();
			String key = entry.getKey();
			JsonNode value = entry.getValue();
			switch (key) {
				case "$and": clauses.add(Filter.and(jsonFilterArray(value))); break;
				case "$or":  clauses.add(Filter.or(jsonFilterArray(value)));  break;
				case "$nor": clauses.add(Filter.nor(jsonFilterArray(value))); break;
				default: {
					Filter fieldFilter = jsonFieldClause(key, value);
					if (fieldFilter != null) clauses.add(fieldFilter);
				}
			}
		}
		return combine(clauses);
	}

	private static Filter[] jsonFilterArray(JsonNode arrayNode) {
		List<Filter> subs = new ArrayList<>();
		if (arrayNode != null && arrayNode.isArray()) {
			for (JsonNode element : arrayNode) {
				Filter f = jsonNodeToFilter(element);
				if (f != null) subs.add(f);
			}
		}
		return subs.toArray(new Filter[0]);
	}

	private static Filter jsonFieldClause(String field, JsonNode value) {
		if (value.isObject()) {
			List<Filter> ops = new ArrayList<>();
			Iterator<Map.Entry<String, JsonNode>> entries = value.fields();
			while (entries.hasNext()) {
				Map.Entry<String, JsonNode> e = entries.next();
				Filter op = jsonOperatorClause(field, e.getKey(), e.getValue());
				if (op != null) ops.add(op);
			}
			return combine(ops);
		}
		if (value.isArray()) {
			return Filter.in(field, jsonScalarArray(value)); // shorthand: array → $in
		}
		return Filter.eq(field, jsonScalar(value));
	}

	private static Filter jsonOperatorClause(String field, String op, JsonNode value) {
		switch (op) {
			case "$eq":             return Filter.eq(field, jsonScalar(value));
			case "$ne":             return Filter.ne(field, jsonScalar(value));
			case "$gt":             return Filter.gt(field, jsonScalar(value));
			case "$gte":            return Filter.gte(field, jsonScalar(value));
			case "$lt":             return Filter.lt(field, jsonScalar(value));
			case "$lte":            return Filter.lte(field, jsonScalar(value));
			case "$in":             return Filter.in(field, jsonScalarArray(value));
			case "$nin":            return Filter.nin(field, jsonScalarArray(value));
			case "$regex":          return value.isTextual() ? Filter.regex(field, value.asText()) : null;
			case "$text":           return value.isTextual() ? Filter.text(field, value.asText()) : null;
			case "$empty":          return Filter.empty(field);
			case "$geoWithin":       return Filter.geolocWithin(field, jsonGeometry(value));
			case "$geoWithinSphere": return Filter.geolocWithinSphere(field, jsonGeometry(value));
			default:                return null; // unknown operator — skip
		}
	}

	private static GeoJsonObject jsonGeometry(JsonNode node) {
		try {
			return FILTER_JSON.treeToValue(node, GeoJsonObject.class);
		} catch (Exception e) {
			throw new ApiException("Invalid GeoJSON geometry in filter: " + e.getMessage(), e);
		}
	}

	/** A JSON scalar keeps its native type — boolean / long / double / string (null tolerated). */
	private static Object jsonScalar(JsonNode node) {
		if (node == null || node.isNull()) {
			return null;
		}
		if (node.isBoolean()) {
			return node.booleanValue();
		}
		if (node.isIntegralNumber()) {
			return node.longValue();
		}
		if (node.isNumber()) {
			return node.doubleValue();
		}
		return node.asText();
	}

	private static Object[] jsonScalarArray(JsonNode node) {
		List<Object> values = new ArrayList<>();
		if (node != null && node.isArray()) {
			for (JsonNode element : node) {
				values.add(jsonScalar(element));
			}
		} else if (node != null && !node.isNull()) {
			values.add(jsonScalar(node));
		}
		return values.toArray();
	}

	private static Filter combine(List<Filter> clauses) {
		if (clauses.isEmpty()) {
			return null;
		}
		return clauses.size() == 1 ? clauses.get(0) : Filter.and(clauses.toArray(new Filter[0]));
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
