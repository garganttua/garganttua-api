package com.garganttua.api.core.integ.event;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.event.IEvent;
import com.garganttua.api.commons.event.IEventPublisher;
import com.garganttua.api.commons.operation.BusinessOperation;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.core.observability.IObserver;
import com.garganttua.core.observability.ObservableEvent;
import com.garganttua.core.reflection.IClass;

/**
 * Verifies that domain business events ({@code IEvent}) declared via
 * {@code .events(...)} are published through the observability stream: the
 * {@code IEventPublisher} is bridged onto the domain's observable registry and
 * receives a rich {@code IEvent} (operation, in/out, caller, tenant, outcome) at
 * each {@code invoke()} boundary — on success and on failure alike.
 */
@DisplayName("Event publishing through observability")
class EventPublishingIntegrationTest extends AbstractCrudIntegrationTest {

	/** Captures every published IEvent so the tests can assert on them. */
	static class RecordingPublisher implements IEventPublisher {
		final List<IEvent> events = new CopyOnWriteArrayList<>();

		@Override
		public void publishEvent(IEvent event) {
			this.events.add(event);
		}

		IEvent last() {
			return this.events.isEmpty() ? null : this.events.get(this.events.size() - 1);
		}
	}

	private IApi context;
	private IDomain<?> userCtx;
	private CapturingDao userDao;
	private RecordingPublisher publisher;

	@BeforeEach
	void setUp() throws ApiException {
		userDao = new CapturingDao();
		publisher = new RecordingPublisher();

		IApiBuilder builder = newBuilder();
		builder.domain(IClass.getClass(User.class))
				.tenant(true)
				.superTenant("superTenant")
				.events(publisher)
				.entity()
					.id("id").uuid("uuid").tenantId("tenantId")
					.mandatory("name")
				.up()
				.dto(IClass.getClass(UserDto.class))
					.id("id").uuid("uuid").tenantId("tenantId")
					.db(userDao)
				.up()
				.security().disable(true).up()
			.up();

		context = buildAndStart(builder);
		userCtx = context.getDomain("users").orElseThrow();
	}

	private OperationRequest superTenantCreate(User entity) {
		OperationRequest req = new OperationRequest(new HashMap<>());
		req.arg(IOperationRequest.OPERATION,
				OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class)));
		req.arg(IOperationRequest.TENANT_ID, "SUPER_TENANT");
		req.arg(IOperationRequest.REQUESTED_TENANT_ID, "SUPER_TENANT");
		req.arg(IOperationRequest.SUPER_TENANT, true);
		req.arg(IOperationRequest.SUPER_OWNER, true);
		req.arg("entity", entity);
		return req;
	}

	@Nested
	@DisplayName("Successful operation")
	class Success {

		@Test
		@DisplayName("a created entity publishes a rich IEvent (operation, in, out, code, tenant)")
		void createPublishesRichEvent() {
			User user = new User();
			user.setName("Alice");
			user.setEmail("alice@example.com");

			IOperationResponse resp = userCtx.invoke(superTenantCreate(user));
			assertEquals(OperationResponseCode.CREATED, resp.getResponseCode(),
					() -> "create should succeed; got " + resp.getResponse());

			assertEquals(1, publisher.events.size(), "exactly one event must be published");
			IEvent ev = publisher.last();

			assertNotNull(ev.getOperation(), "the event must carry the operation");
			assertEquals(BusinessOperation.create, ev.getOperation().getBusinessOperation());
			assertSame(user, ev.getIn(), "the event 'in' must be the operation input entity");

			assertInstanceOf(User.class, ev.getOut(), "the event 'out' must be the produced entity");
			assertEquals("Alice", ((User) ev.getOut()).getName());

			assertEquals(OperationResponseCode.CREATED, ev.getCode(), "the event must carry the outcome code");
			assertEquals("SUPER_TENANT", ev.getTenantId(), "the event must carry the caller's tenant");
			assertNull(ev.getExceptionMessage(), "a success carries no exception message");

			assertNotNull(ev.getInDate());
			assertNotNull(ev.getOutDate());
			assertFalse(ev.getOutDate().before(ev.getInDate()), "outDate must not precede inDate");
		}

		@Test
		@DisplayName("each invocation publishes its own event, in order")
		void multipleInvocationsPublishInOrder() {
			User a = new User(); a.setName("A"); a.setEmail("a@x.io");
			User b = new User(); b.setName("B"); b.setEmail("b@x.io");

			userCtx.invoke(superTenantCreate(a));
			userCtx.invoke(superTenantCreate(b));

			assertEquals(2, publisher.events.size());
			assertSame(a, publisher.events.get(0).getIn());
			assertSame(b, publisher.events.get(1).getIn());
		}
	}

	@Nested
	@DisplayName("Failed operation")
	class Failure {

		@Test
		@DisplayName("a rejected operation still publishes an IEvent carrying the error code")
		void failurePublishesEvent() {
			User invalid = new User();          // missing mandatory "name"
			invalid.setEmail("noname@x.io");

			IOperationResponse resp = userCtx.invoke(superTenantCreate(invalid));
			assertNotEquals(OperationResponseCode.OK, resp.getResponseCode(),
					"the operation must be rejected");

			assertEquals(1, publisher.events.size(), "failures are published too");
			IEvent ev = publisher.last();
			assertNotEquals(OperationResponseCode.OK, ev.getCode(),
					"the event must carry the failure outcome code");
			assertNotEquals(0, ev.getExceptionCode(), "a failure event must carry a non-zero exception code");
		}
	}

	@Nested
	@DisplayName("Transport: through observability")
	class ThroughObservability {

		@Test
		@DisplayName("the published IEvent is the very payload of the observability End event")
		void eventRidesObservabilityPayload() {
			List<Object> payloads = new ArrayList<>();
			IObserver<ObservableEvent> spy = event -> {
				if (event.payload() != null) {
					payloads.add(event.payload());
				}
			};
			userCtx.addObserver(spy);

			User user = new User();
			user.setName("Carol");
			user.setEmail("carol@x.io");
			userCtx.invoke(superTenantCreate(user));

			// The spy saw an IEvent-bearing observable event...
			List<Object> events = payloads.stream().filter(p -> p instanceof IEvent).toList();
			assertEquals(1, events.size(), "exactly one IEvent must ride the observability stream");
			// ...and it is the same instance the publisher received — proving the
			// publisher is fed off the observability payload, not a parallel path.
			assertSame(events.get(0), publisher.last(),
					"the publisher's event must be the very payload carried by observability");
		}
	}
}
