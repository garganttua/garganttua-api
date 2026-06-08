package com.garganttua.api.core.domain;
import com.garganttua.api.core.api.ApiBuilder;
import com.garganttua.api.core.security.WorkflowSecurityBuilder;
import com.garganttua.core.reflection.annotations.Reflected;

import java.util.Objects;

import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.Scope;
import com.garganttua.api.commons.operation.TechnicalOperation;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.IDomainWorkflowBuilder;
import com.garganttua.api.commons.context.dsl.security.IWorkflowSecurityBuilder;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.expression.dsl.IExpressionContextBuilder;
import com.garganttua.core.injection.context.dsl.IInjectionContextBuilder;
import com.garganttua.core.workflow.IWorkflow;
import com.garganttua.core.workflow.dsl.IWorkflowBuilder;
import com.garganttua.core.workflow.dsl.IWorkflowsBuilder;

@Reflected
public class DomainWorkflowBuilder<E>
		extends AbstractAutomaticLinkedBuilder<IDomainWorkflowBuilder<E>, IDomainBuilder<E>, IWorkflow>
		implements IDomainWorkflowBuilder<E> {

	private final String workflowName;
	private IWorkflowBuilder internalBuilder; // lazy, opened on first touch
	private WorkflowSecurityBuilder<E> securityBuilder;

	private String pathSuffix;
	private String completePath;
	private Scope scope;
	private TechnicalOperation operation;
	private boolean custom;

	private IInjectionContextBuilder injectionContextBuilder;
	private IExpressionContextBuilder expressionContextBuilder;

	public DomainWorkflowBuilder(String workflowName, IDomainBuilder<E> up) {
		super(up);
		this.workflowName = Objects.requireNonNull(workflowName, "Workflow name cannot be null");
		// internalBuilder is opened lazily via ensureInternalBuilder() — the
		// IWorkflowsBuilder may not be available yet when DomainBuilder
		// instantiates the default CRUD placeholders (initDefaultCrudWorkflows
		// runs at domain() time, well before apiBuilder.provide(workflowsBuilder)).
		// For default CRUD workflows, the assembler builds the workflow itself
		// and this internalBuilder is never opened.
	}

	private synchronized IWorkflowBuilder ensureInternalBuilder() {
		if (this.internalBuilder == null) {
			IWorkflowsBuilder wb = resolveWorkflowsBuilder();
			this.internalBuilder = wb.workflow(this.workflowName).name(this.workflowName);
		}
		return this.internalBuilder;
	}

	private IWorkflowsBuilder resolveWorkflowsBuilder() {
		Object api = this.up() != null ? this.up().up() : null;
		if (api instanceof ApiBuilder ab) {
			IWorkflowsBuilder wb = ab.getWorkflowsBuilder();
			if (wb != null) {
				return wb;
			}
		}
		throw new IllegalStateException(
				"IWorkflowsBuilder not provided to ApiBuilder — call "
				+ "apiBuilder.provide(workflowsBuilder) before configuring workflow '"
				+ this.workflowName + "' (core 2.0.0-ALPHA02 dropped the public "
				+ "WorkflowBuilder.create() factory, so the api has no fallback).");
	}

	@Override
	public IDomainWorkflowBuilder<E> pathSuffix(String suffix) {
		this.pathSuffix = Objects.requireNonNull(suffix, "Path suffix cannot be null");
		this.custom = true;
		return this;
	}

	@Override
	public IDomainWorkflowBuilder<E> completePath(String path) {
		this.completePath = Objects.requireNonNull(path, "Complete path cannot be null");
		this.custom = true;
		return this;
	}

	@Override
	public IDomainWorkflowBuilder<E> scope(Scope scope) {
		this.scope = Objects.requireNonNull(scope, "Scope cannot be null");
		this.custom = true;
		return this;
	}

	@Override
	public IDomainWorkflowBuilder<E> operation(TechnicalOperation operation) {
		this.operation = Objects.requireNonNull(operation, "Operation cannot be null");
		this.custom = true;
		return this;
	}

	@Override
	public IWorkflowSecurityBuilder<E> security() {
		if (this.securityBuilder == null) {
			this.securityBuilder = new WorkflowSecurityBuilder<>(this);
		}
		return this.securityBuilder;
	}

	@Override
	public IDomainWorkflowBuilder<E> variable(String name, Object value) {
		ensureInternalBuilder().variable(name, value);
		return this;
	}

	@Override
	public IDomainWorkflowBuilder<E> inlineAll() {
		ensureInternalBuilder().inlineAll();
		return this;
	}

	@Override
	public IWorkflowBuilder stages() {
		return ensureInternalBuilder();
	}

	void setDependencyBuilders(IInjectionContextBuilder injectionContextBuilder,
			IExpressionContextBuilder expressionContextBuilder) {
		this.injectionContextBuilder = injectionContextBuilder;
		this.expressionContextBuilder = expressionContextBuilder;
	}

	@Override
	protected synchronized IWorkflow doBuild() throws ApiException {
		IWorkflowBuilder ib = ensureInternalBuilder();
		// Core 2.0.0-ALPHA02 reduced WorkflowBuilder's accepted dependencies
		// to IInjectionContextBuilder + IObservabilityBuilder — expression
		// context is now resolved via the IScriptingEnvironment that the
		// parent WorkflowsBuilder materializes from its IScriptsBuilder, not
		// fed in directly here.
		if (this.injectionContextBuilder != null) {
			try {
				ib.provide(this.injectionContextBuilder);
			} catch (com.garganttua.core.dsl.DslException e) {
				throw new ApiException("Failed to wire IInjectionContextBuilder into workflow '"
						+ this.workflowName + "': " + e.getMessage(), e);
			}
		}
		try {
			return ib.build();
		} catch (com.garganttua.core.dsl.DslException e) {
			throw new ApiException("Failed to build workflow '" + this.workflowName
					+ "': " + e.getMessage(), e);
		}
	}

	@Override
	protected void doAutoDetection() throws ApiException {
	}

	String getWorkflowName() {
		return this.workflowName;
	}

	String getPathSuffix() {
		return this.pathSuffix;
	}

	String getCompletePath() {
		return this.completePath;
	}

	Scope getScope() {
		return this.scope;
	}

	TechnicalOperation getOperation() {
		return this.operation;
	}

	boolean isCustom() {
		return this.custom;
	}

	void setCustom(boolean custom) {
		this.custom = custom;
	}

	Access getAccess() {
		return this.securityBuilder != null ? this.securityBuilder.getAccess() : Access.authenticated;
	}

	boolean hasAuthority() {
		return this.securityBuilder != null && this.securityBuilder.hasAuthority();
	}

	String getCustomAuthority() {
		return this.securityBuilder != null ? this.securityBuilder.getCustomAuthority() : null;
	}

	boolean isSecurityDisabled() {
		return this.securityBuilder != null && this.securityBuilder.isDisabled();
	}

	IWorkflowBuilder getInternalBuilder() {
		return ensureInternalBuilder();
	}
}
