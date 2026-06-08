package com.garganttua.api.core.integ.autodetect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.MimeType;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.serialization.ISerializer;
import com.garganttua.api.commons.serialization.Serializer;
import com.garganttua.core.reflection.IClass;

@DisplayName("@Serializer auto-detection")
class SerializerAutoDetectTest extends AbstractCrudIntegrationTest {

	// ----- Annotated fixtures (discovered via classpath scan) -----

	@Serializer
	public static class AutoDetectedJsonSerializer implements ISerializer {
		@Override public MimeType mimeType() { return MimeType.APPLICATION_JSON; }
		@Override public byte[] serialize(Object object) { return new byte[0]; }
		@Override public <T> T deserialize(byte[] data, IClass<T> type) { return null; }
	}

	@Serializer
	public static class AutoDetectedXmlSerializer implements ISerializer {
		@Override public MimeType mimeType() { return MimeType.APPLICATION_XML; }
		@Override public byte[] serialize(Object object) { return new byte[0]; }
		@Override public <T> T deserialize(byte[] data, IClass<T> type) { return null; }
	}


	@Nested
	@DisplayName("discovery")
	class Discovery {

		@Test
		@DisplayName("autoDetect + withPackage picks up @Serializer classes")
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

			List<ISerializer> all = api.getSerializers();
			assertEquals(2, all.size(), "Both @Serializer classes should have been auto-registered");

			List<Class<?>> classes = all.stream().<Class<?>>map(Object::getClass).toList();
			assertTrue(classes.contains(AutoDetectedJsonSerializer.class),
					"JSON serializer should be registered, got: " + classes);
			assertTrue(classes.contains(AutoDetectedXmlSerializer.class),
					"XML serializer should be registered, got: " + classes);
		}

		@Test
		@DisplayName("without autoDetect, no serializer is registered even when packages are configured")
		void noAutoDetectNoRegistration() throws ApiException {
			IApiBuilder builder = newBuilder();
			((com.garganttua.api.core.api.ApiBuilder) builder).withPackage("com.garganttua.api.core.integ.autodetect");
			// autoDetect NOT enabled
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
			assertEquals(0, api.getSerializers().size());
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
			assertEquals(0, api.getSerializers().size());
		}

	}

}
