package com.garganttua.api.core.integ.authorization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.operation.OperationType;
import com.garganttua.api.commons.operation.Scope;
import com.garganttua.api.commons.operation.TechnicalOperation;
import com.garganttua.api.commons.security.authorization.IAuthorizationProtocol;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.core.reflection.IClass;

@DisplayName("VERIFY_AUTHORIZATION with @AuthorizationProtocol decoders")
class AuthorizationProtocolIntegrationTest extends AbstractCrudIntegrationTest {

	/** Minimal authorization entity for the tests. No interface contract — the
	 *  framework treats it as a POJO, validation comes from the DSL. */
	static class CannedAuth {
		final String marker;
		CannedAuth(String marker) { this.marker = marker; }
	}

	/** Records every call so tests can assert delegation. */
	static class RecordingProtocol implements IAuthorizationProtocol {
		final String scheme;
		final AtomicInteger decodeCount = new AtomicInteger();
		String lastValue;
		Object canned;
		boolean throwOnDecode;

		RecordingProtocol(String scheme, Object canned) {
			this.scheme = scheme;
			this.canned = canned;
		}

		@Override public String scheme() { return scheme; }
		@Override public IClass<?> targetDomain() { return IClass.getClass(User.class); }

		@Override
		public Object decode(String value, IApi api) throws ApiException {
			this.lastValue = value;
			this.decodeCount.incrementAndGet();
			if (throwOnDecode) throw new ApiException("decode failed for scheme " + scheme);
			return canned;
		}
	}

	private RecordingProtocol bearer;
	private RecordingProtocol basic;
	private CapturingDao dao;

	@BeforeEach
	void setUp() {
		bearer = new RecordingProtocol("Bearer", new CannedAuth("bearer-token"));
		basic = new RecordingProtocol("Basic", new CannedAuth("basic-creds"));
		dao = new CapturingDao();
	}

	private IApi lastBuilt;

	private IDomain<?> buildDomain(boolean securityEnabled) throws ApiException {
		IApiBuilder builder = newBuilder();
		builder.authorizationProtocol(bearer);
		builder.authorizationProtocol(basic);

		var domainBuilder = builder.domain(IClass.getClass(User.class))
				.tenant(true)
				.superTenant("superTenant")
				.entity().id("id").uuid("uuid").tenantId("tenantId").up()
				.dto(IClass.getClass(UserDto.class))
					.id("id").uuid("uuid").tenantId("tenantId")
					.db(dao)
				.up();

		if (securityEnabled) {
			domainBuilder.security()
					.disable(false)
					.readOneAccess(Access.authenticated)
				.up();
		} else {
			// Security is ON by default now; this branch tests the no-gate path,
			// so opt out explicitly.
			domainBuilder.security().disable(true).up();
		}
		domainBuilder.up();

		this.lastBuilt = buildAndStart(builder);
		return this.lastBuilt.getDomain("users").orElseThrow();
	}

	private OperationRequest readOneRequest(String rawAuthorization) {
		// Seed an entity to avoid 404 noise. Use the same tenantId as the
		// caller below so the request reaches the entity.
		User seeded = new User();
		seeded.setUuid("u-1");
		seeded.setTenantId("acme");
		seeded.setName("alice");
		dao.getStorage().add(seeded);

		// Non-super-tenant caller so VERIFY_AUTHORIZATION actually runs and
		// the decoder paths under test get exercised (super-tenant would
		// bypass the entire script). Operation has authority=false so the
		// downstream VERIFY_AUTHORITY check doesn't 403 us — this suite is
		// about decoder routing, not authority enforcement.
		OperationRequest req = tenantRequest(
				OperationDefinition.readOne("users", IClass.getClass(User.class),
						false, null, com.garganttua.api.commons.operation.Access.authenticated),
				"acme");
		req.arg("type", "uuid");
		req.arg("identifier", "u-1");
		if (rawAuthorization != null) {
			req.arg("rawAuthorization", rawAuthorization);
		}
		return req;
	}

	private OperationDefinition anonymousReadOne() {
		return new OperationDefinition("users", TechnicalOperation.read, IClass.getClass(User.class),
				Scope.oneEntity, OperationType.standard, false, null, Access.anonymous);
	}

	@Nested
	@DisplayName("Decoding flow")
	class DecodingFlow {

		@Test
		@DisplayName("Bearer token in rawAuthorization → decoded by Bearer protocol, authorization populated")
		void bearerDecoded() throws ApiException {
			IDomain<?> ctx = buildDomain(true);
			OperationRequest req = readOneRequest("Bearer xyz.signed.payload");

			IOperationResponse resp = ctx.invoke(req);

			assertEquals(OperationResponseCode.OK, resp.getResponseCode(),
					() -> "Got: " + resp.getResponse());
			assertEquals(1, bearer.decodeCount.get(), "Bearer.decode should be invoked once");
			assertEquals("xyz.signed.payload", bearer.lastValue, "Decoder receives the post-scheme value");
			assertEquals(0, basic.decodeCount.get(), "Basic.decode must not be invoked");
		}

		@Test
		@DisplayName("Basic header → routed to Basic protocol by scheme matching")
		void basicRouted() throws ApiException {
			IDomain<?> ctx = buildDomain(true);
			OperationRequest req = readOneRequest("Basic dXNlcjpwd2Q=");

			IOperationResponse resp = ctx.invoke(req);

			assertEquals(OperationResponseCode.OK, resp.getResponseCode(),
					() -> "Got: " + resp.getResponse());
			assertEquals(1, basic.decodeCount.get(), "Basic.decode should be invoked once");
			assertEquals("dXNlcjpwd2Q=", basic.lastValue);
			assertEquals(0, bearer.decodeCount.get());
		}

		@Test
		@DisplayName("Scheme matching is case-insensitive")
		void caseInsensitiveScheme() throws ApiException {
			IDomain<?> ctx = buildDomain(true);
			OperationRequest req = readOneRequest("BEARER xyz");

			IOperationResponse resp = ctx.invoke(req);

			assertEquals(OperationResponseCode.OK, resp.getResponseCode());
			assertEquals(1, bearer.decodeCount.get());
		}
	}

	@Nested
	@DisplayName("Skip cases")
	class SkipCases {

		@Test
		@DisplayName("Pre-populated authorization (Mode B) → decoder is NOT invoked")
		void prePopulatedAuthorization() throws ApiException {
			IDomain<?> ctx = buildDomain(true);
			OperationRequest req = readOneRequest("Bearer xyz");
			// Caller has already decoded the authorization
			req.arg("authorization", new CannedAuth("client-supplied"));

			IOperationResponse resp = ctx.invoke(req);

			assertEquals(OperationResponseCode.OK, resp.getResponseCode());
			assertEquals(0, bearer.decodeCount.get(),
					"When authorization is pre-populated, no decoder should run");
			assertEquals(0, basic.decodeCount.get());
		}

		@Test
		@DisplayName("Anonymous operation + no header → no decoder invoked, pipeline succeeds")
		void anonymousNoHeader() throws ApiException {
			IDomain<?> ctx = buildDomain(true);
			OperationRequest req = superTenantRequest(anonymousReadOne());
			req.arg("type", "uuid");
			req.arg("identifier", "u-anon");

			User seeded = new User();
			seeded.setUuid("u-anon");
			seeded.setTenantId("SUPER_TENANT");
			dao.getStorage().add(seeded);

			IOperationResponse resp = ctx.invoke(req);
			assertEquals(OperationResponseCode.OK, resp.getResponseCode());
			assertEquals(0, bearer.decodeCount.get());
			assertEquals(0, basic.decodeCount.get());
		}

		@Test
		@DisplayName("No security enabled → decoder is NOT invoked even with rawAuthorization present")
		void noSecurityNoDecode() throws ApiException {
			IDomain<?> ctx = buildDomain(false);
			OperationRequest req = readOneRequest("Bearer xyz");

			IOperationResponse resp = ctx.invoke(req);
			assertEquals(OperationResponseCode.OK, resp.getResponseCode());
			assertEquals(0, bearer.decodeCount.get(),
					"Without security stage, VERIFY_AUTHORIZATION isn't even wired");
		}
	}

	@Nested
	@DisplayName("Error cases")
	class ErrorCases {

		@Test
		@DisplayName("Non-anonymous op + no header → 401, decoder not invoked")
		void missingHeader() throws ApiException {
			IDomain<?> ctx = buildDomain(true);
			OperationRequest req = readOneRequest(null);

			IOperationResponse resp = ctx.invoke(req);
			assertEquals(OperationResponseCode.UNAUTHORIZED, resp.getResponseCode(),
					() -> "Expected 401, got: " + resp.getResponseCode() + " / " + resp.getResponse());
			assertEquals(0, bearer.decodeCount.get());
			assertEquals(0, basic.decodeCount.get());
		}

		@Test
		@DisplayName("Unknown scheme → 401, no decoder invoked")
		void unknownScheme() throws ApiException {
			IDomain<?> ctx = buildDomain(true);
			OperationRequest req = readOneRequest("Digest abc=");

			IOperationResponse resp = ctx.invoke(req);
			assertEquals(OperationResponseCode.UNAUTHORIZED, resp.getResponseCode(),
					() -> "Expected 401, got: " + resp.getResponseCode());
			assertEquals(0, bearer.decodeCount.get());
			assertEquals(0, basic.decodeCount.get());
		}

		@Test
		@DisplayName("Malformed header (no whitespace separator) → 400")
		void malformedHeader() throws ApiException {
			IDomain<?> ctx = buildDomain(true);
			OperationRequest req = readOneRequest("BearerNoSpace");

			IOperationResponse resp = ctx.invoke(req);
			assertEquals(OperationResponseCode.CLIENT_ERROR, resp.getResponseCode(),
					() -> "Expected 400, got: " + resp.getResponseCode());
			assertEquals(0, bearer.decodeCount.get());
		}

		@Test
		@DisplayName("Decoder throws → 401 (decode failure mapped via the script)")
		void decoderThrows() throws ApiException {
			bearer.throwOnDecode = true;
			IDomain<?> ctx = buildDomain(true);
			OperationRequest req = readOneRequest("Bearer poison");

			IOperationResponse resp = ctx.invoke(req);
			assertEquals(OperationResponseCode.UNAUTHORIZED, resp.getResponseCode());
			assertEquals(1, bearer.decodeCount.get(), "Decoder must have been invoked once");
		}
	}

	@Nested
	@DisplayName("Optional authentication on anonymous operations")
	class OptionalAuthenticationOnAnonymous {

		private OperationRequest anonymousRequest(String rawAuthorization) {
			User seeded = new User();
			seeded.setUuid("u-1");
			seeded.setTenantId("acme");
			seeded.setName("alice");
			dao.getStorage().add(seeded);

			OperationRequest req = tenantRequest(anonymousReadOne(), "acme");
			req.arg("type", "uuid");
			req.arg("identifier", "u-1");
			if (rawAuthorization != null) {
				req.arg("rawAuthorization", rawAuthorization);
			}
			return req;
		}

		@Test
		@DisplayName("anonymous op + NO token → authorization step skipped, succeeds as anonymous")
		void anonymousNoTokenSkips() throws ApiException {
			IDomain<?> ctx = buildDomain(true);

			IOperationResponse resp = ctx.invoke(anonymousRequest(null));

			assertEquals(OperationResponseCode.OK, resp.getResponseCode(), () -> "Got: " + resp.getResponse());
			assertEquals(0, bearer.decodeCount.get(), "no token presented → no decode (unchanged anonymous path)");
		}

		@Test
		@DisplayName("anonymous op + VALID token → the token IS decoded and honoured (optional authentication)")
		void anonymousWithValidTokenVerifies() throws ApiException {
			IDomain<?> ctx = buildDomain(true);

			IOperationResponse resp = ctx.invoke(anonymousRequest("Bearer xyz.signed.payload"));

			assertEquals(OperationResponseCode.OK, resp.getResponseCode(), () -> "Got: " + resp.getResponse());
			assertEquals(1, bearer.decodeCount.get(),
					"an anonymous op that carries a token now verifies it (it was skipped before)");
		}

		@Test
		@DisplayName("anonymous op + INVALID token → 401 (a presented token must still be valid)")
		void anonymousWithInvalidTokenRejected() throws ApiException {
			bearer.throwOnDecode = true;
			IDomain<?> ctx = buildDomain(true);

			IOperationResponse resp = ctx.invoke(anonymousRequest("Bearer poison"));

			assertEquals(OperationResponseCode.UNAUTHORIZED, resp.getResponseCode(),
					() -> "a present-but-invalid token on an anonymous op must 401. Got: " + resp.getResponse());
			assertEquals(1, bearer.decodeCount.get(), "the decoder was invoked on the presented token");
		}
	}

	@Nested
	@DisplayName("Registration")
	class Registration {

		@Test
		@DisplayName("IApi.getAuthorizationProtocols() exposes registered protocols in registration order")
		void exposed() throws ApiException {
			buildDomain(true);
			List<IAuthorizationProtocol> all = lastBuilt.getAuthorizationProtocols();
			assertEquals(2, all.size());
			assertSame(bearer, all.get(0), "Registration order: Bearer first");
			assertSame(basic, all.get(1));
		}
	}
}
