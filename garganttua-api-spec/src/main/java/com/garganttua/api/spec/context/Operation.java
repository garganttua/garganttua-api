package com.garganttua.api.spec.context;

import java.util.Objects;

import com.garganttua.api.spec.Pluralizer;
import com.garganttua.api.spec.Singularizer;

public record Operation(String domainName, TechnicalOperation operation, Class<?> entity, Scope scope,
		OperationType type) {

	public static Operation readOne(String domainName, Class<?> entity) {
		return new Operation(domainName, TechnicalOperation.read, entity, Scope.oneEntity, OperationType.standard);
	}

	public static Operation createOne(String domainName, Class<?> entity) {
		return new Operation(domainName, TechnicalOperation.create, entity, Scope.oneEntity, OperationType.standard);
	}

	public static Operation useCase(String domainName, TechnicalOperation operation, Class<?> entity,
			Scope scope) {
		return new Operation(domainName, operation, entity, scope, OperationType.usesCase);
	}

	public static Operation deleteAll(String domainName, Class<?> entity) {
		return new Operation(domainName, TechnicalOperation.delete, entity, Scope.allEntities, OperationType.standard);
	}

	public static Operation deleteOne(String domainName, Class<?> entity) {
		return new Operation(domainName, TechnicalOperation.delete, entity, Scope.oneEntity, OperationType.standard);
	}

	public static Operation updateOne(String domainName, Class<?> entity) {
		return new Operation(domainName, TechnicalOperation.update, entity, Scope.oneEntity, OperationType.standard);
	}

	public static Operation readAll(String domainName, Class<?> entity) {
		return new Operation(domainName, TechnicalOperation.read, entity, Scope.allEntities, OperationType.standard);
	}

	public static Operation authenticate(String domainName, Class<?> entity) {
		return new Operation(domainName, TechnicalOperation.create, entity, Scope.oneEntity,
				OperationType.authentication);
	}

	public static Operation workflow(String domainName, TechnicalOperation operation, Class<?> entity,
			Scope scope) {
		return new Operation(domainName, operation, entity, scope, OperationType.workflow);
	}

	public String getPath() {
		if( this.type == OperationType.authentication )
			return "/" + Pluralizer.toPlural(entity.getSimpleName().toLowerCase()) + "/authenticate";
		if (this.scope == Scope.oneEntity)
			return "/" + Pluralizer.toPlural(entity.getSimpleName().toLowerCase()) + "/${uuid}";
		return "/" + Pluralizer.toPlural(entity.getSimpleName().toLowerCase());
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
