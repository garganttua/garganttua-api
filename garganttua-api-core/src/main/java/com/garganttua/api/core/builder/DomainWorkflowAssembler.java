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
 * Assembles the merged workflow for a domain from its business rules, security, CRUD
 * and authorization stages. Stage ordering follows the pipeline documented in PIPELINE.md:
 * <ol>
 *   <li>init-codes — initialize all code variables to 405</li>
 *   <li>business-rules — TENANT_RULES (if multitenancy) + OWNER_RULES (if owner/owned)</li>
 *   <li>security — VERIFY_ACCESS + VERIFY_TENANT + VERIFY_OWNER (conditional)</li>
 *   <li>business operations — CRUD/AUTHENTICATE (guarded by preceding stages)</li>
 *   <li>create-authorization — after successful authenticate</li>
 *   <li>exit-code — propagate first error code</li>
 * </ol>
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
	private final boolean multiTenancyEnabled;
	private final boolean isOwnerOrOwned;
	private final IInjectionContextBuilder injectionContextBuilder;
	private final IExpressionContextBuilder expressionContextBuilder;

	DomainWorkflowAssembler(String domainName,
			Map<String, DomainWorkflowBuilder<E>> workflows,
			boolean securityEnabled,
			boolean hasAuthorization,
			boolean multiTenancyEnabled,
			boolean isOwnerOrOwned,
			IInjectionContextBuilder injectionContextBuilder,
			IExpressionContextBuilder expressionContextBuilder) {
		this.domainName = domainName;
		this.workflows = workflows;
		this.securityEnabled = securityEnabled;
		this.hasAuthorization = hasAuthorization;
		this.multiTenancyEnabled = multiTenancyEnabled;
		this.isOwnerOrOwned = isOwnerOrOwned;
		this.injectionContextBuilder = injectionContextBuilder;
		this.expressionContextBuilder = expressionContextBuilder;
	}

	IWorkflow assemble() {
		IWorkflowBuilder builder = WorkflowBuilder.create().name(this.domainName);
		if (this.injectionContextBuilder != null) {
			builder.provide(this.injectionContextBuilder);
		}
		if (this.expressionContextBuilder != null) {
			builder.provide(this.expressionContextBuilder);
		}

		List<String> allCodeVars = collectCodeVars();

		buildInitCodesStage(builder, allCodeVars);

		// Stage 5 — business rules (no guard, always run when configured)
		List<String> businessRuleCodeVars = buildBusinessRulesStages(builder);

		// Stage 6 — security (guarded by business rules)
		String businessRulesGuard = buildCompoundGuard(businessRuleCodeVars);
		List<String> securityCodeVars = buildSecurityStages(builder, businessRulesGuard);

		// Stage 8 — business operations (guarded by all security stages)
		String fullGuard = buildCompoundGuard(securityCodeVars);
		List<String> operationCodeVars = buildBusinessOperationStages(builder, fullGuard);
		operationCodeVars.addAll(buildCreateAuthorizationStage(builder, fullGuard));

		buildExitCodeStage(builder, allCodeVars, operationCodeVars);

		return builder.build();
	}

	private List<String> collectCodeVars() {
		List<String> codeVars = new ArrayList<>();

		// Business rules code vars
		if (multiTenancyEnabled) {
			codeVars.add("_tenant_rules_tenant_rules_code");
		}
		if (isOwnerOrOwned) {
			codeVars.add("_owner_rules_owner_rules_code");
		}

		// Security code vars
		if (securityEnabled) {
			codeVars.add("_verify_access_verify_access_code");
			if (multiTenancyEnabled) {
				codeVars.add("_verify_tenant_verify_tenant_code");
			}
			if (isOwnerOrOwned) {
				codeVars.add("_verify_owner_verify_owner_code");
			}
		}

		// CRUD operation code vars
		for (String label : this.workflows.keySet()) {
			if (CRUD_SCRIPT_PATHS.containsKey(label)) {
				String sanitized = label.replace("-", "_");
				codeVars.add("_" + sanitized + "_" + sanitized + "_code");
			}
		}

		// Authorization code var
		if (hasAuthorization) {
			codeVars.add("_create_authorization_create_authorization_code");
		}

		return codeVars;
	}

	private void buildInitCodesStage(IWorkflowBuilder builder, List<String> allCodeVars) {
		if (allCodeVars.isEmpty()) return;

		// Collect business rules code vars — these are initialized to 0 (pass by default)
		// because they may be skipped (e.g. for authenticate operations) and must not
		// block downstream security/CRUD stages when skipped.
		List<String> passThruVars = new ArrayList<>();
		if (multiTenancyEnabled) passThruVars.add("_tenant_rules_tenant_rules_code");
		if (isOwnerOrOwned) passThruVars.add("_owner_rules_owner_rules_code");

		StringBuilder initCodeScript = new StringBuilder();
		for (String codeVar : allCodeVars) {
			int initialValue = passThruVars.contains(codeVar) ? 0 : 405;
			initCodeScript.append(codeVar).append(" <- ").append(initialValue).append("\n");
		}
		builder.stage("init-codes")
				.script(initCodeScript.toString())
					.name("init-codes")
					.inline()
					.up()
				.up();
	}

	/**
	 * Stage 5 — Business rules. Returns the list of code variable names for the guard chain.
	 */
	/** Guard to skip business rules for authenticate operations (no caller available). */
	private static final String NOT_AUTHENTICATE_GUARD =
			"equals(equals(businessOperation(@0), \"authenticate\"), false)";

	private List<String> buildBusinessRulesStages(IWorkflowBuilder builder) {
		List<String> codeVars = new ArrayList<>();

		if (multiTenancyEnabled) {
			builder.stage("tenant-rules")
					.when(NOT_AUTHENTICATE_GUARD)
					.script("classpath:scripts/business/TENANT_RULES.gs")
						.name("tenant-rules")
						.input("operationRequest", "@0")
						.input("repository", "@1")
						.input("domainContext", "@2")
						.up()
					.up();
			codeVars.add("_tenant_rules_tenant_rules_code");
		}

		if (isOwnerOrOwned) {
			String ownerGuard = multiTenancyEnabled
					? "and(" + NOT_AUTHENTICATE_GUARD + ", equals(@_tenant_rules_tenant_rules_code, 0))"
					: NOT_AUTHENTICATE_GUARD;
			builder.stage("owner-rules")
					.when(ownerGuard)
					.script("classpath:scripts/business/OWNER_RULES.gs")
						.name("owner-rules")
						.input("operationRequest", "@0")
						.input("repository", "@1")
						.input("domainContext", "@2")
						.up()
					.up();
			codeVars.add("_owner_rules_owner_rules_code");
		}

		return codeVars;
	}

	/**
	 * Stage 6 — Security checks. Returns the accumulated list of code variable names
	 * (business rules + security) for the guard chain.
	 */
	private List<String> buildSecurityStages(IWorkflowBuilder builder, String businessRulesGuard) {
		List<String> codeVars = new ArrayList<>();

		// Carry forward business rules code vars
		if (multiTenancyEnabled) {
			codeVars.add("_tenant_rules_tenant_rules_code");
		}
		if (isOwnerOrOwned) {
			codeVars.add("_owner_rules_owner_rules_code");
		}

		if (!securityEnabled) return codeVars;

		// VERIFY_ACCESS — authorization token check
		var accessScript = builder.stage("verify-access")
				.script("classpath:scripts/security/VERIFY_ACCESS.gs")
					.name("verify-access")
					.input("operationRequest", "@0")
					.input("repository", "@1")
					.input("domainContext", "@2");
		if (businessRulesGuard != null) {
			accessScript.when(businessRulesGuard);
		}
		accessScript.up().up();
		codeVars.add("_verify_access_verify_access_code");

		// Guard for subsequent security scripts: business rules + verify-access
		String accessGuard = buildCompoundGuard(codeVars);

		// VERIFY_TENANT — tenant access check
		if (multiTenancyEnabled) {
			var tenantScript = builder.stage("verify-tenant")
					.script("classpath:scripts/security/VERIFY_TENANT.gs")
						.name("verify-tenant")
						.input("operationRequest", "@0")
						.input("repository", "@1")
						.input("domainContext", "@2");
			if (accessGuard != null) {
				tenantScript.when(accessGuard);
			}
			tenantScript.up().up();
			codeVars.add("_verify_tenant_verify_tenant_code");
		}

		// VERIFY_OWNER — owner access check
		if (isOwnerOrOwned) {
			String ownerGuard = buildCompoundGuard(codeVars);
			var ownerScript = builder.stage("verify-owner")
					.script("classpath:scripts/security/VERIFY_OWNER.gs")
						.name("verify-owner")
						.input("operationRequest", "@0")
						.input("repository", "@1")
						.input("domainContext", "@2");
			if (ownerGuard != null) {
				ownerScript.when(ownerGuard);
			}
			ownerScript.up().up();
			codeVars.add("_verify_owner_verify_owner_code");
		}

		return codeVars;
	}

	/**
	 * Builds a compound guard expression from a list of code variable names.
	 * Returns null if the list is empty.
	 * Single var: "equals(@var, 0)"
	 * Multiple vars: nested "and(equals(@a, 0), and(equals(@b, 0), equals(@c, 0)))"
	 */
	private String buildCompoundGuard(List<String> codeVars) {
		if (codeVars.isEmpty()) return null;
		if (codeVars.size() == 1) {
			return "equals(@" + codeVars.get(0) + ", 0)";
		}
		// Build right-to-left: and(a, and(b, c))
		String guard = "equals(@" + codeVars.get(codeVars.size() - 1) + ", 0)";
		for (int i = codeVars.size() - 2; i >= 0; i--) {
			guard = "and(equals(@" + codeVars.get(i) + ", 0), " + guard + ")";
		}
		return guard;
	}

	private List<String> buildBusinessOperationStages(IWorkflowBuilder builder, String guard) {
		List<String> operationCodeVars = new ArrayList<>();
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
				if (guard != null) {
					scriptBuilder.when(guard);
				}
				scriptBuilder.up().up();
				String sanitized = label.replace("-", "_");
				operationCodeVars.add("_" + sanitized + "_" + sanitized + "_code");
			}
		}
		return operationCodeVars;
	}

	private List<String> buildCreateAuthorizationStage(IWorkflowBuilder builder, String guard) {
		if (!hasAuthorization) return List.of();

		String createAuthGuard = "equals(@_authenticate_authenticate_code, 0)";
		if (guard != null) {
			createAuthGuard = "and(" + createAuthGuard + ", " + guard + ")";
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
		return List.of("_create_authorization_create_authorization_code");
	}

	/**
	 * Builds the exit-code stage. Error codes are checked across ALL code vars (infrastructure + operations).
	 * Success (code 0) is only checked on operation code vars — infrastructure stages returning 0
	 * just means "checks passed", not "operation succeeded".
	 */
	private void buildExitCodeStage(IWorkflowBuilder builder, List<String> allCodeVars, List<String> operationCodeVars) {
		StringBuilder exitCodeScript = new StringBuilder("405 -> 405\n");
		// Error codes from any stage (infrastructure or operation)
		for (int code : List.of(500, 409, 404, 403, 401, 400)) {
			for (String codeVar : allCodeVars) {
				exitCodeScript.append("    | equals(@").append(codeVar).append(", ").append(code).append(") -> ").append(code).append("\n");
			}
		}
		// Success code only from operation stages
		for (String codeVar : operationCodeVars) {
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
