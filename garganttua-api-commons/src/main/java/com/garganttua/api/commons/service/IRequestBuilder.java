package com.garganttua.api.commons.service;

import java.util.List;
import java.util.UUID;

import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.pageable.IPageable;
import com.garganttua.api.commons.sort.ISort;

public interface IRequestBuilder {

	// --- OperationDefinition ---

	IRequestBuilder operation(OperationDefinition operation);

	// --- Caller (full object) ---

	IRequestBuilder caller(ICaller caller);

	// --- Caller fields (individual) ---

	IRequestBuilder tenantId(String tenantId);

	IRequestBuilder requestedTenantId(String requestedTenantId);

	IRequestBuilder ownerId(String ownerId);

	IRequestBuilder callerId(String callerId);

	IRequestBuilder superTenant(boolean superTenant);

	IRequestBuilder superOwner(boolean superOwner);

	IRequestBuilder authorities(List<String> authorities);

	// --- Request data ---

	IRequestBuilder body(Object body);

	IRequestBuilder entityUuid(String uuid);

	IRequestBuilder filter(IFilter filter);

	IRequestBuilder page(IPageable page);

	IRequestBuilder sort(ISort sort);

	/** readAll output shape — full entities (default) / only uuids / only ids. */
	IRequestBuilder mode(ReadAllOutputMode mode);

	/**
	 * Field projection ("select") — restrict a read to the given ENTITY field names. The read then
	 * yields sparse objects carrying only those fields. Empty/no fields means "the whole entity".
	 */
	IRequestBuilder select(String... fields);

	/** Alias of {@link #select(String...)}. */
	default IRequestBuilder fields(String... fields) {
		return select(fields);
	}

	// --- Tracing ---

	IRequestBuilder executionUuid(UUID executionUuid);

	IRequestBuilder correlationUuid(UUID correlationUuid);

	// --- Generic param ---

	IRequestBuilder param(String key, Object value);

	<T> IRequestBuilder param(ArgKey<T> key, T value);

	// --- CRUD shortcuts ---

	IRequestBuilder createOne(Object body);

	IRequestBuilder readOne(String uuid);

	IRequestBuilder readAll();

	IRequestBuilder updateOne(String uuid, Object body);

	IRequestBuilder deleteOne(String uuid);

	IRequestBuilder deleteAll();

	// --- Build & execute ---

	IRequest build();

	default IOperationResponse execute() {
		return build().execute();
	}

}
