package com.garganttua.api.core.service;

import java.util.List;
import java.util.UUID;

import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IDomain;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.operation.OperationDefinition;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.service.ArgKey;
import com.garganttua.api.spec.service.IOperationRequest;
import com.garganttua.api.spec.service.IOperationResponse;
import com.garganttua.api.spec.service.IRequest;
import com.garganttua.api.spec.service.IRequestBuilder;
import com.garganttua.api.spec.sort.ISort;

public class RequestBuilder implements IRequestBuilder {

	private final IDomain<?> domainContext;
	private final IOperationRequest operationRequest;

	public RequestBuilder(IDomain<?> domainContext) {
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

	@Override
	public IRequestBuilder createOne(Object body) {
		operation(OperationDefinition.createOneWithStandardSecurity(
				domainContext.getDomainName(), domainContext.getEntityClass()));
		param("entity", body);
		return this;
	}

	@Override
	public IRequestBuilder readOne(String uuid) {
		operation(OperationDefinition.readOneWithStandardSecurity(
				domainContext.getDomainName(), domainContext.getEntityClass()));
		param("type", "uuid");
		param("identifier", uuid);
		return this;
	}

	@Override
	public IRequestBuilder readAll() {
		operation(OperationDefinition.readAllWithStandardSecurity(
				domainContext.getDomainName(), domainContext.getEntityClass()));
		return this;
	}

	@Override
	public IRequestBuilder updateOne(String uuid, Object body) {
		operation(OperationDefinition.updateOneWithStandardSecurity(
				domainContext.getDomainName(), domainContext.getEntityClass()));
		param("type", "uuid");
		param("identifier", uuid);
		param("entity", body);
		return this;
	}

	@Override
	public IRequestBuilder deleteOne(String uuid) {
		operation(OperationDefinition.deleteOneWithStandardSecurity(
				domainContext.getDomainName(), domainContext.getEntityClass()));
		param("type", "uuid");
		param("identifier", uuid);
		return this;
	}

	@Override
	public IRequestBuilder deleteAll() {
		operation(OperationDefinition.deleteAllWithStandardSecurity(
				domainContext.getDomainName(), domainContext.getEntityClass()));
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
