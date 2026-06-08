package com.garganttua.api.core.integ.autodetect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.security.authorization.AuthorizationProtocol;

import com.garganttua.api.commons.security.authorization.IAuthorizationProtocol;
import com.garganttua.core.reflection.IClass;

@DisplayName("@AuthorizationProtocol auto-detection")
class AuthorizationProtocolAutoDetectTest extends AbstractCrudIntegrationTest {

	// ----- Annotated fixtures (discovered via classpath scan) -----

	@AuthorizationProtocol
	public static class AutoDetectedBearerProtocol implements IAuthorizationProtocol {
		@Override public String scheme() { return "Bearer"; }
		@Override public IClass<?> targetDomain() { return IClass.getClass(Object.class); }
		@Override public Object decode(String value, IApi api) { return null; }
	}

	@AuthorizationProtocol
	public static class AutoDetectedBasicProtocol implements IAuthorizationProtocol {
		@Override public String scheme() { return "Basic"; }
		@Override public IClass<?> targetDomain() { return IClass.getClass(Object.class); }
		@Override public Object decode(String value, IApi api) { return null; }
	}

	@Nested
	@DisplayName("discovery")
	class Discovery {

		@Test
		@DisplayName("autoDetect + withPackage picks up @AuthorizationProtocol classes")
		void picksUpAnnotated() throws ApiException {
			IApiBuilder builder = newBuilder();
			((com.garganttua.api.core.api.ApiBuilder) builder).withPackage("com.garganttua.api.core.integ.autodetect");
			((com.garganttua.core.dsl.IAutomaticBuilder<?, ?>) builder).autoDetect(true);
			builder.includeFrameworkPackages(false); // test asserts "no user packages → 0 discovered" — opt out of the framework asset scan
			builder.domain(IClass.getClass(User.class))
					.tenant(true)
					.superTenant("superTenant")
					.entity().id("id").uuid("uuid").tenantId("tenantId").up()
					.dto(IClass.getClass(UserDto.class))
						.id("id").uuid("uuid").tenantId("tenantId")
						.db(new CapturingDao())
					.up()
				.up();

			IApi api = buildAndStart(builder);

			List<IAuthorizationProtocol> all = api.getAuthorizationProtocols();
			assertEquals(2, all.size(),
					"Both @AuthorizationProtocol classes should have been auto-registered, got: " + all);

			List<Class<?>> classes = all.stream().<Class<?>>map(Object::getClass).toList();
			assertTrue(classes.contains(AutoDetectedBearerProtocol.class),
					"Bearer protocol should be registered, got: " + classes);
			assertTrue(classes.contains(AutoDetectedBasicProtocol.class),
					"Basic protocol should be registered, got: " + classes);
		}

		@Test
		@DisplayName("without autoDetect, no protocol is registered even when packages are configured")
		void noAutoDetectNoRegistration() throws ApiException {
			IApiBuilder builder = newBuilder();
			((com.garganttua.api.core.api.ApiBuilder) builder).withPackage("com.garganttua.api.core.integ.autodetect");
			builder.domain(IClass.getClass(User.class))
					.tenant(true)
					.superTenant("superTenant")
					.entity().id("id").uuid("uuid").tenantId("tenantId").up()
					.dto(IClass.getClass(UserDto.class))
						.id("id").uuid("uuid").tenantId("tenantId")
						.db(new CapturingDao())
					.up()
				.up();

			IApi api = buildAndStart(builder);
			assertEquals(0, api.getAuthorizationProtocols().size());
		}

		@Test
		@DisplayName("autoDetect without packages is a no-op — no crash, no registration")
		void autoDetectNoPackages() throws ApiException {
			IApiBuilder builder = newBuilder();
			((com.garganttua.core.dsl.IAutomaticBuilder<?, ?>) builder).autoDetect(true);
			builder.includeFrameworkPackages(false); // test asserts "no user packages → 0 discovered" — opt out of the framework asset scan
			builder.domain(IClass.getClass(User.class))
					.tenant(true)
					.superTenant("superTenant")
					.entity().id("id").uuid("uuid").tenantId("tenantId").up()
					.dto(IClass.getClass(UserDto.class))
						.id("id").uuid("uuid").tenantId("tenantId")
						.db(new CapturingDao())
					.up()
				.up();

			IApi api = buildAndStart(builder);
			assertEquals(0, api.getAuthorizationProtocols().size());
		}
	}
}
