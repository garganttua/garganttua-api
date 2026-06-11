package com.garganttua.dao.mongodb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.definition.DtoComposition;
import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.definition.IDtoDefinition;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;
import com.mongodb.DBRef;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

/**
 * End-to-end mapping of {@code @Composed} fields by {@link MongoDao}: on write only a
 * {@link DBRef} is persisted (never the embedded DTO), and on read the reference is eagerly
 * resolved back into the composed DTO. Exercises both a 1-1 ({@code ref}) and a 1-N
 * ({@code tags}) composition.
 */
@DisplayName("MongoDao — @Composed write emits DBRefs, read resolves them")
class MongoDaoCompositionTest {

	public static class SubDto {
		private String uuid;
		public String getUuid() { return uuid; }
		public void setUuid(String uuid) { this.uuid = uuid; }
	}

	public static class TagDto {
		private String uuid;
		public String getUuid() { return uuid; }
		public void setUuid(String uuid) { this.uuid = uuid; }
	}

	public static class ItemDto {
		private String uuid;
		private String tenantId;
		private SubDto ref;          // 1-1
		private List<TagDto> tags;   // 1-N
		public String getUuid() { return uuid; }
		public void setUuid(String uuid) { this.uuid = uuid; }
		public String getTenantId() { return tenantId; }
		public void setTenantId(String tenantId) { this.tenantId = tenantId; }
		public SubDto getRef() { return ref; }
		public void setRef(SubDto ref) { this.ref = ref; }
		public List<TagDto> getTags() { return tags; }
		public void setTags(List<TagDto> tags) { this.tags = tags; }
	}

	@BeforeAll
	static void installReflection() {
		// Cold-start garganttua-core's ServiceLoader so IClass.getClass(...) has an
		// IReflection installed (mirrors the binding tests' bootstrap).
		com.garganttua.core.bootstrap.dsl.Bootstrap.builder();
	}

	private MongoDatabase database;
	private MongoCollection<Document> itemsCollection;
	private MongoCollection<Document> subsCollection;
	private MongoCollection<Document> tagsCollection;
	private MongoDao dao;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setUp() throws Exception {
		this.database = mock(MongoDatabase.class);
		this.itemsCollection = mock(MongoCollection.class);
		this.subsCollection = mock(MongoCollection.class);
		this.tagsCollection = mock(MongoCollection.class);
		when(this.database.getCollection("items")).thenReturn(this.itemsCollection);
		when(this.database.getCollection("subs")).thenReturn(this.subsCollection);
		when(this.database.getCollection("tags")).thenReturn(this.tagsCollection);

		this.dao = new MongoDao(this.database, "items");
		this.dao.registerDomain(domainDefinition());
	}

	private IDomainDefinition domainDefinition() throws Exception {
		IDtoDefinition<Object> dtoDefinition = mock(IDtoDefinition.class);
		when(dtoDefinition.dtoClass()).thenReturn((IClass) IClass.getClass(ItemDto.class));
		when(dtoDefinition.uuid()).thenReturn(new ObjectAddress("uuid"));
		when(dtoDefinition.compositions()).thenReturn(List.of(
				new DtoComposition(new ObjectAddress("ref"), "subs"),
				new DtoComposition(new ObjectAddress("tags"), "tags")));

		IDomainDefinition domainDefinition = mock(IDomainDefinition.class);
		when(domainDefinition.dtoDefinitions()).thenReturn(List.of(dtoDefinition));
		return domainDefinition;
	}

	@Nested
	@DisplayName("write")
	class Write {

		@Test
		@DisplayName("a composed 1-1 field is stored as a single DBRef to the target collection, not the DTO")
		void oneToOneStoredAsDbRef() throws Exception {
			ItemDto item = new ItemDto();
			item.setUuid("i1");
			SubDto sub = new SubDto();
			sub.setUuid("s1");
			item.setRef(sub);

			Document doc = dao.dtoToDocument(item);

			Object ref = doc.get("ref");
			assertFalse(ref instanceof SubDto, "the embedded DTO must NOT be persisted");
			DBRef dbRef = assertInstanceOf(DBRef.class, ref);
			assertEquals("subs", dbRef.getCollectionName());
			assertEquals("s1", dbRef.getId());
			assertEquals("i1", doc.get("uuid"), "non-composed fields are stored verbatim");
		}

		@Test
		@DisplayName("a composed 1-N field is stored as a List of DBRefs, one per element, in order")
		void oneToManyStoredAsDbRefList() throws Exception {
			ItemDto item = new ItemDto();
			item.setUuid("i1");
			item.setTags(tagsWithUuids("t1", "t2"));

			Document doc = dao.dtoToDocument(item);

			Object tags = doc.get("tags");
			List<?> refs = assertInstanceOf(List.class, tags);
			assertEquals(2, refs.size());
			DBRef first = assertInstanceOf(DBRef.class, refs.get(0));
			DBRef second = assertInstanceOf(DBRef.class, refs.get(1));
			assertEquals("tags", first.getCollectionName());
			assertEquals("t1", first.getId());
			assertEquals("tags", second.getCollectionName());
			assertEquals("t2", second.getId());
		}
	}

	@Nested
	@DisplayName("read")
	class Read {

		@SuppressWarnings("unchecked")
		@Test
		@DisplayName("reading an item resolves both the 1-1 and the 1-N DBRefs into composed DTOs")
		void resolvesReferencesOnRead() throws Exception {
			Document itemDoc = new Document("uuid", "i1")
					.append("tenantId", "tenant")
					.append("ref", new DBRef("subs", "s1"))
					.append("tags", List.of(new DBRef("tags", "t1"), new DBRef("tags", "t2")));

			// items collection yields the one stored item document
			FindIterable<Document> itemsFind = mock(FindIterable.class);
			when(itemsCollection.find(any(Bson.class))).thenReturn(itemsFind);
			MongoCursor<Document> itemsCursor = mock(MongoCursor.class);
			when(itemsCursor.hasNext()).thenReturn(true, false);
			when(itemsCursor.next()).thenReturn(itemDoc);
			when(itemsFind.iterator()).thenReturn(itemsCursor);

			// the referenced sub document, fetched from the "subs" collection by uuid
			FindIterable<Document> subsFind = mock(FindIterable.class);
			when(subsCollection.find(any(Bson.class))).thenReturn(subsFind);
			when(subsFind.first()).thenReturn(new Document("uuid", "s1"));

			// the two referenced tag documents, fetched from "tags" in resolution order
			FindIterable<Document> tagsFind = mock(FindIterable.class);
			when(tagsCollection.find(any(Bson.class))).thenReturn(tagsFind);
			when(tagsFind.first()).thenReturn(new Document("uuid", "t1"), new Document("uuid", "t2"));

			List<Object> results = dao.find(Optional.empty(), Optional.empty(), Optional.empty());

			assertEquals(1, results.size());
			ItemDto item = assertInstanceOf(ItemDto.class, results.get(0));
			assertEquals("i1", item.getUuid());
			assertEquals("tenant", item.getTenantId());

			assertNotNull(item.getRef(), "the 1-1 reference must be resolved");
			assertInstanceOf(SubDto.class, item.getRef());
			assertEquals("s1", item.getRef().getUuid());

			assertNotNull(item.getTags(), "the 1-N reference must be resolved");
			assertEquals(2, item.getTags().size());
			assertInstanceOf(TagDto.class, item.getTags().get(0));
			assertEquals("t1", item.getTags().get(0).getUuid());
			assertEquals("t2", item.getTags().get(1).getUuid());
		}
	}

	private static List<TagDto> tagsWithUuids(String... uuids) {
		List<TagDto> tags = new ArrayList<>();
		for (String uuid : uuids) {
			TagDto tag = new TagDto();
			tag.setUuid(uuid);
			tags.add(tag);
		}
		return tags;
	}
}
