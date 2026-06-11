package com.garganttua.api.starter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("GarganttuaConfig")
class GarganttuaConfigTest {

	private static final UnaryOperator<String> NO_ENV = name -> null;

	@Nested
	@DisplayName("environment-variable override")
	class EnvOverride {

		@Test
		@DisplayName("GARGANTTUA_SERVER_PORT (9090) overrides a file server.port (7000)")
		void envWins() {
			GarganttuaConfig config = new GarganttuaConfig(
					Map.of("server.port", "7000"),
					name -> "GARGANTTUA_SERVER_PORT".equals(name) ? "9090" : null);

			assertEquals(9090, config.getInt("server.port").orElseThrow());
		}

		@Test
		@DisplayName("with no env var, the file value (7000) is returned")
		void fileFallback() {
			GarganttuaConfig config = new GarganttuaConfig(Map.of("server.port", "7000"), NO_ENV);

			assertEquals(7000, config.getInt("server.port").orElseThrow());
		}

		@Test
		@DisplayName("an env var supplies a comma-separated list when no file value exists")
		void envList() {
			GarganttuaConfig config = new GarganttuaConfig(
					Map.of(),
					name -> "GARGANTTUA_API_PACKAGES".equals(name) ? "com.x, com.y" : null);

			assertArrayEquals(new String[] { "com.x", "com.y" },
					config.getStringList("api.packages").orElseThrow());
		}
	}

	@Nested
	@DisplayName("typed accessors")
	class Typed {

		@Test
		@DisplayName("a YAML list is returned element-for-element")
		void yamlList() {
			GarganttuaConfig config = new GarganttuaConfig(
					Map.of("api.packages", List.of("com.a", "com.b")), NO_ENV);

			assertArrayEquals(new String[] { "com.a", "com.b" },
					config.getStringList("api.packages").orElseThrow());
		}

		@Test
		@DisplayName("a comma-separated scalar is split and trimmed")
		void csvScalar() {
			GarganttuaConfig config = new GarganttuaConfig(
					Map.of("api.packages", "com.a, com.b ,com.c"), NO_ENV);

			assertArrayEquals(new String[] { "com.a", "com.b", "com.c" },
					config.getStringList("api.packages").orElseThrow());
		}

		@Test
		@DisplayName("booleans parse and absent keys are empty")
		void booleansAndMissing() {
			GarganttuaConfig config = new GarganttuaConfig(Map.of("api.multiTenant", "false"), NO_ENV);

			assertFalse(config.getBoolean("api.multiTenant").orElseThrow());
			assertTrue(config.getString("does.not.exist").isEmpty());
			assertTrue(config.getInt("does.not.exist").isEmpty());
			assertTrue(config.getStringList("does.not.exist").isEmpty());
		}
	}

	@Nested
	@DisplayName("load() merges the standard locations")
	class Load {

		@Test
		@DisplayName("application.properties (8080) wins over application.yaml (7000); the yaml list survives")
		void propertiesWinOverYaml() {
			GarganttuaConfig config = GarganttuaConfig.load();

			assertEquals(8080, config.getInt("server.port").orElseThrow(),
					"the properties file should override the yaml server.port");
			assertEquals(Boolean.FALSE, config.getBoolean("api.multiTenant").orElseThrow());
			assertArrayEquals(new String[] { "com.example.one", "com.example.two" },
					config.getStringList("api.packages").orElseThrow(),
					"api.packages should come from the yaml list");
		}
	}
}
