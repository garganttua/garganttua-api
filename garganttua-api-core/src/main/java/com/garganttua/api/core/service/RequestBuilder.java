package com.garganttua.api.core.service;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.operation.BusinessOperation;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.pageable.IPageable;
import com.garganttua.api.commons.service.ArgKey;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.IRequest;
import com.garganttua.api.commons.service.IRequestBuilder;
import com.garganttua.api.commons.sort.ISort;
import com.garganttua.core.reflection.IClass;

public class RequestBuilder implements IRequestBuilder {

	private final IDomain<?> domainContext;
	private final IOperationRequest operationRequest;


	public static IRequestBuilder builder(IDomain<?> domain) {
		return new RequestBuilder(domain);
	}

	private RequestBuilder(IDomain<?> domainContext) {
		this.domainContext = domainContext;
		this.operationRequest = new OperationRequest(null);
	}

	@Override
	public IRequestBuilder operation(OperationDefinition operation) {
		this.operationRequest.arg(IOperationRequest.OPERATION, operation);
		return this;
	}

	@Override
	public IRequestBuilder caller(ICaller caller) {
		if (caller != null) {
			tenantId(caller.tenantId());
			requestedTenantId(caller.requestedTenantId());
			ownerId(caller.ownerId());
			callerId(caller.callerId());
			superTenant(caller.superTenant());
			superOwner(caller.superOwner());
			authorities(caller.authorities());
		}
		return this;
	}

	@Override
	public IRequestBuilder tenantId(String tenantId) {
		this.operationRequest.arg(IOperationRequest.TENANT_ID, tenantId);
		return this;
	}

	@Override
	public IRequestBuilder requestedTenantId(String requestedTenantId) {
		this.operationRequest.arg(IOperationRequest.REQUESTED_TENANT_ID, requestedTenantId);
		return this;
	}

	@Override
	public IRequestBuilder ownerId(String ownerId) {
		this.operationRequest.arg(IOperationRequest.OWNER_ID, ownerId);
		return this;
	}

	@Override
	public IRequestBuilder callerId(String callerId) {
		this.operationRequest.arg(IOperationRequest.CALLER_ID, callerId);
		return this;
	}

	@Override
	public IRequestBuilder superTenant(boolean superTenant) {
		this.operationRequest.arg(IOperationRequest.SUPER_TENANT, superTenant);
		return this;
	}

	@Override
	public IRequestBuilder superOwner(boolean superOwner) {
		this.operationRequest.arg(IOperationRequest.SUPER_OWNER, superOwner);
		return this;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public IRequestBuilder authorities(List<String> authorities) {
		this.operationRequest.arg((ArgKey) IOperationRequest.AUTHORITIES, authorities);
		return this;
	}

	@Override
	public IRequestBuilder body(Object body) {
		this.operationRequest.arg(IOperationRequest.BODY, body);
		return this;
	}

	@Override
	public IRequestBuilder entityUuid(String uuid) {
		this.operationRequest.arg(IOperationRequest.ENTITY_UUID, uuid);
		return this;
	}

	@Override
	public IRequestBuilder filter(IFilter filter) {
		this.operationRequest.arg(IOperationRequest.FILTER, filter);
		return this;
	}

	@Override
	public IRequestBuilder page(IPageable page) {
		this.operationRequest.arg(IOperationRequest.PAGE, page);
		return this;
	}

	@Override
	public IRequestBuilder sort(ISort sort) {
		this.operationRequest.arg(IOperationRequest.SORT, sort);
		return this;
	}

	@Override
	public IRequestBuilder mode(com.garganttua.api.commons.service.ReadAllOutputMode mode) {
		// The enum constant name IS the wire value read by READ_ALL.gs (:arg(@0, "mode")).
		this.operationRequest.arg(IOperationRequest.MODE, mode == null ? null : mode.name());
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public IRequestBuilder select(String... fields) {
		// Trim + drop blanks; an empty selection means "no projection" (the whole entity).
		List<String> list = (fields == null) ? List.of()
				: java.util.Arrays.stream(fields)
						.filter(f -> f != null && !f.isBlank())
						.map(String::trim)
						.toList();
		this.operationRequest.arg((ArgKey) IOperationRequest.PROJECTION, list.isEmpty() ? null : list);
		return this;
	}

	@Override
	public IRequestBuilder executionUuid(UUID executionUuid) {
		this.operationRequest.arg(IOperationRequest.EXECUTION_UUID, executionUuid);
		return this;
	}

	@Override
	public IRequestBuilder correlationUuid(UUID correlationUuid) {
		this.operationRequest.arg(IOperationRequest.CORRELATION_UUID, correlationUuid);
		return this;
	}

	@Override
	public IRequestBuilder param(String key, Object value) {
		this.operationRequest.arg(key, value);
		return this;
	}

	@Override
	public <T> IRequestBuilder param(ArgKey<T> key, T value) {
		this.operationRequest.arg(key, value);
		return this;
	}

	/**
	 * Resolves the {@link OperationDefinition} for a given CRUD slot by first
	 * looking up the domain's registered operations (so any DSL-configured
	 * access / authority overrides are honoured), falling back to the supplied
	 * "standard security" default when the domain doesn't expose that op (e.g.
	 * the CRUD flag was turned off via {@code .creation(false)}).
	 */
	private OperationDefinition resolveOperation(BusinessOperation businessOp,
			BiFunction<String, IClass<?>, OperationDefinition> fallback) {
		return domainContext.getDomainDefinition().operations().stream()
				.filter(op -> op.getBusinessOperation() == businessOp)
				.findFirst()
				.orElseGet(() -> fallback.apply(domainContext.getDomainName(), domainContext.getEntityClass()));
	}

	@Override
	public IRequestBuilder createOne(Object body) {
		operation(resolveOperation(BusinessOperation.create,
				OperationDefinition::createOneWithStandardSecurity));
		param("entity", body);
		return this;
	}

	@Override
	public IRequestBuilder readOne(String uuid) {
		operation(resolveOperation(BusinessOperation.readOne,
				OperationDefinition::readOneWithStandardSecurity));
		param("type", "uuid");
		param("identifier", uuid);
		return this;
	}

	@Override
	public IRequestBuilder readAll() {
		operation(resolveOperation(BusinessOperation.readAll,
				OperationDefinition::readAllWithStandardSecurity));
		return this;
	}

	@Override
	public IRequestBuilder updateOne(String uuid, Object body) {
		operation(resolveOperation(BusinessOperation.update,
				OperationDefinition::updateOneWithStandardSecurity));
		param("type", "uuid");
		param("identifier", uuid);
		param("entity", body);
		return this;
	}

	@Override
	public IRequestBuilder deleteOne(String uuid) {
		operation(resolveOperation(BusinessOperation.deleteOne,
				OperationDefinition::deleteOneWithStandardSecurity));
		param("type", "uuid");
		param("identifier", uuid);
		return this;
	}

	@Override
	public IRequestBuilder deleteAll() {
		operation(resolveOperation(BusinessOperation.deleteAll,
				OperationDefinition::deleteAllWithStandardSecurity));
		return this;
	}

	@Override
	public IRequest build() {
		IDomain<?> ctx = this.domainContext;
		IOperationRequest req = this.operationRequest;
		return new IRequest() {
			@Override
			public IOperationRequest operationRequest() {
				return req;
			}

			@Override
			public IOperationResponse execute() {
				return ctx.invoke(req);
			}
		};
	}

}
