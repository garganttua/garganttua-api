package com.garganttua.api.core.service;

import com.garganttua.api.core.caller.Caller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.operation.OperationPath;
import com.garganttua.api.commons.service.ArgKey;
import com.garganttua.api.commons.service.IOperationRequest;

public class OperationRequest implements IOperationRequest {

	private final Map<String, Object> args;

	public OperationRequest(Map<String, Object> args) {
		this.args = args != null ? new HashMap<>(args) : new HashMap<>();
	}

	@Override
	public Map<String, Object> args() {
		return this.args;
	}

	@Override
	public <T> Optional<T> arg(ArgKey<T> key) {
		T value = (T) this.args.get(key.name());
		return Optional.ofNullable(value);
	}

	@Override
	public <T> void arg(ArgKey<T> key, T value) {
		this.args.put(key.name(), value);
	}

	@Override
	public String domain() {
		OperationPath path = operationPath();
		return path != null ? path.domain() : null;
	}

	@Override
	public ICaller caller() {
		return new Caller(
				arg(TENANT_ID).orElse(null),
				arg(REQUESTED_TENANT_ID).orElse(null),
				arg(CALLER_ID).orElse(null),
				arg(OWNER_ID).orElse(null),
				arg(SUPER_TENANT).orElse(null) == Boolean.TRUE,
				arg(SUPER_OWNER).orElse(null) == Boolean.TRUE,
				(List<String>) arg(AUTHORITIES).orElse(null));
	}

	@Override
	public OperationDefinition operation() {
		return arg(OPERATION).orElse(null);
	}

	@Override
	public OperationPath operationPath() {
		String path = arg(PATH).orElse(null);
		return path != null ? new OperationPath(path) : null;
	}

	@Override
	public Optional<?> arg(String key) {
		return Optional.ofNullable(this.args.get(key));
	}

	@Override
	public void arg(String key, Object value) {
		this.args.put(key, value);
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
