package com.garganttua.api.core.integ.crud;

import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.workflow.IWorkflow;
import com.garganttua.core.workflow.WorkflowExecutionOptions;
import com.garganttua.core.workflow.WorkflowInput;
import com.garganttua.core.workflow.WorkflowResult;

/**
 * Base class for CRUD script integration tests.
 * Executes workflow scripts directly, bypassing Domain.invoke().
 * This isolates the test to the .gs script behavior only.
 */
public abstract class AbstractCrudScriptTest extends AbstractCrudIntegrationTest {

	protected WorkflowResult executeScript(IDomain<?> ctx, IOperationRequest request) {
		IWorkflow workflow = ctx.getWorkflow();
		if (workflow == null) {
			throw new AssertionError("No workflow configured for domain: " + ctx.getDomainName());
		}

		// Set caller arg only when caller info is present (normally done by Domain.invoke())
		ICaller caller = request.caller();
		if (caller != null && caller.tenantId() != null) {
			request.arg("caller", caller);
		}

		// Set domain context and repository on request (needed by lifecycle hook expressions)
		request.arg("domainContext", ctx);
		request.arg("repository", ctx.getRepository());

		Map<String, Object> params = new java.util.LinkedHashMap<>();
		params.put("$1", ctx.getRepository());
		params.put("$2", ctx);
		// $3 = apiContext — required by stages that decode/serialize/extract/verify-authorization
		if (ctx instanceof com.garganttua.api.core.domain.Domain<?> dc) {
			params.put("$3", dc.getApiContext());
		}

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

	/**
	 * Non-super tenant script request. Use this when the test wants to exercise
	 * the full security pipeline (token decoding, rejection) — the
	 * {@code superTenantScriptRequest} variant bypasses VERIFY_AUTHORIZATION.
	 */
	protected static OperationRequest tenantScriptRequest(OperationDefinition operation, String tenantId) {
		OperationRequest request = new OperationRequest(new HashMap<>());
		request.arg(IOperationRequest.OPERATION, operation);
		request.arg(IOperationRequest.TENANT_ID, tenantId);
		request.arg(IOperationRequest.REQUESTED_TENANT_ID, tenantId);
		request.arg(IOperationRequest.SUPER_TENANT, false);
		request.arg(IOperationRequest.SUPER_OWNER, false);
		return request;
	}
}
