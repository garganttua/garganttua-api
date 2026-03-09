package com.garganttua.api.spec.definition;

import com.garganttua.api.spec.operation.Access;
import com.garganttua.api.spec.operation.Scope;
import com.garganttua.api.spec.operation.TechnicalOperation;

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
