package com.garganttua.api.commons.starter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.dao.IDaoFactory;
import com.garganttua.api.commons.endpoint.IInterface;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

/**
 * The handle an {@link IApiAutoConfiguration} receives to contribute to the API
 * under construction: the {@link IApiBuilder} being configured, the parsed
 * {@link IConfig}, and convenience registrations for a default DAO factory, a
 * default HTTP interface, and resources to close on shutdown.
 */
public final class AutoConfigurationContext {

	private final IApiBuilder apiBuilder;
	private final IConfig config;
	private final List<AutoCloseable> resources = new ArrayList<>();

	public AutoConfigurationContext(IApiBuilder apiBuilder, IConfig config) {
		this.apiBuilder = Objects.requireNonNull(apiBuilder, "apiBuilder cannot be null");
		this.config = Objects.requireNonNull(config, "config cannot be null");
	}

	public IApiBuilder apiBuilder() {
		return this.apiBuilder;
	}

	public IConfig config() {
		return this.config;
	}

	/** Registers the DAO factory consulted for any dto with no explicit {@code .db(...)}. */
	public void registerDefaultDao(IDaoFactory factory) throws ApiException {
		this.apiBuilder.defaultDao(Objects.requireNonNull(factory, "factory cannot be null"));
	}

	/** Registers the interface attached to every scanned domain with no explicit {@code .interfasse(...)}. */
	public void registerDefaultInterface(
			ISupplierBuilder<? extends IInterface, ? extends ISupplier<? extends IInterface>> iface)
			throws ApiException {
		this.apiBuilder.defaultInterface(Objects.requireNonNull(iface, "interface cannot be null"));
	}

	/** Registers a resource (e.g. a {@code MongoClient}) closed when the application stops. */
	public void registerResource(AutoCloseable resource) {
		this.resources.add(Objects.requireNonNull(resource, "resource cannot be null"));
	}

	/** Closes every registered resource, swallowing individual failures. */
	public void closeResources() {
		for (AutoCloseable resource : this.resources) {
			try {
				resource.close();
			} catch (Exception ignored) {
				// best-effort shutdown — a failing close must not mask the others
			}
		}
	}
}
