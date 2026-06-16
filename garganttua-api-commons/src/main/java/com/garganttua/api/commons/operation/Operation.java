package com.garganttua.api.commons.operation;

import com.garganttua.api.commons.Pluralizer;
import com.garganttua.api.commons.Singularizer;
import com.garganttua.core.reflection.IClass;

public record Operation(
		TechnicalOperation technicalOperation,
		Scope scope,
		BusinessOperation businessOperation,
		String operationName,
		OperationType operationType,
		OperationPath path) {

	static Operation from(OperationDefinition def) {
		return new Operation(
				def.technicalOperation(),
				def.scope(),
				computeBusinessOperation(def.technicalOperation(), def.scope(), def.type()),
				// getOperationName()/getPath() honour a use case's own name + route; for CRUD they
				// fall back to the computed defaults.
				def.getOperationName(),
				def.type(),
				def.getPath());
	}

	static BusinessOperation computeBusinessOperation(TechnicalOperation techOp, Scope scope, OperationType type) {
		if (type == OperationType.authentication)
			return BusinessOperation.authenticate;
		if (type == OperationType.refreshAuthorization)
			return BusinessOperation.refreshAuthorization;
		if (type == OperationType.usesCase)
			return BusinessOperation.useCase;
		if (type == OperationType.workflow)
			return BusinessOperation.workflow;

		return switch (techOp) {
			case create -> BusinessOperation.create;
			case delete -> (scope == Scope.oneEntity) ? BusinessOperation.deleteOne : BusinessOperation.deleteAll;
			case read -> (scope == Scope.oneEntity) ? BusinessOperation.readOne : BusinessOperation.readAll;
			case update -> BusinessOperation.update;
		};
	}

	static String computeOperationName(TechnicalOperation techOp, Scope scope, OperationType type, IClass<?> entity) {
		String entityName = entity.getSimpleName().toLowerCase();
		if (type == OperationType.authentication) {
			if (scope == Scope.allEntities || scope == Scope.listOfEntities) {
				return "authenticate-all-" + Pluralizer.toPlural(entityName);
			}
			return "authenticate-one-" + Singularizer.toSingular(entityName);
		}
		if (type == OperationType.refreshAuthorization) {
			return "refresh-authorization-" + Singularizer.toSingular(entityName);
		}
		if (scope == Scope.allEntities || scope == Scope.listOfEntities) {
			return techOp + "-" + scope + "-" + Pluralizer.toPlural(entityName);
		}
		if (scope == Scope.oneEntity) {
			return techOp + "-" + scope + "-" + Singularizer.toSingular(entityName);
		}
		return techOp + "-one-" + Singularizer.toSingular(entityName);
	}

	static OperationPath computePath(IClass<?> entity, OperationType type, Scope scope) {
		String base = "/" + Pluralizer.toPlural(entity.getSimpleName().toLowerCase());
		if (type == OperationType.authentication)
			return new OperationPath(base + "/authenticate");
		if (type == OperationType.refreshAuthorization)
			return new OperationPath(base + "/refresh");
		if (scope == Scope.oneEntity)
			return new OperationPath(base + "/${uuid}");
		return new OperationPath(base);
	}

	boolean pathMatches(OperationPath other) {
		if (this.path == null || other == null) return false;
		String thisPattern = this.path.path();
		String otherPath = other.path();
		if (thisPattern == null || otherPath == null) return false;
		String[] patternParts = thisPattern.split("/", -1);
		String[] pathParts = otherPath.split("/", -1);
		if (patternParts.length != pathParts.length) return false;
		for (int i = 0; i < patternParts.length; i++) {
			if (patternParts[i].startsWith("${") && patternParts[i].endsWith("}")) {
				continue;
			}
			if (!patternParts[i].equals(pathParts[i])) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return operationName;
	}
}
