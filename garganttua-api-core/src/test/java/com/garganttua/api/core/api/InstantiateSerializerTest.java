package com.garganttua.api.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.MimeType;
import com.garganttua.api.commons.serialization.ISerializer;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.dsl.ReflectionBuilder;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.core.reflections.ReflectionsAnnotationScanner;

@DisplayName("ApiBuilder.instantiateSerializer")
class InstantiateSerializerTest {

	static {
		// Required so IClass.getClass(...) can resolve classes in this test
		IClass.setReflection(ReflectionBuilder.builder()
				.withProvider(new RuntimeReflectionProvider())
				.withScanner(new ReflectionsAnnotationScanner())
				.build());
	}

	// ----- Fixtures -----

	public static class NotASerializer {
		public NotASerializer() {}
	}

	public static class PrivateCtorSerializer implements ISerializer {
		private PrivateCtorSerializer() {}
		@Override public MimeType mimeType() { return MimeType.APPLICATION_JSON; }
		@Override public byte[] serialize(Object object) { return new byte[0]; }
		@Override public <T> T deserialize(byte[] data, IClass<T> type) { return null; }
	}

	public static class ThrowingCtorSerializer implements ISerializer {
		public ThrowingCtorSerializer() { throw new IllegalStateException("boom"); }
		@Override public MimeType mimeType() { return MimeType.APPLICATION_JSON; }
		@Override public byte[] serialize(Object object) { return new byte[0]; }
		@Override public <T> T deserialize(byte[] data, IClass<T> type) { return null; }
	}

	public static class ValidSerializer implements ISerializer {
		public ValidSerializer() {}
		@Override public MimeType mimeType() { return MimeType.APPLICATION_JSON; }
		@Override public byte[] serialize(Object object) { return new byte[]{1, 2, 3}; }
		@Override public <T> T deserialize(byte[] data, IClass<T> type) { return null; }
	}

	// ----- Tests -----

	@Test
	@DisplayName("throws when the class does not implement ISerializer")
	void rejectsNonSerializer() {
		ApiException ex = assertThrows(ApiException.class,
				() -> ApiBuilder.instantiateSerializer(IClass.getClass(NotASerializer.class)));
		assertTrue(ex.getMessage().contains("does not implement")
				&& ex.getMessage().contains("ISerializer"),
				"Expected message to cite missing ISerializer, got: " + ex.getMessage());
		assertTrue(ex.getMessage().contains(NotASerializer.class.getName()),
				"Error should name the offending class, got: " + ex.getMessage());
	}

	@Test
	@DisplayName("throws when the class has no public no-arg constructor")
	void rejectsInaccessibleCtor() {
		ApiException ex = assertThrows(ApiException.class,
				() -> ApiBuilder.instantiateSerializer(IClass.getClass(PrivateCtorSerializer.class)));
		assertTrue(ex.getMessage().contains("no-arg constructor"),
				"Expected message to mention no-arg constructor, got: " + ex.getMessage());
	}

	@Test
	@DisplayName("wraps exceptions from the constructor in an ApiException")
	void wrapsConstructorException() {
		ApiException ex = assertThrows(ApiException.class,
				() -> ApiBuilder.instantiateSerializer(IClass.getClass(ThrowingCtorSerializer.class)));
		assertTrue(ex.getMessage().contains("Failed to instantiate"),
				"Expected 'Failed to instantiate' prefix, got: " + ex.getMessage());
		assertTrue(ex.getMessage().contains(ThrowingCtorSerializer.class.getName()),
				"Error should name the offending class, got: " + ex.getMessage());
	}

	@Test
	@DisplayName("returns an instance of the expected class when well-formed")
	void instantiatesValidSerializer() {
		ISerializer instance = ApiBuilder.instantiateSerializer(IClass.getClass(ValidSerializer.class));
		assertSame(ValidSerializer.class, instance.getClass());
		assertEquals(MimeType.APPLICATION_JSON, instance.mimeType());
		// Sanity: the instance is usable (exercises the no-arg constructor path fully)
		assertEquals(3, instance.serialize(new Object()).length);
	}
}
