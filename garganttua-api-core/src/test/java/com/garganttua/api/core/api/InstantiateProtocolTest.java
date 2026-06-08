package com.garganttua.api.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.protocol.IProtocol;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.dsl.ReflectionBuilder;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.core.reflections.ReflectionsAnnotationScanner;

@DisplayName("ApiBuilder.instantiateProtocol")
class InstantiateProtocolTest {

	static {
		IClass.setReflection(ReflectionBuilder.builder()
				.withProvider(new RuntimeReflectionProvider())
				.withScanner(new ReflectionsAnnotationScanner())
				.build());
	}

	// ----- Fixtures -----

	public static class NotAProtocol {
		public NotAProtocol() {}
	}

	public static class PrivateCtorProtocol implements IProtocol<Object, Object> {
		private PrivateCtorProtocol() {}
		@Override public IClass<Object> requestType() { return IClass.getClass(Object.class); }
		@Override public ICaller getCaller(Object r) { return null; }
		@Override public byte[] getRawBody(Object r) { return null; }
		@Override public String getAuthorization(Object r) { return null; }
		@Override public String getContentType(Object r) { return null; }
		@Override public String getAccept(Object r) { return null; }
		@Override public String getPath(Object r) { return null; }
		@Override public String getMethod(Object r) { return null; }
		@Override public Map<String, String> getQueryParameters(Object r) { return null; }
		@Override public Object buildResponse(Object r, Object out, int code) { return null; }
	}

	public static class ThrowingCtorProtocol implements IProtocol<Object, Object> {
		public ThrowingCtorProtocol() { throw new IllegalStateException("boom"); }
		@Override public IClass<Object> requestType() { return IClass.getClass(Object.class); }
		@Override public ICaller getCaller(Object r) { return null; }
		@Override public byte[] getRawBody(Object r) { return null; }
		@Override public String getAuthorization(Object r) { return null; }
		@Override public String getContentType(Object r) { return null; }
		@Override public String getAccept(Object r) { return null; }
		@Override public String getPath(Object r) { return null; }
		@Override public String getMethod(Object r) { return null; }
		@Override public Map<String, String> getQueryParameters(Object r) { return null; }
		@Override public Object buildResponse(Object r, Object out, int code) { return null; }
	}

	public static class ValidProtocol implements IProtocol<String, String> {
		public ValidProtocol() {}
		@Override public IClass<String> requestType() { return IClass.getClass(String.class); }
		@Override public ICaller getCaller(String r) { return null; }
		@Override public byte[] getRawBody(String r) { return r == null ? null : r.getBytes(); }
		@Override public String getAuthorization(String r) { return null; }
		@Override public String getContentType(String r) { return "text/plain"; }
		@Override public String getAccept(String r) { return null; }
		@Override public String getPath(String r) { return "/"; }
		@Override public String getMethod(String r) { return "POST"; }
		@Override public Map<String, String> getQueryParameters(String r) { return Map.of(); }
		@Override public String buildResponse(String r, Object out, int code) { return "response=" + code; }
	}

	// ----- Tests -----

	@Test
	@DisplayName("throws when the class does not implement IProtocol")
	void rejectsNonProtocol() {
		ApiException ex = assertThrows(ApiException.class,
				() -> ApiBuilder.instantiateProtocol(IClass.getClass(NotAProtocol.class)));
		assertTrue(ex.getMessage().contains("does not implement")
				&& ex.getMessage().contains("IProtocol"),
				"Expected message to cite missing IProtocol, got: " + ex.getMessage());
		assertTrue(ex.getMessage().contains(NotAProtocol.class.getName()),
				"Error should name the offending class, got: " + ex.getMessage());
	}

	@Test
	@DisplayName("throws when the class has no public no-arg constructor")
	void rejectsInaccessibleCtor() {
		ApiException ex = assertThrows(ApiException.class,
				() -> ApiBuilder.instantiateProtocol(IClass.getClass(PrivateCtorProtocol.class)));
		assertTrue(ex.getMessage().contains("no-arg constructor"),
				"Expected message to mention no-arg constructor, got: " + ex.getMessage());
	}

	@Test
	@DisplayName("wraps exceptions from the constructor in an ApiException")
	void wrapsConstructorException() {
		ApiException ex = assertThrows(ApiException.class,
				() -> ApiBuilder.instantiateProtocol(IClass.getClass(ThrowingCtorProtocol.class)));
		assertTrue(ex.getMessage().contains("Failed to instantiate"),
				"Expected 'Failed to instantiate' prefix, got: " + ex.getMessage());
		assertTrue(ex.getMessage().contains(ThrowingCtorProtocol.class.getName()),
				"Error should name the offending class, got: " + ex.getMessage());
	}

	@Test
	@DisplayName("returns an instance of the expected class when well-formed")
	void instantiatesValidProtocol() {
		IProtocol<?, ?> instance = ApiBuilder.instantiateProtocol(IClass.getClass(ValidProtocol.class));
		assertSame(ValidProtocol.class, instance.getClass());
		assertEquals("response=201",
				((ValidProtocol) instance).buildResponse("req", new byte[]{1}, 201));
	}
}
