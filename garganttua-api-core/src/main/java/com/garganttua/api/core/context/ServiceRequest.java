package com.garganttua.api.core.context;

import com.garganttua.api.spec.context.Operation;
import com.garganttua.api.spec.service.IServiceRequest;

public record ServiceRequest(
		Operation operation,
		Object[] args
) implements IServiceRequest {

	public static IServiceRequest of(Operation operation, Object... args) {
		return new ServiceRequest(operation, args);
	}

	public static IServiceRequest noArgs(Operation operation) {
		return new ServiceRequest(operation, new Object[0]);
	}

}
