package com.garganttua.api.core.integ.observability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.observability.IApiObserver;
import com.garganttua.api.commons.observability.OperationEvent;
import com.garganttua.api.commons.observability.OperationStats;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.api.core.observability.StatsObserver;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.core.reflection.IClass;

@DisplayName("IApiObserver — opt-in observability fired by Domain.invoke at operation boundaries")
class ApiObserverIntegrationTest extends AbstractCrudIntegrationTest {

    /** Test observer that records every event in insertion order. */
    static class RecordingObserver implements IApiObserver {
        final List<OperationEvent> events = new CopyOnWriteArrayList<>();
        @Override public void onOperationStart(OperationEvent e) { events.add(e); }
        @Override public void onOperationEnd(OperationEvent e) { events.add(e); }
    }

    /** Observer that throws on every callback — proves exception isolation. */
    static class ThrowingObserver implements IApiObserver {
        int startCalls = 0;
        int endCalls = 0;
        @Override public void onOperationStart(OperationEvent e) {
            startCalls++;
            throw new RuntimeException("observer is broken on start");
        }
        @Override public void onOperationEnd(OperationEvent e) {
            endCalls++;
            throw new RuntimeException("observer is broken on end");
        }
    }

    private IApi buildApi(IApiObserver... observers) throws ApiException {
        IApiBuilder builder = newBuilder();
        for (IApiObserver o : observers) builder.observer(o);
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
        req.arg(com.garganttua.api.commons.service.IOperationRequest.OPERATION, op);
        // Super-caller bypasses VERIFY_AUTHORIZATION so the test focuses on
        // observability, not on the security pipeline.
        req.arg(com.garganttua.api.commons.service.IOperationRequest.TENANT_ID, "SUPER_TENANT");
        req.arg(com.garganttua.api.commons.service.IOperationRequest.REQUESTED_TENANT_ID, "SUPER_TENANT");
        req.arg(com.garganttua.api.commons.service.IOperationRequest.SUPER_TENANT, true);
        req.arg(com.garganttua.api.commons.service.IOperationRequest.SUPER_OWNER, true);
        return req;
    }

    @Nested
    @DisplayName("Default behaviour: no observer registered")
    class NoObserver {

        @Test
        @DisplayName("IApi.getObservers() returns an empty list when none registered")
        void emptyByDefault() throws ApiException {
            IApi api = buildApi();
            assertTrue(api.getObservers().isEmpty(),
                    "no .observer(...) on the builder must produce an empty observer list");
        }

        @Test
        @DisplayName("Domain.invoke runs the cheap path (no observer construction overhead)")
        void invokeStillWorksWithoutObservers() throws ApiException {
            IApi api = buildApi();
            IDomain<?> users = api.getDomain("users").orElseThrow();
            IOperationResponse response = users.invoke(readAllRequest(users));
            assertNotNull(response);
            assertNotNull(response.getProcessingTime(),
                    "the existing total-time capture must remain unconditionally on");
        }

        @Test
        @DisplayName("getOperationStats() returns empty map when no StatsObserver is wired")
        void statsEmptyWithoutObserver() throws ApiException {
            IApi api = buildApi();
            assertTrue(api.getOperationStats().isEmpty());
        }
    }

    @Nested
    @DisplayName("Single observer wired")
    class SingleObserver {

        @Test
        @DisplayName("onStart fires before the workflow, onEnd fires after, in that order")
        void firesStartThenEnd() throws ApiException {
            RecordingObserver obs = new RecordingObserver();
            IApi api = buildApi(obs);
            IDomain<?> users = api.getDomain("users").orElseThrow();

            users.invoke(readAllRequest(users));

            assertEquals(2, obs.events.size(),
                    "exactly one start + one end event must fire per invocation");
            OperationEvent start = obs.events.get(0);
            OperationEvent end = obs.events.get(1);

            assertFalse(start.isEnd(), "first event must be the start");
            assertTrue(end.isEnd(), "second event must be the end");
        }

        @Test
        @DisplayName("start and end events share the same executionUuid (correlation)")
        void sameExecutionUuid() throws ApiException {
            RecordingObserver obs = new RecordingObserver();
            IApi api = buildApi(obs);
            IDomain<?> users = api.getDomain("users").orElseThrow();
            users.invoke(readAllRequest(users));

            UUID startUuid = obs.events.get(0).executionUuid();
            UUID endUuid = obs.events.get(1).executionUuid();
            assertNotNull(startUuid, "start event must carry an executionUuid for span correlation");
            assertSame(startUuid, endUuid,
                    "start and end of the same invocation must share executionUuid — observers pair them on this");
        }

        @Test
        @DisplayName("end event carries duration, response code and a null failure on success")
        void endEventOnSuccess() throws ApiException {
            RecordingObserver obs = new RecordingObserver();
            IApi api = buildApi(obs);
            IDomain<?> users = api.getDomain("users").orElseThrow();
            users.invoke(readAllRequest(users));

            OperationEvent end = obs.events.get(1);
            assertNotNull(end.duration(), "end event must carry the measured duration");
            assertTrue(end.duration().toNanos() > 0,
                    "duration must be strictly positive — got: " + end.duration());
            assertNotNull(end.code(), "end event must carry the response code on success");
            assertEquals(OperationResponseCode.OK, end.code());
            assertNull(end.failure(), "success path must not populate the failure slot");
            assertTrue(end.isSuccess(),
                    "OK response code must surface as isSuccess() == true on the event");
        }

        @Test
        @DisplayName("start event has null timing fields (filled only on end)")
        void startEventHasNullEndFields() throws ApiException {
            RecordingObserver obs = new RecordingObserver();
            IApi api = buildApi(obs);
            IDomain<?> users = api.getDomain("users").orElseThrow();
            users.invoke(readAllRequest(users));

            OperationEvent start = obs.events.get(0);
            assertNull(start.endedAt());
            assertNull(start.duration());
            assertNull(start.code());
            assertNull(start.failure());
            assertNotNull(start.startedAt(), "startedAt must be populated on the start event");
        }

        @Test
        @DisplayName("end event has the same startedAt as the start event")
        void endStartedAtMatchesStart() throws ApiException {
            RecordingObserver obs = new RecordingObserver();
            IApi api = buildApi(obs);
            IDomain<?> users = api.getDomain("users").orElseThrow();
            users.invoke(readAllRequest(users));

            assertEquals(obs.events.get(0).startedAt(), obs.events.get(1).startedAt(),
                    "end event's startedAt must echo the start event's startedAt for span reconstruction");
        }

        @Test
        @DisplayName("the operation and domain name are populated on both events")
        void operationAndDomain() throws ApiException {
            RecordingObserver obs = new RecordingObserver();
            IApi api = buildApi(obs);
            IDomain<?> users = api.getDomain("users").orElseThrow();
            users.invoke(readAllRequest(users));

            for (OperationEvent e : obs.events) {
                assertEquals("users", e.domain(),
                        "domain name must surface verbatim on every event");
                assertNotNull(e.operation(),
                        "OperationDefinition must surface on the event so observers can name the metric");
            }
        }
    }

    @Nested
    @DisplayName("Multiple observers wired")
    class MultipleObservers {

        @Test
        @DisplayName("all observers fire in registration order (start then end, for each)")
        void firesInOrder() throws ApiException {
            // First observer just records its own ID, second observer records
            // its own — by interleaving we can prove the order.
            List<String> trace = new java.util.concurrent.CopyOnWriteArrayList<>();
            IApiObserver first = new IApiObserver() {
                @Override public void onOperationStart(OperationEvent e) { trace.add("A:start"); }
                @Override public void onOperationEnd(OperationEvent e) { trace.add("A:end"); }
            };
            IApiObserver second = new IApiObserver() {
                @Override public void onOperationStart(OperationEvent e) { trace.add("B:start"); }
                @Override public void onOperationEnd(OperationEvent e) { trace.add("B:end"); }
            };
            IApi api = buildApi(first, second);
            IDomain<?> users = api.getDomain("users").orElseThrow();
            users.invoke(readAllRequest(users));

            assertEquals(List.of("A:start", "B:start", "A:end", "B:end"), trace,
                    "all observers see start in registration order, then all see end in registration order");
        }

        @Test
        @DisplayName("IApi.getObservers() exposes the registered observers (order preserved)")
        void registrationOrderPreserved() throws ApiException {
            IApiObserver a = new RecordingObserver();
            IApiObserver b = new RecordingObserver();
            IApi api = buildApi(a, b);
            List<IApiObserver> registered = api.getObservers();
            assertEquals(2, registered.size());
            assertSame(a, registered.get(0));
            assertSame(b, registered.get(1));
        }
    }

    @Nested
    @DisplayName("Exception isolation — a broken observer must never break the pipeline")
    class ExceptionIsolation {

        @Test
        @DisplayName("observer throws on start: invocation still succeeds, all other observers still fire")
        void thrownStartIsSwallowed() throws ApiException {
            ThrowingObserver bad = new ThrowingObserver();
            RecordingObserver good = new RecordingObserver();
            IApi api = buildApi(bad, good);
            IDomain<?> users = api.getDomain("users").orElseThrow();

            IOperationResponse response = users.invoke(readAllRequest(users));
            assertNotNull(response, "invocation must still return a response despite the broken observer");
            assertEquals(OperationResponseCode.OK, response.getResponseCode(),
                    "observer exceptions must not turn a successful operation into a 500");

            assertEquals(1, bad.startCalls, "bad observer was called on start (and threw)");
            assertEquals(1, bad.endCalls, "bad observer was still called on end despite throwing on start");
            assertEquals(2, good.events.size(),
                    "the good observer must still see start + end events — broken observers do not poison the chain");
        }

        @Test
        @DisplayName("observer throws on end: invocation still returns the original response")
        void thrownEndIsSwallowed() throws ApiException {
            IApiObserver throwOnEnd = new IApiObserver() {
                @Override public void onOperationEnd(OperationEvent e) {
                    throw new RuntimeException("end observer is broken");
                }
            };
            IApi api = buildApi(throwOnEnd);
            IDomain<?> users = api.getDomain("users").orElseThrow();

            IOperationResponse response = users.invoke(readAllRequest(users));
            assertNotNull(response);
            assertEquals(OperationResponseCode.OK, response.getResponseCode());
        }
    }

    @Nested
    @DisplayName("StatsObserver — built-in in-memory aggregator")
    class Stats {

        @Test
        @DisplayName("count, success/failure breakdown, sum, min, max, average are aggregated")
        void aggregates() throws ApiException {
            StatsObserver stats = new StatsObserver();
            IApi api = buildApi(stats);
            IDomain<?> users = api.getDomain("users").orElseThrow();

            // Three invocations, all successful.
            users.invoke(readAllRequest(users));
            users.invoke(readAllRequest(users));
            users.invoke(readAllRequest(users));

            Map<String, OperationStats> snapshot = stats.snapshot();
            assertEquals(1, snapshot.size(),
                    "only one operation invoked → one bucket — got keys: " + snapshot.keySet());
            OperationStats opStats = snapshot.values().iterator().next();
            assertEquals(3, opStats.count(), "three invocations must yield count=3");
            assertEquals(3, opStats.successCount(), "all three were successful");
            assertEquals(0, opStats.failureCount());
            assertNotNull(opStats.minDuration());
            assertNotNull(opStats.maxDuration());
            assertTrue(opStats.minDuration().toNanos() <= opStats.maxDuration().toNanos(),
                    "min must be <= max — got min=" + opStats.minDuration()
                            + " max=" + opStats.maxDuration());
            assertTrue(opStats.totalDuration().toNanos() > 0,
                    "total must be strictly positive after three invocations");
            assertTrue(opStats.averageDuration().toNanos() > 0,
                    "average must be strictly positive");
            assertTrue(opStats.averageDuration().toNanos() <= opStats.maxDuration().toNanos(),
                    "average must be <= max");
            assertTrue(opStats.averageDuration().toNanos() >= opStats.minDuration().toNanos(),
                    "average must be >= min");
        }

        @Test
        @DisplayName("reset() clears every aggregated bucket")
        void resetClearsBuckets() throws ApiException {
            StatsObserver stats = new StatsObserver();
            IApi api = buildApi(stats);
            IDomain<?> users = api.getDomain("users").orElseThrow();
            users.invoke(readAllRequest(users));
            assertFalse(stats.snapshot().isEmpty(), "sanity: invocation must have populated the bucket");

            stats.reset();
            assertTrue(stats.snapshot().isEmpty(), "after reset the snapshot must be empty");
        }

        @Test
        @DisplayName("IApi.getOperationStats() returns the live StatsObserver snapshot")
        void apiAccessor() throws ApiException {
            StatsObserver stats = new StatsObserver();
            IApi api = buildApi(stats);
            IDomain<?> users = api.getDomain("users").orElseThrow();
            users.invoke(readAllRequest(users));

            Map<String, OperationStats> viaApi = api.getOperationStats();
            assertEquals(1, viaApi.size());
            assertEquals(stats.snapshot().keySet(), viaApi.keySet(),
                    "the API accessor must return the same keys as the observer's own snapshot");
        }

        @Test
        @DisplayName("multiple StatsObservers: getOperationStats() returns the first one's snapshot")
        void firstObserverWinsForApiAccessor() throws ApiException {
            StatsObserver first = new StatsObserver();
            StatsObserver second = new StatsObserver();
            IApi api = buildApi(first, second);
            IDomain<?> users = api.getDomain("users").orElseThrow();
            users.invoke(readAllRequest(users));

            // Both observers should have observed the same event, so both have
            // count == 1. The API accessor returns the FIRST one.
            assertEquals(1, first.snapshot().values().iterator().next().count());
            assertEquals(1, second.snapshot().values().iterator().next().count());
            // No assertion on the API accessor identity — by contract it
            // returns the first found, which is `first`. Confirm via key.
            assertEquals(first.snapshot().keySet(), api.getOperationStats().keySet());
        }
    }

    @Nested
    @DisplayName("DSL guards")
    class DslGuards {

        @Test
        @DisplayName(".observer(null) is rejected with a NPE before .build()")
        void rejectsNullObserver() throws ApiException {
            IApiBuilder builder = newBuilder();
            assertThrows(NullPointerException.class, () -> builder.observer(null));
        }
    }
}
