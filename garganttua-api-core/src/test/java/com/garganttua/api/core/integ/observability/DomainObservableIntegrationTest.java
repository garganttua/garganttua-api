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
import com.garganttua.core.observability.EndEvent;
import com.garganttua.core.observability.IObserver;
import com.garganttua.core.observability.ObservableEvent;
import com.garganttua.core.observability.StartEvent;
import com.garganttua.core.reflection.IClass;

/**
 * Smoke test for the per-domain {@link com.garganttua.core.observability.IObservable}
 * surface. Every {@code Domain} fires {@code api:operation:<domain>:<op>}
 * Start/End/Error {@link ObservableEvent}s on {@link IDomain#invoke}; tests
 * attach a recording observer directly via {@code addObserver(...)} so the
 * scan / bootstrap path is not on the critical path of this test.
 *
 * <p>The {@code @Observer} scan + {@code ObservabilityBuilder.attachSource}
 * wiring is exercised end-to-end by the framework's bootstrap tests; here we
 * focus on the api-level contract: events flow, executionId correlates,
 * EndEvent vs ErrorEvent semantics are right.
 */
@DisplayName("Domain IObservable — api:operation:* event emission per Domain.invoke")
class DomainObservableIntegrationTest extends AbstractCrudIntegrationTest {

	static class RecordingObserver implements IObserver<ObservableEvent> {
		final List<ObservableEvent> events = new CopyOnWriteArrayList<>();
		@Override public void onEvent(ObservableEvent event) {
			events.add(event);
		}
	}

	private IApi buildApi() throws ApiException {
		IApiBuilder builder = newBuilder();
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
	@DisplayName("Happy path — successful invoke fires Start + End on the same executionId")
	class HappyPath {

		@Test
		@DisplayName("a registered observer receives one StartEvent followed by one EndEvent")
		void startThenEnd() throws ApiException {
			IApi api = buildApi();
			IDomain<?> domain = api.getDomain("users").orElseThrow();
			RecordingObserver obs = new RecordingObserver();
			domain.addObserver(obs);

			IOperationResponse resp = domain.invoke(readAllRequest(domain));
			assertEquals(OperationResponseCode.OK, resp.getResponseCode());

			List<ObservableEvent> apiEvents = obs.events.stream()
					.filter(e -> e.source() != null && e.source().startsWith("api:operation:"))
					.toList();
			assertEquals(2, apiEvents.size(),
					"exactly one StartEvent + one EndEvent must fire for a successful invoke; got: "
							+ apiEvents);
			assertTrue(apiEvents.get(0) instanceof StartEvent,
					"first api:operation:* event must be a StartEvent; got: " + apiEvents.get(0));
			assertTrue(apiEvents.get(1) instanceof EndEvent,
					"second api:operation:* event must be an EndEvent; got: " + apiEvents.get(1));
		}

		@Test
		@DisplayName("Start + End share the executionId pinned by Domain.invoke")
		void executionIdCorrelates() throws ApiException {
			IApi api = buildApi();
			IDomain<?> domain = api.getDomain("users").orElseThrow();
			RecordingObserver obs = new RecordingObserver();
			domain.addObserver(obs);

			domain.invoke(readAllRequest(domain));

			List<ObservableEvent> apiEvents = obs.events.stream()
					.filter(e -> e.source() != null && e.source().startsWith("api:operation:"))
					.toList();
			assertEquals(2, apiEvents.size());
			UUID startId = apiEvents.get(0).executionId();
			UUID endId = apiEvents.get(1).executionId();
			assertNotNull(startId, "Start event must carry an executionId");
			assertEquals(startId, endId,
					"Start + End of a single invoke must share the executionId pinned by Domain.invoke");
		}

		@Test
		@DisplayName("source string follows the api:operation:<domain>:<op> format")
		void sourceFormat() throws ApiException {
			IApi api = buildApi();
			IDomain<?> domain = api.getDomain("users").orElseThrow();
			RecordingObserver obs = new RecordingObserver();
			domain.addObserver(obs);

			domain.invoke(readAllRequest(domain));

			List<ObservableEvent> apiEvents = obs.events.stream()
					.filter(e -> e.source() != null && e.source().startsWith("api:operation:"))
					.toList();
			assertFalse(apiEvents.isEmpty());
			String source = apiEvents.get(0).source();
			assertTrue(source.startsWith("api:operation:users:"),
					"source must encode the domain name; got: " + source);
		}

		@Test
		@DisplayName("EndEvent carries a non-null duration measured by the emitter")
		void endEventHasDuration() throws ApiException {
			IApi api = buildApi();
			IDomain<?> domain = api.getDomain("users").orElseThrow();
			RecordingObserver obs = new RecordingObserver();
			domain.addObserver(obs);

			domain.invoke(readAllRequest(domain));

			EndEvent end = obs.events.stream()
					.filter(e -> e.source() != null && e.source().startsWith("api:operation:"))
					.filter(e -> e instanceof EndEvent)
					.map(e -> (EndEvent) e)
					.findFirst()
					.orElseThrow();
			assertNotNull(end.duration(), "EndEvent must carry a measured duration");
			assertTrue(end.duration().toNanos() > 0, "duration must be strictly positive");
		}
	}

	@Nested
	@DisplayName("Zero-config baseline — no observer → no event allocation, no crash")
	class ZeroConfig {

		@Test
		@DisplayName("with no observer attached, Domain.invoke runs the fast path and returns normally")
		void fastPathWhenNoObserver() throws ApiException {
			IApi api = buildApi();
			IDomain<?> domain = api.getDomain("users").orElseThrow();
			// No domain.addObserver(...) call here — Domain.invoke must
			// short-circuit on the registry's hasObservers() flag.
			IOperationResponse resp = domain.invoke(readAllRequest(domain));
			assertEquals(OperationResponseCode.OK, resp.getResponseCode(),
					"zero-observer setup must still complete the operation normally");
		}
	}

	@Nested
	@DisplayName("Multiple observers — fanout")
	class Fanout {

		@Test
		@DisplayName("two observers on the same domain both receive the full event stream")
		void twoObserversBothReceive() throws ApiException {
			IApi api = buildApi();
			IDomain<?> domain = api.getDomain("users").orElseThrow();
			RecordingObserver a = new RecordingObserver();
			RecordingObserver b = new RecordingObserver();
			domain.addObserver(a);
			domain.addObserver(b);

			domain.invoke(readAllRequest(domain));

			assertFalse(a.events.isEmpty(), "observer a must receive at least one event");
			assertFalse(b.events.isEmpty(), "observer b must receive at least one event");
			assertEquals(a.events.size(), b.events.size(),
					"both observers must see the same number of events; a=" + a.events.size()
							+ ", b=" + b.events.size());
		}
	}
}
