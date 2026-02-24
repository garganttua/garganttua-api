package com.garganttua.api.spec.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.javatuples.Pair;

import com.garganttua.api.spec.definition.IDomainDefinition;
import com.garganttua.api.spec.definition.IDtoDefinition;
import com.garganttua.api.spec.definition.IEntityDefinition;
import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.service.IOperationRequest;
import com.garganttua.api.spec.service.IOperationResponse;
import com.garganttua.core.lifecycle.ILifecycle;
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

	default Map<Class<?>, IDtoDefinition<?>> getDtos(){
		return this.getDomainDefinition().dtoDefinitions().stream()
				.collect(Collectors.toMap(IDtoDefinition::dtoClass, dto -> (IDtoDefinition<?>) dto));
	}

	// Entity class access
	default Class<E> getEntityClass() {
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

	IOperationResponse invoke(IOperationRequest request);

	IOperationResponse invoke(IOperationRequest request, WorkflowExecutionOptions options);

	Optional<IWorkflow> getWorkflow(String name);

	Map<String, IWorkflow> getWorkflows();
}
