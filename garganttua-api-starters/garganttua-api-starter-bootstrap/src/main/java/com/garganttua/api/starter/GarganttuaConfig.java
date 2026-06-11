package com.garganttua.api.starter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.UnaryOperator;

import org.yaml.snakeyaml.Yaml;

import com.garganttua.api.commons.starter.IConfig;

/**
 * Externalized configuration assembled, in increasing precedence, from
 * {@code application.yaml} (or {@code .yml}), {@code application.properties}, and
 * environment variables. Files are looked up first on the classpath then in the
 * current working directory (CWD wins). An environment variable overrides any
 * file value: key {@code server.port} ← {@code GARGANTTUA_SERVER_PORT}
 * (prefix {@code GARGANTTUA_}, upper-cased, dots → underscores).
 *
 * <p>YAML is flattened to dotted keys; a YAML list is kept as a {@code List}
 * value so {@link #getStringList(String)} can return it verbatim.
 */
public final class GarganttuaConfig implements IConfig {

	private static final String ENV_PREFIX = "GARGANTTUA_";

	private final Map<String, Object> values;
	private final UnaryOperator<String> env;

	GarganttuaConfig(Map<String, Object> values) {
		this(values, System::getenv);
	}

	/** Test seam: injects the environment-variable lookup (default {@code System::getenv}). */
	GarganttuaConfig(Map<String, Object> values, UnaryOperator<String> env) {
		this.values = Objects.requireNonNull(values, "values cannot be null");
		this.env = Objects.requireNonNull(env, "env cannot be null");
	}

	/** Loads the merged configuration from the standard locations. */
	public static GarganttuaConfig load() {
		Map<String, Object> merged = new LinkedHashMap<>();
		// classpath first, CWD overrides — yaml then properties (properties win).
		mergeYaml(merged, classpath("application.yaml"));
		mergeYaml(merged, classpath("application.yml"));
		mergeYaml(merged, file("application.yaml"));
		mergeYaml(merged, file("application.yml"));
		mergeProperties(merged, classpath("application.properties"));
		mergeProperties(merged, file("application.properties"));
		return new GarganttuaConfig(merged);
	}

	@Override
	public Optional<String> getString(String key) {
		return raw(key).map(GarganttuaConfig::asScalar);
	}

	@Override
	public Optional<Boolean> getBoolean(String key) {
		return getString(key).map(s -> Boolean.parseBoolean(s.trim()));
	}

	@Override
	public Optional<Integer> getInt(String key) {
		return getString(key).map(s -> Integer.parseInt(s.trim()));
	}

	@Override
	public Optional<String[]> getStringList(String key) {
		String env = env(key);
		if (env != null) {
			return Optional.of(splitCsv(env));
		}
		Object value = this.values.get(key);
		if (value == null) {
			return Optional.empty();
		}
		if (value instanceof List<?> list) {
			List<String> out = new ArrayList<>(list.size());
			for (Object item : list) {
				out.add(String.valueOf(item).trim());
			}
			return Optional.of(out.toArray(new String[0]));
		}
		return Optional.of(splitCsv(String.valueOf(value)));
	}

	private Optional<Object> raw(String key) {
		String env = env(key);
		if (env != null) {
			return Optional.of(env);
		}
		return Optional.ofNullable(this.values.get(key));
	}

	private String env(String key) {
		String name = ENV_PREFIX + key.toUpperCase(Locale.ROOT).replace('.', '_');
		return this.env.apply(name);
	}

	private static String asScalar(Object value) {
		if (value instanceof List<?> list) {
			return list.isEmpty() ? "" : String.valueOf(list.get(0));
		}
		return String.valueOf(value);
	}

	private static String[] splitCsv(String csv) {
		String trimmed = csv.trim();
		if (trimmed.isEmpty()) {
			return new String[0];
		}
		String[] parts = trimmed.split(",");
		for (int i = 0; i < parts.length; i++) {
			parts[i] = parts[i].trim();
		}
		return parts;
	}

	@SuppressWarnings("unchecked")
	private static void mergeYaml(Map<String, Object> target, InputStream in) {
		if (in == null) {
			return;
		}
		try (InputStream stream = in) {
			Object root = new Yaml().load(stream);
			if (root instanceof Map<?, ?> map) {
				flatten("", (Map<String, Object>) map, target);
			}
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read YAML configuration: " + e.getMessage(), e);
		}
	}

	private static void mergeProperties(Map<String, Object> target, InputStream in) {
		if (in == null) {
			return;
		}
		try (InputStream stream = in) {
			Properties props = new Properties();
			props.load(stream);
			for (String name : props.stringPropertyNames()) {
				target.put(name, props.getProperty(name));
			}
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read properties configuration: " + e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private static void flatten(String prefix, Map<String, Object> source, Map<String, Object> target) {
		for (Map.Entry<String, Object> entry : source.entrySet()) {
			String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
			Object value = entry.getValue();
			if (value instanceof Map<?, ?> nested) {
				flatten(key, (Map<String, Object>) nested, target);
			} else {
				target.put(key, value);
			}
		}
	}

	private static InputStream classpath(String resource) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if (loader == null) {
			loader = GarganttuaConfig.class.getClassLoader();
		}
		return loader.getResourceAsStream(resource);
	}

	private static InputStream file(String name) {
		try {
			Path path = Path.of(name);
			return Files.exists(path) ? Files.newInputStream(path) : null;
		} catch (IOException e) {
			return null;
		}
	}
}
