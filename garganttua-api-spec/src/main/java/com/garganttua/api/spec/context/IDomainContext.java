package com.garganttua.api.spec.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.javatuples.Pair;

import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.definition.IDomainDefinition;
import com.garganttua.api.spec.definition.IDtoDefinition;
import com.garganttua.api.spec.definition.IEntityDefinition;
import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.service.IOperationRequest;
import com.garganttua.api.spec.service.IRequestBuilder;
import com.garganttua.api.spec.operation.Access;
import com.garganttua.api.spec.operation.OperationDefinition;
import com.garganttua.api.spec.service.IOperationResponse;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.core.lifecycle.ILifecycle;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.workflow.IWorkflow;
import com.garganttua.core.workflow.WorkflowExecutionOptions;

public interface IDomainContext<E> extends ILifecycle {

	IDomainDefinition<E> getDomainDefinition();

	default IEntityDefinition<E> getEntityDefinition() {
		return getDomainDefinition().entityDefinition();
	}

	default String getDomainName(){
		return getDomainDefinition().domainName();
	}

	default String getDomain() {
		return getDomainName();
	}

	default Map<IClass<?>, IDtoDefinition<?>> getDtos(){
		return this.getDomainDefinition().dtoDefinitions().stream()
				.collect(Collectors.toMap(IDtoDefinition::dtoClass, dto -> (IDtoDefinition<?>) dto));
	}

	// Entity class access
	default IClass<E> getEntityClass() {
		return getEntityDefinition().entityClass();
	}

	// Field addresses from entity definition
	default ObjectAddress getUuidFieldAddress() {
		return getEntityDefinition().uuid();
	}

	default ObjectAddress getTenantIdFieldAddress() {
		return getEntityDefinition().tenantId();
	}

	default ObjectAddress getOwnerIdFieldAddress() {
		return getDomainDefinition().owned();
	}

	default List<ObjectAddress> getMandatoryFields() {
		return getEntityDefinition().mandatories();
	}

	default List<Pair<ObjectAddress, UnicityScope>> getUnicityFields() {
		return getEntityDefinition().unicities();
	}

	default Map<ObjectAddress, String> getAuthorizedUpdateFieldsAndAuthorizations() {
		List<Pair<ObjectAddress, String>> updates = getEntityDefinition().updates();
		if (updates == null) return Map.of();
		Map<ObjectAddress, String> result = new HashMap<>();
		for (Pair<ObjectAddress, String> pair : updates) {
			result.put(pair.getValue0(), pair.getValue1());
		}
		return result;
	}

	// Domain-level flags
	default boolean isPublicEntity() {
		return Boolean.TRUE.equals(getDomainDefinition().publik());
	}

	default boolean isTenantEntity() {
		return Boolean.TRUE.equals(getDomainDefinition().tenant());
	}

	default boolean isOwnedEntity() {
		return getDomainDefinition().owned() != null;
	}

	default boolean isGeolocalized() {
		return getDomainDefinition().geolocalized() != null;
	}

	default boolean isMultiTenant() {
		return true;
	}

	/**
	 * Returns true if tenantId is mandatory for the given operation.
	 * TenantId is mandatory when:
	 * - the entity is not public AND the operation access level is tenant or owner
	 */
	default boolean isTenantIdMandatoryForOperation(OperationDefinition operation) {
		if (operation == null) return false;
		if (isPublicEntity()) return false;
		Access access = operation.access();
		return access == Access.tenant || access == Access.owner;
	}

	/**
	 * Returns true if ownerId is mandatory for the given operation.
	 * OwnerId is mandatory when:
	 * - the entity is owned AND the operation access level is owner
	 */
	default boolean isOwnerIdMandatoryForOperation(OperationDefinition operation) {
		if (operation == null) return false;
		if (!isOwnedEntity()) return false;
		return operation.access() == Access.owner;
	}

	// Hook method addresses (to be implemented by concrete class)
	default ObjectAddress getAfterGetMethodAddress() { return null; }
	default ObjectAddress getBeforeCreateMethodAddress() { return null; }
	default ObjectAddress getAfterCreateMethodAddress() { return null; }
	default ObjectAddress getBeforeUpdateMethodAddress() { return null; }
	default ObjectAddress getAfterUpdateMethodAddress() { return null; }
	default ObjectAddress getBeforeDeleteMethodAddress() { return null; }
	default ObjectAddress getAfterDeleteMethodAddress() { return null; }

	// Repository access (implemented by concrete class)
	IRepository getRepository();

	// --- Workflow invocation ---

	IOperationResponse invoke(IOperationRequest request);

	IOperationResponse invoke(IOperationRequest request, WorkflowExecutionOptions options);

	IWorkflow getWorkflow();

	// --- Request builder ---

	IRequestBuilder request();

	// --- CRUD convenience methods ---

	default IOperationResponse createOne(Object body, ICaller caller) {
		IOperationRequest request = buildRequest(
				OperationDefinition.createOneWithStandardSecurity(getDomainName(), getEntityClass()), caller);
		request.arg(IOperationRequest.BODY, body);
		return invoke(request);
	}

	default IOperationResponse readOne(String uuid, ICaller caller) {
		IOperationRequest request = buildRequest(
				OperationDefinition.readOneWithStandardSecurity(getDomainName(), getEntityClass()), caller);
		request.arg(IOperationRequest.ENTITY_UUID, uuid);
		return invoke(request);
	}

	default IOperationResponse readAll(ICaller caller) {
		return readAll(null, null, null, caller);
	}

	default IOperationResponse readAll(IFilter filter, IPageable page, ISort sort, ICaller caller) {
		IOperationRequest request = buildRequest(
				OperationDefinition.readAllWithStandardSecurity(getDomainName(), getEntityClass()), caller);
		if (filter != null) request.arg(IOperationRequest.FILTER, filter);
		if (page != null) request.arg(IOperationRequest.PAGE, page);
		if (sort != null) request.arg(IOperationRequest.SORT, sort);
		return invoke(request);
	}

	default IOperationResponse updateOne(String uuid, Object body, ICaller caller) {
		IOperationRequest request = buildRequest(
				OperationDefinition.updateOneWithStandardSecurity(getDomainName(), getEntityClass()), caller);
		request.arg(IOperationRequest.ENTITY_UUID, uuid);
		request.arg(IOperationRequest.BODY, body);
		return invoke(request);
	}

	default IOperationResponse deleteOne(String uuid, ICaller caller) {
		IOperationRequest request = buildRequest(
				OperationDefinition.deleteOneWithStandardSecurity(getDomainName(), getEntityClass()), caller);
		request.arg(IOperationRequest.ENTITY_UUID, uuid);
		return invoke(request);
	}

	default IOperationResponse deleteAll(ICaller caller) {
		IOperationRequest request = buildRequest(
				OperationDefinition.deleteAllWithStandardSecurity(getDomainName(), getEntityClass()), caller);
		return invoke(request);
	}

	// --- Internal helpers ---

	@SuppressWarnings("rawtypes")
	private IOperationRequest buildRequest(OperationDefinition operation, ICaller caller) {
		IOperationRequest request = IOperationRequest.create();
		request.arg(IOperationRequest.OPERATION, operation);
		if (caller != null) {
			request.arg(IOperationRequest.TENANT_ID, caller.tenantId());
			request.arg(IOperationRequest.REQUESTED_TENANT_ID, caller.requestedTenantId());
			request.arg(IOperationRequest.CALLER_ID, caller.callerId());
			request.arg(IOperationRequest.OWNER_ID, caller.ownerId());
			request.arg(IOperationRequest.SUPER_TENANT, caller.superTenant());
			request.arg(IOperationRequest.SUPER_OWNER, caller.superOwner());
			request.arg(IOperationRequest.AUTHORITIES, (List) caller.authorities());
		}
		return request;
	}
}
