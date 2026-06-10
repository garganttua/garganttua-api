package com.garganttua.api.core.integ;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.api.core.service.OperationResponse;
import com.garganttua.api.core.service.RequestBuilder;
import com.garganttua.core.reflection.IClass;

/**
 * Pins the {@code processingTime} field on {@link IOperationResponse}:
 * {@code Domain.invoke()} stamps every response with the wall-clock
 * duration it took to traverse the workflow. Surfaces in REST adapters,
 * audit logs, and metrics — must always be set, never negative, and
 * reflect the actual work done.
 */
@DisplayName("OperationResponse processing time")
class OperationResponseProcessingTimeTest extends AbstractCrudIntegrationTest {

    private IApi buildUnsecuredDomain(StubDao dao) throws ApiException {
        // multiTenant(false) keeps the fixture minimal — no tenant domain required.
        IApiBuilder builder = newBaseBuilder().multiTenant(false);
        builder.domain(IClass.getClass(Product.class))
                .entity().id("id").uuid("uuid").up()
                .dto(IClass.getClass(ProductDto.class))
                    .id("id").uuid("uuid")
                    .db(dao)
                .up()
                .security().disable(true).up()
                .readAll(true)
            .up();
        IApi api = builder.build();
        api.onInit();
        api.onStart();
        return api;
    }

    @Nested
    @DisplayName("Default semantics")
    class Defaults {

        @Test
        @DisplayName("IOperationResponse.getProcessingTime() defaults to null when not implemented")
        void defaultIsNull() {
            IOperationResponse stub = new IOperationResponse() {
                @Override public OperationResponseCode getResponseCode() { return OperationResponseCode.OK; }
                @Override public Object getResponse() { return "x"; }
            };
            assertNull(stub.getProcessingTime(),
                    "Bare implementations that don't override getProcessingTime() must return null — "
                            + "stamping is the responsibility of the path that produces the response.");
        }

        @Test
        @DisplayName("OperationResponse static factories produce instances with null processing time (caller stamps it)")
        void staticFactoriesUnstamped() {
            assertNull(OperationResponse.ok("payload").getProcessingTime());
            assertNull(OperationResponse.notFound("nope").getProcessingTime());
            assertNull(OperationResponse.unauthorized("no").getProcessingTime());
        }

        @Test
        @DisplayName("withProcessingTime returns a NEW instance carrying the duration; the original is unchanged")
        void withProcessingTimeIsImmutable() {
            OperationResponse original = OperationResponse.ok("payload");
            Duration d = Duration.ofMillis(42);
            OperationResponse stamped = original.withProcessingTime(d);

            assertNull(original.getProcessingTime(), "original must stay unstamped");
            assertEquals(d, stamped.getProcessingTime(), "copy must carry the duration");
            assertEquals(original.getResponseCode(), stamped.getResponseCode());
            assertEquals(original.getResponse(), stamped.getResponse());
        }

        @Test
        @DisplayName("withProcessingTime(null) clears the timing")
        void withProcessingTimeNullClears() {
            OperationResponse stamped = OperationResponse.ok("x").withProcessingTime(Duration.ofMillis(10));
            assertNotNull(stamped.getProcessingTime());
            assertNull(stamped.withProcessingTime(null).getProcessingTime(),
                    "passing null must wipe the timing back to absent");
        }
    }

    @Nested
    @DisplayName("Stamping by Domain.invoke()")
    class StampedByDomainInvoke {

        @Test
        @DisplayName("a successful readAll carries a non-null processingTime that's > 0 and < 10s")
        void successfulCallIsTimed() throws ApiException {
            IApi api = buildUnsecuredDomain(new StubDao());
            IDomain<?> domain = api.getDomain("products").orElseThrow();

            IOperationResponse response = RequestBuilder.builder(domain)
                    .caller(Caller.createTenantCaller("acme"))
                    .readAll()
                    .build()
                    .execute();

            assertEquals(OperationResponseCode.OK, response.getResponseCode(),
                    "sanity: readAll must succeed; got " + response);
            Duration time = response.getProcessingTime();
            assertNotNull(time, "processingTime must be stamped by Domain.invoke()");
            assertFalse(time.isNegative(), "processingTime must never be negative; got " + time);
            assertFalse(time.isZero(), "processingTime must be > 0 — even a no-op readAll takes nonzero ns; got " + time);
            assertTrue(time.toMillis() < 10_000,
                    "a trivial in-memory readAll must take < 10s; got " + time.toMillis() + "ms");
        }

        @Test
        @DisplayName("a no-caller call (auto-anonymous) on an unsecured domain still gets a stamped processingTime")
        void anonymousCallIsAlsoTimed() throws ApiException {
            IApi api = buildUnsecuredDomain(new StubDao());
            IDomain<?> domain = api.getDomain("products").orElseThrow();

            // No .caller(...) on the builder. Since 2026-05-19, Domain.invoke
            // auto-creates an anonymous caller in that case (mon général's
            // request) — anonymous traffic on an unsecured domain passes
            // through fine. The point of this test is just that the response
            // is still stamped with a duration regardless of the path taken.
            IOperationResponse response = RequestBuilder.builder(domain)
                    .readAll()
                    .build()
                    .execute();

            // Sanity: the unsecured Product domain has no .security() so
            // VERIFY_AUTHORIZATION never runs and the anonymous caller flows
            // through to a clean OK.
            assertEquals(OperationResponseCode.OK, response.getResponseCode(),
                    "anonymous traffic on an unsecured domain must succeed; got " + response);
            Duration time = response.getProcessingTime();
            assertNotNull(time, "even fast paths must be stamped");
            assertFalse(time.isNegative());
        }

        @Test
        @DisplayName("processingTime in toString() shows millis (operator-friendly log line)")
        void toStringIncludesMillis() {
            OperationResponse stamped = OperationResponse.ok("x").withProcessingTime(Duration.ofMillis(123));
            String s = stamped.toString();
            assertTrue(s.contains("processingTime=123ms"),
                    "toString should expose processingTime in ms; got: " + s);
        }

        @Test
        @DisplayName("processingTime is omitted from toString() when null (no noise on legacy paths)")
        void toStringOmitsWhenUnset() {
            String s = OperationResponse.ok("x").toString();
            assertFalse(s.contains("processingTime"),
                    "unstamped responses must NOT mention processingTime in toString; got: " + s);
        }
    }
}
