package com.garganttua.api.spec.service;

import java.util.Map;

import com.garganttua.api.spec.context.IExecutionContext;
import com.garganttua.api.spec.context.Operation;

public interface IDomainServiceRuntime {

	@FunctionalInterface
	interface Allowed {
		boolean isAllowed();
	}

	IServiceResponse createEntity(IExecutionContext executionContext, Object entity);

	IServiceResponse getEntities(
			IExecutionContext executionContext);

	IServiceResponse getEntity(IExecutionContext executionContext, String uuid);

	IServiceResponse updateEntity(IExecutionContext executionContext, String uuid, Object entity);

	IServiceResponse deleteEntity(IExecutionContext executionContext, String uuid);

	IServiceResponse deleteAll(
			IExecutionContext executionContext);

	IServiceResponse executeServiceCommand(IExecutionContext executionContext, Allowed allowed, IServiceCommand command,
			Map<String, String> customParameters, Operation operation);

}
