package com.garganttua.api.starter.javalin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.dto.annotations.Dto;
import com.garganttua.api.commons.dto.annotations.DtoId;
import com.garganttua.api.commons.dto.annotations.DtoUuid;
import com.garganttua.api.commons.entity.annotations.Entity;
import com.garganttua.api.commons.entity.annotations.EntityId;
import com.garganttua.api.commons.entity.annotations.EntityPublic;
import com.garganttua.api.commons.entity.annotations.EntityUuid;
import com.garganttua.api.starter.GarganttuaApplication;

/**
 * Proves the Spring-Boot-style experience end to end: annotate {@code @Entity}/
 * {@code @Dto}, drop an {@code application.yaml}, call
 * {@link GarganttuaApplication#run}, and a real Javalin server serves CRUD over
 * HTTP — no {@code ApiBuilder} DSL, no explicit {@code .db(...)} (the in-memory
 * default-DAO auto-config supplies it) and no explicit {@code .interfasse(...)}
 * (the Javalin default-interface auto-config attaches it).
 */
@DisplayName("GarganttuaApplication — end-to-end over real Javalin HTTP")
@TestInstance(Lifecycle.PER_CLASS)
class GarganttuaApplicationE2ETest {

	private static final String BASE = "http://localhost:7099/widgets";

	private IApi api;
	private final HttpClient http = HttpClient.newHttpClient();

	/** A bootstrap source whose package is irrelevant — api.packages drives the scan. */
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
		this.api = GarganttuaApplication.run(TestApp.class);
	}

	@AfterAll
	void shutdown() {
		if (this.api != null) {
			this.api.onStop();
		}
	}

	@Nested
	@DisplayName("CRUD round-trip")
	class CrudRoundTrip {

		@Test
		@DisplayName("POST creates (201) then GET lists it back (200) with the concrete name")
		void postThenGet() throws Exception {
			HttpResponse<String> created = http.send(HttpRequest.newBuilder()
					.uri(URI.create(BASE))
					.header("Content-Type", "application/json")
					.header("Accept", "application/json")
					.POST(BodyPublishers.ofString("{\"name\":\"alice\"}"))
					.build(), BodyHandlers.ofString());

			assertEquals(201, created.statusCode(),
					"create should map to HTTP 201 CREATED; body=" + created.body());
			assertTrue(created.body().contains("\"name\":\"alice\""),
					"created body should echo the name; was: " + created.body());

			HttpResponse<String> all = http.send(HttpRequest.newBuilder()
					.uri(URI.create(BASE))
					.header("Accept", "application/json")
					.GET()
					.build(), BodyHandlers.ofString());

			assertEquals(200, all.statusCode(), "readAll should map to HTTP 200; body=" + all.body());
			assertTrue(all.body().contains("\"name\":\"alice\""),
					"readAll body should contain the created widget; was: " + all.body());
			// A uuid was generated server-side and persisted through the default DAO.
			assertTrue(all.body().contains("\"uuid\":\""),
					"readAll body should carry a generated uuid; was: " + all.body());
		}

		@Test
		@DisplayName("the API booted and exposes the auto-discovered 'widgets' domain")
		void domainDiscovered() {
			assertNotNull(api.getDomain("widgets").orElse(null),
					"the @Entity Widget should have produced a 'widgets' domain with no DSL");
		}
	}
}
