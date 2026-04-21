package com.garganttua.api.commons.operation;

import java.util.Objects;

import com.garganttua.api.commons.Pluralizer;
import com.garganttua.api.commons.Singularizer;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;

public record OperationDefinition(String domainName, TechnicalOperation technicalOperation, IClass<?> entity, Scope scope,
		OperationType type, boolean authority, Access access) {

	// --- Static factory methods ---

	public static OperationDefinition readOneWithStandardSecurity(String domainName, IClass<?> entity) {
		return new OperationDefinition(domainName, TechnicalOperation.read, entity, Scope.oneEntity, OperationType.standard, true, Access.tenant);
	}

	public static OperationDefinition createOneWithStandardSecurity(String domainName, IClass<?> entity) {
		return new OperationDefinition(domainName, TechnicalOperation.create, entity, Scope.oneEntity, OperationType.standard, true, Access.tenant);
	}

	public static OperationDefinition useCaseWithStandardSecurity(String domainName, TechnicalOperation operation, IClass<?> entity,
			Scope scope) {
		return new OperationDefinition(domainName, operation, entity, scope, OperationType.usesCase, true, Access.tenant);
	}

	public static OperationDefinition deleteAllWithStandardSecurity(String domainName, IClass<?> entity) {
		return new OperationDefinition(domainName, TechnicalOperation.delete, entity, Scope.allEntities, OperationType.standard, true, Access.tenant);
	}

	public static OperationDefinition deleteOneWithStandardSecurity(String domainName, IClass<?> entity) {
		return new OperationDefinition(domainName, TechnicalOperation.delete, entity, Scope.oneEntity, OperationType.standard, true, Access.tenant);
	}

	public static OperationDefinition updateOneWithStandardSecurity(String domainName, IClass<?> entity) {
		return new OperationDefinition(domainName, TechnicalOperation.update, entity, Scope.oneEntity, OperationType.standard, true, Access.tenant);
	}

	public static OperationDefinition readAllWithStandardSecurity(String domainName, IClass<?> entity) {
		return new OperationDefinition(domainName, TechnicalOperation.read, entity, Scope.allEntities, OperationType.standard, true, Access.tenant);
	}

	public static OperationDefinition authenticate(String domainName, IClass<?> entity) {
		return new OperationDefinition(domainName, TechnicalOperation.create, entity, Scope.oneEntity,
				OperationType.authentication, false, Access.anonymous);
	}

	public static OperationDefinition workflowWithStandardSecurity(String domainName, TechnicalOperation operation, IClass<?> entity,
			Scope scope) {
		return new OperationDefinition(domainName, operation, entity, scope, OperationType.workflow, true, Access.authenticated);
	}

	public static OperationDefinition readOne(String domainName, IClass<?> entity, boolean authority, Access access) {
		return new OperationDefinition(domainName, TechnicalOperation.read, entity, Scope.oneEntity, OperationType.standard, authority, access);
	}

	public static OperationDefinition createOne(String domainName, IClass<?> entity, boolean authority, Access access) {
		return new OperationDefinition(domainName, TechnicalOperation.create, entity, Scope.oneEntity, OperationType.standard, authority, access);
	}

	public static OperationDefinition readAll(String domainName, IClass<?> entity, boolean authority, Access access) {
		return new OperationDefinition(domainName, TechnicalOperation.read, entity, Scope.allEntities, OperationType.standard, authority, access);
	}

	public static OperationDefinition updateOne(String domainName, IClass<?> entity, boolean authority, Access access) {
		return new OperationDefinition(domainName, TechnicalOperation.update, entity, Scope.oneEntity, OperationType.standard, authority, access);
	}

	public static OperationDefinition deleteOne(String domainName, IClass<?> entity, boolean authority, Access access) {
		return new OperationDefinition(domainName, TechnicalOperation.delete, entity, Scope.oneEntity, OperationType.standard, authority, access);
	}

	public static OperationDefinition deleteAll(String domainName, IClass<?> entity, boolean authority, Access access) {
		return new OperationDefinition(domainName, TechnicalOperation.delete, entity, Scope.allEntities, OperationType.standard, authority, access);
	}

	public static OperationDefinition useCase(String domainName, TechnicalOperation operation, IClass<?> entity, Scope scope, boolean authority, Access access) {
		return new OperationDefinition(domainName, operation, entity, scope, OperationType.usesCase, authority, access);
	}

	public static OperationDefinition workflow(String domainName, TechnicalOperation operation, IClass<?> entity, Scope scope, boolean authority, Access access) {
		return new OperationDefinition(domainName, operation, entity, scope, OperationType.workflow, authority, access);
	}

	// --- Operation derivation ---

	public Operation operation() {
		return Operation.from(this);
	}

	// --- Delegation to Operation computation ---

	public OperationPath getPath() {
		return Operation.computePath(entity, type, scope);
	}

	public String getOperationName() {
		return Operation.computeOperationName(technicalOperation, scope, type, entity);
	}

	public BusinessOperation getBusinessOperation() {
		return Operation.computeBusinessOperation(technicalOperation, scope, type);
	}

	// --- Matching ---

	public boolean match(IOperationRequest request) {
		if (request == null) return false;
		OperationDefinition requestOp = request.operation();
		if (requestOp != null) {
			return this.equals(requestOp);
		}
		OperationPath requestPath = request.operationPath();
		if (requestPath == null) return false;
		TechnicalOperation requestTechOp = request.arg(IOperationRequest.TECHNICAL_OPERATION).orElse(null);
		if (requestTechOp != null && requestTechOp != this.technicalOperation) return false;
		return this.operation().pathMatches(requestPath);
	}

	// --- Identity ---

	public String key() {
		return this.toString();
	}

	@Override
	public String toString() {
		return this.domainName + "-" + technicalOperation + "-" + scope + "-"
				+ ((scope == Scope.allEntities || scope == Scope.listOfEntities)
						? Pluralizer.toPlural(this.entity.getSimpleName().toLowerCase())
						: Singularizer.toSingular(this.entity.getSimpleName().toLowerCase()));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		OperationDefinition other = (OperationDefinition) obj;
		return Objects.equals(domainName, other.domainName) &&
				Objects.equals(entity, other.entity) &&
				technicalOperation == other.technicalOperation &&
				scope == other.scope &&
				type == other.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(domainName, entity, technicalOperation, scope, type);
	}
}
