package com.garganttua.api.core.integ.crud;

import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.operation.OperationDefinition;
import com.garganttua.api.spec.service.IOperationRequest;
import com.garganttua.core.workflow.IWorkflow;
import com.garganttua.core.workflow.WorkflowExecutionOptions;
import com.garganttua.core.workflow.WorkflowInput;
import com.garganttua.core.workflow.WorkflowResult;

/**
 * Base class for CRUD script integration tests.
 * Executes workflow scripts directly, bypassing DomainContext.invoke().
 * This isolates the test to the .gs script behavior only.
 */
public abstract class AbstractCrudScriptTest extends AbstractCrudIntegrationTest {

	protected WorkflowResult executeScript(IDomainContext<?> ctx, String workflowName, IOperationRequest request) {
		IWorkflow workflow = ctx.getWorkflow(workflowName)
				.orElseThrow(() -> new AssertionError("Workflow not found: " + workflowName));

		// Set caller arg only when caller info is present (normally done by DomainContext.invoke())
		ICaller caller = request.caller();
		if (caller != null && caller.tenantId() != null) {
			request.arg("caller", caller);
		}

		// Set domain context and repository on request (needed by lifecycle hook expressions)
		request.arg("domainContext", ctx);
		request.arg("repository", ctx.getRepository());

		Map<String, Object> params = new HashMap<>();
		params.put("$1", ctx.getRepository());
		params.put("$2", ctx);

		WorkflowInput input = WorkflowInput.of(request, params);
		return workflow.execute(input, WorkflowExecutionOptions.none());
	}

	protected static OperationRequest superTenantScriptRequest(OperationDefinition operation) {
		OperationRequest request = new OperationRequest(new HashMap<>());
		request.arg(IOperationRequest.OPERATION, operation);
		request.arg(IOperationRequest.TENANT_ID, "SUPER_TENANT");
		request.arg(IOperationRequest.REQUESTED_TENANT_ID, "SUPER_TENANT");
		request.arg(IOperationRequest.SUPER_TENANT, true);
		request.arg(IOperationRequest.SUPER_OWNER, true);
		return request;
	}
}
