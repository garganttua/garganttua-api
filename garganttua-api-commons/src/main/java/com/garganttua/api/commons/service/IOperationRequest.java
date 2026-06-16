package com.garganttua.api.commons.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.operation.OperationPath;
import com.garganttua.api.commons.operation.TechnicalOperation;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.pageable.IPageable;
import com.garganttua.api.commons.repository.IRepository;
import com.garganttua.api.commons.sort.ISort;
import com.garganttua.core.reflection.IClass;

public interface IOperationRequest {

	// --- Typed argument keys ---

	ArgKey<String> PATH = ArgKey.of("path", IClass.getClass(String.class));
	ArgKey<TechnicalOperation> TECHNICAL_OPERATION = ArgKey.of("technicalOperation", IClass.getClass(TechnicalOperation.class));
	ArgKey<OperationDefinition> OPERATION = ArgKey.of("operation", IClass.getClass(OperationDefinition.class));

	ArgKey<Object> RAW_REQUEST = ArgKey.of("rawRequest", IClass.getClass(Object.class));
	ArgKey<Byte[]> RAW_BODY = ArgKey.of("rawBody", IClass.getClass(Byte[].class));
	ArgKey<Object> BODY = ArgKey.of("body", IClass.getClass(Object.class));
	ArgKey<String> ENTITY_UUID = ArgKey.of("entityUuid", IClass.getClass(String.class));
	ArgKey<IFilter> FILTER = ArgKey.of("filter", IClass.getClass(IFilter.class));
	// "pageable" — MUST match what READ_ALL.gs reads (:arg(@0, "pageable")), so the DSL .page()
	// and IDomain.readAll(page) actually reach the pipeline.
	ArgKey<IPageable> PAGE = ArgKey.of("pageable", IClass.getClass(IPageable.class));
	ArgKey<ISort> SORT = ArgKey.of("sort", IClass.getClass(ISort.class));
	/** readAll output mode — "full" / "uuid" / "id" (see READ_ALL.gs). */
	ArgKey<String> MODE = ArgKey.of("mode", IClass.getClass(String.class));
	/**
	 * Field projection ("select") — the list of ENTITY field names a read should return. When set,
	 * a read yields sparse maps carrying only those fields (see READ_ALL.gs / READ_ONE.gs and
	 * {@code projectFields}). Value is a {@code List<String>}; absent/empty means "no projection"
	 * (the whole entity). Name {@code "projection"} is what the scripts read via {@code :arg(@0,"projection")}.
	 */
	@SuppressWarnings("rawtypes")
	ArgKey<List> PROJECTION = ArgKey.of("projection", IClass.getClass(List.class));

	ArgKey<String> CALLER_ID = ArgKey.of("callerId", IClass.getClass(String.class));
	ArgKey<String> TENANT_ID = ArgKey.of("tenantId", IClass.getClass(String.class));
	ArgKey<String> REQUESTED_TENANT_ID = ArgKey.of("requestedTenantId", IClass.getClass(String.class));
	ArgKey<String> OWNER_ID = ArgKey.of("ownerId", IClass.getClass(String.class));
	ArgKey<String> REQUESTED_OWNER_ID = ArgKey.of("requestedOwnerId", IClass.getClass(String.class));

	ArgKey<Boolean> SUPER_TENANT = ArgKey.of("superTenant", IClass.getClass(Boolean.class));
	ArgKey<Boolean> SUPER_OWNER = ArgKey.of("superOwner", IClass.getClass(Boolean.class));

	@SuppressWarnings("rawtypes")
	ArgKey<List> AUTHORITIES = ArgKey.of("authorities", IClass.getClass(List.class));

	ArgKey<UUID> EXECUTION_UUID = ArgKey.of("executionUuid", IClass.getClass(UUID.class));
	ArgKey<UUID> CORRELATION_UUID = ArgKey.of("correlationUuid", IClass.getClass(UUID.class));

	ArgKey<Object> AUTHORIZATION = ArgKey.of("authorization", IClass.getClass(Object.class));
	ArgKey<Byte[]> RAW_AUTHORIZATION = ArgKey.of("rawAuthorization", IClass.getClass(Byte[].class));

	ArgKey<IApi> API_CONTEXT = ArgKey.of("apiContext", IClass.getClass(IApi.class));
	@SuppressWarnings("rawtypes")
	ArgKey<IDomain> DOMAIN_CONTEXT = ArgKey.of("domainContext", IClass.getClass(IDomain.class));
	ArgKey<IRepository> REPOSITORY = ArgKey.of("repository", IClass.getClass(IRepository.class));

	// --- Factory ---

	/**
	 * A fresh map-backed request. Returns a NAMED {@code @Reflected} type
	 * ({@link MapBackedOperationRequest}) rather than an anonymous class, so it
	 * resolves under native-image with no hand-written reflect-config.
	 */
	static IOperationRequest create() {
		return new MapBackedOperationRequest();
	}

	// --- Core methods ---

	String domain();

	ICaller caller();

	OperationDefinition operation();

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
