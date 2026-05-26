package com.garganttua.api.core.integ.observability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
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
import com.garganttua.core.observability.EndEvent;
import com.garganttua.core.observability.ErrorEvent;
import com.garganttua.core.observability.IObserver;
import com.garganttua.core.observability.ObservableEvent;
import com.garganttua.core.observability.StartEvent;
import com.garganttua.core.reflection.IClass;

/**
 * Smoke test for the {@code apiBuilder.workflowObserver(...)} DSL hook — the
 * deeper, core-level counterpart of {@code IApiObserver}. We register an
 * {@code IObserver<ObservableEvent>} on the api builder, run a single readAll
 * through {@code Domain.invoke}, and assert that:
 * <ul>
 *   <li>start/end events from the workflow stream actually reached the
 *       observer (so the wiring at build time is correct),</li>
 *   <li>multiple registrations are all fired,</li>
 *   <li>no event is fired when no observer is registered (zero overhead is
 *       handled inside core's {@code ObservableRegistry.hasObservers} —
 *       nothing to assert here beyond a smoke check that the build still
 *       succeeds).</li>
 * </ul>
 */
@DisplayName("workflowObserver — apiBuilder shortcut wiring core ObservableEvents onto every domain workflow")
class WorkflowObserverIntegrationTest extends AbstractCrudIntegrationTest {

	static class RecordingObserver implements IObserver<ObservableEvent> {
		final List<ObservableEvent> events = new CopyOnWriteArrayList<>();
		@Override public void onEvent(ObservableEvent event) {
			events.add(event);
		}
	}

	private IApi buildApi(IObserver<ObservableEvent>... observers) throws ApiException {
		return buildApi(null, observers);
	}

	private IApi buildApi(com.garganttua.core.workflow.WorkflowTimingConfig timing,
			IObserver<ObservableEvent>... observers) throws ApiException {
		IApiBuilder builder = newBuilder();
		if (timing != null) builder.workflowTiming(timing);
		for (IObserver<ObservableEvent> o : observers) builder.workflowObserver(o);
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

	private OperationRequest readAllRequest(IDomain<?> domain) {
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

	@Nested
	@DisplayName("Basic wiring")
	class BasicWiring {

		@Test
		@DisplayName("workflow events reach a single registered observer")
		void singleObserverReceivesEvents() throws ApiException {
			RecordingObserver observer = new RecordingObserver();
			IApi api = buildApi(observer);
			IDomain<?> domain = api.getDomain("users").orElseThrow();

			IOperationResponse resp = domain.invoke(readAllRequest(domain));

			assertEquals(OperationResponseCode.OK, resp.getResponseCode(),
					"readAll should succeed; got " + resp.getResponseCode() + " : " + resp.getResponse());

			// We don't pin the exact count — engines emit a variable number of
			// events per execution as the framework evolves. We only assert
			// that the observer DID receive events, and that at least one
			// Start and one End showed up (proves the wiring goes both ways).
			assertFalse(observer.events.isEmpty(),
					"observer registered via apiBuilder.workflowObserver(...) should have received at least one event");
			assertTrue(observer.events.stream().anyMatch(e -> e instanceof StartEvent),
					"observer should have received at least one StartEvent, got: " + observer.events);
			assertTrue(observer.events.stream().anyMatch(e -> e instanceof EndEvent),
					"observer should have received at least one EndEvent, got: " + observer.events);
		}

		@Test
		@DisplayName("multiple observers all receive the same event stream (in registration order is not asserted; presence is)")
		void multipleObserversAllReceiveEvents() throws ApiException {
			RecordingObserver first = new RecordingObserver();
			RecordingObserver second = new RecordingObserver();
			IApi api = buildApi(first, second);
			IDomain<?> domain = api.getDomain("users").orElseThrow();

			domain.invoke(readAllRequest(domain));

			assertFalse(first.events.isEmpty(), "first observer must receive events");
			assertFalse(second.events.isEmpty(), "second observer must receive events");
			assertEquals(first.events.size(), second.events.size(),
					"both observers must see the same number of events; " +
					"first=" + first.events.size() + " second=" + second.events.size());
		}

		@Test
		@DisplayName("every event shares the same executionId — single logical execution correlation")
		void eventsShareExecutionId() throws ApiException {
			RecordingObserver observer = new RecordingObserver();
			IApi api = buildApi(observer);
			IDomain<?> domain = api.getDomain("users").orElseThrow();

			domain.invoke(readAllRequest(domain));

			assertFalse(observer.events.isEmpty());
			java.util.UUID first = observer.events.get(0).executionId();
			assertNotNull(first, "every observable event must carry an executionId");
			for (ObservableEvent e : observer.events) {
				assertEquals(first, e.executionId(),
						"all events of a single Domain.invoke must share an executionId " +
						"(garganttua-core's ObservableContextHolder.Session propagation); " +
						"saw " + e.executionId() + " for source " + e.source());
			}
		}
	}

	@Nested
	@DisplayName("Exception isolation")
	class ExceptionIsolation {

		@Test
		@DisplayName("an observer that throws does not break the workflow nor starve sibling observers")
		void throwingObserverIsIsolated() throws ApiException {
			IObserver<ObservableEvent> throwing = event -> {
				throw new RuntimeException("observer is broken");
			};
			RecordingObserver healthy = new RecordingObserver();
			IApi api = buildApi(throwing, healthy);
			IDomain<?> domain = api.getDomain("users").orElseThrow();

			IOperationResponse resp = domain.invoke(readAllRequest(domain));

			assertEquals(OperationResponseCode.OK, resp.getResponseCode(),
					"a throwing workflow observer must not turn a successful operation into a 500");
			assertFalse(healthy.events.isEmpty(),
					"the healthy observer must still receive events despite the sibling throwing");
		}
	}

	@Nested
	@DisplayName("Zero-config baseline")
	class ZeroConfigBaseline {

		@Test
		@DisplayName("when no workflowObserver is registered, build still succeeds and operations run normally")
		void noObserverIsNoOp() throws ApiException {
			IApi api = buildApi();
			IDomain<?> domain = api.getDomain("users").orElseThrow();
			IOperationResponse resp = domain.invoke(readAllRequest(domain));
			assertEquals(OperationResponseCode.OK, resp.getResponseCode());
		}
	}

	@Nested
	@DisplayName("workflowTiming — per-stage / per-script markers via WorkflowTimingConfig")
	class WorkflowTiming {

		@Test
		@DisplayName("without timing, observers see engine events but no stage:/script: sources")
		void withoutTimingNoStageSources() throws ApiException {
			RecordingObserver observer = new RecordingObserver();
			IApi api = buildApi(observer);
			IDomain<?> domain = api.getDomain("users").orElseThrow();

			domain.invoke(readAllRequest(domain));

			assertFalse(observer.events.isEmpty(),
					"observer must still receive engine events (mapper / runtime / scriptcontext / …) when timing is off");
			boolean anyStage = observer.events.stream()
					.anyMatch(e -> e.source() != null && e.source().startsWith("stage:"));
			boolean anyScript = observer.events.stream()
					.anyMatch(e -> e.source() != null && e.source().startsWith("script:"));
			assertFalse(anyStage,
					"stage:<name> events must NOT appear when workflowTiming is disabled (default); got: "
							+ observer.events.stream().map(ObservableEvent::source).toList());
			assertFalse(anyScript,
					"script:<stage>.<name> events must NOT appear when workflowTiming is disabled (default)");
		}

		@Test
		@DisplayName("with timing enabled (stages + scripts), observers see stage:<name> AND script:<stage>.<name> events")
		void withTimingStageAndScriptSourcesFire() throws ApiException {
			RecordingObserver observer = new RecordingObserver();
			IApi api = buildApi(
					com.garganttua.core.workflow.WorkflowTimingConfig.of().stages(true).scripts(true),
					observer);
			IDomain<?> domain = api.getDomain("users").orElseThrow();

			// Sanity: the generated script must contain the observe() calls
			// the ScriptGenerator emits when timing is on.
			String script = domain.getWorkflow().getGeneratedScript();
			assertTrue(script != null && script.contains("observe(\"start\", \"stage:"),
					"timing-enabled script must contain observe(\"start\", \"stage:...\") markers");

			// Dump the first few stage: observe lines so the failure msg shows
			// the exact syntax in case dispatch breaks.
			String head = script.lines()
					.filter(l -> l.contains("observe(") && l.contains("stage:"))
					.limit(4)
					.reduce("", (a, b) -> a + b + " | ");

			domain.invoke(readAllRequest(domain));

			List<String> sources = observer.events.stream()
					.map(ObservableEvent::source)
					.filter(java.util.Objects::nonNull)
					.toList();
			boolean anyStage = sources.stream().anyMatch(s -> s.startsWith("stage:"));
			boolean anyScript = sources.stream().anyMatch(s -> s.startsWith("script:"));
			assertTrue(anyStage,
					"at least one stage:<name> event must fire when workflowTiming.stages(true) is set; "
							+ "sources seen: " + sources + "; observe() lines in script: " + head);
			assertTrue(anyScript,
					"at least one script:<stage>.<name> event must fire when workflowTiming.scripts(true) is set; sources seen: " + sources);
		}

		@Test
		@DisplayName("stage and script events share the same executionId as the surrounding engine events")
		void timingEventsShareExecutionId() throws ApiException {
			RecordingObserver observer = new RecordingObserver();
			IApi api = buildApi(
					com.garganttua.core.workflow.WorkflowTimingConfig.of().stages(true).scripts(true),
					observer);
			IDomain<?> domain = api.getDomain("users").orElseThrow();

			domain.invoke(readAllRequest(domain));

			java.util.UUID first = observer.events.get(0).executionId();
			for (ObservableEvent e : observer.events) {
				assertEquals(first, e.executionId(),
						"every event of a single Domain.invoke (engine AND stage/script) must share an executionId; "
								+ "saw " + e.executionId() + " for source " + e.source());
			}
		}
	}
}
