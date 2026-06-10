package com.garganttua.api.core.integ;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import com.garganttua.api.core.service.RequestBuilder;
import com.garganttua.core.reflection.IClass;

/**
 * End-to-end pin for the parlant error messages produced by
 * {@code Domain.invoke()}. Before this fix the response carried
 * {@code "Workflow execution failed"} whenever no script-level message
 * reached the workflow result — mon général's regression report
 * (2026-05-19). Now the framework always produces a context-aware
 * message naming the operation and the domain, even on rejection paths.
 */
@DisplayName("Domain.invoke — error response messages are parlant")
class DomainInvokeErrorResponseTest extends AbstractCrudIntegrationTest {

    private IApi buildUnsecuredProducts(StubDao dao) throws ApiException {
        IApiBuilder builder = newBaseBuilder().multiTenant(false);
        builder.domain(IClass.getClass(Product.class))
                .entity().id("id").uuid("uuid").up()
                .dto(IClass.getClass(ProductDto.class))
                    .id("id").uuid("uuid")
                    .db(dao)
                .up()
                .security().disable(true).up()
                .readAll(true).readOne(true).creation(true)
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
                .readAll(true).readOne(true).creation(true)
            .up();
        IApi api = builder.build();
        api.onInit();
        api.onStart();
        return api;
    }

    private static String responseString(IOperationResponse response) {
        Object body = response.getResponse();
        return body == null ? "" : body.toString();
    }

    private static void assertNotGeneric(IOperationResponse response) {
        String body = responseString(response);
        assertFalse(body.equalsIgnoreCase("Workflow execution failed"),
                "response must not fall back to the old generic 'Workflow execution failed'; got: " + body);
        assertNotNull(body, "response body must not be null on a failed call");
        assertFalse(body.isBlank(), "response body must not be blank on a failed call");
    }

    @Nested
    @DisplayName("Direct rejection paths")
    class DirectRejection {

        @Test
        @DisplayName("no caller -> auto-anonymous, request reaches the operation on unsecured domain")
        void noCallerAutoAnonymous() throws ApiException {
            // Since 2026-05-19, missing caller materializes an anonymous one
            // instead of being rejected with 400. The unsecured Product
            // domain accepts the anonymous traffic.
            IApi api = buildUnsecuredProducts(new StubDao());
            IDomain<?> domain = api.getDomain("products").orElseThrow();

            IOperationResponse response = RequestBuilder.builder(domain)
                    .readAll()
                    .build()
                    .execute();

            assertEquals(OperationResponseCode.OK, response.getResponseCode(),
                    "anonymous traffic on an unsecured domain must succeed; got " + response);
        }

        @Test
        @DisplayName("multi-tenant: malformed super caller (null tenantId) -> 400 carrying a parlant 'missing tenantId' Throwable")
        @SuppressWarnings("deprecation")
        void malformedSuperCallerIsRejected() throws ApiException {
            // The tenantId-binding guard is multi-tenant only — in non-tenant mode a
            // tenantless caller is legitimate (see NonTenantOwnerCallerTest).
            IApi api = buildMultiTenantProducts(new StubDao());
            IDomain<?> domain = api.getDomain("products").orElseThrow();

            IOperationResponse response = RequestBuilder.builder(domain)
                    .caller(com.garganttua.api.core.caller.Caller.createSuperCaller())
                    .readAll()
                    .build()
                    .execute();

            assertEquals(OperationResponseCode.CLIENT_ERROR, response.getResponseCode());
            Throwable cause = response.getException().orElseThrow(
                    () -> new AssertionError("response must carry an exception on failure; got: " + response));
            assertTrue(cause.getMessage().contains("missing tenantId"),
                    "rejection message must name the actual problem; got: " + cause.getMessage());
            assertNotGeneric(response);
        }
    }

    @Nested
    @DisplayName("Workflow-level failures")
    class WorkflowFailures {

        @Test
        @DisplayName("readOne on a missing uuid -> 404 with a parlant body (not 'Workflow execution failed')")
        void readOneNotFound() throws ApiException {
            IApi api = buildUnsecuredProducts(new StubDao());
            IDomain<?> domain = api.getDomain("products").orElseThrow();

            IOperationResponse response = RequestBuilder.builder(domain)
                    .caller(Caller.createTenantCaller("acme"))
                    .readOne("does-not-exist")
                    .build()
                    .execute();

            assertEquals(OperationResponseCode.NOT_FOUND, response.getResponseCode(),
                    "readOne on a missing uuid must yield NOT_FOUND; got " + response);
            assertNotGeneric(response);
        }
    }

    // The fallback helper (Domain.defaultMessageForCode) is pinned exhaustively
    // by DomainErrorMessageTest in com.garganttua.api.core.context — it stays
    // package-private to keep the surface tight.
}
