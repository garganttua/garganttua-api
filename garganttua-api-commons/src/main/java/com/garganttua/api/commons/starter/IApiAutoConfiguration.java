package com.garganttua.api.commons.starter;

import com.garganttua.api.commons.ApiException;

/**
 * Spring-Boot-style auto-configuration contributed by a starter on the
 * classpath. The bootstrap runner discovers every implementation via
 * {@link java.util.ServiceLoader} (registered under
 * {@code META-INF/services/com.garganttua.api.commons.starter.IApiAutoConfiguration}),
 * sorts them by {@link #order()}, and invokes {@link #apply} on each before the
 * API is built — so simply adding a starter jar wires its persistence/transport
 * with no code from the user.
 */
public interface IApiAutoConfiguration {

	/** Lower runs first. Convention: persistence {@code 0}, transport {@code 100}. */
	default int order() {
		return 0;
	}

	void apply(AutoConfigurationContext context) throws ApiException;
}
