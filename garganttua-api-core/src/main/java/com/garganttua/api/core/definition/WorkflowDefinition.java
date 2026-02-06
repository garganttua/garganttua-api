package com.garganttua.api.core.definition;

import com.garganttua.api.spec.context.Access;
import com.garganttua.api.spec.context.Scope;
import com.garganttua.api.spec.context.TechnicalOperation;
import com.garganttua.api.spec.definition.IWorkflowDefinition;

public record WorkflowDefinition(
		String workflowName,
		String pathSuffix,
		String completePath,
		Scope scope,
		TechnicalOperation operation,
		Access access,
		boolean authority,
		boolean custom
) implements IWorkflowDefinition {

}
