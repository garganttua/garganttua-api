package com.garganttua.api.core.integ.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.api.core.caller.Caller;
import com.garganttua.core.reflection.IClass;

/**
 * CREATE-time field authority through the real create pipeline ({@code IDomain.createOne(body,
 * caller)} → CREATE_ONE.gs → {@code createEntity}). The domain declares {@code .create("name")}
 * (free) and {@code .create("email", "user-set-email")} (guarded), so creation is a whitelist:
 * {@code name}/{@code email} survive per authority, every other client-supplied field is stripped.
 */
@DisplayName("Create-time field authority (entity().create(field[, authority])) — via the real createOne path")
class CreateFieldAuthorityIntegrationTest extends AbstractCrudIntegrationTest {

	private IDomain<?> users;

	@BeforeEach
	void setUp() throws ApiException {
		IApiBuilder builder = newBuilder();
		builder.domain(IClass.getClass(User.class))
				.tenant(true)
				.superTenant("superTenant")
				.entity()
					.id("id").uuid("uuid").tenantId("tenantId")
					.create("name")                       // free to valorize
					.create("email", "user-set-email")    // requires the authority
				.up()
				.dto(IClass.getClass(UserDto.class))
					.id("id").uuid("uuid").tenantId("tenantId")
					.db(new StubDao())
				.up()
				.security().disable(true).up()
			.up();
		IApi api = buildAndStart(builder);
		users = api.getDomain("users").orElseThrow();
	}

	private static ICaller caller(List<String> authorities) {
		return new Caller("superTenant", "superTenant", "u1", "u1", false, false, authorities);
	}

	private static User body(String name, String email) {
		User u = new User();
		u.setName(name);
		u.setEmail(email);
		u.setEnabled(false); // a field NOT in the whitelist — must be stripped
		return u;
	}

	private static User created(IOperationResponse response) {
		assertEquals(OperationResponseCode.CREATED, response.getResponseCode(),
				"create should succeed. got=" + response.getResponseCode() + " / " + response.getResponse());
		return (User) response.getResponse();
	}

	@Test
	@DisplayName("a caller WITH the authority valorizes the guarded field; the free field is always kept")
	void authorizedCallerSetsGuardedField() throws ApiException {
		User result = created(users.createOne(body("Alice", "alice@example.com"), caller(List.of("user-set-email"))));

		assertEquals("Alice", result.getName(), "the free 'name' field is kept");
		assertEquals("alice@example.com", result.getEmail(), "the guarded 'email' is kept — caller carries the authority");
	}

	@Test
	@DisplayName("a caller WITHOUT the authority has the guarded field stripped; the free field survives")
	void unauthorizedCallerLosesGuardedField() throws ApiException {
		User result = created(users.createOne(body("Bob", "bob@example.com"), caller(List.of("some-other-role"))));

		assertEquals("Bob", result.getName(), "the free 'name' field is kept");
		assertNull(result.getEmail(), "the guarded 'email' must be stripped — caller lacks 'user-set-email'");
	}

	@Test
	@DisplayName("an undeclared field the client valorized is stripped (whitelist semantics)")
	void undeclaredFieldStripped() throws ApiException {
		// body() sets enabled=false, but 'enabled' is not in the create whitelist.
		User result = created(users.createOne(body("Carol", "carol@example.com"), caller(List.of("user-set-email"))));

		assertNull(result.getEnabled(), "a field not declared .create(...) must be stripped at creation");
		assertEquals("superTenant", result.getTenantId(), "framework stamping still runs after the strip (tenantId from caller)");
	}

	@Test
	@DisplayName("super-tenant/owner does NOT bypass — without the authority the guarded field is stripped")
	void superCallerDoesNotBypass() throws ApiException {
		ICaller superNoAuth = new Caller("superTenant", "superTenant", "root", "root", true, true, List.of());

		User result = created(users.createOne(body("Dave", "dave@example.com"), superNoAuth));

		assertEquals("Dave", result.getName());
		assertNull(result.getEmail(), "a super caller without 'user-set-email' gets no bypass — email stripped");
	}

	@Test
	@DisplayName("a framework/bootstrap write bypasses the whitelist — a seeded guarded field is preserved")
	void bootstrapWriteBypassesWhitelist() throws ApiException {
		StubDao dao = new StubDao();
		User seed = new User();
		seed.setUuid("seed-uuid");
		seed.setName("Seed");
		seed.setEmail("seed@example.com");   // guarded field, set by a server-trusted seed
		seed.setTenantId("superTenant");

		IApiBuilder builder = newBuilder();
		builder.domain(IClass.getClass(User.class))
				.tenant(true)
				.superTenant("superTenant")
				.entity()
					.id("id").uuid("uuid").tenantId("tenantId")
					.create("name")
					.create("email", "user-set-email")   // guarded — a client without the authority loses it
				.up()
				.dto(IClass.getClass(UserDto.class))
					.id("id").uuid("uuid").tenantId("tenantId")
					.db(dao)
				.up()
				.creation(true)
				.create(seed)                            // declared startup entity → bootstrapCreate
				.security().disable(true).up()
			.up();
		buildAndStart(builder);                          // runs the startup write at doStart()

		UserDto persisted = (UserDto) dao.getStorage().stream()
				.filter(UserDto.class::isInstance).findFirst().orElseThrow();
		assertEquals("Seed", persisted.getName());
		assertEquals("seed@example.com", persisted.getEmail(),
				"the seeded guarded field must be PRESERVED — a bootstrap write bypasses the create whitelist "
						+ "(the bootstrap caller carries no authorities, but the write is framework-internal)");
	}
}
