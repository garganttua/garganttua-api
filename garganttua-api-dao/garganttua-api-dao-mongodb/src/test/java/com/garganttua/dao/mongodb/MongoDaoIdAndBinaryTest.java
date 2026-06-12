package com.garganttua.dao.mongodb;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
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
import com.mongodb.client.model.ReplaceOptions;

/**
 * Two persistence invariants of {@link MongoDao}:
 * <ul>
 *   <li>the domain uuid is projected onto {@code _id} on write, so {@code save()} upserts (rather
 *       than always inserting → duplicates) and the uuid is recoverable from {@code _id} on read;</li>
 *   <li>a {@code byte[]} field — decoded by the driver as {@link Binary} — is unwrapped back to
 *       {@code byte[]} on read (token signatures, key material).</li>
 * </ul>
 */
@DisplayName("MongoDao — uuid↔_id projection and Binary→byte[] on read")
class MongoDaoIdAndBinaryTest {

	public static class SignedDto {
		private String uuid;
		private byte[] signature;
		public String getUuid() { return uuid; }
		public void setUuid(String uuid) { this.uuid = uuid; }
		public byte[] getSignature() { return signature; }
		public void setSignature(byte[] signature) { this.signature = signature; }
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
		when(this.database.getCollection("signed")).thenReturn(this.collection);

		IDtoDefinition<Object> dtoDefinition = mock(IDtoDefinition.class);
		when(dtoDefinition.dtoClass()).thenReturn((IClass) IClass.getClass(SignedDto.class));
		when(dtoDefinition.uuid()).thenReturn(new ObjectAddress("uuid"));
		when(dtoDefinition.compositions()).thenReturn(List.of());
		IDomainDefinition domainDefinition = mock(IDomainDefinition.class);
		when(domainDefinition.dtoDefinitions()).thenReturn(List.of(dtoDefinition));

		this.dao = new MongoDao(this.database, "signed");
		this.dao.registerDomain(domainDefinition);
	}

	@SuppressWarnings("unchecked")
	private void stubFindReturning(Document stored) {
		FindIterable<Document> find = mock(FindIterable.class);
		when(collection.find(any(Bson.class))).thenReturn(find);
		MongoCursor<Document> cursor = mock(MongoCursor.class);
		when(cursor.hasNext()).thenReturn(true, false);
		when(cursor.next()).thenReturn(stored);
		when(find.iterator()).thenReturn(cursor);
	}

	@Nested
	@DisplayName("uuid ↔ _id projection")
	class IdProjection {

		@Test
		@DisplayName("write stores the uuid under both its field name and _id")
		void writeProjectsUuidOntoId() throws Exception {
			SignedDto dto = new SignedDto();
			dto.setUuid("u-42");

			Document doc = dao.dtoToDocument(dto);

			assertEquals("u-42", doc.get("_id"), "the uuid must be projected onto _id so save() upserts");
			assertEquals("u-42", doc.get("uuid"), "the uuid stays under its field name so uuid filters still match");
		}

		@Test
		@DisplayName("save() upserts by _id (replaceOne), never a blind insertOne, once a uuid is present")
		void saveUpserts() throws Exception {
			SignedDto dto = new SignedDto();
			dto.setUuid("u-42");

			dao.save(dto);

			verify(collection).replaceOne(any(Bson.class), any(Document.class), any(ReplaceOptions.class));
			verify(collection, never()).insertOne(any(Document.class));
		}

		@Test
		@DisplayName("read recovers the uuid from _id when the document carries no uuid field")
		void readRecoversUuidFromId() throws Exception {
			stubFindReturning(new Document("_id", "u-42"));   // _id only — no "uuid" key

			List<Object> results = dao.find(Optional.empty(), Optional.empty(), Optional.empty());

			SignedDto dto = (SignedDto) results.get(0);
			assertEquals("u-42", dto.getUuid(), "the uuid must be recovered from _id");
		}
	}

	@Nested
	@DisplayName("Binary → byte[]")
	class BinaryField {

		@Test
		@DisplayName("a byte[] field stored as BSON binary is unwrapped back to byte[] on read")
		void binaryUnwrappedToByteArray() throws Exception {
			byte[] signature = { 1, 2, 3, 4, 5, -7, -8 };
			stubFindReturning(new Document("_id", "u-1")
					.append("uuid", "u-1")
					.append("signature", new Binary(signature)));

			List<Object> results = dao.find(Optional.empty(), Optional.empty(), Optional.empty());

			SignedDto dto = (SignedDto) results.get(0);
			assertArrayEquals(signature, dto.getSignature(),
					"a byte[] field must read back as byte[], not org.bson.types.Binary");
		}
	}
}
