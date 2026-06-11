package com.garganttua.api.starter.mongodb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.net.InetSocketAddress;
import java.net.Socket;

import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.dto.annotations.Dto;
import com.garganttua.api.commons.dto.annotations.DtoId;
import com.garganttua.api.commons.dto.annotations.DtoUuid;
import com.garganttua.api.commons.entity.annotations.Entity;
import com.garganttua.api.commons.entity.annotations.EntityId;
import com.garganttua.api.commons.entity.annotations.EntityPublic;
import com.garganttua.api.commons.entity.annotations.EntityUuid;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.starter.GarganttuaApplication;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;

/**
 * Proves the MongoDB starter end to end against a REAL mongod: boot via
 * {@link GarganttuaApplication}, persist through the auto-wired default
 * {@code MongoDao} (no {@code .db(...)} anywhere), and confirm the document
 * actually lands in the {@code widgets} collection. Skipped when no mongod is
 * reachable on {@code localhost:27017}.
 */
@DisplayName("GarganttuaApplication — MongoDB starter against a real mongod")
@TestInstance(Lifecycle.PER_CLASS)
class GarganttuaApplicationMongoTest {

	private static final String URI = "mongodb://localhost:27017";
	private static final String DATABASE = "garganttua_starter_it";

	private IApi api;
	private final ICaller caller = Caller.createAnonymousCaller();

	static final class TestApp {
	}

	@Entity
	@EntityPublic
	public static class Widget {
		@EntityId
		private String id;
		@EntityUuid
		private String uuid;
		private String name;

		public String getId() { return this.id; }
		public void setId(String id) { this.id = id; }
		public String getUuid() { return this.uuid; }
		public void setUuid(String uuid) { this.uuid = uuid; }
		public String getName() { return this.name; }
		public void setName(String name) { this.name = name; }
	}

	@Dto(entityClass = Widget.class)
	public static class WidgetDto {
		@DtoId
		private String id;
		@DtoUuid
		private String uuid;
		private String name;

		public String getId() { return this.id; }
		public void setId(String id) { this.id = id; }
		public String getUuid() { return this.uuid; }
		public void setUuid(String uuid) { this.uuid = uuid; }
		public String getName() { return this.name; }
		public void setName(String name) { this.name = name; }
	}

	@BeforeAll
	void boot() {
		assumeTrue(mongoReachable(), "no mongod on localhost:27017 — skipping the real MongoDB integration test");
		dropDatabase();
		this.api = GarganttuaApplication.run(TestApp.class);
	}

	@AfterAll
	void shutdown() {
		if (this.api != null) {
			this.api.onStop();
		}
		dropDatabase();
	}

	@Test
	@DisplayName("createOne persists a document the MongoDB driver can read back")
	void persistsToMongo() {
		IDomain<?> widgets = api.getDomain("widgets").orElseThrow();

		Widget widget = new Widget();
		widget.setName("gizmo");
		IOperationResponse created = widgets.createOne(widget, caller);
		assertEquals(OperationResponseCode.CREATED, created.getResponseCode(),
				"create should succeed; response=" + created.getResponse());

		IOperationResponse all = widgets.readAll(caller);
		assertEquals(OperationResponseCode.OK, all.getResponseCode(),
				"readAll should succeed; response=" + all.getResponse());

		// Strongest proof: read the raw document straight from mongod.
		try (MongoClient client = MongoClients.create(URI)) {
			MongoCollection<Document> collection = client.getDatabase(DATABASE).getCollection("widgets");
			assertEquals(1L, collection.countDocuments(), "exactly one widget should be persisted");
			Document stored = collection.find().first();
			assertNotNull(stored, "the widget document should exist in mongod");
			assertEquals("gizmo", stored.getString("name"), "the persisted name must round-trip");
			assertTrue(stored.getString("uuid") != null && !stored.getString("uuid").isBlank(),
					"a uuid should have been generated and persisted");
		}
	}

	private static boolean mongoReachable() {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress("localhost", 27017), 500);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static void dropDatabase() {
		try (MongoClient client = MongoClients.create(URI)) {
			client.getDatabase(DATABASE).drop();
		} catch (Exception ignored) {
			// best-effort cleanup
		}
	}
}
