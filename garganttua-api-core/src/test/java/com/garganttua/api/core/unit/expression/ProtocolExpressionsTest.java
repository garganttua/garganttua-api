package com.garganttua.api.core.unit.expression;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.expression.ProtocolExpressions;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.protocol.IProtocol;
import com.garganttua.api.commons.pageable.IPageable;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.sort.ISort;
import com.garganttua.api.commons.sort.SortDirection;
import com.garganttua.core.reflection.IClass;

@DisplayName("ProtocolExpressions")
class ProtocolExpressionsTest {

	/** Fixture request classes. */
	static class FakeRequest {}
	static class FakeRequestSubtype extends FakeRequest {}
	static class OtherRequest {}

	/**
	 * Stub protocol implementation — records the last argument each getter received
	 * so tests can assert delegation. Returns pre-set canned values.
	 */
	static class StubProtocol implements IProtocol<FakeRequest, String> {
		ICaller cannedCaller;
		byte[] cannedRawBody;
		String cannedAuth;
		String cannedContentType;
		String cannedAccept;
		String cannedPath;
		String cannedMethod;
		Map<String, String> cannedParams = new HashMap<>();
		String cannedResponse;

		Object lastBuildOutput;
		int lastBuildStatusCode;
		String lastContentType;
		FakeRequest lastRequest;

		@Override public IClass<FakeRequest> requestType() { return IClass.getClass(FakeRequest.class); }
		@Override public ICaller getCaller(FakeRequest r) { lastRequest = r; return cannedCaller; }
		@Override public byte[] getRawBody(FakeRequest r) { lastRequest = r; return cannedRawBody; }
		@Override public String getAuthorization(FakeRequest r) { lastRequest = r; return cannedAuth; }
		@Override public String getContentType(FakeRequest r) { lastRequest = r; return cannedContentType; }
		@Override public String getAccept(FakeRequest r) { lastRequest = r; return cannedAccept; }
		@Override public String getPath(FakeRequest r) { lastRequest = r; return cannedPath; }
		@Override public String getMethod(FakeRequest r) { lastRequest = r; return cannedMethod; }
		@Override public Map<String, String> getQueryParameters(FakeRequest r) { lastRequest = r; return cannedParams; }
		@Override public String buildResponse(FakeRequest r, Object output, int statusCode) {
			lastRequest = r; lastBuildOutput = output; lastBuildStatusCode = statusCode;
			return cannedResponse;
		}
		@Override public String buildResponse(FakeRequest r, Object output, int statusCode, String contentType) {
			lastContentType = contentType;
			return buildResponse(r, output, statusCode);
		}
	}

	private static IApi apiWithProtocols(IProtocol<?, ?>... protocols) {
		IApi api = mock(IApi.class);
		when(api.getProtocols()).thenReturn(List.of(protocols));
		return api;
	}

	@Nested
	@DisplayName("resolveProtocol")
	class ResolveProtocol {

		@Test
		@DisplayName("picks the first matching protocol by requestType().isInstance()")
		void picksFirstMatching() {
			StubProtocol p1 = new StubProtocol();
			StubProtocol p2 = new StubProtocol();
			IApi api = apiWithProtocols(p1, p2);

			IProtocol<?, ?> found = ProtocolExpressions.resolveProtocol(api, new FakeRequest());
			assertSame(p1, found, "First registered match should win");
		}

		@Test
		@DisplayName("matches subtypes of the protocol's requestType (isAssignableFrom semantics)")
		void matchesSubtypes() {
			StubProtocol p = new StubProtocol();
			IApi api = apiWithProtocols(p);

			IProtocol<?, ?> found = ProtocolExpressions.resolveProtocol(api, new FakeRequestSubtype());
			assertSame(p, found);
		}

		@Test
		@DisplayName("throws when no protocol registered for the request class (→ 415)")
		void noMatch() {
			IApi api = apiWithProtocols(new StubProtocol());
			ApiException ex = assertThrows(ApiException.class,
					() -> ProtocolExpressions.resolveProtocol(api, new OtherRequest()));
			assertTrue(ex.getMessage().contains("No protocol registered"),
					"Got: " + ex.getMessage());
			assertTrue(ex.getMessage().contains(OtherRequest.class.getName()),
					"Error should name the offending type, got: " + ex.getMessage());
		}

		@Test
		@DisplayName("throws when API context is null")
		void nullApi() {
			assertThrows(ApiException.class,
					() -> ProtocolExpressions.resolveProtocol(null, new FakeRequest()));
		}

		@Test
		@DisplayName("throws when raw request is null")
		void nullRequest() {
			IApi api = apiWithProtocols(new StubProtocol());
			assertThrows(ApiException.class,
					() -> ProtocolExpressions.resolveProtocol(api, null));
		}

		@Test
		@DisplayName("unwraps Optional for both api and request")
		void unwrapsOptionals() {
			StubProtocol p = new StubProtocol();
			IApi api = apiWithProtocols(p);
			FakeRequest req = new FakeRequest();

			IProtocol<?, ?> found = ProtocolExpressions.resolveProtocol(Optional.of(api), Optional.of(req));
			assertSame(p, found);
		}
	}

	@Nested
	@DisplayName("extraction delegates")
	class Extraction {

		@Test
		@DisplayName("extractCaller delegates and returns the protocol's ICaller")
		void extractCallerDelegates() {
			ICaller caller = new Caller("t", "t", "c", null, false, false, null);
			StubProtocol p = new StubProtocol();
			p.cannedCaller = caller;
			FakeRequest req = new FakeRequest();

			ICaller result = ProtocolExpressions.extractCaller(p, req);
			assertSame(caller, result);
			assertSame(req, p.lastRequest);
		}

		@Test
		@DisplayName("extractRawBody returns the bytes from getRawBody")
		void extractRawBodyDelegates() {
			byte[] body = "hello".getBytes(StandardCharsets.UTF_8);
			StubProtocol p = new StubProtocol();
			p.cannedRawBody = body;

			byte[] result = ProtocolExpressions.extractRawBody(p, new FakeRequest());
			assertArrayEquals(body, result);
		}

		@Test
		@DisplayName("extractAuthorization returns the protocol's auth string")
		void extractAuthorizationDelegates() {
			StubProtocol p = new StubProtocol();
			p.cannedAuth = "Bearer abc";
			assertEquals("Bearer abc", ProtocolExpressions.extractAuthorization(p, new FakeRequest()));
		}

		@Test
		@DisplayName("extractContentType returns the protocol's content-type")
		void extractContentTypeDelegates() {
			StubProtocol p = new StubProtocol();
			p.cannedContentType = "application/json";
			assertEquals("application/json", ProtocolExpressions.extractContentType(p, new FakeRequest()));
		}

		@Test
		@DisplayName("extractAccept returns the protocol's accept header")
		void extractAcceptDelegates() {
			StubProtocol p = new StubProtocol();
			p.cannedAccept = "application/json, */*;q=0.5";
			assertEquals("application/json, */*;q=0.5",
					ProtocolExpressions.extractAccept(p, new FakeRequest()));
		}

		@Test
		@DisplayName("extractPath returns the protocol's path")
		void extractPathDelegates() {
			StubProtocol p = new StubProtocol();
			p.cannedPath = "/users/42";
			assertEquals("/users/42", ProtocolExpressions.extractPath(p, new FakeRequest()));
		}

		@Test
		@DisplayName("extractMethod returns the protocol's verb")
		void extractMethodDelegates() {
			StubProtocol p = new StubProtocol();
			p.cannedMethod = "POST";
			assertEquals("POST", ProtocolExpressions.extractMethod(p, new FakeRequest()));
		}

		@Test
		@DisplayName("extractQueryParameters returns the protocol's map")
		void extractQueryParametersDelegates() {
			StubProtocol p = new StubProtocol();
			p.cannedParams = Map.of("a", "1", "b", "2");
			Map<String, String> result = ProtocolExpressions.extractQueryParameters(p, new FakeRequest());
			assertEquals(2, result.size());
			assertEquals("1", result.get("a"));
			assertEquals("2", result.get("b"));
		}

		@Test
		@DisplayName("extraction throws when protocol or request is null")
		void rejectsNulls() {
			StubProtocol p = new StubProtocol();
			assertThrows(ApiException.class, () -> ProtocolExpressions.extractRawBody(null, new FakeRequest()));
			assertThrows(ApiException.class, () -> ProtocolExpressions.extractRawBody(p, null));
		}
	}

	@Nested
	@DisplayName("buildProtocolResponse")
	class BuildResponse {

		@Test
		@DisplayName("passes byte[] payload and status code through to the protocol")
		void passesBytesAndStatus() {
			StubProtocol p = new StubProtocol();
			p.cannedResponse = "OK";
			byte[] payload = "body".getBytes(StandardCharsets.UTF_8);
			FakeRequest req = new FakeRequest();

			Object result = ProtocolExpressions.buildProtocolResponse(p, req, payload, 201, "application/json");
			assertEquals("OK", result);
			assertSame(req, p.lastRequest);
			assertSame(payload, p.lastBuildOutput);
			assertEquals(201, p.lastBuildStatusCode);
			assertEquals("application/json", p.lastContentType,
					"the negotiated content type must be forwarded to the protocol");
		}

		@Test
		@DisplayName("forwards a null content type (serialize skipped) unchanged")
		void forwardsNullContentType() {
			StubProtocol p = new StubProtocol();
			p.cannedResponse = "OK";

			ProtocolExpressions.buildProtocolResponse(p, new FakeRequest(), null, 200, null);
			assertNull(p.lastContentType, "a null content type must pass through as null");
		}

		@Test
		@DisplayName("passes raw object payload (e.g. DTO) when serialize was skipped")
		void passesRawObject() {
			StubProtocol p = new StubProtocol();
			p.cannedResponse = "OK";
			Object dto = new Object();

			ProtocolExpressions.buildProtocolResponse(p, new FakeRequest(), dto, 200, null);
			assertSame(dto, p.lastBuildOutput);
		}

		@Test
		@DisplayName("defaults status code to 200 when null")
		void defaultStatusTo200() {
			StubProtocol p = new StubProtocol();
			p.cannedResponse = "OK";

			ProtocolExpressions.buildProtocolResponse(p, new FakeRequest(), null, null, null);
			assertEquals(200, p.lastBuildStatusCode);
		}

		@Test
		@DisplayName("parses status code from string")
		void parsesStringStatus() {
			StubProtocol p = new StubProtocol();
			p.cannedResponse = "OK";

			ProtocolExpressions.buildProtocolResponse(p, new FakeRequest(), null, "404", null);
			assertEquals(404, p.lastBuildStatusCode);
		}

		@Test
		@DisplayName("throws when protocol or rawRequest is null")
		void rejectsNulls() {
			StubProtocol p = new StubProtocol();
			assertThrows(ApiException.class,
					() -> ProtocolExpressions.buildProtocolResponse(null, new FakeRequest(), null, 200, null));
			assertThrows(ApiException.class,
					() -> ProtocolExpressions.buildProtocolResponse(p, null, null, 200, null));
		}
	}

	@Nested
	@DisplayName("setCallerArgs")
	class SetCallerArgs {

		@Test
		@DisplayName("writes every ICaller field onto the operation request's arg map")
		void writesAllFields() {
			IOperationRequest req = new OperationRequest(new HashMap<>());
			ICaller caller = new Caller("tenant-1", "tenant-1", "user-7", "owner-3",
					true, false, List.of("ROLE_USER", "ROLE_ADMIN"));

			ICaller returned = ProtocolExpressions.setCallerArgs(req, caller);
			assertSame(caller, returned);

			assertEquals("tenant-1", req.arg(IOperationRequest.TENANT_ID).orElse(null));
			assertEquals("tenant-1", req.arg(IOperationRequest.REQUESTED_TENANT_ID).orElse(null));
			assertEquals("user-7", req.arg(IOperationRequest.CALLER_ID).orElse(null));
			assertEquals("owner-3", req.arg(IOperationRequest.OWNER_ID).orElse(null));
			assertEquals(List.of("ROLE_USER", "ROLE_ADMIN"),
					req.arg(IOperationRequest.AUTHORITIES).orElse(null));
			assertEquals(Boolean.TRUE, req.arg(IOperationRequest.SUPER_TENANT).orElse(null));
			assertEquals(Boolean.FALSE, req.arg(IOperationRequest.SUPER_OWNER).orElse(null));
		}

		@Test
		@DisplayName("returns null and writes nothing when caller is null")
		void nullCallerIsNoOp() {
			IOperationRequest req = new OperationRequest(new HashMap<>());
			ICaller returned = ProtocolExpressions.setCallerArgs(req, null);
			assertNull(returned);
			// Only the caller-derived args could possibly be present — we expect none
			assertTrue(req.args().isEmpty() || !req.args().containsKey(IOperationRequest.CALLER_ID.name()));
		}

		@Test
		@DisplayName("throws when request is null")
		void rejectsNullRequest() {
			ICaller caller = new Caller("t", "t", "c", null, false, false, null);
			assertThrows(ApiException.class, () -> ProtocolExpressions.setCallerArgs(null, caller));
		}
	}

	@Nested
	@DisplayName("applyReadParamsFromQuery — HTTP query params → typed readAll args")
	class ApplyReadParams {

		@Test
		@DisplayName("page+size → IPageable, sort=field,desc → ISort, mode=uuid → MODE")
		void translatesAll() {
			OperationRequest req = new OperationRequest(new HashMap<>());
			Map<String, Object> qp = new HashMap<>();
			qp.put("page", "1");
			qp.put("size", "2");
			qp.put("sort", "name,desc");
			qp.put("mode", "uuid");

			Object out = ProtocolExpressions.applyReadParamsFromQuery(req, qp);
			assertSame(req, out, "returns the request unchanged");

			IPageable page = req.arg(IOperationRequest.PAGE).orElseThrow();
			assertEquals(1, page.getPageIndex());
			assertEquals(2, page.getPageSize());

			ISort sort = req.arg(IOperationRequest.SORT).orElseThrow();
			assertEquals("name", sort.getFieldName());
			assertEquals(SortDirection.desc, sort.getDirection());

			assertEquals("uuid", req.arg(IOperationRequest.MODE).orElseThrow());
		}

		@Test
		@DisplayName("sort without a direction defaults to asc; page defaults to 0 when only size is given")
		void defaults() {
			OperationRequest req = new OperationRequest(new HashMap<>());
			Map<String, Object> qp = new HashMap<>();
			qp.put("size", "5");
			qp.put("sort", "label");

			ProtocolExpressions.applyReadParamsFromQuery(req, qp);

			IPageable page = req.arg(IOperationRequest.PAGE).orElseThrow();
			assertEquals(0, page.getPageIndex());
			assertEquals(5, page.getPageSize());

			ISort sort = req.arg(IOperationRequest.SORT).orElseThrow();
			assertEquals("label", sort.getFieldName());
			assertEquals(SortDirection.asc, sort.getDirection());
		}

		@Test
		@DisplayName("no read params → no typed args set (size=0 also yields no page)")
		void noOp() {
			OperationRequest req = new OperationRequest(new HashMap<>());
			Map<String, Object> qp = new HashMap<>();
			qp.put("size", "0"); // size 0 must NOT create a page

			ProtocolExpressions.applyReadParamsFromQuery(req, qp);

			assertTrue(req.arg(IOperationRequest.PAGE).isEmpty(), "no pageable for size 0");
			assertTrue(req.arg(IOperationRequest.SORT).isEmpty());
			assertTrue(req.arg(IOperationRequest.MODE).isEmpty());
		}

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("fields=name,email → PROJECTION list (trimmed, blanks dropped)")
		void parsesFields() {
			OperationRequest req = new OperationRequest(new HashMap<>());
			Map<String, Object> qp = new HashMap<>();
			qp.put("fields", " name , , email ");

			ProtocolExpressions.applyReadParamsFromQuery(req, qp);

			List<String> projection = (List<String>) req.arg(IOperationRequest.PROJECTION).orElseThrow();
			assertEquals(List.of("name", "email"), projection,
					"comma-split, trimmed, with empty segments dropped");
		}

		@Test
		@DisplayName("absent / blank fields → no PROJECTION arg")
		void noFields() {
			OperationRequest req = new OperationRequest(new HashMap<>());
			Map<String, Object> qp = new HashMap<>();
			qp.put("fields", "   ");

			ProtocolExpressions.applyReadParamsFromQuery(req, qp);

			assertTrue(req.arg(IOperationRequest.PROJECTION).isEmpty(), "a blank fields param sets no projection");
		}

		private static com.garganttua.api.commons.filter.IFilter filterOf(String query) {
			OperationRequest req = new OperationRequest(new HashMap<>());
			Map<String, Object> qp = new HashMap<>();
			qp.put("filter", query);
			ProtocolExpressions.applyReadParamsFromQuery(req, qp);
			return req.arg(IOperationRequest.FILTER).orElse(null);
		}

		@Test
		@DisplayName("filter=name:eq:Alice → $field(name) → $eq(Alice)")
		void parsesSingleEq() {
			com.garganttua.api.commons.filter.IFilter f = filterOf("name:eq:Alice");
			assertEquals("$field", f.getName());
			assertEquals("name", f.getValue());
			com.garganttua.api.commons.filter.IFilter op = f.getFilters().get(0);
			assertEquals("$eq", op.getName());
			assertEquals("Alice", op.getValue());
		}

		@Test
		@DisplayName("multi-clause filter=name:eq:Alice;age:gt:30 → $and; numeric value coerced to Long")
		void parsesMultiClauseAnd() {
			com.garganttua.api.commons.filter.IFilter f = filterOf("name:eq:Alice;age:gt:30");
			assertEquals("$and", f.getName());
			assertEquals(2, f.getFilters().size());

			com.garganttua.api.commons.filter.IFilter ageField = f.getFilters().get(1);
			assertEquals("$field", ageField.getName());
			assertEquals("age", ageField.getValue());
			com.garganttua.api.commons.filter.IFilter gt = ageField.getFilters().get(0);
			assertEquals("$gt", gt.getName());
			assertEquals(30L, gt.getValue(), "a numeric filter value is coerced to Long so a numeric field matches");
		}

		@Test
		@DisplayName("filter=enabled:eq:true → boolean value coerced to Boolean.TRUE")
		void coercesBoolean() {
			com.garganttua.api.commons.filter.IFilter f = filterOf("enabled:eq:true");
			assertEquals(Boolean.TRUE, f.getFilters().get(0).getValue());
		}

		@Test
		@DisplayName("filter=url:eq:http://x.io → value may contain colons (split on the first two only)")
		void valueKeepsColons() {
			com.garganttua.api.commons.filter.IFilter f = filterOf("url:eq:http://x.io");
			assertEquals("url", f.getValue());
			assertEquals("http://x.io", f.getFilters().get(0).getValue());
		}

		@Test
		@DisplayName("filter=role:in:admin,user → $in with the comma-separated values")
		void parsesIn() {
			com.garganttua.api.commons.filter.IFilter f = filterOf("role:in:admin,user");
			assertEquals("$field", f.getName());
			assertEquals("role", f.getValue());
			com.garganttua.api.commons.filter.IFilter in = f.getFilters().get(0);
			assertEquals("$in", in.getName());
			assertEquals(2, in.getFilters().size(), "two values in the $in list");
			assertEquals("admin", in.getFilters().get(0).getValue());
			assertEquals("user", in.getFilters().get(1).getValue());
		}

		@Test
		@DisplayName("filter=deletedAt:empty → $empty (no value)")
		void parsesEmpty() {
			com.garganttua.api.commons.filter.IFilter f = filterOf("deletedAt:empty:");
			assertEquals("$field", f.getName());
			assertEquals("deletedAt", f.getValue());
			assertEquals("$empty", f.getFilters().get(0).getName());
		}

		@Test
		@DisplayName("absent / blank / unknown-operator filter → no FILTER arg")
		void noFilter() {
			assertNull(filterOf("   "), "a blank filter sets nothing");
			assertNull(filterOf("name:bogus:x"), "an unknown operator is skipped → no filter");
		}

		// ----- JSON (Mongo-like) syntax -----

		@Test
		@DisplayName("JSON filter={\"name\":\"Alice\"} → $field(name) → $eq(Alice)")
		void jsonScalarEq() {
			com.garganttua.api.commons.filter.IFilter f = filterOf("{\"name\":\"Alice\"}");
			assertEquals("$field", f.getName());
			assertEquals("name", f.getValue());
			assertEquals("$eq", f.getFilters().get(0).getName());
			assertEquals("Alice", f.getFilters().get(0).getValue());
		}

		@Test
		@DisplayName("JSON {\"age\":{\"$gte\":18,\"$lt\":65}} → $and(gte,lt); JSON numbers keep their type (Long)")
		void jsonOperatorObject() {
			com.garganttua.api.commons.filter.IFilter f = filterOf("{\"age\":{\"$gte\":18,\"$lt\":65}}");
			assertEquals("$and", f.getName(), "two operators on one field are AND-combined");
			assertEquals(2, f.getFilters().size());
			com.garganttua.api.commons.filter.IFilter gte = f.getFilters().get(0).getFilters().get(0);
			assertEquals("$gte", gte.getName());
			assertEquals(18L, gte.getValue(), "a JSON integer is a Long, not a String");
		}

		@Test
		@DisplayName("JSON {\"$or\":[{\"a\":1},{\"b\":2}]} → $or with two sub-filters")
		void jsonOr() {
			com.garganttua.api.commons.filter.IFilter f = filterOf("{\"$or\":[{\"a\":1},{\"b\":2}]}");
			assertEquals("$or", f.getName());
			assertEquals(2, f.getFilters().size());
			assertEquals("a", f.getFilters().get(0).getValue());
			assertEquals("b", f.getFilters().get(1).getValue());
		}

		@Test
		@DisplayName("JSON {\"role\":[\"admin\",\"user\"]} → $in shorthand; JSON {\"x\":true} → Boolean")
		void jsonArrayShorthandAndBoolean() {
			com.garganttua.api.commons.filter.IFilter in = filterOf("{\"role\":[\"admin\",\"user\"]}");
			assertEquals("$in", in.getFilters().get(0).getName());
			assertEquals(2, in.getFilters().get(0).getFilters().size());

			com.garganttua.api.commons.filter.IFilter b = filterOf("{\"enabled\":true}");
			assertEquals(Boolean.TRUE, b.getFilters().get(0).getValue());
		}

		@Test
		@DisplayName("JSON geospatial {\"location\":{\"$geoWithin\":{Polygon}}} → $geoWithin carrying a parsed GeoJSON geometry")
		void jsonGeoWithin() {
			String poly = "{\"location\":{\"$geoWithin\":{\"type\":\"Polygon\",\"coordinates\":"
					+ "[[[0,0],[0,4],[4,4],[4,0],[0,0]]]}}}";
			com.garganttua.api.commons.filter.IFilter f = filterOf(poly);
			assertEquals("$field", f.getName());
			assertEquals("location", f.getValue());
			com.garganttua.api.commons.filter.IFilter geo = f.getFilters().get(0);
			assertEquals("$geoWithin", geo.getName());
			assertInstanceOf(org.geojson.Polygon.class, geo.getValue(),
					"the geometry must be parsed into an org.geojson geometry, not left as raw JSON");
		}

		@Test
		@DisplayName("malformed JSON filter raises a parlant ApiException")
		void jsonMalformedThrows() {
			ApiException ex = assertThrows(ApiException.class, () -> filterOf("{not valid json"));
			assertTrue(ex.getMessage().contains("Invalid JSON filter"), "got: " + ex.getMessage());
		}
	}
}
