package com.garganttua.api.starter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.starter.AutoConfigurationContext;
import com.garganttua.api.commons.starter.IApiAutoConfiguration;
import com.garganttua.api.core.api.ApiBuilder;
import com.garganttua.core.bootstrap.dsl.Bootstrap;
import com.garganttua.core.bootstrap.dsl.IBootstrap;
import com.garganttua.core.bootstrap.dsl.IBuiltRegistry;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.dsl.IBuilder;

/**
 * Spring-Boot-style entry point: one call boots a fully wired, running API.
 *
 * <pre>{@code
 * public final class MyApp {
 *     public static void main(String[] args) {
 *         GarganttuaApplication.run(MyApp.class, args);
 *     }
 * }
 * }</pre>
 *
 * <p>{@link #run} reads {@link GarganttuaConfig externalized config}, applies
 * top-level API settings, runs every {@link IApiAutoConfiguration} discovered on
 * the classpath (persistence, transport, …), then drives garganttua-core's
 * {@code Bootstrap} — which installs the reflection stack and auto-wires the
 * injection/expression/runtimes/scripts/workflows builders — and finally
 * {@code onInit()/onStart()}s the API. Domains, DAOs and HTTP interfaces are
 * discovered from {@code @Entity}/{@code @Dto} annotations and from the
 * starters on the classpath, so a typical app writes no {@code ApiBuilder} DSL.
 */
public final class GarganttuaApplication {

	private GarganttuaApplication() {
	}

	/** Builds, initializes and starts the API, then returns it without blocking. */
	public static IApi run(Class<?> source, String... args) {
		GarganttuaConfig config = GarganttuaConfig.load();
		String[] packages = config.getStringList("api.packages")
				.orElseGet(() -> new String[] { source.getPackageName() });

		// Bootstrap.builder() runs garganttua-core's ServiceLoader cold-start,
		// which installs the IReflection stack globally — this MUST happen before
		// ApiBuilder.builder(), which needs reflection to declare its dependencies.
		IBootstrap bootstrap = Bootstrap.builder();

		IApiBuilder apiBuilder = ApiBuilder.builder();
		// Enable the ApiBuilder's own auto-detection so its build() scans the
		// declared packages for @Entity/@Dto (and security) annotations.
		((com.garganttua.core.dsl.IAutomaticBuilder<?, ?>) apiBuilder).autoDetect(true);
		config.getBoolean("api.multiTenant").ifPresent(apiBuilder::multiTenant);
		config.getString("api.superTenantId").ifPresent(apiBuilder::superTenantId);
		config.getBoolean("api.superTenantAutoCreate").ifPresent(apiBuilder::superTenantAutoCreate);
		apiBuilder.packages(packages);

		AutoConfigurationContext context = new AutoConfigurationContext(apiBuilder, config);
		applyAutoConfigurations(context);

		// Bootstrap's build() already onInit()s and onStart()s every lifecycle
		// object it builds (the IApi included), so the API is running on return.
		IApi api = build(bootstrap, source, apiBuilder, packages);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				api.onStop();
			} finally {
				context.closeResources();
			}
		}, "garganttua-shutdown"));
		return api;
	}

	/** Same as {@link #run} but blocks the calling thread until JVM shutdown. */
	public static void runAndWait(Class<?> source, String... args) {
		run(source, args);
		try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private static void applyAutoConfigurations(AutoConfigurationContext context) {
		List<IApiAutoConfiguration> autoConfigs = new ArrayList<>();
		ServiceLoader.load(IApiAutoConfiguration.class).forEach(autoConfigs::add);
		autoConfigs.sort(Comparator.comparingInt(IApiAutoConfiguration::order));
		for (IApiAutoConfiguration autoConfig : autoConfigs) {
			autoConfig.apply(context);
		}
	}

	private static IApi build(IBootstrap bootstrap, Class<?> source, IApiBuilder apiBuilder, String[] packages) {
		try {
			// autoDetect(true) enables the SPI fallback; withBuilder registers our
			// configured ApiBuilder BEFORE load() so the SPI doesn't create a second
			// one; load() then discovers the injection/expression/runtimes/scripts/
			// workflows builders (via IBootstrapBuilderFactory) and feeds them to it.
			bootstrap.autoDetect(true)
					.withApplicationName(source.getSimpleName())
					.withBuilder((IBuilder<?>) apiBuilder)
					.withPackages(packages)
					.load();
			IBuiltRegistry registry = bootstrap.build();
			// The registry is keyed by concrete class (Api), so request(IApi) misses;
			// pick the IApi out of the built objects to stay free of the impl class.
			return registry.toList().stream()
					.filter(IApi.class::isInstance)
					.map(IApi.class::cast)
					.findFirst()
					.orElseThrow(() -> new ApiException(
							"Bootstrap produced no IApi — is garganttua-api-core on the classpath?"));
		} catch (DslException e) {
			throw new ApiException("Bootstrap failed to assemble the API: " + e.getMessage(), e);
		}
	}
}
