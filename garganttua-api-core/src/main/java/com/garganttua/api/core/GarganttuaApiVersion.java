package com.garganttua.api.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.garganttua.core.observability.Logger;

/**
 * Provides version information for Garganttua API.
 *
 * <p>Reads from {@code garganttua-api-version.properties} which Maven filters
 * at build time (see {@code garganttua-api-core/pom.xml}). Modelled on
 * {@code com.garganttua.core.bootstrap.GarganttuaVersion} so the same banner
 * customisation pattern applies symmetrically across both layers.
 *
 * @since 3.0.0-ALPHA01
 */
public final class GarganttuaApiVersion {
	private static final Logger log = Logger.getLogger(GarganttuaApiVersion.class);


	private static final String PROPERTIES_FILE = "garganttua-api-version.properties";
	private static final String UNKNOWN = "UNKNOWN";

	private static final Properties PROPERTIES = new Properties();
	private static boolean loaded = false;

	static {
		loadProperties();
	}

	private GarganttuaApiVersion() {
	}

	private static synchronized void loadProperties() {
		if (loaded) {
			return;
		}
		try (InputStream is = GarganttuaApiVersion.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
			if (is != null) {
				PROPERTIES.load(is);
				log.debug("Loaded Garganttua API version properties: {}", PROPERTIES);
			} else {
				log.warn("Garganttua API version properties file not found: {}", PROPERTIES_FILE);
			}
		} catch (IOException e) {
			log.warn("Failed to load Garganttua API version properties", e);
		}
		loaded = true;
	}

	/** Returns the Garganttua API version (e.g., {@code "3.0.0-ALPHA01"}). */
	public static String getVersion() {
		return PROPERTIES.getProperty("garganttua.api.version", UNKNOWN);
	}

	/** Returns the Garganttua API name (e.g., {@code "Garganttua API"}). */
	public static String getName() {
		return PROPERTIES.getProperty("garganttua.api.name", "Garganttua API");
	}

	/** Returns the Maven group ID (e.g., {@code "com.garganttua"}). */
	public static String getGroupId() {
		return PROPERTIES.getProperty("garganttua.api.groupId", UNKNOWN);
	}

	/** Returns the Maven artifact ID (e.g., {@code "garganttua-api-core"}). */
	public static String getArtifactId() {
		return PROPERTIES.getProperty("garganttua.api.artifactId", UNKNOWN);
	}

	/** Returns a formatted string like {@code "Garganttua API v3.0.0-ALPHA01"}. */
	public static String getFullVersion() {
		return getName() + " v" + getVersion();
	}
}
