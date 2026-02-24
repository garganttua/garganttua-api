package com.garganttua.api.spec.definition;

import com.garganttua.api.spec.context.Access;
import com.garganttua.api.spec.context.Scope;
import com.garganttua.api.spec.context.TechnicalOperation;

public interface IWorkflowDefinition {

	String workflowName();

	String pathSuffix();

	String completePath();

	Scope scope();

	TechnicalOperation operation();

	Access access();

	boolean authority();

	boolean custom();
}
