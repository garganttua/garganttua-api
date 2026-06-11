package com.garganttua.dao.mongodb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.definition.IDtoDefinition;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

/**
 * Round-trip type fidelity: MongoDB's Document codec is lossy across the JVM type system (an enum
 * decodes back as a String, a {@link Instant} as a {@link Date}, a 32-bit value as an Integer).
 * {@link MongoDao} adapts each stored value back to its declared field type on read, and stores an
 * enum by name on write.
 */
@DisplayName("MongoDao — BSON↔Java type fidelity on the round trip")
class MongoDaoTypeFidelityTest {

	public enum Status {
		ACTIVE, DISABLED
	}

	public static class TypedDto {
		private String uuid;
		private Status status;       // BSON String  → enum
		private Instant createdAt;   // BSON datetime → Instant (driver decodes to Date)
		private Long count;          // BSON Int32    → Long
		private int age;             // BSON String   → int
		public String getUuid() { return uuid; }
		public void setUuid(String uuid) { this.uuid = uuid; }
		public Status getStatus() { return status; }
		public void setStatus(Status status) { this.status = status; }
		public Instant getCreatedAt() { return createdAt; }
		public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
		public Long getCount() { return count; }
		public void setCount(Long count) { this.count = count; }
		public int getAge() { return age; }
		public void setAge(int age) { this.age = age; }
	}

	@BeforeAll
	static void installReflection() {
		com.garganttua.core.bootstrap.dsl.Bootstrap.builder();
	}

	private MongoDatabase database;
	private MongoCollection<Document> collection;
	private MongoDao dao;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setUp() throws Exception {
		this.database = mock(MongoDatabase.class);
		this.collection = mock(MongoCollection.class);
		when(this.database.getCollection("typed")).thenReturn(this.collection);

		IDtoDefinition<Object> dtoDefinition = mock(IDtoDefinition.class);
		when(dtoDefinition.dtoClass()).thenReturn((IClass) IClass.getClass(TypedDto.class));
		when(dtoDefinition.uuid()).thenReturn(new ObjectAddress("uuid"));
		when(dtoDefinition.compositions()).thenReturn(List.of());
		IDomainDefinition domainDefinition = mock(IDomainDefinition.class);
		when(domainDefinition.dtoDefinitions()).thenReturn(List.of(dtoDefinition));

		this.dao = new MongoDao(this.database, "typed");
		this.dao.registerDomain(domainDefinition);
	}

	@Nested
	@DisplayName("read")
	class Read {

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("each lossy BSON value is adapted back to its declared field type")
		void coercesEachStoredValue() throws Exception {
			Instant when = Instant.parse("2026-06-11T08:30:00Z");
			Document stored = new Document("uuid", "u1")
					.append("status", "ACTIVE")                 // String → Status.ACTIVE
					.append("createdAt", Date.from(when))       // Date   → Instant
					.append("count", 5)                         // Integer→ Long 5
					.append("age", "42");                       // String → int 42

			FindIterable<Document> find = mock(FindIterable.class);
			when(collection.find(any(Bson.class))).thenReturn(find);
			MongoCursor<Document> cursor = mock(MongoCursor.class);
			when(cursor.hasNext()).thenReturn(true, false);
			when(cursor.next()).thenReturn(stored);
			when(find.iterator()).thenReturn(cursor);

			List<Object> results = dao.find(Optional.empty(), Optional.empty(), Optional.empty());

			assertEquals(1, results.size());
			TypedDto dto = assertInstanceOf(TypedDto.class, results.get(0));
			assertEquals("u1", dto.getUuid());
			assertSame(Status.ACTIVE, dto.getStatus(), "the String must become the declared enum constant");
			assertEquals(when, dto.getCreatedAt(), "the Date must become the exact same Instant");
			assertInstanceOf(Long.class, dto.getCount());
			assertEquals(5L, dto.getCount(), "the 32-bit Integer must widen to the declared Long");
			assertEquals(42, dto.getAge(), "the String must parse into the declared primitive int");
		}
	}

	@Nested
	@DisplayName("write")
	class Write {

		@Test
		@DisplayName("an enum field is stored by name; the other declared values are handed over verbatim")
		void storesEnumByName() throws Exception {
			Instant when = Instant.parse("2026-06-11T08:30:00Z");
			TypedDto dto = new TypedDto();
			dto.setUuid("u1");
			dto.setStatus(Status.DISABLED);
			dto.setCreatedAt(when);
			dto.setCount(7L);

			Document doc = dao.dtoToDocument(dto);

			assertEquals("DISABLED", doc.get("status"), "the enum is persisted as its name, codec-independent");
			assertEquals("u1", doc.get("uuid"));
			assertEquals(7L, doc.get("count"));
			assertSame(when, doc.get("createdAt"), "the Instant is left for the driver's codec, untouched");
		}
	}
}
