package com.garganttua.api.core.integ.protocol;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.MimeType;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.protocol.IProtocol;
import com.garganttua.api.commons.serialization.ISerializer;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.core.reflection.IClass;

/**
 * Pins what {@code Domain.invoke} reports for a Mode-A error: the
 * {@link IOperationResponse} carries the real failure (a non-OK code + the
 * exception message) even though the Mode-A RESPONSE stage cannot set it on the
 * wire (the pipeline does not yet write an {@code exitCode}). The Javalin
 * transport relies on exactly this {@code IOperationResponse} to author a correct
 * HTTP status + body rather than the always-200 default.
 */
@DisplayName("Mode-A error: Domain.invoke surfaces the failure on the IOperationResponse")
class ErrorResponseReproTest extends AbstractCrudIntegrationTest {

	static class JsonOnlySerializer implements ISerializer {
		@Override public MimeType mimeType() { return MimeType.APPLICATION_JSON; }
		@Override public byte[] serialize(Object o) {
			return (o instanceof List<?> l ? "count=" + l.size() : String.valueOf(o)).getBytes(StandardCharsets.UTF_8);
		}
		@Override public <T> T deserialize(byte[] d, IClass<T> t) { throw new UnsupportedOperationException(); }
	}

	static class SuperProtocol implements IProtocol<FakeHttpRequest, Object> {
		@Override public IClass<FakeHttpRequest> requestType() { return IClass.getClass(FakeHttpRequest.class); }
		@Override public ICaller getCaller(FakeHttpRequest r) {
			return new Caller(r.tenantId(), r.tenantId(), r.callerId(), null, true, true, List.of());
		}
		@Override public byte[] getRawBody(FakeHttpRequest r) { return r.body(); }
		@Override public String getAuthorization(FakeHttpRequest r) { return r.authorization(); }
		@Override public String getContentType(FakeHttpRequest r) { return r.contentType(); }
		@Override public String getAccept(FakeHttpRequest r) { return r.accept(); }
		@Override public String getPath(FakeHttpRequest r) { return r.path(); }
		@Override public String getMethod(FakeHttpRequest r) { return r.method(); }
		@Override public Map<String, String> getQueryParameters(FakeHttpRequest r) { return r.queryParameters(); }
		@Override public Object buildResponse(FakeHttpRequest r, Object output, int statusCode) { return output; }
	}

	private IDomain<?> userCtx;

	@BeforeEach
	void setUp() throws ApiException {
		IApiBuilder builder = newBuilder();
		builder.serializer(new JsonOnlySerializer());
		builder.protocol(new SuperProtocol());
		builder.domain(IClass.getClass(User.class))
				.tenant(true).superTenant("superTenant")
				.entity().id("id").uuid("uuid").tenantId("tenantId").up()
				.dto(IClass.getClass(UserDto.class)).id("id").uuid("uuid").tenantId("tenantId").db(new CapturingDao()).up()
			.up();
		IApi context = buildAndStart(builder);
		userCtx = context.getDomain("users").orElseThrow();
	}

	@Test
	@DisplayName("an unsatisfiable Accept fails the invoke with the serializer error message")
	void badAcceptFailsInvoke() {
		// readAll with Accept: application/xml — only JSON is registered → serialize fails.
		FakeHttpRequest http = new FakeHttpRequest(
				"/users", "GET", null, null, "application/xml",
				"Bearer x", "SUPER_TENANT", "caller-1", Map.of());

		OperationRequest req = new OperationRequest(new HashMap<>());
		req.arg(IOperationRequest.OPERATION,
				OperationDefinition.readAllWithStandardSecurity("users", IClass.getClass(User.class)));
		req.arg(IOperationRequest.TENANT_ID, "SUPER_TENANT");
		req.arg(IOperationRequest.REQUESTED_TENANT_ID, "SUPER_TENANT");
		req.arg(IOperationRequest.SUPER_TENANT, true);
		req.arg(IOperationRequest.SUPER_OWNER, true);
		req.arg("rawRequest", http);

		IOperationResponse resp = userCtx.invoke(req);

		assertNotEquals(OperationResponseCode.OK, resp.getResponseCode(),
				"an unsatisfiable Accept must fail the operation, not report OK");
		assertInstanceOf(Throwable.class, resp.getResponse(),
				"the failure must carry a Throwable so the transport can surface its message");
		assertTrue(((Throwable) resp.getResponse()).getMessage().contains("No acceptable serializer"),
				"the message must name the negotiation failure; got: " + resp.getResponse());
	}
}
