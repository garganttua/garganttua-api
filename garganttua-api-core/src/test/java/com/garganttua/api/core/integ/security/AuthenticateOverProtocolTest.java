package com.garganttua.api.core.integ.security;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.integ.crud.AbstractCrudScriptTest;
import com.garganttua.api.core.integ.protocol.FakeHttpRequest;
import com.garganttua.api.core.security.authentication.AuthenticateCredentialsSupplierBuilder;
import com.garganttua.api.core.security.authentication.AuthenticationRequest;
import com.garganttua.api.core.security.authentication.AuthenticatorDefinitionSupplierBuilder;
import com.garganttua.api.core.security.authentication.PrincipalSupplierBuilder;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.MimeType;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.protocol.IProtocol;
import com.garganttua.api.commons.security.authentication.IAuthentication;
import com.garganttua.api.commons.security.authenticator.AuthenticatorScope;
import com.garganttua.api.commons.serialization.ISerializer;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;

/**
 * End-to-end proof that the authenticate operation is reachable over a Mode-A
 * (HTTP-shaped) transport: the credentials body is deserialized into an
 * {@link AuthenticationRequest} (via {@code resolveBodyType} now special-casing
 * {@code OperationType.authentication}) and fed to {@code AUTHENTICATE.gs}, which
 * runs the authenticator and returns a result — the JavalinInterface login route
 * relies on exactly this.
 */
@DisplayName("Authenticate over a Mode-A protocol (HTTP login)")
class AuthenticateOverProtocolTest extends AbstractCrudScriptTest {

	record FakeHttpResponse(int statusCode, byte[] body, String contentType) {}

	static class FakeHttpProtocol implements IProtocol<FakeHttpRequest, FakeHttpResponse> {
		@Override public IClass<FakeHttpRequest> requestType() { return IClass.getClass(FakeHttpRequest.class); }
		@Override public ICaller getCaller(FakeHttpRequest r) {
			return new com.garganttua.api.core.caller.Caller(r.tenantId(), r.tenantId(), r.callerId(), null, false, false, java.util.List.of());
		}
		@Override public byte[] getRawBody(FakeHttpRequest r) { return r.body(); }
		@Override public String getAuthorization(FakeHttpRequest r) { return r.authorization(); }
		@Override public String getContentType(FakeHttpRequest r) { return r.contentType(); }
		@Override public String getAccept(FakeHttpRequest r) { return r.accept(); }
		@Override public String getPath(FakeHttpRequest r) { return r.path(); }
		@Override public String getMethod(FakeHttpRequest r) { return r.method(); }
		@Override public Map<String, String> getQueryParameters(FakeHttpRequest r) { return r.queryParameters(); }
		@Override public FakeHttpResponse buildResponse(FakeHttpRequest request, Object output, int statusCode, String contentType) {
			byte[] body = (output instanceof byte[] b) ? b
					: (output == null ? new byte[0] : String.valueOf(output).getBytes(StandardCharsets.UTF_8));
			return new FakeHttpResponse(statusCode, body, contentType);
		}
		@Override public FakeHttpResponse buildResponse(FakeHttpRequest request, Object output, int statusCode) {
			return buildResponse(request, output, statusCode, null);
		}
	}

	/** Serializer that reads a pipe-form credentials body into an AuthenticationRequest. */
	static class CredsSerializer implements ISerializer {
		@Override public MimeType mimeType() { return MimeType.APPLICATION_JSON; }
		@Override public byte[] serialize(Object object) {
			if (object instanceof IAuthentication a) {
				return ("authenticated=" + a.authenticated()).getBytes(StandardCharsets.UTF_8);
			}
			return object == null ? new byte[0] : object.toString().getBytes(StandardCharsets.UTF_8);
		}
		@Override
		@SuppressWarnings("unchecked")
		public <T> T deserialize(byte[] data, IClass<T> type) throws ApiException {
			if (!AuthenticationRequest.class.getName().equals(type.getName())) {
				throw new ApiException("CredsSerializer only deserializes AuthenticationRequest, got " + type.getName());
			}
			String[] parts = new String(data, StandardCharsets.UTF_8).split("\\|", -1);
			if (parts.length < 3) throw new ApiException("Malformed credentials body");
			// Credentials as a STRING — exactly how a JSON body deserializes the
			// Object credentials field over HTTP (never a byte[]). The supplier must
			// accept this and encode it to UTF-8 for the login+password authenticator.
			return (T) new AuthenticationRequest(parts[0], parts[1]);
		}
	}

	private IApi context;
	private IDomain<?> userCtx;
	private CapturingDao userDao;
	private FakeHttpProtocol protocol;

	@BeforeEach
	void setUp() throws ApiException {
		userDao = new CapturingDao();
		protocol = new FakeHttpProtocol();
		StubAuthentication stubAuth = new StubAuthentication();

		IApiBuilder builder = newBuilder();
		builder.serializer(new CredsSerializer());
		builder.protocol(protocol);

		var authBuilder = builder.security()
				.authentication(new FixedSupplierBuilder<>(stubAuth, IClass.getClass(StubAuthentication.class)));
		authBuilder.authenticate("authenticate")
				.withParam(0, new PrincipalSupplierBuilder())
				.withParam(1, new AuthenticateCredentialsSupplierBuilder())
				.withParam(2, new AuthenticatorDefinitionSupplierBuilder());
		authBuilder.up();

		builder.domain(IClass.getClass(User.class))
				.tenant(true)
				.superTenant("superTenant")
				.entity().id("id").uuid("uuid").tenantId("tenantId").up()
				.dto(IClass.getClass(UserDto.class)).id("id").uuid("uuid").tenantId("tenantId").db(userDao).up()
				.security()
					.authenticator()
						.login("id")
						.scope(AuthenticatorScope.tenant)
						.enabled("enabled").accountNonLocked("accountNonLocked")
						.accountNonExpired("accountNonExpired").credentialsNonExpired("credentialsNonExpired")
						.alwaysEnabled(true)
						.authentication(authBuilder)
					.up()
				.up()
			.up();

		context = buildAndStart(builder);
		userCtx = context.getDomain("users").orElseThrow();

		UserDto john = new UserDto();
		john.setId("john@example.com");
		john.setUuid("user-uuid-1");
		john.setTenantId("SUPER_TENANT");
		john.setName("John");
		userDao.save(john);
	}

	@Test
	@DisplayName("POST-shaped /users/authenticate deserializes credentials and authenticates")
	void httpLoginAuthenticates() {
		// The credentials travel in the body — the protocol + the special-cased
		// resolveBodyType turn them into an AuthenticationRequest for AUTHENTICATE.gs.
		FakeHttpRequest http = new FakeHttpRequest(
				"/users/authenticate", "POST",
				"john@example.com|valid-password|SUPER_TENANT".getBytes(StandardCharsets.UTF_8),
				"application/json", "application/json",
				null, "SUPER_TENANT", null, Map.of());

		OperationRequest req = new OperationRequest(new HashMap<>());
		req.arg(IOperationRequest.OPERATION,
				OperationDefinition.authenticate("users", IClass.getClass(User.class)));
		req.arg("rawRequest", http);

		IOperationResponse resp = userCtx.invoke(req);

		assertInstanceOf(FakeHttpResponse.class, resp.getResponse(),
				"Mode-A authenticate must produce a transport response; got " + resp.getResponse());
		FakeHttpResponse httpResp = (FakeHttpResponse) resp.getResponse();
		assertEquals(200, httpResp.statusCode(),
				"a valid login must succeed; body=" + new String(httpResp.body(), StandardCharsets.UTF_8));
		assertEquals("authenticated=true", new String(httpResp.body(), StandardCharsets.UTF_8),
				"the body must be the serialized successful authentication");
		assertEquals("application/json", httpResp.contentType(),
				"the response must be labelled with the negotiated media type");
	}

	@Test
	@DisplayName("the authenticate operation is exposed in the domain's configured operations")
	void authenticateIsExposedInOperations() {
		boolean exposed = userCtx.getDomainDefinition().operations().stream()
				.anyMatch(op -> op.getBusinessOperation()
						== com.garganttua.api.commons.operation.BusinessOperation.authenticate);
		assertTrue(exposed,
				"operations() must include authenticate so transports (e.g. JavalinInterface) can route it");
	}

	@Test
	@DisplayName("wrong password over the protocol does not authenticate")
	void httpLoginWrongPassword() {
		FakeHttpRequest http = new FakeHttpRequest(
				"/users/authenticate", "POST",
				"john@example.com|WRONG|SUPER_TENANT".getBytes(StandardCharsets.UTF_8),
				"application/json", "application/json",
				null, "SUPER_TENANT", null, Map.of());

		OperationRequest req = new OperationRequest(new HashMap<>());
		req.arg(IOperationRequest.OPERATION,
				OperationDefinition.authenticate("users", IClass.getClass(User.class)));
		req.arg("rawRequest", http);

		IOperationResponse resp = userCtx.invoke(req);

		// The authenticator ran (the credentials reached it via the deserialized
		// AuthenticationRequest) but rejected the password — the login is not OK.
		assertNotEquals(com.garganttua.api.commons.service.OperationResponseCode.OK, resp.getResponseCode(),
				"a wrong password must not yield a successful login; got " + resp.getResponse());
	}
}
