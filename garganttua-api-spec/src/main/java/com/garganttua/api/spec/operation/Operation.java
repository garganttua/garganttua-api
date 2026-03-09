package com.garganttua.api.spec.operation;

import java.util.Objects;

import com.garganttua.api.spec.Pluralizer;
import com.garganttua.api.spec.Singularizer;
import com.garganttua.core.reflection.IClass;

public record Operation(String domainName, TechnicalOperation operation, IClass<?> entity, Scope scope,
		OperationType type, boolean authority, Access access) {

	public static Operation readOneWithStandardSecurity(String domainName, IClass<?> entity) {
		return new Operation(domainName, TechnicalOperation.read, entity, Scope.oneEntity, OperationType.standard, true, Access.tenant);
	}

	public static Operation createOneWithStandardSecurity(String domainName, IClass<?> entity) {
		return new Operation(domainName, TechnicalOperation.create, entity, Scope.oneEntity, OperationType.standard, true, Access.tenant);
	}

	public static Operation useCaseWithStandardSecurity(String domainName, TechnicalOperation operation, IClass<?> entity,
			Scope scope) {
		return new Operation(domainName, operation, entity, scope, OperationType.usesCase, true, Access.tenant);
	}

	public static Operation deleteAllWithStandardSecurity(String domainName, IClass<?> entity) {
		return new Operation(domainName, TechnicalOperation.delete, entity, Scope.allEntities, OperationType.standard, true, Access.tenant);
	}

	public static Operation deleteOneWithStandardSecurity(String domainName, IClass<?> entity) {
		return new Operation(domainName, TechnicalOperation.delete, entity, Scope.oneEntity, OperationType.standard, true, Access.tenant);
	}

	public static Operation updateOneWithStandardSecurity(String domainName, IClass<?> entity) {
		return new Operation(domainName, TechnicalOperation.update, entity, Scope.oneEntity, OperationType.standard, true, Access.tenant);
	}

	public static Operation readAllWithStandardSecurity(String domainName, IClass<?> entity) {
		return new Operation(domainName, TechnicalOperation.read, entity, Scope.allEntities, OperationType.standard, true, Access.tenant);
	}

	public static Operation authenticate(String domainName, IClass<?> entity) {
		return new Operation(domainName, TechnicalOperation.create, entity, Scope.oneEntity,
				OperationType.authentication, false, Access.anonymous);
	}

	public static Operation workflowWithStandardSecurity(String domainName, TechnicalOperation operation, IClass<?> entity,
			Scope scope) {
		return new Operation(domainName, operation, entity, scope, OperationType.workflow, true, Access.authenticated);
	}

	public static Operation readOne(String domainName, IClass<?> entity, boolean authority, Access access) {
		return new Operation(domainName, TechnicalOperation.read, entity, Scope.oneEntity, OperationType.standard, authority, access);
	}

	public static Operation createOne(String domainName, IClass<?> entity, boolean authority, Access access) {
		return new Operation(domainName, TechnicalOperation.create, entity, Scope.oneEntity, OperationType.standard, authority, access);
	}

	public static Operation readAll(String domainName, IClass<?> entity, boolean authority, Access access) {
		return new Operation(domainName, TechnicalOperation.read, entity, Scope.allEntities, OperationType.standard, authority, access);
	}

	public static Operation updateOne(String domainName, IClass<?> entity, boolean authority, Access access) {
		return new Operation(domainName, TechnicalOperation.update, entity, Scope.oneEntity, OperationType.standard, authority, access);
	}

	public static Operation deleteOne(String domainName, IClass<?> entity, boolean authority, Access access) {
		return new Operation(domainName, TechnicalOperation.delete, entity, Scope.oneEntity, OperationType.standard, authority, access);
	}

	public static Operation deleteAll(String domainName, IClass<?> entity, boolean authority, Access access) {
		return new Operation(domainName, TechnicalOperation.delete, entity, Scope.allEntities, OperationType.standard, authority, access);
	}

	public static Operation useCase(String domainName, TechnicalOperation operation, IClass<?> entity, Scope scope, boolean authority, Access access) {
		return new Operation(domainName, operation, entity, scope, OperationType.usesCase, authority, access);
	}

	public static Operation workflow(String domainName, TechnicalOperation operation, IClass<?> entity, Scope scope, boolean authority, Access access) {
		return new Operation(domainName, operation, entity, scope, OperationType.workflow, authority, access);
	}

	public OperationPath getPath() {
		String base = "/" + Pluralizer.toPlural(entity.getSimpleName().toLowerCase());
		if (this.type == OperationType.authentication)
			return new OperationPath(base + "/authenticate");
		if (this.scope == Scope.oneEntity)
			return new OperationPath(base + "/${uuid}");
		return new OperationPath(base);
	}

	public String getOperationName() {
		if (this.type == OperationType.authentication) {
			if (scope == Scope.allEntities || scope == Scope.listOfEntities) {
				return "authenticate-all-" + Pluralizer.toPlural(entity.getSimpleName().toLowerCase());
			}
			if (scope == Scope.oneEntity) {
				return "authenticate-one-" + Singularizer.toSingular(entity.getSimpleName().toLowerCase());
			}
		}
		if (scope == Scope.allEntities || scope == Scope.listOfEntities) {
			return operation.toString() + "-" + scope.toString() + "-" + Pluralizer.toPlural(entity.getSimpleName().toLowerCase());
		}
		if (scope == Scope.oneEntity) {
			return operation.toString() + "-" + scope.toString() + "-" + Singularizer.toSingular(entity.getSimpleName().toLowerCase());
		}
		return operation + "-one-" + Singularizer.toSingular(entity.getSimpleName().toLowerCase());
	}

	public BusinessOperation getBusinessOperation() {
		if (this.type == OperationType.authentication)
			return BusinessOperation.authenticate;
		if (this.type == OperationType.usesCase)
			return BusinessOperation.useCase;
		if (this.type == OperationType.workflow)
			return BusinessOperation.workflow;

		switch (operation) {
			case create:
				return BusinessOperation.create;
			case delete:
				if (scope == Scope.oneEntity)
					return BusinessOperation.deleteOne;
				else
					return BusinessOperation.deleteAll;
			case read:
			default:
				if (scope == Scope.oneEntity)
					return BusinessOperation.readOne;
				else
					return BusinessOperation.readAll;

			case update:
				return BusinessOperation.update;
		}
	}

	public String key(){
		return this.toString();
	}

	@Override
	public String toString() {
		return this.domainName + "-" + operation + "-" + scope + "-"
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
		Operation other = (Operation) obj;
		return Objects.equals(domainName, other.domainName) &&
				Objects.equals(entity, other.entity) &&
				operation == other.operation &&
				scope == other.scope &&
				type == other.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(domainName, entity, operation, scope, type);
	}
}
