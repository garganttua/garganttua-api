package com.garganttua.api.spec.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.context.Operation;
import com.garganttua.api.spec.context.OperationPath;
import com.garganttua.api.spec.context.TechnicalOperation;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.security.authorization.IAuthorization;
import com.garganttua.api.spec.sort.ISort;

public interface IOperationRequest {

	// --- Typed argument keys ---

	ArgKey<String> PATH = ArgKey.of("path", String.class);
	ArgKey<TechnicalOperation> TECHNICAL_OPERATION = ArgKey.of("technicalOperation", TechnicalOperation.class);
	ArgKey<Operation> OPERATION = ArgKey.of("operation", Operation.class);

	ArgKey<Object> RAW_REQUEST = ArgKey.of("rawRequest", Object.class);
	ArgKey<Byte[]> RAW_BODY = ArgKey.of("rawBody", Byte[].class);
	ArgKey<Object> BODY = ArgKey.of("body", Object.class);
	ArgKey<IFilter> FILTER = ArgKey.of("filter", IFilter.class);
	ArgKey<IPageable> PAGE = ArgKey.of("page", IPageable.class);
	ArgKey<ISort> SORT = ArgKey.of("sort", ISort.class);

	ArgKey<String> CALLER_ID = ArgKey.of("callerId", String.class);
	ArgKey<String> TENANT_ID = ArgKey.of("tenantId", String.class);
	ArgKey<String> REQUESTED_TENANT_ID = ArgKey.of("requestedTenantId", String.class);
	ArgKey<String> OWNER_ID = ArgKey.of("ownerId", String.class);
	ArgKey<String> REQUESTED_OWNER_ID = ArgKey.of("requestedOwnerId", String.class);

	ArgKey<Boolean> SUPER_TENANT = ArgKey.of("superTenant", Boolean.class);
	ArgKey<Boolean> SUPER_OWNER = ArgKey.of("superOwner", Boolean.class);

	@SuppressWarnings("rawtypes")
	ArgKey<List> AUTHORITIES = ArgKey.of("authorities", List.class);

	ArgKey<UUID> EXECUTION_UUID = ArgKey.of("executionUuid", UUID.class);
	ArgKey<UUID> CORRELATION_UUID = ArgKey.of("correlationUuid", UUID.class);

	ArgKey<IAuthorization> AUTHORIZATION = ArgKey.of("authorization", IAuthorization.class);
	ArgKey<Byte[]> RAW_AUTHORIZATION = ArgKey.of("rawAuthorization", Byte[].class);

	@SuppressWarnings("rawtypes")
	ArgKey<IDomainContext> DOMAIN_CONTEXT = ArgKey.of("domainContext", IDomainContext.class);
	ArgKey<IRepository> REPOSITORY = ArgKey.of("repository", IRepository.class);

	// --- Core methods ---

	String domain();

	ICaller caller();

	Operation operation();

	Map<String, Object> args();

	OperationPath operationPath();

	UUID executionUuid();

	UUID correlationUuid();

	// --- Typed argument access ---

	<T> Optional<T> arg(ArgKey<T> key);

	<T> void arg(ArgKey<T> key, T value);

	// --- String-based access (backward compatibility) ---

	default Optional<?> arg(String key) {
		return Optional.ofNullable(args().get(key));
	}

	default void arg(String key, Object value) {
		args().put(key, value);
	}

}
