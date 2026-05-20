package com.garganttua.api.core.integ.observability.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.observability.IApiObserver;
import com.garganttua.api.commons.observability.OperationEvent;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.core.reflection.IClass;

/**
 * Reference example showing how to wire {@link IApiObserver} to a
 * Micrometer-style metrics backend. This test deliberately does NOT
 * depend on Micrometer — instead it inlines a tiny "registry" of the
 * same shape (Counter, Timer) so the example compiles in test scope
 * with zero external dependency, while documenting the integration
 * pattern for users who do bring Micrometer.
 *
 * <p>Translating to a real Micrometer setup is a one-line swap:
 * replace {@link FakeMeterRegistry} with
 * {@code io.micrometer.core.instrument.MeterRegistry}, and call
 * {@code registry.counter("name", tags).increment()} /
 * {@code registry.timer("name", tags).record(duration)} from the same
 * spots. The observer code is unchanged.
 */
@DisplayName("Example: how to expose Phase 1 observability via a Micrometer-style registry")
class MicrometerStyleObserverExampleTest extends AbstractCrudIntegrationTest {

    // ─── Fake Micrometer-style registry (test-scope only) ──────────────
    //
    // Mirrors the Counter / Timer / tags interface so the user-facing
    // observer code matches what a real Micrometer wiring would look
    // like, line for line.

    static class FakeMeterRegistry {
        private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();
        private final Map<String, FakeTimer> timers = new ConcurrentHashMap<>();

        AtomicLong counter(String name, String... tags) {
            return counters.computeIfAbsent(metricKey(name, tags), k -> new AtomicLong());
        }

        FakeTimer timer(String name, String... tags) {
            return timers.computeIfAbsent(metricKey(name, tags), k -> new FakeTimer());
        }

        Map<String, AtomicLong> counters() { return counters; }
        Map<String, FakeTimer> timers() { return timers; }

        private static String metricKey(String name, String... tags) {
            StringBuilder sb = new StringBuilder(name);
            for (int i = 0; i + 1 < tags.length; i += 2) {
                sb.append('{').append(tags[i]).append('=').append(tags[i + 1]).append('}');
            }
            return sb.toString();
        }
    }

    static class FakeTimer {
        private final AtomicLong count = new AtomicLong();
        private final AtomicLong totalNanos = new AtomicLong();

        void record(Duration duration) {
            count.incrementAndGet();
            totalNanos.addAndGet(duration.toNanos());
        }

        long count() { return count.get(); }
        Duration totalTime() { return Duration.ofNanos(totalNanos.get()); }
        Duration mean() {
            long c = count.get();
            return c == 0 ? Duration.ZERO : Duration.ofNanos(totalNanos.get() / c);
        }
    }

    // ─── The actual observer — the only piece the user writes ──────────
    //
    // This is the "Micrometer adapter" pattern. Replace FakeMeterRegistry
    // with io.micrometer.core.instrument.MeterRegistry and the same code
    // works against Prometheus / Datadog / Grafana Cloud.

    static class MicrometerStyleObserver implements IApiObserver {
        private final FakeMeterRegistry registry;
        private static final String METRIC_DURATION = "garganttua.api.operation.duration";
        private static final String METRIC_COUNT    = "garganttua.api.operation.count";

        MicrometerStyleObserver(FakeMeterRegistry registry) {
            this.registry = registry;
        }

        @Override
        public void onOperationEnd(OperationEvent e) {
            if (e.operation() == null) return;
            String op = e.operation().toString();
            String outcome = e.isSuccess() ? "success" : "failure";

            // A Counter — operations per { op, outcome }.
            registry.counter(METRIC_COUNT, "operation", op, "outcome", outcome).incrementAndGet();

            // A Timer — distribution of durations per { op, outcome }.
            // In real Micrometer the timer also produces histograms, p99,
            // etc., out of the box; here we only track total + count.
            if (e.duration() != null) {
                registry.timer(METRIC_DURATION, "operation", op, "outcome", outcome)
                        .record(e.duration());
            }
        }
    }

    // ─── The test that proves the pattern compiles and runs ────────────

    @Test
    @DisplayName("a Micrometer-style observer correctly receives count + duration per operation")
    void wireUpAndObserveTraffic() throws ApiException {
        FakeMeterRegistry registry = new FakeMeterRegistry();
        MicrometerStyleObserver observer = new MicrometerStyleObserver(registry);

        IApiBuilder builder = newBuilder();
        builder.observer(observer);
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
        IApi api = buildAndStart(builder);
        IDomain<?> users = api.getDomain("users").orElseThrow();

        // Fire three successful reads.
        for (int i = 0; i < 3; i++) {
            users.invoke(superTenantRequest());
        }

        // The Counter for { operation=users-read-all-users, outcome=success }
        // must report 3, and the Timer must have recorded 3 samples.
        String operationKey = OperationDefinition
                .readAllWithStandardSecurity("users", IClass.getClass(User.class))
                .toString();

        AtomicLong counter = registry.counter(
                "garganttua.api.operation.count",
                "operation", operationKey, "outcome", "success");
        assertEquals(3L, counter.get(),
                "the counter must have ticked three times — registry keys: "
                        + registry.counters().keySet());

        FakeTimer timer = registry.timer(
                "garganttua.api.operation.duration",
                "operation", operationKey, "outcome", "success");
        assertEquals(3L, timer.count(),
                "the timer must have recorded three samples");
        assertNotNull(timer.mean());
        assertTrue(timer.mean().toNanos() > 0,
                "mean duration must be strictly positive — got: " + timer.mean());
        assertTrue(timer.totalTime().toNanos() >= timer.mean().toNanos(),
                "total >= mean for n >= 1");
    }

    private OperationRequest superTenantRequest() {
        OperationDefinition op = OperationDefinition.readAllWithStandardSecurity(
                "users", IClass.getClass(User.class));
        OperationRequest req = new OperationRequest(new java.util.HashMap<>());
        req.arg(IOperationRequest.OPERATION, op);
        req.arg(IOperationRequest.TENANT_ID, "SUPER_TENANT");
        req.arg(IOperationRequest.REQUESTED_TENANT_ID, "SUPER_TENANT");
        req.arg(IOperationRequest.SUPER_TENANT, true);
        req.arg(IOperationRequest.SUPER_OWNER, true);
        return req;
    }
}
