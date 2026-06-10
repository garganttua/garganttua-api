package com.garganttua.api.binding.jackson;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.MimeType;
import com.garganttua.api.commons.serialization.Serializer;
import com.garganttua.core.reflection.IClass;

@DisplayName("Jackson JSON + XML serializers")
class JacksonSerializersTest {

	/** A plain DTO with public fields — Jackson auto-detects them for both JSON and XML. */
	public static class Person {
		public String name;
		public int age;

		public Person() {}

		public Person(String name, int age) {
			this.name = name;
			this.age = age;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Person p)) {
				return false;
			}
			return this.age == p.age && Objects.equals(this.name, p.name);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.name, this.age);
		}
	}

	static {
		// Cold-start garganttua-core's ServiceLoader so IClass.getClass(...) has an
		// IReflection installed. Must run BEFORE the PERSON field initializer below
		// (static initializers run in textual order, ahead of any @BeforeAll).
		com.garganttua.core.bootstrap.dsl.Bootstrap.builder();
	}

	private static final IClass<Person> PERSON = IClass.getClass(Person.class);

	@Nested
	@DisplayName("JSON (application/json)")
	class Json {

		private final JacksonJsonSerializer json = new JacksonJsonSerializer();

		@Test
		@DisplayName("advertises application/json")
		void mime() {
			assertSame(MimeType.APPLICATION_JSON, json.mimeType());
		}

		@Test
		@DisplayName("serialize produces JSON bytes carrying every field value")
		void serializeProducesJson() throws ApiException {
			byte[] bytes = json.serialize(new Person("Alice", 30));
			String text = new String(bytes, StandardCharsets.UTF_8);

			assertTrue(text.startsWith("{") && text.endsWith("}"), "must be a JSON object; got: " + text);
			assertTrue(text.contains("\"name\":\"Alice\""), "name must be present; got: " + text);
			assertTrue(text.contains("\"age\":30"), "age must be present; got: " + text);
		}

		@Test
		@DisplayName("deserialize reconstructs the exact field values")
		void deserializeReconstructs() throws ApiException {
			byte[] bytes = "{\"name\":\"Bob\",\"age\":42}".getBytes(StandardCharsets.UTF_8);
			Person p = json.deserialize(bytes, PERSON);

			assertNotNull(p);
			assertEquals("Bob", p.name);
			assertEquals(42, p.age);
		}

		@Test
		@DisplayName("round-trip is value-preserving")
		void roundTrip() throws ApiException {
			Person original = new Person("Chloé", 27);
			Person back = json.deserialize(json.serialize(original), PERSON);
			assertEquals(original, back, "deserialize(serialize(x)) must equal x");
		}

		@Test
		@DisplayName("an unknown field on the wire is ignored, not a failure")
		void lenientOnUnknown() throws ApiException {
			byte[] bytes = "{\"name\":\"Dan\",\"age\":5,\"ghost\":true}".getBytes(StandardCharsets.UTF_8);
			Person p = json.deserialize(bytes, PERSON);
			assertEquals("Dan", p.name);
			assertEquals(5, p.age);
		}

		@Test
		@DisplayName("malformed JSON raises an ApiException naming the format")
		void malformedRaises() {
			byte[] bytes = "{not json".getBytes(StandardCharsets.UTF_8);
			ApiException e = assertThrows(ApiException.class, () -> json.deserialize(bytes, PERSON));
			assertTrue(e.getMessage().contains("application/json"),
					"the error must name the failing format; got: " + e.getMessage());
		}
	}

	@Nested
	@DisplayName("XML (application/xml)")
	class Xml {

		private final JacksonXmlSerializer xml = new JacksonXmlSerializer();

		@Test
		@DisplayName("advertises application/xml")
		void mime() {
			assertSame(MimeType.APPLICATION_XML, xml.mimeType());
		}

		@Test
		@DisplayName("serialize produces XML bytes carrying every field value")
		void serializeProducesXml() throws ApiException {
			byte[] bytes = xml.serialize(new Person("Alice", 30));
			String text = new String(bytes, StandardCharsets.UTF_8);

			assertTrue(text.contains("<name>Alice</name>"), "name element must be present; got: " + text);
			assertTrue(text.contains("<age>30</age>"), "age element must be present; got: " + text);
			assertTrue(text.contains("<Person"), "the root element must be the type name; got: " + text);
		}

		@Test
		@DisplayName("deserialize reconstructs the exact field values")
		void deserializeReconstructs() throws ApiException {
			byte[] bytes = "<Person><name>Bob</name><age>42</age></Person>".getBytes(StandardCharsets.UTF_8);
			Person p = xml.deserialize(bytes, PERSON);

			assertNotNull(p);
			assertEquals("Bob", p.name);
			assertEquals(42, p.age);
		}

		@Test
		@DisplayName("round-trip is value-preserving")
		void roundTrip() throws ApiException {
			Person original = new Person("Chloé", 27);
			Person back = xml.deserialize(xml.serialize(original), PERSON);
			assertEquals(original, back, "deserialize(serialize(x)) must equal x");
		}

		@Test
		@DisplayName("malformed XML raises an ApiException naming the format")
		void malformedRaises() {
			byte[] bytes = "<Person><name>oops".getBytes(StandardCharsets.UTF_8);
			ApiException e = assertThrows(ApiException.class, () -> xml.deserialize(bytes, PERSON));
			assertTrue(e.getMessage().contains("application/xml"),
					"the error must name the failing format; got: " + e.getMessage());
		}
	}

	/** A DTO carrying a java.time value — the case that used to 500 without the JSR-310 module. */
	public static class Temporal {
		public String label;
		public java.time.Instant expiration;

		public Temporal() {}

		public Temporal(String label, java.time.Instant expiration) {
			this.label = label;
			this.expiration = expiration;
		}
	}

	private static final IClass<Temporal> TEMPORAL = IClass.getClass(Temporal.class);

	@Nested
	@DisplayName("java.time support (JSR-310)")
	class JavaTime {

		private static final java.time.Instant WHEN = java.time.Instant.parse("2026-06-10T12:34:56Z");

		@Test
		@DisplayName("JSON: a non-null Instant renders as an ISO-8601 string (not a 500), and round-trips")
		void jsonInstant() throws ApiException {
			JacksonJsonSerializer json = new JacksonJsonSerializer();
			String text = new String(json.serialize(new Temporal("key", WHEN)), StandardCharsets.UTF_8);

			assertTrue(text.contains("\"2026-06-10T12:34:56Z\""),
					"Instant must be an ISO-8601 string, not a numeric timestamp; got: " + text);

			Temporal back = json.deserialize(text.getBytes(StandardCharsets.UTF_8), TEMPORAL);
			assertEquals(WHEN, back.expiration, "the Instant must round-trip to the exact value");
			assertEquals("key", back.label);
		}

		@Test
		@DisplayName("XML: a non-null Instant renders as an ISO-8601 string (not a 500), and round-trips")
		void xmlInstant() throws ApiException {
			JacksonXmlSerializer xml = new JacksonXmlSerializer();
			String text = new String(xml.serialize(new Temporal("key", WHEN)), StandardCharsets.UTF_8);

			assertTrue(text.contains("<expiration>2026-06-10T12:34:56Z</expiration>"),
					"Instant must be an ISO-8601 element value; got: " + text);

			Temporal back = xml.deserialize(text.getBytes(StandardCharsets.UTF_8), TEMPORAL);
			assertEquals(WHEN, back.expiration, "the Instant must round-trip to the exact value");
			assertEquals("key", back.label);
		}
	}

	@Nested
	@DisplayName("Common contract")
	class Contract {

		@Test
		@DisplayName("the two serializers advertise distinct, non-overlapping MIME types")
		void distinctMimeTypes() {
			assertEquals(MimeType.APPLICATION_JSON, new JacksonJsonSerializer().mimeType());
			assertEquals(MimeType.APPLICATION_XML, new JacksonXmlSerializer().mimeType());
		}

		@Test
		@DisplayName("both carry @Serializer and expose a public no-arg constructor (auto-detection contract)")
		void autoDetectable() throws Exception {
			for (Class<?> c : new Class<?>[] {JacksonJsonSerializer.class, JacksonXmlSerializer.class}) {
				assertNotNull(c.getAnnotation(Serializer.class),
						c.getSimpleName() + " must be annotated @Serializer to be auto-detected");
				assertNotNull(c.getConstructor(), c.getSimpleName() + " must expose a public no-arg constructor");
			}
		}

		@Test
		@DisplayName("serializing null yields empty bytes; deserializing empty bytes yields null")
		void nullAndEmpty() throws ApiException {
			assertArrayEquals(new byte[0], new JacksonJsonSerializer().serialize(null));
			assertArrayEquals(new byte[0], new JacksonXmlSerializer().serialize(null));
			assertNull(new JacksonJsonSerializer().deserialize(new byte[0], PERSON));
			assertNull(new JacksonXmlSerializer().deserialize(new byte[0], PERSON));
		}
	}
}
