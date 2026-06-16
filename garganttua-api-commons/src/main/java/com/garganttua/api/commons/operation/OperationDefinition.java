package com.garganttua.api.commons.operation;

import java.util.Objects;

import com.garganttua.api.commons.Pluralizer;
import com.garganttua.api.commons.Singularizer;
import com.garganttua.api.commons.definition.IUseCaseDefinition;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;

public record OperationDefinition(String domainName, TechnicalOperation technicalOperation, IClass<?> entity, Scope scope,
		OperationType type, boolean authority, String authorityName, Access access, IUseCaseDefinition useCase) {

	/**
	 * Backwards-compatible constructor for non-use-case operations (CRUD, authenticate, …): they
	 * carry no {@link IUseCaseDefinition}. Keeps every existing 8-arg call site compiling.
	 */
	public OperationDefinition(String domainName, TechnicalOperation technicalOperation, IClass<?> entity, Scope scope,
			OperationType type, boolean authority, String authorityName, Access access) {
		this(domainName, technicalOperation, entity, scope, type, authority, authorityName, access, null);
	}

	/** The use case's name when this is a use-case operation, else {@code null} — the identity discriminator. */
	public String useCaseName() {
		return useCase != null ? useCase.name() : null;
	}

	// --- Static factory methods ---

	public static OperationDefinition readOneWithStandardSecurity(String domainName, IClass<?> entity) {
		return new OperationDefinition(domainName, TechnicalOperation.read, entity, Scope.oneEntity, OperationType.standard, true, null, Access.authenticated);
	}

	public static OperationDefinition createOneWithStandardSecurity(String domainName, IClass<?> entity) {
		return new OperationDefinition(domainName, TechnicalOperation.create, entity, Scope.oneEntity, OperationType.standard, true, null, Access.authenticated);
	}

	public static OperationDefinition useCaseWithStandardSecurity(String domainName, TechnicalOperation operation, IClass<?> entity,
			Scope scope) {
		return new OperationDefinition(domainName, operation, entity, scope, OperationType.usesCase, true, null, Access.authenticated);
	}

	public static OperationDefinition deleteAllWithStandardSecurity(String domainName, IClass<?> entity) {
		return new OperationDefinition(domainName, TechnicalOperation.delete, entity, Scope.allEntities, OperationType.standard, true, null, Access.authenticated);
	}

	public static OperationDefinition deleteOneWithStandardSecurity(String domainName, IClass<?> entity) {
		return new OperationDefinition(domainName, TechnicalOperation.delete, entity, Scope.oneEntity, OperationType.standard, true, null, Access.authenticated);
	}

	public static OperationDefinition updateOneWithStandardSecurity(String domainName, IClass<?> entity) {
		return new OperationDefinition(domainName, TechnicalOperation.update, entity, Scope.oneEntity, OperationType.standard, true, null, Access.authenticated);
	}

	public static OperationDefinition readAllWithStandardSecurity(String domainName, IClass<?> entity) {
		return new OperationDefinition(domainName, TechnicalOperation.read, entity, Scope.allEntities, OperationType.standard, true, null, Access.authenticated);
	}

	public static OperationDefinition authenticate(String domainName, IClass<?> entity) {
		return new OperationDefinition(domainName, TechnicalOperation.create, entity, Scope.oneEntity,
				OperationType.authentication, false, null, Access.anonymous);
	}

	/**
	 * Refresh-authorization operation: trades a still-valid (non-expired,
	 * non-revoked) authorization for a freshly issued one. Runs on the
	 * authenticator domain. Anonymous from the framework's perspective — the
	 * caller authenticates by presenting the existing authorization in the
	 * request body, and the script validates it directly.
	 */
	public static OperationDefinition refreshAuthorization(String domainName, IClass<?> entity) {
		return new OperationDefinition(domainName, TechnicalOperation.create, entity, Scope.oneEntity,
				OperationType.refreshAuthorization, false, null, Access.anonymous);
	}

	public static OperationDefinition workflowWithStandardSecurity(String domainName, TechnicalOperation operation, IClass<?> entity,
			Scope scope) {
		return new OperationDefinition(domainName, operation, entity, scope, OperationType.workflow, true, null, Access.authenticated);
	}

	public static OperationDefinition readOne(String domainName, IClass<?> entity, boolean authority, String authorityName, Access access) {
		return new OperationDefinition(domainName, TechnicalOperation.read, entity, Scope.oneEntity, OperationType.standard, authority, authorityName, access);
	}

	public static OperationDefinition createOne(String domainName, IClass<?> entity, boolean authority, String authorityName, Access access) {
		return new OperationDefinition(domainName, TechnicalOperation.create, entity, Scope.oneEntity, OperationType.standard, authority, authorityName, access);
	}

	public static OperationDefinition readAll(String domainName, IClass<?> entity, boolean authority, String authorityName, Access access) {
		return new OperationDefinition(domainName, TechnicalOperation.read, entity, Scope.allEntities, OperationType.standard, authority, authorityName, access);
	}

	public static OperationDefinition updateOne(String domainName, IClass<?> entity, boolean authority, String authorityName, Access access) {
		return new OperationDefinition(domainName, TechnicalOperation.update, entity, Scope.oneEntity, OperationType.standard, authority, authorityName, access);
	}

	public static OperationDefinition deleteOne(String domainName, IClass<?> entity, boolean authority, String authorityName, Access access) {
		return new OperationDefinition(domainName, TechnicalOperation.delete, entity, Scope.oneEntity, OperationType.standard, authority, authorityName, access);
	}

	public static OperationDefinition deleteAll(String domainName, IClass<?> entity, boolean authority, String authorityName, Access access) {
		return new OperationDefinition(domainName, TechnicalOperation.delete, entity, Scope.allEntities, OperationType.standard, authority, authorityName, access);
	}

	/**
	 * Builds the operation for a domain use case from its definition. The verb / scope / access /
	 * authority and the rich spec (path, in/out, binder) all come from {@code useCase}, which is kept
	 * on the operation so the path, body type and identity read from it.
	 */
	public static OperationDefinition useCase(String domainName, IClass<?> entity, IUseCaseDefinition useCase) {
		TechnicalOperation verb = useCase.operation() != null ? useCase.operation() : TechnicalOperation.read;
		Scope scope = useCase.scope() != null ? useCase.scope() : Scope.allEntities;
		return new OperationDefinition(domainName, verb, entity, scope,
				OperationType.usesCase, useCase.authority(), useCase.authorityName(), useCase.access(), useCase);
	}

	public static OperationDefinition workflow(String domainName, TechnicalOperation operation, IClass<?> entity, Scope scope, boolean authority, String authorityName, Access access) {
		return new OperationDefinition(domainName, operation, entity, scope, OperationType.workflow, authority, authorityName, access);
	}

	// --- Operation derivation ---

	public Operation operation() {
		return Operation.from(this);
	}

	// --- Delegation to Operation computation ---

	public OperationPath getPath() {
		if (useCase != null && useCase.path() != null) {
			return useCase.path();
		}
		return Operation.computePath(entity, type, scope);
	}

	public String getOperationName() {
		if (useCase != null) {
			return useCase.name();
		}
		return Operation.computeOperationName(technicalOperation, scope, type, entity);
	}

	public BusinessOperation getBusinessOperation() {
		return Operation.computeBusinessOperation(technicalOperation, scope, type);
	}

	/**
	 * Resolves the authority name actually enforced by the pipeline:
	 * <ul>
	 *   <li>{@code null} when {@link #authority()} is {@code false} (no check);</li>
	 *   <li>the explicit {@link #authorityName()} when one was configured;</li>
	 *   <li>otherwise the auto-generated default
	 *       {@code <domainName>:<businessOperation.label>}.</li>
	 * </ul>
	 */
	public String effectiveAuthorityName() {
		if (!authority) return null;
		if (authorityName != null && !authorityName.isBlank()) return authorityName;
		return this.getDefaultAuthorityName();
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
		return this.domainName + "-" + this.getDefaultAuthorityName();
	}

	private String getDefaultAuthorityName() {
		if (useCase != null) {
			return "usecase-" + useCase.name();
		}
		return technicalOperation + "-" + scope + "-"
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
				type == other.type &&
				// Use cases of the same shape are distinct operations — discriminate by name, so the
				// transport / matching / workflow assembly never conflate two use cases on a domain.
				Objects.equals(useCaseName(), other.useCaseName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(domainName, entity, technicalOperation, scope, type, useCaseName());
	}
}
