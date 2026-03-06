package com.garganttua.api.spec.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.context.Operation;
import com.garganttua.api.spec.context.OperationPath;
import com.garganttua.api.spec.context.TechnicalOperation;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.security.authorization.IAuthorization;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.core.reflection.runtime.RuntimeClass;

public interface IOperationRequest {

	// --- Typed argument keys ---

	ArgKey<String> PATH = ArgKey.of("path", RuntimeClass.of(String.class));
	ArgKey<TechnicalOperation> TECHNICAL_OPERATION = ArgKey.of("technicalOperation", RuntimeClass.of(TechnicalOperation.class));
	ArgKey<Operation> OPERATION = ArgKey.of("operation", RuntimeClass.of(Operation.class));

	ArgKey<Object> RAW_REQUEST = ArgKey.of("rawRequest", RuntimeClass.of(Object.class));
	ArgKey<Byte[]> RAW_BODY = ArgKey.of("rawBody", RuntimeClass.of(Byte[].class));
	ArgKey<Object> BODY = ArgKey.of("body", RuntimeClass.of(Object.class));
	ArgKey<String> ENTITY_UUID = ArgKey.of("entityUuid", RuntimeClass.of(String.class));
	ArgKey<IFilter> FILTER = ArgKey.of("filter", RuntimeClass.of(IFilter.class));
	ArgKey<IPageable> PAGE = ArgKey.of("page", RuntimeClass.of(IPageable.class));
	ArgKey<ISort> SORT = ArgKey.of("sort", RuntimeClass.of(ISort.class));

	ArgKey<String> CALLER_ID = ArgKey.of("callerId", RuntimeClass.of(String.class));
	ArgKey<String> TENANT_ID = ArgKey.of("tenantId", RuntimeClass.of(String.class));
	ArgKey<String> REQUESTED_TENANT_ID = ArgKey.of("requestedTenantId", RuntimeClass.of(String.class));
	ArgKey<String> OWNER_ID = ArgKey.of("ownerId", RuntimeClass.of(String.class));
	ArgKey<String> REQUESTED_OWNER_ID = ArgKey.of("requestedOwnerId", RuntimeClass.of(String.class));

	ArgKey<Boolean> SUPER_TENANT = ArgKey.of("superTenant", RuntimeClass.of(Boolean.class));
	ArgKey<Boolean> SUPER_OWNER = ArgKey.of("superOwner", RuntimeClass.of(Boolean.class));

	@SuppressWarnings("rawtypes")
	ArgKey<List> AUTHORITIES = ArgKey.of("authorities", RuntimeClass.of(List.class));

	ArgKey<UUID> EXECUTION_UUID = ArgKey.of("executionUuid", RuntimeClass.of(UUID.class));
	ArgKey<UUID> CORRELATION_UUID = ArgKey.of("correlationUuid", RuntimeClass.of(UUID.class));

	ArgKey<IAuthorization> AUTHORIZATION = ArgKey.of("authorization", RuntimeClass.of(IAuthorization.class));
	ArgKey<Byte[]> RAW_AUTHORIZATION = ArgKey.of("rawAuthorization", RuntimeClass.of(Byte[].class));

	ArgKey<IApiContext> API_CONTEXT = ArgKey.of("apiContext", RuntimeClass.of(IApiContext.class));
	@SuppressWarnings("rawtypes")
	ArgKey<IDomainContext> DOMAIN_CONTEXT = ArgKey.of("domainContext", RuntimeClass.of(IDomainContext.class));
	ArgKey<IRepository> REPOSITORY = ArgKey.of("repository", RuntimeClass.of(IRepository.class));

	// --- Factory ---

	@SuppressWarnings("unchecked")
	static IOperationRequest create() {
		final Map<String, Object> map = new java.util.HashMap<>();
		return new IOperationRequest() {
			@Override
			public Map<String, Object> args() {
				return map;
			}

			@Override
			public <T> Optional<T> arg(ArgKey<T> key) {
				return Optional.ofNullable((T) map.get(key.name()));
			}

			@Override
			public <T> void arg(ArgKey<T> key, T value) {
				map.put(key.name(), value);
			}

			@Override
			public String domain() {
				OperationPath path = operationPath();
				return path != null ? path.domain() : null;
			}

			@Override
			public ICaller caller() {
				return new ICaller() {
					@Override public String tenantId() { return arg(TENANT_ID).orElse(null); }
					@Override public String requestedTenantId() { return arg(REQUESTED_TENANT_ID).orElse(null); }
					@Override public String callerId() { return arg(CALLER_ID).orElse(null); }
					@Override public String ownerId() { return arg(OWNER_ID).orElse(null); }
					@Override public boolean superTenant() { return Boolean.TRUE.equals(arg(SUPER_TENANT).orElse(null)); }
					@Override public boolean superOwner() { return Boolean.TRUE.equals(arg(SUPER_OWNER).orElse(null)); }
					@Override public List<String> authorities() { return (List<String>) arg(AUTHORITIES).orElse(null); }
				};
			}

			@Override
			public Operation operation() {
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
		};
	}

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
