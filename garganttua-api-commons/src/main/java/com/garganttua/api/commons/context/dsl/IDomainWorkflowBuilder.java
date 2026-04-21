package com.garganttua.api.commons.context.dsl;

import com.garganttua.api.commons.operation.Scope;
import com.garganttua.api.commons.operation.TechnicalOperation;
import com.garganttua.api.commons.context.dsl.security.IWorkflowSecurityBuilder;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.workflow.IWorkflow;
import com.garganttua.core.workflow.dsl.IWorkflowBuilder;

public interface IDomainWorkflowBuilder<E>
		extends IAutomaticLinkedBuilder<IDomainWorkflowBuilder<E>, IDomainBuilder<E>, IWorkflow> {

	IDomainWorkflowBuilder<E> pathSuffix(String suffix);

	IDomainWorkflowBuilder<E> completePath(String path);

	IDomainWorkflowBuilder<E> scope(Scope scope);

	IDomainWorkflowBuilder<E> operation(TechnicalOperation operation);

	IWorkflowSecurityBuilder<E> security();

	IDomainWorkflowBuilder<E> variable(String name, Object value);

	IDomainWorkflowBuilder<E> inlineAll();

	IWorkflowBuilder stages();

}
