package com.garganttua.api.core.integ.autodetect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.protocol.IProtocol;
import com.garganttua.api.commons.protocol.Protocol;
import com.garganttua.core.reflection.IClass;

@DisplayName("@Protocol auto-detection")
class ProtocolAutoDetectTest extends AbstractCrudIntegrationTest {

	// ----- Annotated fixtures (discovered via classpath scan) -----

	public static class HttpLikeRequest {}
	public static class GrpcLikeRequest {}

	@Protocol
	public static class AutoDetectedHttpProtocol implements IProtocol<HttpLikeRequest, Object> {
		@Override public IClass<HttpLikeRequest> requestType() { return IClass.getClass(HttpLikeRequest.class); }
		@Override public ICaller getCaller(HttpLikeRequest r) { return null; }
		@Override public byte[] getRawBody(HttpLikeRequest r) { return null; }
		@Override public String getAuthorization(HttpLikeRequest r) { return null; }
		@Override public String getContentType(HttpLikeRequest r) { return null; }
		@Override public String getAccept(HttpLikeRequest r) { return null; }
		@Override public String getPath(HttpLikeRequest r) { return "/"; }
		@Override public String getMethod(HttpLikeRequest r) { return "GET"; }
		@Override public Map<String, String> getQueryParameters(HttpLikeRequest r) { return Map.of(); }
		@Override public Object buildResponse(HttpLikeRequest r, Object out, int code) { return out; }
	}

	@Protocol
	public static class AutoDetectedGrpcProtocol implements IProtocol<GrpcLikeRequest, Object> {
		@Override public IClass<GrpcLikeRequest> requestType() { return IClass.getClass(GrpcLikeRequest.class); }
		@Override public ICaller getCaller(GrpcLikeRequest r) { return null; }
		@Override public byte[] getRawBody(GrpcLikeRequest r) { return null; }
		@Override public String getAuthorization(GrpcLikeRequest r) { return null; }
		@Override public String getContentType(GrpcLikeRequest r) { return null; }
		@Override public String getAccept(GrpcLikeRequest r) { return null; }
		@Override public String getPath(GrpcLikeRequest r) { return "/"; }
		@Override public String getMethod(GrpcLikeRequest r) { return "POST"; }
		@Override public Map<String, String> getQueryParameters(GrpcLikeRequest r) { return Map.of(); }
		@Override public Object buildResponse(GrpcLikeRequest r, Object out, int code) { return out; }
	}

	@Nested
	@DisplayName("discovery")
	class Discovery {

		@Test
		@DisplayName("autoDetect + withPackage picks up @Protocol classes")
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

			List<IProtocol<?, ?>> all = api.getProtocols();
			assertEquals(2, all.size(),
					"Both @Protocol classes should have been auto-registered, got: " + all);

			List<Class<?>> classes = all.stream().<Class<?>>map(Object::getClass).toList();
			assertTrue(classes.contains(AutoDetectedHttpProtocol.class),
					"HTTP protocol should be registered, got: " + classes);
			assertTrue(classes.contains(AutoDetectedGrpcProtocol.class),
					"gRPC protocol should be registered, got: " + classes);
		}

		@Test
		@DisplayName("without autoDetect, no protocol is registered even when packages are configured")
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
			assertEquals(0, api.getProtocols().size());
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
			assertEquals(0, api.getProtocols().size());
		}
	}
}
