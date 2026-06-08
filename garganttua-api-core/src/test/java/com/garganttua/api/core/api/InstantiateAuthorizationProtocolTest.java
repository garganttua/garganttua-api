package com.garganttua.api.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;

import com.garganttua.api.commons.security.authorization.IAuthorizationProtocol;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.dsl.ReflectionBuilder;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.core.reflections.ReflectionsAnnotationScanner;

@DisplayName("ApiBuilder.instantiateAuthorizationProtocol")
class InstantiateAuthorizationProtocolTest {

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

	public static class PrivateCtorProtocol implements IAuthorizationProtocol {
		private PrivateCtorProtocol() {}
		@Override public String scheme() { return "Bearer"; }
		@Override public IClass<?> targetDomain() { return IClass.getClass(Object.class); }
		@Override public Object decode(String v, IApi api) { return null; }
	}

	public static class ThrowingCtorProtocol implements IAuthorizationProtocol {
		public ThrowingCtorProtocol() { throw new IllegalStateException("boom"); }
		@Override public String scheme() { return "Bearer"; }
		@Override public IClass<?> targetDomain() { return IClass.getClass(Object.class); }
		@Override public Object decode(String v, IApi api) { return null; }
	}

	public static class ValidProtocol implements IAuthorizationProtocol {
		public ValidProtocol() {}
		@Override public String scheme() { return "Bearer"; }
		@Override public IClass<?> targetDomain() { return IClass.getClass(Object.class); }
		@Override public Object decode(String v, IApi api) { return null; }
	}

	// ----- Tests -----

	@Test
	@DisplayName("throws when the class does not implement IAuthorizationProtocol")
	void rejectsNonProtocol() {
		ApiException ex = assertThrows(ApiException.class,
				() -> ApiBuilder.instantiateAuthorizationProtocol(IClass.getClass(NotAProtocol.class)));
		assertTrue(ex.getMessage().contains("does not implement")
				&& ex.getMessage().contains("IAuthorizationProtocol"),
				"Expected message to cite missing IAuthorizationProtocol, got: " + ex.getMessage());
		assertTrue(ex.getMessage().contains(NotAProtocol.class.getName()),
				"Error should name the offending class, got: " + ex.getMessage());
	}

	@Test
	@DisplayName("throws when the class has no public no-arg constructor")
	void rejectsInaccessibleCtor() {
		ApiException ex = assertThrows(ApiException.class,
				() -> ApiBuilder.instantiateAuthorizationProtocol(IClass.getClass(PrivateCtorProtocol.class)));
		assertTrue(ex.getMessage().contains("no-arg constructor"),
				"Expected message to mention no-arg constructor, got: " + ex.getMessage());
	}

	@Test
	@DisplayName("wraps exceptions from the constructor in an ApiException")
	void wrapsConstructorException() {
		ApiException ex = assertThrows(ApiException.class,
				() -> ApiBuilder.instantiateAuthorizationProtocol(IClass.getClass(ThrowingCtorProtocol.class)));
		assertTrue(ex.getMessage().contains("Failed to instantiate"),
				"Expected 'Failed to instantiate' prefix, got: " + ex.getMessage());
		assertTrue(ex.getMessage().contains(ThrowingCtorProtocol.class.getName()),
				"Error should name the offending class, got: " + ex.getMessage());
	}

	@Test
	@DisplayName("returns an instance of the expected class when well-formed")
	void instantiatesValidProtocol() {
		IAuthorizationProtocol instance = ApiBuilder.instantiateAuthorizationProtocol(IClass.getClass(ValidProtocol.class));
		assertSame(ValidProtocol.class, instance.getClass());
		assertEquals("Bearer", instance.scheme());
	}
}
