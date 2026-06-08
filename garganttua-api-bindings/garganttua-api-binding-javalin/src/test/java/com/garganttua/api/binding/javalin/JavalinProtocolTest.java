package com.garganttua.api.binding.javalin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.caller.ICaller;

import io.javalin.http.Context;
import io.javalin.http.HandlerType;

@DisplayName("JavalinProtocol — Context ↔ pipeline adapter")
class JavalinProtocolTest {

	private Context ctx;
	private JavalinProtocol protocol;

	@BeforeEach
	void setUp() {
		ctx = mock(Context.class);
		protocol = new JavalinProtocol();
	}

	@Nested
	@DisplayName("requestType")
	class RequestType {
		@Test
		@DisplayName("routes on io.javalin.http.Context")
		void routesOnContext() {
			assertTrue(protocol.requestType().represents(Context.class),
					"the protocol must declare Context as its request type");
		}
	}

	@Nested
	@DisplayName("getCaller")
	class GetCaller {
		@Test
		@DisplayName("no identifying header → anonymous caller")
		void anonymousWhenNoHeaders() {
			when(ctx.header(anyString())).thenReturn(null);

			ICaller caller = protocol.getCaller(ctx);

			assertTrue(caller.anonymous(), "a header-less request must yield an anonymous caller");
			assertNull(caller.callerId());
			assertNull(caller.tenantId());
			assertFalse(caller.superTenant());
			assertFalse(caller.superOwner());
			// The framework's Caller.createAnonymousCaller() carries null authorities by convention.
			assertNull(caller.authorities(), "anonymous caller authorities are null per the framework factory");
		}

		@Test
		@DisplayName("X-Tenant-Id / X-Owner-Id / X-Caller-Id are read; super flags never trusted")
		void readsHeaders() {
			when(ctx.header(JavalinProtocol.TENANT_HEADER)).thenReturn("T1");
			when(ctx.header(JavalinProtocol.OWNER_HEADER)).thenReturn("O1");
			when(ctx.header(JavalinProtocol.CALLER_HEADER)).thenReturn("C1");

			ICaller caller = protocol.getCaller(ctx);

			assertFalse(caller.anonymous(), "a caller id present means not anonymous");
			assertEquals("T1", caller.tenantId());
			assertEquals("T1", caller.requestedTenantId());
			assertEquals("O1", caller.ownerId());
			assertEquals("C1", caller.callerId());
			assertFalse(caller.superTenant(), "transport must NOT assert superTenant");
			assertFalse(caller.superOwner(), "transport must NOT assert superOwner");
		}
	}

	@Nested
	@DisplayName("getRawBody")
	class GetRawBody {
		@Test
		@DisplayName("non-empty body is returned verbatim")
		void nonEmptyBody() {
			byte[] payload = "Alice|alice@x.io".getBytes(StandardCharsets.UTF_8);
			when(ctx.bodyAsBytes()).thenReturn(payload);

			assertArrayEquals(payload, protocol.getRawBody(ctx));
		}

		@Test
		@DisplayName("empty body → null (bodyless request contract)")
		void emptyBodyIsNull() {
			when(ctx.bodyAsBytes()).thenReturn(new byte[0]);
			assertNull(protocol.getRawBody(ctx), "an empty body must surface as null, not an empty array");
		}
	}

	@Nested
	@DisplayName("header passthrough")
	class Headers {
		@Test
		@DisplayName("authorization / content-type / accept come from the matching headers")
		void headerPassthrough() {
			when(ctx.header("Authorization")).thenReturn("Bearer xyz");
			when(ctx.header("Content-Type")).thenReturn("application/json");
			when(ctx.header("Accept")).thenReturn("application/xml");

			assertEquals("Bearer xyz", protocol.getAuthorization(ctx));
			assertEquals("application/json", protocol.getContentType(ctx));
			assertEquals("application/xml", protocol.getAccept(ctx));
		}
	}

	@Nested
	@DisplayName("path / method")
	class PathMethod {
		@Test
		@DisplayName("path passes through; method is the verb name")
		void pathAndMethod() {
			when(ctx.path()).thenReturn("/users/42");
			when(ctx.method()).thenReturn(HandlerType.POST);

			assertEquals("/users/42", protocol.getPath(ctx));
			assertEquals("POST", protocol.getMethod(ctx),
					"method must be the canonical verb name from HandlerType");
		}
	}

	@Nested
	@DisplayName("getQueryParameters")
	class QueryParams {
		@Test
		@DisplayName("multi-value entries collapse to the first value")
		void collapsesToFirst() {
			when(ctx.queryParamMap()).thenReturn(Map.of(
					"page", List.of("3", "99"),
					"sort", List.of("name")));

			Map<String, String> params = protocol.getQueryParameters(ctx);

			assertEquals("3", params.get("page"), "first value of a multi-valued param wins");
			assertEquals("name", params.get("sort"));
			assertEquals(2, params.size());
		}

		@Test
		@DisplayName("empty query map → empty result")
		void emptyMap() {
			when(ctx.queryParamMap()).thenReturn(Map.of());
			assertTrue(protocol.getQueryParameters(ctx).isEmpty());
		}
	}

	@Nested
	@DisplayName("buildResponse")
	class BuildResponse {
		@Test
		@DisplayName("byte[] output is written as the result with the given status")
		void writesBytes() {
			byte[] body = "Alice|alice@x.io".getBytes(StandardCharsets.UTF_8);

			Context returned = protocol.buildResponse(ctx, body, 201);

			assertSame(ctx, returned, "buildResponse must return the same Context it wrote to");
			verify(ctx).status(201);
			verify(ctx).result(body);
		}

		@Test
		@DisplayName("non-byte output is stringified")
		void stringifiesNonBytes() {
			protocol.buildResponse(ctx, 12345, 200);

			verify(ctx).status(200);
			verify(ctx).result("12345");
		}

		@Test
		@DisplayName("null output writes status only, no body")
		void nullOutputStatusOnly() {
			protocol.buildResponse(ctx, null, 204);

			verify(ctx).status(204);
			verify(ctx, never()).result(any(byte[].class));
			verify(ctx, never()).result(anyString());
		}

		@Test
		@DisplayName("the negotiated content type labels the response before the body is written")
		void labelsContentType() {
			byte[] body = "[{\"id\":\"acme\"}]".getBytes(StandardCharsets.UTF_8);

			protocol.buildResponse(ctx, body, 200, "application/json");

			verify(ctx).status(200);
			verify(ctx).contentType("application/json");
			verify(ctx).result(body);
		}

		@Test
		@DisplayName("a null/blank content type leaves Javalin's default — no contentType call")
		void nullContentTypeNotSet() {
			protocol.buildResponse(ctx, "x".getBytes(StandardCharsets.UTF_8), 200, null);
			protocol.buildResponse(ctx, "y".getBytes(StandardCharsets.UTF_8), 200, "  ");

			verify(ctx, never()).contentType(anyString());
		}

		@Test
		@DisplayName("the legacy 3-arg overload still works (no content type)")
		void threeArgStillWorks() {
			byte[] body = "z".getBytes(StandardCharsets.UTF_8);

			Context returned = protocol.buildResponse(ctx, body, 201);

			assertSame(ctx, returned);
			verify(ctx).status(201);
			verify(ctx).result(body);
			verify(ctx, never()).contentType(anyString());
		}
	}
}
