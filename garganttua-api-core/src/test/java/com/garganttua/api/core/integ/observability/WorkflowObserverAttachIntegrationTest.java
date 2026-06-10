package com.garganttua.api.core.integ.observability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.core.dsl.dependency.IDependentBuilder;
import com.garganttua.core.expression.dsl.ExpressionContextBuilder;
import com.garganttua.core.expression.dsl.IExpressionContextBuilder;
import com.garganttua.core.injection.context.dsl.IInjectionContextBuilder;
import com.garganttua.core.injection.context.dsl.InjectionContextBuilder;
import com.garganttua.core.observability.EndEvent;
import com.garganttua.core.observability.IObservable;
import com.garganttua.core.observability.IObserver;
import com.garganttua.core.observability.ObservableEvent;
import com.garganttua.core.observability.StartEvent;
import com.garganttua.core.observability.dsl.IObservabilityBuilder;
import com.garganttua.core.observability.dsl.ObservabilityBuilder;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.dsl.IReflectionBuilder;
import com.garganttua.core.reflection.dsl.ReflectionBuilder;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.core.reflections.ReflectionsAnnotationScanner;
import com.garganttua.core.runtime.RuntimeContextFactory;
import com.garganttua.core.runtime.dsl.IRuntimesBuilder;
import com.garganttua.core.runtime.dsl.RuntimesBuilder;
import com.garganttua.core.script.dsl.IScriptsBuilder;
import com.garganttua.core.script.dsl.ScriptsBuilder;
import com.garganttua.core.workflow.IWorkflow;
import com.garganttua.core.workflow.WorkflowTimingConfig;
import com.garganttua.core.workflow.dsl.IWorkflowsBuilder;
import com.garganttua.core.workflow.dsl.WorkflowsBuilder;

/**
 * End-to-end coverage for the {@code IApiBuilder.workflowTiming(...)} surface and
 * the workflow-source attach wired in {@code ApiBuilder.build()}.
 *
 * <p>Two co-dependent gaps are exercised here:
 * <ol>
 *   <li><b>Gap #1</b> — without {@code workflowTiming(...)} the generated workflow
 *       script carries no {@code observe("start"|"end", ...)} markers, so no
 *       {@code stage:*} / {@code script:*} event can ever fire. The setter is the
 *       only consumer-reachable path to inject them.</li>
 *   <li><b>Gap #2</b> — even when generated, the markers fire on the workflow's
 *       own {@code ObservableRegistry} (pushed during {@code execute()}). They only
 *       reach a scanned/subscribed observer because {@code ApiBuilder.build()}
 *       attaches each domain's {@code IWorkflow} (an {@link IObservable}) to the
 *       observability binding.</li>
 * </ol>
 *
 * <p>The {@code stage:<name>} / {@code script:<stage>.<name>} source format is
 * produced by core's {@code ScriptGenerator} and asserted verbatim below.
 */
@DisplayName("Workflow timing observability — workflowTiming(...) + workflow source attach")
class WorkflowObserverAttachIntegrationTest extends AbstractCrudIntegrationTest {

	static class RecordingObserver implements IObserver<ObservableEvent> {
		final List<ObservableEvent> events = new CopyOnWriteArrayList<>();
		@Override public void onEvent(ObservableEvent event) {
			events.add(event);
		}
	}

	// ───── Builder stack ─────
	//
	// Mirrors AbstractCrudIntegrationTest.newBaseBuilder but keeps the
	// IInjectionContextBuilder reference around so an ObservabilityBuilder can be
	// stood up and provided, exercising the build()-time attachSource path.

	private static final class Stack {
		IApiBuilder api;
		IInjectionContextBuilder injection;
	}

	private static Stack newStack() throws ApiException {
		IReflectionBuilder reflectionBuilder = ReflectionBuilder.builder()
				.withProvider(new RuntimeReflectionProvider())
				.withScanner(new ReflectionsAnnotationScanner());
		IClass.setReflection(reflectionBuilder.build());

		IApiBuilder builder = com.garganttua.api.core.api.ApiBuilder.builder();

		IInjectionContextBuilder injectionContextBuilder = InjectionContextBuilder.builder()
				.childContextFactory(new RuntimeContextFactory());
		((IDependentBuilder<IInjectionContextBuilder, ?>) injectionContextBuilder).provide(reflectionBuilder);
		injectionContextBuilder.build();

		IExpressionContextBuilder expressionContextBuilder = ExpressionContextBuilder.builder();
		expressionContextBuilder.autoDetect(true);
		expressionContextBuilder.withPackage("com.garganttua.core.expression.functions");
		expressionContextBuilder.withPackage("com.garganttua.core.script.functions");
		expressionContextBuilder.withPackage("com.garganttua.core.observability");
		expressionContextBuilder.withPackage("com.garganttua.api.core.expression");
		((IDependentBuilder<IExpressionContextBuilder, ?>) expressionContextBuilder).provide(injectionContextBuilder);
		expressionContextBuilder.build();

		((IDependentBuilder<IApiBuilder, IApi>) builder).provide(reflectionBuilder);
		((IDependentBuilder<IApiBuilder, IApi>) builder).provide(injectionContextBuilder);
		((IDependentBuilder<IApiBuilder, IApi>) builder).provide(expressionContextBuilder);

		IRuntimesBuilder runtimesBuilder = RuntimesBuilder.builder();
		((IDependentBuilder<IRuntimesBuilder, ?>) runtimesBuilder).provide(injectionContextBuilder);
		IScriptsBuilder scriptsBuilder = ScriptsBuilder.builder();
		((IDependentBuilder<IScriptsBuilder, ?>) scriptsBuilder).provide(injectionContextBuilder);
		((IDependentBuilder<IScriptsBuilder, ?>) scriptsBuilder).provide(expressionContextBuilder);
		((IDependentBuilder<IScriptsBuilder, ?>) scriptsBuilder).provide(runtimesBuilder);
		IWorkflowsBuilder workflowsBuilder = WorkflowsBuilder.builder();
		((IDependentBuilder<IWorkflowsBuilder, ?>) workflowsBuilder).provide(injectionContextBuilder);
		((IDependentBuilder<IWorkflowsBuilder, ?>) workflowsBuilder).provide(scriptsBuilder);
		((IDependentBuilder<IApiBuilder, IApi>) builder).provide(workflowsBuilder);

		builder.superTenantId("SUPER_TENANT").superTenantAutoCreate(false);

		Stack stack = new Stack();
		stack.api = builder;
		stack.injection = injectionContextBuilder;
		return stack;
	}

	private static void declareUsersDomain(IApiBuilder builder) throws ApiException {
		builder.domain(IClass.getClass(User.class))
				.tenant(true)
				.superTenant("superTenant")
				.entity()
					.id("id").uuid("uuid").tenantId("tenantId")
				.up()
				.dto(IClass.getClass(UserDto.class))
					.id("id").uuid("uuid").tenantId("tenantId")
					.db(new CapturingDao())
				.up()
				.security().disable(true).up()
			.up();
	}

	private static OperationRequest readAllRequest(IDomain<?> domain) {
		OperationDefinition op = OperationDefinition.readAllWithStandardSecurity(
				domain.getDomainName(), IClass.getClass(User.class));
		OperationRequest req = new OperationRequest(new java.util.HashMap<>());
		req.arg(IOperationRequest.OPERATION, op);
		req.arg(IOperationRequest.TENANT_ID, "SUPER_TENANT");
		req.arg(IOperationRequest.REQUESTED_TENANT_ID, "SUPER_TENANT");
		req.arg(IOperationRequest.SUPER_TENANT, true);
		req.arg(IOperationRequest.SUPER_OWNER, true);
		return req;
	}

	private static List<ObservableEvent> withPrefix(RecordingObserver obs, String prefix) {
		return obs.events.stream()
				.filter(e -> e.source() != null && e.source().startsWith(prefix))
				.toList();
	}

	@Nested
	@DisplayName("Gap #1 — workflowTiming(...) is the switch that injects the markers")
	class TimingSwitch {

		/**
		 * Attaches the recorder directly to the domain's workflow registry, so this
		 * test isolates marker *generation* from the build()-time attach path. With
		 * timing enabled, executing the workflow must fire stage and script events.
		 */
		@Test
		@DisplayName("stages(true).scripts(true) → stage:* and script:* Start+End fire on Domain.invoke")
		void timingEnabledFiresStageAndScriptEvents() throws ApiException {
			Stack stack = newStack();
			stack.api.workflowTiming(WorkflowTimingConfig.of().stages(true).scripts(true));
			declareUsersDomain(stack.api);
			IApi api = buildAndStart(stack.api);

			IDomain<?> domain = api.getDomain("users").orElseThrow();
			IWorkflow wf = domain.getWorkflow();
			assertTrue(wf instanceof IObservable, "domain workflow must be observable");
			RecordingObserver obs = new RecordingObserver();
			((IObservable) wf).addObserver(obs);

			IOperationResponse resp = domain.invoke(readAllRequest(domain));
			assertEquals(OperationResponseCode.OK, resp.getResponseCode(),
					"timing instrumentation must not alter the operation outcome");

			List<ObservableEvent> stageEvents = withPrefix(obs, "stage:");
			assertFalse(stageEvents.isEmpty(),
					"at least one stage:* event must fire when stage timing is on; got sources: "
							+ obs.events.stream().map(ObservableEvent::source).toList());
			assertTrue(stageEvents.stream().anyMatch(e -> e instanceof StartEvent),
					"a stage:* StartEvent must fire");
			assertTrue(stageEvents.stream().anyMatch(e -> e instanceof EndEvent),
					"a stage:* EndEvent must fire");

			List<ObservableEvent> scriptEvents = withPrefix(obs, "script:");
			assertFalse(scriptEvents.isEmpty(),
					"at least one script:* event must fire when script timing is on; got sources: "
							+ obs.events.stream().map(ObservableEvent::source).toList());
			assertTrue(scriptEvents.stream().anyMatch(e -> e instanceof StartEvent),
					"a script:* StartEvent must fire");
			assertTrue(scriptEvents.stream().anyMatch(e -> e instanceof EndEvent),
					"a script:* EndEvent must fire");

			// The readAll business stage runs for this request, so its timing
			// markers must carry the exact stage:readAll / script:readAll.readAll
			// sources — concrete proof the markers are keyed by the real stage and
			// script names, not a generic placeholder.
			assertTrue(stageEvents.stream().anyMatch(e -> "stage:readAll".equals(e.source())),
					"the readAll business stage must emit stage:readAll; got: "
							+ stageEvents.stream().map(ObservableEvent::source).distinct().toList());
			assertTrue(scriptEvents.stream().anyMatch(e -> "script:readAll.readAll".equals(e.source())),
					"the readAll script must emit script:readAll.readAll; got: "
							+ scriptEvents.stream().map(ObservableEvent::source).distinct().toList());

			// exit-code stage is inline and always runs — its timing marker is a
			// stable anchor independent of guard evaluation.
			assertTrue(stageEvents.stream().anyMatch(e -> "stage:exit-code".equals(e.source())),
					"the always-run exit-code stage must emit stage:exit-code; got: "
							+ stageEvents.stream().map(ObservableEvent::source).distinct().toList());
		}

		/**
		 * Default config is {@link WorkflowTimingConfig#disabled()}: no markers are
		 * generated, so a recorder on the workflow sees zero timing events even
		 * though the operation completes normally. Guards criterion #3/#4 — zero
		 * overhead, no events until opted in.
		 */
		@Test
		@DisplayName("default (no workflowTiming call) → not a single stage:* or script:* event")
		void timingDisabledByDefaultFiresNoTimingEvents() throws ApiException {
			Stack stack = newStack();
			declareUsersDomain(stack.api);
			IApi api = buildAndStart(stack.api);

			IDomain<?> domain = api.getDomain("users").orElseThrow();
			RecordingObserver obs = new RecordingObserver();
			((IObservable) domain.getWorkflow()).addObserver(obs);

			IOperationResponse resp = domain.invoke(readAllRequest(domain));
			assertEquals(OperationResponseCode.OK, resp.getResponseCode(),
					"default (timing-disabled) build must still complete the operation normally");

			assertTrue(withPrefix(obs, "stage:").isEmpty(),
					"no stage:* event may fire with timing disabled; got: "
							+ withPrefix(obs, "stage:").stream().map(ObservableEvent::source).toList());
			assertTrue(withPrefix(obs, "script:").isEmpty(),
					"no script:* event may fire with timing disabled; got: "
							+ withPrefix(obs, "script:").stream().map(ObservableEvent::source).toList());
		}

		@Test
		@DisplayName("stages(true).scripts(false) → stage:* fire, script:* stay silent")
		void stagesOnlyOmitsScriptEvents() throws ApiException {
			Stack stack = newStack();
			stack.api.workflowTiming(WorkflowTimingConfig.of().stages(true).scripts(false));
			declareUsersDomain(stack.api);
			IApi api = buildAndStart(stack.api);

			IDomain<?> domain = api.getDomain("users").orElseThrow();
			RecordingObserver obs = new RecordingObserver();
			((IObservable) domain.getWorkflow()).addObserver(obs);

			domain.invoke(readAllRequest(domain));

			assertFalse(withPrefix(obs, "stage:").isEmpty(),
					"stage:* events must still fire when only stages are enabled");
			assertTrue(withPrefix(obs, "script:").isEmpty(),
					"no script:* event may fire when scripts(false); got: "
							+ withPrefix(obs, "script:").stream().map(ObservableEvent::source).toList());
		}
	}

	@Nested
	@DisplayName("Gap #2 — build() attaches the workflow so a subscribed observer receives the markers")
	class BindingAttach {

		/**
		 * Full end-to-end: an observer subscribed on an {@link IObservabilityBuilder}
		 * (the same surface a scanned {@code @Observer} feeds) must receive the
		 * timing events after a Domain.invoke — proving ApiBuilder.build() attached
		 * the workflow as an observability source.
		 */
		@Test
		@DisplayName("subscribed observer receives stage:* Start+End sharing one executionId after invoke")
		void subscribedObserverReceivesStageEvents() throws Exception {
			Stack stack = newStack();
			stack.api.workflowTiming(WorkflowTimingConfig.of().stages(true).scripts(true));

			RecordingObserver obs = new RecordingObserver();
			IObservabilityBuilder observability = ObservabilityBuilder.create();
			((IDependentBuilder<IObservabilityBuilder, ?>) observability).provide(stack.injection);
			observability.subscribe(obs).up();
			((IDependentBuilder<IApiBuilder, IApi>) stack.api).provide(observability);

			declareUsersDomain(stack.api);
			IApi api = buildAndStart(stack.api);

			IDomain<?> domain = api.getDomain("users").orElseThrow();
			IOperationResponse resp = domain.invoke(readAllRequest(domain));
			assertEquals(OperationResponseCode.OK, resp.getResponseCode(),
					"the observed invoke must itself succeed");

			List<ObservableEvent> stageEvents = withPrefix(obs, "stage:");
			assertFalse(stageEvents.isEmpty(),
					"subscribed observer must receive stage:* events — build() must attach the workflow; "
							+ "got sources: " + obs.events.stream().map(ObservableEvent::source).toList());

			StartEvent start = stageEvents.stream()
					.filter(e -> e instanceof StartEvent).map(e -> (StartEvent) e)
					.findFirst().orElseThrow();
			EndEvent end = stageEvents.stream()
					.filter(e -> e instanceof EndEvent).map(e -> (EndEvent) e)
					.filter(e -> e.source().equals(start.source()))
					.findFirst().orElseThrow();
			assertNotNull(start.executionId(), "stage StartEvent must carry an executionId");
			assertEquals(start.executionId(), end.executionId(),
					"Start + End of the same stage must share one executionId");

			List<ObservableEvent> scriptEvents = withPrefix(obs, "script:");
			assertFalse(scriptEvents.isEmpty(),
					"subscribed observer must also receive script:<stage>.<name> events; got sources: "
							+ obs.events.stream().map(ObservableEvent::source).toList());
			assertTrue(scriptEvents.stream().allMatch(e -> e.source().matches("script:[^.]+\\..+")),
					"every script event source must follow script:<stage>.<name>; got: "
							+ scriptEvents.stream().map(ObservableEvent::source).distinct().toList());
		}

		/**
		 * Criterion #4: with no observer subscribed, the build()-time attach is a
		 * no-op and Domain.invoke still completes — core's hasObservers()
		 * short-circuit keeps an attached-but-quiet workflow silent.
		 */
		@Test
		@DisplayName("no subscriber → attach is a harmless no-op, invoke still succeeds")
		void noSubscriberIsNoOp() throws Exception {
			Stack stack = newStack();
			stack.api.workflowTiming(WorkflowTimingConfig.of().stages(true).scripts(true));

			IObservabilityBuilder observability = ObservabilityBuilder.create();
			((IDependentBuilder<IObservabilityBuilder, ?>) observability).provide(stack.injection);
			((IDependentBuilder<IApiBuilder, IApi>) stack.api).provide(observability);

			declareUsersDomain(stack.api);
			IApi api = buildAndStart(stack.api);

			IDomain<?> domain = api.getDomain("users").orElseThrow();
			// Timing is enabled and the workflow is attached to the binding, but no
			// observer is subscribed — core's hasObservers() short-circuit must make
			// the markers inert: the invoke returns a response without throwing, and
			// the build()-time attachSource was a harmless no-op.
			IOperationResponse resp = domain.invoke(readAllRequest(domain));
			assertNotNull(resp, "invoke must return a response even with a zero-subscriber binding");
			assertEquals(OperationResponseCode.OK, resp.getResponseCode(),
					"a zero-subscriber observability setup must not affect the operation outcome");
		}
	}
}
