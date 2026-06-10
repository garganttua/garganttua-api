package com.garganttua.api.core.integ;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
 * Pins the contract change requested by mon général on 2026-05-19:
 * {@code OperationResponse} carries the actual {@link Throwable} on every
 * failure path, not a raw String. Callers that need just a message use
 * {@code getException().get().getMessage()}; callers that need the chain
 * (cause, stack) keep full access through {@link IOperationResponse#getException()}.
 */
@DisplayName("OperationResponse failure path carries a Throwable, not a String")
class OperationResponseFailureCarriesThrowableTest extends AbstractCrudIntegrationTest {

    private IApi buildUnsecuredProducts(StubDao dao) throws ApiException {
        IApiBuilder builder = newBaseBuilder().multiTenant(false);
        builder.domain(IClass.getClass(Product.class))
                .entity().id("id").uuid("uuid").up()
                .dto(IClass.getClass(ProductDto.class))
                    .id("id").uuid("uuid")
                    .db(dao)
                .up()
                .security().disable(true).up()
                .readAll(true).readOne(true)
            .up();
        IApi api = builder.build();
        api.onInit();
        api.onStart();
        return api;
    }

    /** Multi-tenant Products — the tenantId-binding guard for a malformed super caller is multi-tenant only. */
    private IApi buildMultiTenantProducts(StubDao dao) throws ApiException {
        IApiBuilder builder = newBuilder();
        builder.domain(IClass.getClass(Product.class))
                .tenant(true).superTenant("superTenant")
                .entity().id("id").uuid("uuid").tenantId("tenantId").up()
                .dto(IClass.getClass(ProductDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(dao)
                .up()
                .security().disable(true).up()
                .readAll(true).readOne(true)
            .up();
        IApi api = builder.build();
        api.onInit();
        api.onStart();
        return api;
    }

    @Nested
    @DisplayName("Static factories")
    class Factories {

        @Test
        @DisplayName("error(Throwable) carries the EXACT same Throwable instance")
        void errorWithThrowable() {
            IllegalStateException original = new IllegalStateException("boom");
            OperationResponse r = OperationResponse.error(original);

            assertEquals(OperationResponseCode.SERVER_ERROR, r.getResponseCode());
            assertSame(original, r.getResponse(),
                    "the response body MUST BE the original Throwable instance (identity), "
                            + "so callers can recover the full chain/stack");
            assertSame(original, r.getException().orElseThrow(),
                    "getException() must surface the same instance");
        }

        @Test
        @DisplayName("error(String) wraps the message into an ApiException so getException() is uniformly populated")
        void errorWithString() {
            OperationResponse r = OperationResponse.error("something went wrong");

            Throwable t = r.getException().orElseThrow(
                    () -> new AssertionError("String factory must wrap into a Throwable; got: " + r));
            assertTrue(t instanceof ApiException,
                    "String factory must wrap into ApiException for consistency; got: " + t.getClass());
            assertEquals("something went wrong", t.getMessage(),
                    "wrapped exception's message must be the original String");
        }

        @Test
        @DisplayName("badRequest, unauthorized, forbidden, notFound, notAvailable all carry a Throwable")
        void allFailureFactoriesCarryThrowable() {
            for (OperationResponse r : new OperationResponse[]{
                    OperationResponse.badRequest("a"),
                    OperationResponse.unauthorized("b"),
                    OperationResponse.forbidden("c"),
                    OperationResponse.notFound("d"),
                    OperationResponse.notAvailable("e"),
                    OperationResponse.error("f")
            }) {
                assertTrue(r.getResponse() instanceof Throwable,
                        "code " + r.getResponseCode() + " must carry a Throwable on the response; got: "
                                + r.getResponse());
                assertTrue(r.getException().isPresent(),
                        "getException() must be populated for code " + r.getResponseCode());
            }
        }

        @Test
        @DisplayName("ok(payload) keeps the payload on getResponse() and getException() is empty")
        void okPath() {
            String payload = "hello";
            OperationResponse r = OperationResponse.ok(payload);

            assertSame(payload, r.getResponse(), "success payload must be passed through as-is");
            assertEquals(java.util.Optional.empty(), r.getException(),
                    "success responses must NOT expose an exception");
        }
    }

    @Nested
    @DisplayName("End-to-end through Domain.invoke")
    class EndToEnd {

        @Test
        @DisplayName("no caller -> auto-anonymous caller; unsecured domain accepts the call (no exception in response)")
        void noCallerAutoAnonymous() throws ApiException {
            // Since 2026-05-19, Domain.invoke materializes an anonymous caller
            // when none is provided. On an unsecured domain VERIFY_AUTHORIZATION
            // is not in the pipeline at all, so the request reaches the
            // operation and succeeds.
            IApi api = buildUnsecuredProducts(new StubDao());
            IDomain<?> domain = api.getDomain("products").orElseThrow();

            IOperationResponse response = RequestBuilder.builder(domain)
                    .readAll()
                    .build()
                    .execute();

            assertEquals(OperationResponseCode.OK, response.getResponseCode(),
                    "no-caller readAll on an unsecured domain must succeed via the anonymous "
                            + "caller; got " + response);
            assertEquals(java.util.Optional.empty(), response.getException(),
                    "success responses must NOT expose an exception; got: " + response.getException());
        }

        @Test
        @DisplayName("multi-tenant: malformed super caller (null tenantId) is rejected with a parlant 'missing tenantId' exception")
        @SuppressWarnings("deprecation")
        void malformedSuperCallerStillRejected() throws ApiException {
            // Multi-tenant only: in non-tenant mode a tenantless caller is legitimate
            // (see NonTenantOwnerCallerTest).
            IApi api = buildMultiTenantProducts(new StubDao());
            IDomain<?> domain = api.getDomain("products").orElseThrow();

            IOperationResponse response = RequestBuilder.builder(domain)
                    .caller(Caller.createSuperCaller())  // null tenantId + superTenant=true
                    .readAll()
                    .build()
                    .execute();

            assertEquals(OperationResponseCode.CLIENT_ERROR, response.getResponseCode());
            Throwable ex = response.getException().orElseThrow(
                    () -> new AssertionError("response must expose an exception; got: " + response));
            assertTrue(ex instanceof ApiException,
                    "the wrapped exception must be an ApiException; got: " + ex.getClass());
            assertTrue(ex.getMessage().contains("missing tenantId"),
                    "message must name the missing tenantId; got: " + ex.getMessage());
        }

        @Test
        @DisplayName("readOne on a missing uuid -> NOT_FOUND with a Throwable whose message names the missing resource")
        void readOneMissing() throws ApiException {
            IApi api = buildUnsecuredProducts(new StubDao());
            IDomain<?> domain = api.getDomain("products").orElseThrow();

            IOperationResponse response = RequestBuilder.builder(domain)
                    .caller(Caller.createTenantCaller("acme"))
                    .readOne("not-a-real-uuid")
                    .build()
                    .execute();

            assertEquals(OperationResponseCode.NOT_FOUND, response.getResponseCode(),
                    "sanity: missing uuid should yield 404; got " + response);
            Throwable ex = response.getException().orElseThrow(
                    () -> new AssertionError("404 must carry an exception; got: " + response));
            String msg = ex.getMessage();
            assertNotNull(msg, "exception must carry a non-null message; got: " + ex);
            assertFalse(msg.isBlank(), "exception message must be non-blank; got: " + ex);
            assertFalse(msg.equalsIgnoreCase("Workflow execution failed"),
                    "must not fall back to the old generic; got: " + msg);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("failure responses render the exception class and message, not the raw object reference")
        void toStringRendersThrowable() {
            OperationResponse r = OperationResponse.notFound(new ApiException("entity gone"));

            String s = r.toString();
            assertTrue(s.contains("ApiException"),
                    "toString should include the exception's simple class name; got: " + s);
            assertTrue(s.contains("entity gone"),
                    "toString should include the exception's message; got: " + s);
            assertFalse(s.contains("@"),
                    "toString must NOT fall back to Object.toString() (which shows '@hashcode'); got: " + s);
        }

        @Test
        @DisplayName("success responses keep their original payload rendering")
        void toStringSuccessUnchanged() {
            String s = OperationResponse.ok("hello").toString();
            assertTrue(s.contains("hello"),
                    "success toString must still render the payload as-is; got: " + s);
        }
    }
}
