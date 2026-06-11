package com.garganttua.api.commons.starter;

import java.util.Optional;

/**
 * Read-only view over the externalized application configuration (assembled
 * from {@code application.yaml}, {@code application.properties} and environment
 * variables by the bootstrap starter). Keys are dotted, e.g.
 * {@code "server.port"}, {@code "mongodb.uri"}, {@code "api.packages"}.
 *
 * <p>Lives in commons so an auto-configuration ({@link IApiAutoConfiguration})
 * shipped by any add-on starter can read config without depending on the
 * bootstrap starter that produces it.
 */
public interface IConfig {

	Optional<String> getString(String key);

	Optional<Boolean> getBoolean(String key);

	Optional<Integer> getInt(String key);

	/** A YAML list, or a comma-separated scalar, as a trimmed {@code String[]}. */
	Optional<String[]> getStringList(String key);
}
