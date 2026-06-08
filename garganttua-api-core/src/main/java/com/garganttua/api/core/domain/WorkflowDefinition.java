package com.garganttua.api.core.domain;

import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.Scope;
import com.garganttua.api.commons.operation.TechnicalOperation;
import com.garganttua.api.commons.definition.IWorkflowDefinition;

public record WorkflowDefinition(
		String workflowName,
		String pathSuffix,
		String completePath,
		Scope scope,
		TechnicalOperation operation,
		Access access,
		boolean authority,
		String authorityName,
		boolean custom
) implements IWorkflowDefinition {

}
