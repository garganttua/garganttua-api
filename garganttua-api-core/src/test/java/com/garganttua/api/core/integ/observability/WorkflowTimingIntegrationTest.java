package com.garganttua.api.core.integ.observability;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.core.reflection.IClass;

/**
 * Smoke test for the {@code apiBuilder.workflowTiming(...)} DSL hook. The
 * api builder's only job here is to forward the {@code WorkflowTimingConfig}
 * to the workflow compilation so that core's {@code ScriptGenerator} injects
 * the right {@code observe(start|end, source)} markers in the generated
 * script. The downstream "do these markers actually fire events at runtime"
 * contract is owned by garganttua-core's observability test suite — we only
 * verify the script-level wiring here.
 *
 * <p>Workflow-level {@code IObserver<ObservableEvent>} registration is now
 * handled by core's {@code @Observer} scan (bootstrap-discovered via the
 * {@code ObservabilityBuilder}); the api-side DSL no longer exposes a
 * {@code workflowObserver(...)} method.
 */
@DisplayName("workflowTiming — apiBuilder forwards WorkflowTimingConfig to ScriptGenerator")
class WorkflowTimingIntegrationTest extends AbstractCrudIntegrationTest {

	private IApi buildApi(com.garganttua.core.workflow.WorkflowTimingConfig timing) throws ApiException {
		IApiBuilder builder = newBuilder();
		if (timing != null) builder.workflowTiming(timing);
		builder.domain(IClass.getClass(User.class))
				.tenant(true)
				.entity()
					.id("id").uuid("uuid").tenantId("tenantId")
				.up()
				.dto(IClass.getClass(UserDto.class))
					.id("id").uuid("uuid").tenantId("tenantId")
					.db(new CapturingDao())
				.up()
			.up();
		return buildAndStart(builder);
	}

	@Test
	@DisplayName("default (disabled): generated script contains NO stage:/script: observe markers")
	void timingDisabledLeavesScriptUntouched() throws ApiException {
		IApi api = buildApi(null); // default = disabled
		IDomain<?> domain = api.getDomain("users").orElseThrow();
		String script = domain.getWorkflow().getGeneratedScript();
		assertNotNull(script, "the workflow must expose its generated script");

		assertFalse(script.contains("observe(\"start\", \"stage:"),
				"timing-disabled scripts must NOT contain stage: observe() markers; got:\n" + script);
		assertFalse(script.contains("observe(\"start\", \"script:"),
				"timing-disabled scripts must NOT contain script: observe() markers");
	}

	@Test
	@DisplayName("stages(true) + scripts(true): generated script contains both stage: and script: observe markers")
	void timingEnabledInjectsBothMarkerKinds() throws ApiException {
		IApi api = buildApi(
				com.garganttua.core.workflow.WorkflowTimingConfig.of().stages(true).scripts(true));
		IDomain<?> domain = api.getDomain("users").orElseThrow();
		String script = domain.getWorkflow().getGeneratedScript();
		assertNotNull(script);

		assertTrue(script.contains("observe(\"start\", \"stage:"),
				"stages(true) must inject stage:<name> observe() markers; got:\n" + script);
		assertTrue(script.contains("observe(\"end\", \"stage:"),
				"stages(true) must inject paired end markers for every stage");
		assertTrue(script.contains("observe(\"start\", \"script:"),
				"scripts(true) must inject script:<stage>.<name> observe() markers");
		assertTrue(script.contains("observe(\"end\", \"script:"),
				"scripts(true) must inject paired end markers for every script");
	}

	@Test
	@DisplayName("stages(true) alone: only stage: markers — no script: markers")
	void timingStagesOnlyHasNoScriptMarkers() throws ApiException {
		IApi api = buildApi(
				com.garganttua.core.workflow.WorkflowTimingConfig.of().stages(true).scripts(false));
		IDomain<?> domain = api.getDomain("users").orElseThrow();
		String script = domain.getWorkflow().getGeneratedScript();

		assertTrue(script.contains("observe(\"start\", \"stage:"),
				"stages(true) must inject stage: markers");
		assertFalse(script.contains("observe(\"start\", \"script:"),
				"scripts(false) must NOT inject script: markers, even with stages(true)");
	}

	@Test
	@DisplayName("scripts(true) alone: only script: markers — no stage: markers")
	void timingScriptsOnlyHasNoStageMarkers() throws ApiException {
		IApi api = buildApi(
				com.garganttua.core.workflow.WorkflowTimingConfig.of().stages(false).scripts(true));
		IDomain<?> domain = api.getDomain("users").orElseThrow();
		String script = domain.getWorkflow().getGeneratedScript();

		assertFalse(script.contains("observe(\"start\", \"stage:"),
				"stages(false) must NOT inject stage: markers, even with scripts(true)");
		assertTrue(script.contains("observe(\"start\", \"script:"),
				"scripts(true) must inject script: markers");
	}
}
