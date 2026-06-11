package com.garganttua.api.starter.javalin;

import com.garganttua.api.binding.javalin.JavalinInterface;
import com.garganttua.api.binding.javalin.JavalinProtocol;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.starter.AutoConfigurationContext;
import com.garganttua.api.commons.starter.IApiAutoConfiguration;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;

/**
 * Auto-configures the Javalin HTTP transport: reads {@code server.port}
 * (default {@link JavalinInterface#DEFAULT_PORT}), creates a single shared
 * {@link JavalinInterface} registered as the default interface for every domain
 * (its {@code handle(domain)} is per-domain and {@code onStart()} is idempotent,
 * so one server serves them all), and registers the {@link JavalinProtocol}
 * adapter (which is not {@code @Protocol}-annotated, hence not auto-detected).
 *
 * <p>Runs at {@code order() = 100} (transport after persistence). Discovered via
 * {@link java.util.ServiceLoader}.
 */
public final class JavalinAutoConfiguration implements IApiAutoConfiguration {

	@Override
	public int order() {
		return 100;
	}

	@Override
	public void apply(AutoConfigurationContext context) throws ApiException {
		int port = context.config().getInt("server.port").orElse(JavalinInterface.DEFAULT_PORT);

		JavalinInterface shared = new JavalinInterface(port);
		context.registerDefaultInterface(FixedSupplierBuilder.of(shared));

		context.apiBuilder().protocol(new JavalinProtocol());
	}
}
