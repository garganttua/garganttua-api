package com.garganttua.api.core.builder;

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
import com.garganttua.core.workflow.dsl.WorkflowBuilder;

public class DomainWorkflowBuilder<E>
		extends AbstractAutomaticLinkedBuilder<IDomainWorkflowBuilder<E>, IDomainBuilder<E>, IWorkflow>
		implements IDomainWorkflowBuilder<E> {

	private final String workflowName;
	private final IWorkflowBuilder internalBuilder;
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
		this.internalBuilder = WorkflowBuilder.create().name(workflowName);
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
		this.internalBuilder.variable(name, value);
		return this;
	}

	@Override
	public IDomainWorkflowBuilder<E> inlineAll() {
		this.internalBuilder.inlineAll();
		return this;
	}

	@Override
	public IWorkflowBuilder stages() {
		return this.internalBuilder;
	}

	void setDependencyBuilders(IInjectionContextBuilder injectionContextBuilder,
			IExpressionContextBuilder expressionContextBuilder) {
		this.injectionContextBuilder = injectionContextBuilder;
		this.expressionContextBuilder = expressionContextBuilder;
	}

	@Override
	protected synchronized IWorkflow doBuild() throws ApiException {
		if (this.injectionContextBuilder != null) {
			this.internalBuilder.provide(this.injectionContextBuilder);
		}
		if (this.expressionContextBuilder != null) {
			this.internalBuilder.provide(this.expressionContextBuilder);
		}
		return this.internalBuilder.build();
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
		return this.internalBuilder;
	}
}
