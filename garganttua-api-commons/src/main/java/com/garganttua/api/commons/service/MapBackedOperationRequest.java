package com.garganttua.api.commons.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.operation.OperationPath;

/**
 * The map-backed {@link IOperationRequest} returned by
 * {@link IOperationRequest#create()}. This is a NAMED class on purpose: the
 * previous anonymous implementation compiled to {@code IOperationRequest$1},
 * which the AOT/native pipeline could not resolve by name (GraalVM reported
 * "Cannot resolve class: …IOperationRequest$1").
 *
 * <p>Its native descriptor is shipped as a static reflect-config under
 * {@code META-INF/native-image} (see this package's {@code reflect-config.json})
 * rather than via {@code @Reflected}: the AOT annotation processor currently
 * mis-generates descriptors for generic methods (it emits a {@code (T)} cast to
 * the erased type variable), and {@link #arg(ArgKey, Object)} is generic. The
 * stable class name keeps the reflect-config robust (unlike the {@code $1}
 * ordinal it replaces).
 */
final class MapBackedOperationRequest implements IOperationRequest {

	private final Map<String, Object> map = new HashMap<>();

	@Override
	public Map<String, Object> args() {
		return this.map;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> arg(ArgKey<T> key) {
		return Optional.ofNullable((T) this.map.get(key.name()));
	}

	@Override
	public <T> void arg(ArgKey<T> key, T value) {
		this.map.put(key.name(), value);
	}

	@Override
	public String domain() {
		OperationPath path = operationPath();
		return path != null ? path.domain() : null;
	}

	@Override
	public ICaller caller() {
		return new MapBackedCaller(this);
	}

	@Override
	public OperationDefinition operation() {
		return arg(OPERATION).orElse(null);
	}

	@Override
	public OperationPath operationPath() {
		return arg(PATH).map(OperationPath::new).orElse(null);
	}

	@Override
	public UUID executionUuid() {
		return arg(EXECUTION_UUID).orElse(null);
	}

	@Override
	public UUID correlationUuid() {
		return arg(CORRELATION_UUID).orElse(null);
	}
}
