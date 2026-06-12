package com.garganttua.dao.mongodb;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.definition.IDtoDefinition;
import com.garganttua.core.crypto.IKey;
import com.garganttua.core.crypto.IKeyAlgorithm;
import com.garganttua.core.crypto.KeyAlgorithm;
import com.garganttua.core.crypto.KeyType;
import com.garganttua.core.crypto.SignatureAlgorithm;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

/**
 * End-to-end persistence of an {@link IKey}-typed DTO field through {@link MongoDao}: on write the
 * key becomes a self-describing sub-document (so the driver never sees a raw {@code crypto.Key} it
 * has no codec for); on read it is reconstructed back into an {@code IKey}. The DTO field stays
 * {@code IKey}-typed — the conversion is purely persistence.
 */
@DisplayName("MongoDao — IKey key material round-trips as a sub-document")
class MongoDaoKeyMaterialTest {

	public static class KeyDto {
		private String uuid;
		private IKey signingKey;        // private material
		private IKey verificationKey;   // public material
		public String getUuid() { return uuid; }
		public void setUuid(String uuid) { this.uuid = uuid; }
		public IKey getSigningKey() { return signingKey; }
		public void setSigningKey(IKey signingKey) { this.signingKey = signingKey; }
		public IKey getVerificationKey() { return verificationKey; }
		public void setVerificationKey(IKey verificationKey) { this.verificationKey = verificationKey; }
	}

	@BeforeAll
	static void installReflection() {
		com.garganttua.core.bootstrap.dsl.Bootstrap.builder();
	}

	private MongoDatabase database;
	private MongoCollection<Document> collection;
	private MongoDao dao;
	private KeyPair pair;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setUp() throws Exception {
		this.database = mock(MongoDatabase.class);
		this.collection = mock(MongoCollection.class);
		when(this.database.getCollection("keys")).thenReturn(this.collection);

		IDtoDefinition<Object> dtoDefinition = mock(IDtoDefinition.class);
		when(dtoDefinition.dtoClass()).thenReturn((IClass) IClass.getClass(KeyDto.class));
		when(dtoDefinition.uuid()).thenReturn(new ObjectAddress("uuid"));
		when(dtoDefinition.compositions()).thenReturn(List.of());
		IDomainDefinition domainDefinition = mock(IDomainDefinition.class);
		when(domainDefinition.dtoDefinitions()).thenReturn(List.of(dtoDefinition));

		this.dao = new MongoDao(this.database, "keys");
		this.dao.registerDomain(domainDefinition);

		KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
		generator.initialize(new ECGenParameterSpec("secp256r1"));
		this.pair = generator.generateKeyPair();
	}

	private IKey signingKey(KeyType type, byte[] material) {
		IKeyAlgorithm algorithm = KeyAlgorithm.validateKeyAlgorithm("EC-256");
		return com.garganttua.core.crypto.Key.fromSigningMaterial(type, algorithm, SignatureAlgorithm.SHA256, material);
	}

	@Test
	@DisplayName("write: an IKey field is stored as a marked sub-document, never the raw key")
	void writeStoresKeyAsSubDocument() throws Exception {
		KeyDto dto = new KeyDto();
		dto.setUuid("key-1");
		dto.setSigningKey(signingKey(KeyType.PRIVATE, pair.getPrivate().getEncoded()));

		Document document = dao.dtoToDocument(dto);

		Object stored = document.get("signingKey");
		assertFalse(stored instanceof IKey, "the raw IKey must NOT be handed to the driver (no codec for it)");
		Document keyDoc = assertInstanceOf(Document.class, stored);
		assertEquals(Boolean.TRUE, keyDoc.get(IKeyBsonBridge.MARKER));
		assertEquals("PRIVATE", keyDoc.getString("type"));
		assertEquals("EC-256", keyDoc.getString("algorithm"));
		assertEquals("key-1", document.get("uuid"), "non-key fields are stored verbatim");
	}

	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("read: the stored sub-documents are reconstructed into byte-identical IKeys")
	void readReconstructsKeys() throws Exception {
		IKey originalSigning = signingKey(KeyType.PRIVATE, pair.getPrivate().getEncoded());
		IKey originalVerification = signingKey(KeyType.PUBLIC, pair.getPublic().getEncoded());

		Document stored = new Document("uuid", "key-1")
				.append("signingKey", IKeyBsonBridge.toDocument(originalSigning))
				.append("verificationKey", IKeyBsonBridge.toDocument(originalVerification));

		FindIterable<Document> find = mock(FindIterable.class);
		when(collection.find(any(Bson.class))).thenReturn(find);
		MongoCursor<Document> cursor = mock(MongoCursor.class);
		when(cursor.hasNext()).thenReturn(true, false);
		when(cursor.next()).thenReturn(stored);
		when(find.iterator()).thenReturn(cursor);

		List<Object> results = dao.find(Optional.empty(), Optional.empty(), Optional.empty());

		assertEquals(1, results.size());
		KeyDto dto = assertInstanceOf(KeyDto.class, results.get(0));
		assertEquals("key-1", dto.getUuid());

		assertInstanceOf(IKey.class, dto.getSigningKey());
		assertEquals(KeyType.PRIVATE, dto.getSigningKey().getType());
		assertArrayEquals(originalSigning.getKey().getEncoded(), dto.getSigningKey().getKey().getEncoded(),
				"the private signing key must reconstruct byte-identically");
		assertInstanceOf(IKey.class, dto.getVerificationKey());
		assertArrayEquals(originalVerification.getKey().getEncoded(), dto.getVerificationKey().getKey().getEncoded(),
				"the public verification key must reconstruct byte-identically");
	}
}
