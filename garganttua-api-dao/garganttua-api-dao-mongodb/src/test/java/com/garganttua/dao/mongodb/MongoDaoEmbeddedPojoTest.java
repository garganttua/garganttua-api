package com.garganttua.dao.mongodb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.definition.IDtoDefinition;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

/**
 * Embedded (non-DBRef) POJO persistence: a field that is a plain POJO, a {@code List<POJO>} or a
 * GeoJSON-shaped value object is stored as a sub-{@link Document} (written recursively by
 * {@link MongoDao#dtoToDocument}) and rebuilt into its concrete declared type on read (via the
 * symmetric {@code mapValue}). This is distinct from {@code @Composed} compositions, which stay
 * {@code DBRef}s — proven by {@link MongoDaoCompositionTest} staying green. Flat scalar entities are
 * untouched.
 */
@DisplayName("MongoDao — embedded POJO / List<POJO> sub-documents on the round trip")
class MongoDaoEmbeddedPojoTest {

	/** A plain value object embedded as a sub-document (no uuid, no annotations). */
	public static class Address {
		private String street;
		private int zip;
		public String getStreet() { return street; }
		public void setStreet(String street) { this.street = street; }
		public int getZip() { return zip; }
		public void setZip(int zip) { this.zip = zip; }
	}

	/** A GeoJSON-shaped POJO (mirrors palliad's GeoPolygon: type + raw coordinate tree). */
	public static class GeoShape {
		private String type;
		private List<Object> coordinates;
		public String getType() { return type; }
		public void setType(String type) { this.type = type; }
		public List<Object> getCoordinates() { return coordinates; }
		public void setCoordinates(List<Object> coordinates) { this.coordinates = coordinates; }
	}

	public static class RegionDto {
		private String uuid;
		private String name;          // flat scalar — must stay native
		private Address address;      // embedded POJO
		private List<Address> previous; // List of embedded POJOs
		private GeoShape geo;         // GeoJSON-shaped embedded POJO
		public String getUuid() { return uuid; }
		public void setUuid(String uuid) { this.uuid = uuid; }
		public String getName() { return name; }
		public void setName(String name) { this.name = name; }
		public Address getAddress() { return address; }
		public void setAddress(Address address) { this.address = address; }
		public List<Address> getPrevious() { return previous; }
		public void setPrevious(List<Address> previous) { this.previous = previous; }
		public GeoShape getGeo() { return geo; }
		public void setGeo(GeoShape geo) { this.geo = geo; }
	}

	/** Self-referential POJO used to prove the cycle guard. */
	public static class Node {
		private String label;
		private Node self;
		public String getLabel() { return label; }
		public void setLabel(String label) { this.label = label; }
		public Node getSelf() { return self; }
		public void setSelf(Node self) { this.self = self; }
	}

	public static class CyclicDto {
		private String uuid;
		private Node node;
		public String getUuid() { return uuid; }
		public void setUuid(String uuid) { this.uuid = uuid; }
		public Node getNode() { return node; }
		public void setNode(Node node) { this.node = node; }
	}

	@BeforeAll
	static void installReflection() {
		com.garganttua.core.bootstrap.dsl.Bootstrap.builder();
	}

	private MongoDatabase database;
	private MongoCollection<Document> collection;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private MongoDao daoFor(Class<?> dtoClass, String collectionName) {
		this.database = mock(MongoDatabase.class);
		this.collection = mock(MongoCollection.class);
		when(this.database.getCollection(collectionName)).thenReturn(this.collection);

		IDtoDefinition<Object> dtoDefinition = mock(IDtoDefinition.class);
		when(dtoDefinition.dtoClass()).thenReturn((IClass) IClass.getClass(dtoClass));
		when(dtoDefinition.uuid()).thenReturn(new ObjectAddress("uuid"));
		when(dtoDefinition.compositions()).thenReturn(List.of());
		IDomainDefinition domainDefinition = mock(IDomainDefinition.class);
		when(domainDefinition.dtoDefinitions()).thenReturn(List.of(dtoDefinition));

		MongoDao dao = new MongoDao(this.database, collectionName);
		dao.registerDomain(domainDefinition);
		return dao;
	}

	@SuppressWarnings("unchecked")
	private void stubFind(Document stored) {
		FindIterable<Document> find = mock(FindIterable.class);
		when(this.collection.find(any(Bson.class))).thenReturn(find);
		MongoCursor<Document> cursor = mock(MongoCursor.class);
		when(cursor.hasNext()).thenReturn(true, false);
		when(cursor.next()).thenReturn(stored);
		when(find.iterator()).thenReturn(cursor);
	}

	@Nested
	@DisplayName("write")
	class Write {

		@BeforeEach
		void setUp() {
		}

		@Test
		@DisplayName("an embedded POJO, a List<POJO> and a GeoJSON value object all become sub-documents — no POJO stored raw")
		void embedsAsSubDocuments() throws Exception {
			MongoDao dao = daoFor(RegionDto.class, "regions");

			Address main = new Address();
			main.setStreet("rue de Rivoli");
			main.setZip(75001);
			Address old = new Address();
			old.setStreet("quai Saint-Antoine");
			old.setZip(69001);
			GeoShape geo = new GeoShape();
			geo.setType("Polygon");
			geo.setCoordinates(List.of(List.of(2.0, 48.0), List.of(3.0, 49.0)));

			RegionDto dto = new RegionDto();
			dto.setUuid("r1");
			dto.setName("Île-de-France");
			dto.setAddress(main);
			dto.setPrevious(List.of(old));
			dto.setGeo(geo);

			Document doc = dao.dtoToDocument(dto);

			// flat scalar untouched
			assertEquals("Île-de-France", doc.get("name"));
			assertEquals("r1", doc.get("uuid"));
			assertEquals("r1", doc.get("_id"), "the uuid is still projected onto _id at the root");

			// embedded POJO → sub-document with concrete field values
			Document addressDoc = assertInstanceOf(Document.class, doc.get("address"),
					"the embedded Address must be a sub-document, not a raw POJO");
			assertEquals("rue de Rivoli", addressDoc.get("street"));
			assertEquals(75001, addressDoc.get("zip"));
			assertNull(addressDoc.get("_id"), "an embedded sub-document carries no _id (root only)");

			// List<POJO> → list of sub-documents
			List<?> previous = assertInstanceOf(List.class, doc.get("previous"));
			assertEquals(1, previous.size());
			Document oldDoc = assertInstanceOf(Document.class, previous.get(0),
					"each List<Address> element must be a sub-document");
			assertEquals("quai Saint-Antoine", oldDoc.get("street"));
			assertEquals(69001, oldDoc.get("zip"));

			// GeoJSON value object → {type, coordinates} sub-document, numbers preserved
			Document geoDoc = assertInstanceOf(Document.class, doc.get("geo"));
			assertEquals("Polygon", geoDoc.get("type"));
			List<?> coordinates = assertInstanceOf(List.class, geoDoc.get("coordinates"));
			assertEquals(2, coordinates.size());
			List<?> firstPosition = assertInstanceOf(List.class, coordinates.get(0));
			assertEquals(2.0, firstPosition.get(0), "the GeoJSON coordinate numbers survive verbatim");
			assertEquals(48.0, firstPosition.get(1));
		}

		@Test
		@DisplayName("a self-referential embedded POJO is refused with a parlant cycle error")
		void cycleRefused() {
			MongoDao dao = daoFor(CyclicDto.class, "cyclics");

			Node node = new Node();
			node.setLabel("loop");
			node.setSelf(node);
			CyclicDto dto = new CyclicDto();
			dto.setUuid("c1");
			dto.setNode(node);

			ApiException ex = assertThrows(ApiException.class, () -> dao.dtoToDocument(dto));
			assertTrue(ex.getMessage().contains("Cycle detected"),
					"the error must name the cycle; was: " + ex.getMessage());
			assertTrue(ex.getMessage().contains(Node.class.getName()),
					"the error must name the offending type; was: " + ex.getMessage());
		}
	}

	@Nested
	@DisplayName("read")
	class Read {

		@Test
		@DisplayName("sub-documents are rebuilt into the concrete declared types — Address, List<Address>, GeoShape — not raw Documents")
		void rebuildsConcreteTypes() throws Exception {
			MongoDao dao = daoFor(RegionDto.class, "regions");

			Document stored = new Document("uuid", "r1")
					.append("name", "Île-de-France")
					.append("address", new Document("street", "rue de Rivoli").append("zip", 75001))
					.append("previous", List.of(new Document("street", "quai Saint-Antoine").append("zip", 69001)))
					.append("geo", new Document("type", "Polygon")
							.append("coordinates", List.of(List.of(2.0, 48.0), List.of(3.0, 49.0))));
			stubFind(stored);

			List<Object> results = dao.find(Optional.empty(), Optional.empty(), Optional.empty());
			assertEquals(1, results.size());
			RegionDto dto = assertInstanceOf(RegionDto.class, results.get(0));

			assertEquals("r1", dto.getUuid());
			assertEquals("Île-de-France", dto.getName());

			Address address = dto.getAddress();
			assertInstanceOf(Address.class, address, "the sub-document must become a concrete Address, not a Document");
			assertEquals("rue de Rivoli", address.getStreet());
			assertEquals(75001, address.getZip(), "the int field is coerced back from the stored Integer");

			List<Address> previous = dto.getPrevious();
			assertEquals(1, previous.size());
			Address old = assertInstanceOf(Address.class, previous.get(0),
					"the List element type is recovered from the field's generic signature");
			assertEquals("quai Saint-Antoine", old.getStreet());
			assertEquals(69001, old.getZip());

			GeoShape geo = dto.getGeo();
			assertInstanceOf(GeoShape.class, geo);
			assertEquals("Polygon", geo.getType());
			assertEquals(2, geo.getCoordinates().size());
			List<?> firstPosition = assertInstanceOf(List.class, geo.getCoordinates().get(0));
			assertEquals(2.0, firstPosition.get(0));
			assertEquals(48.0, firstPosition.get(1));
		}
	}

	@Nested
	@DisplayName("backward compatibility")
	class Flat {

		public static class FlatDto {
			private String uuid;
			private String name;
			private int count;
			public String getUuid() { return uuid; }
			public void setUuid(String uuid) { this.uuid = uuid; }
			public String getName() { return name; }
			public void setName(String name) { this.name = name; }
			public int getCount() { return count; }
			public void setCount(int count) { this.count = count; }
		}

		@Test
		@DisplayName("a flat scalar entity round-trips unchanged — no sub-documents introduced")
		void flatEntityUnchanged() throws Exception {
			MongoDao dao = daoFor(FlatDto.class, "flats");

			FlatDto dto = new FlatDto();
			dto.setUuid("f1");
			dto.setName("plain");
			dto.setCount(3);

			Document doc = dao.dtoToDocument(dto);
			assertSame(String.class, doc.get("name").getClass(), "a flat String stays a String, not a sub-document");
			assertEquals("plain", doc.get("name"));
			assertEquals(3, doc.get("count"));
			assertEquals("f1", doc.get("_id"));

			Document stored = new Document("uuid", "f1").append("name", "plain").append("count", 3);
			stubFind(stored);
			List<Object> results = dao.find(Optional.empty(), Optional.empty(), Optional.empty());
			FlatDto read = assertInstanceOf(FlatDto.class, results.get(0));
			assertEquals("plain", read.getName());
			assertEquals(3, read.getCount());
		}
	}

	@Nested
	@DisplayName("geo bonus")
	class Geo {

		/** Mirrors MongoFilterGeoTest.geoFilter: a $field node with one comparison child. */
		private IFilter geoFilter(String operator, String field, Object geometry) {
			IFilter comparison = mock(IFilter.class);
			when(comparison.getName()).thenReturn(operator);
			when(comparison.getValue()).thenReturn(geometry);
			IFilter fieldFilter = mock(IFilter.class);
			when(fieldFilter.getName()).thenReturn("$field");
			when(fieldFilter.getValue()).thenReturn(field);
			when(fieldFilter.getFilters()).thenReturn(List.of(comparison));
			return fieldFilter;
		}

		@Test
		@DisplayName("a $geoWithin filter targets an embedded sub-document field by dotted path")
		void geoWithinOnEmbeddedField() throws Exception {
			org.geojson.Polygon square = new org.geojson.Polygon(
					new org.geojson.LngLatAlt(0, 0),
					new org.geojson.LngLatAlt(0, 1),
					new org.geojson.LngLatAlt(1, 1),
					new org.geojson.LngLatAlt(0, 0));

			Bson bson = MongoFilterConverter.convert(geoFilter("$geoWithin", "geo.coordinates", square));
			BsonDocument predicate = bson.toBsonDocument(BsonDocument.class, MongoClientSettings.getDefaultCodecRegistry());

			assertTrue(predicate.containsKey("geo.coordinates"),
					"the $geoWithin must key on the dotted sub-document path");
			BsonArray rings = predicate.getDocument("geo.coordinates").getDocument("$geoWithin")
					.getDocument("$geometry").getArray("coordinates");
			assertEquals(1, rings.size(), "a simple polygon has one exterior ring");
			assertEquals(4, rings.get(0).asArray().size(), "the closed ring keeps its positions");
		}
	}
}
