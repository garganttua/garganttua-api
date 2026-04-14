package com.garganttua.api.core.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.operation.BusinessOperation;
import com.garganttua.core.expression.dsl.IExpressionContextBuilder;
import com.garganttua.core.injection.context.dsl.IInjectionContextBuilder;
import com.garganttua.core.workflow.IWorkflow;
import com.garganttua.core.workflow.dsl.IWorkflowBuilder;
import com.garganttua.core.workflow.dsl.WorkflowBuilder;

/**
 * Assembles the merged workflow for a domain from its CRUD, security and authorization stages.
 * Extracted from {@link DomainBuilder#doBuild()} to reduce class size.
 */
class DomainWorkflowAssembler<E> {

	static final Map<String, String> CRUD_SCRIPT_PATHS = Map.of(
			BusinessOperation.create.getLabel(), "scripts/business/CREATE_ONE.gs",
			BusinessOperation.readAll.getLabel(), "scripts/business/READ_ALL.gs",
			BusinessOperation.readOne.getLabel(), "scripts/business/READ_ONE.gs",
			BusinessOperation.update.getLabel(), "scripts/business/UPDATE_ONE.gs",
			BusinessOperation.deleteOne.getLabel(), "scripts/business/DELETE_ONE.gs",
			BusinessOperation.deleteAll.getLabel(), "scripts/business/DELETE_ALL.gs",
			BusinessOperation.authenticate.getLabel(), "scripts/business/AUTHENTICATE.gs"
	);

	private final String domainName;
	private final Map<String, DomainWorkflowBuilder<E>> workflows;
	private final boolean securityEnabled;
	private final boolean hasAuthorization;
	private final IInjectionContextBuilder injectionContextBuilder;
	private final IExpressionContextBuilder expressionContextBuilder;

	DomainWorkflowAssembler(String domainName,
			Map<String, DomainWorkflowBuilder<E>> workflows,
			boolean securityEnabled,
			boolean hasAuthorization,
			IInjectionContextBuilder injectionContextBuilder,
			IExpressionContextBuilder expressionContextBuilder) {
		this.domainName = domainName;
		this.workflows = workflows;
		this.securityEnabled = securityEnabled;
		this.hasAuthorization = hasAuthorization;
		this.injectionContextBuilder = injectionContextBuilder;
		this.expressionContextBuilder = expressionContextBuilder;
	}

	IWorkflow assemble() {
		IWorkflowBuilder mergedBuilder = WorkflowBuilder.create().name(this.domainName);
		if (this.injectionContextBuilder != null) {
			mergedBuilder.provide(this.injectionContextBuilder);
		}
		if (this.expressionContextBuilder != null) {
			mergedBuilder.provide(this.expressionContextBuilder);
		}

		List<String> codeVars = collectCodeVars();

		buildInitCodesStage(mergedBuilder, codeVars);
		buildSecurityStage(mergedBuilder);

		String securityGuard = securityEnabled
				? "equals(@_security_verify_access_code, 0)"
				: null;

		buildBusinessStages(mergedBuilder, securityGuard);
		buildCreateAuthorizationStage(mergedBuilder, securityGuard);
		buildExitCodeStage(mergedBuilder, codeVars);

		return mergedBuilder.build();
	}

	private List<String> collectCodeVars() {
		List<String> codeVars = new ArrayList<>();
		if (securityEnabled) {
			codeVars.add("_security_verify_access_code");
		}
		for (String label : this.workflows.keySet()) {
			if (CRUD_SCRIPT_PATHS.containsKey(label)) {
				String sanitized = label.replace("-", "_");
				codeVars.add("_" + sanitized + "_" + sanitized + "_code");
			}
		}
		if (hasAuthorization) {
			codeVars.add("_create_authorization_create_authorization_code");
		}
		return codeVars;
	}

	private void buildInitCodesStage(IWorkflowBuilder builder, List<String> codeVars) {
		if (codeVars.isEmpty()) return;

		StringBuilder initCodeScript = new StringBuilder();
		for (String codeVar : codeVars) {
			initCodeScript.append(codeVar).append(" <- 405\n");
		}
		builder.stage("init-codes")
				.script(initCodeScript.toString())
					.name("init-codes")
					.inline()
					.up()
				.up();
	}

	private void buildSecurityStage(IWorkflowBuilder builder) {
		if (!securityEnabled) return;

		builder.stage("security")
				.script("classpath:scripts/security/VERIFY_ACCESS.gs")
					.name("verify-access")
					.input("operationRequest", "@0")
					.input("repository", "@1")
					.input("domainContext", "@2")
					.up()
				.up();
	}

	private void buildBusinessStages(IWorkflowBuilder builder, String securityGuard) {
		for (Map.Entry<String, DomainWorkflowBuilder<E>> entry : this.workflows.entrySet()) {
			String label = entry.getKey();
			DomainWorkflowBuilder<E> wb = entry.getValue();
			if (wb.isSecurityDisabled()) {
				continue;
			}

			String scriptPath = CRUD_SCRIPT_PATHS.get(label);
			if (scriptPath != null) {
				var scriptBuilder = builder.stage(label)
						.when("equals(businessOperation(@0), \"" + label + "\")")
						.script("classpath:" + scriptPath)
							.name(label)
							.input("operationRequest", "@0")
							.input("repository", "@1")
							.input("domainContext", "@2");
				if (securityGuard != null) {
					scriptBuilder.when(securityGuard);
				}
				scriptBuilder.up().up();
			}
		}
	}

	private void buildCreateAuthorizationStage(IWorkflowBuilder builder, String securityGuard) {
		if (!hasAuthorization) return;

		String createAuthGuard = "equals(@_authenticate_authenticate_code, 0)";
		if (securityGuard != null) {
			createAuthGuard = "and(" + createAuthGuard + ", " + securityGuard + ")";
		}
		builder.stage("create-authorization")
				.when("equals(businessOperation(@0), \"authenticate\")")
				.script("classpath:scripts/business/CREATE_AUTHORIZATION.gs")
					.name("create-authorization")
					.input("operationRequest", "@0")
					.input("repository", "@1")
					.input("domainContext", "@2")
					.input("authResult", "@output")
					.output("output", "output")
					.when(createAuthGuard)
					.up()
				.up();
	}

	private void buildExitCodeStage(IWorkflowBuilder builder, List<String> codeVars) {
		StringBuilder exitCodeScript = new StringBuilder("405 -> 405\n");
		for (int code : List.of(500, 409, 404, 403, 401, 400)) {
			for (String codeVar : codeVars) {
				exitCodeScript.append("    | equals(@").append(codeVar).append(", ").append(code).append(") -> ").append(code).append("\n");
			}
		}
		for (String codeVar : codeVars) {
			exitCodeScript.append("    | equals(@").append(codeVar).append(", 0) -> 0\n");
		}
		builder.stage("exit-code")
				.script(exitCodeScript.toString())
					.name("propagate-exit-code")
					.inline()
					.up()
				.up();
	}
}
