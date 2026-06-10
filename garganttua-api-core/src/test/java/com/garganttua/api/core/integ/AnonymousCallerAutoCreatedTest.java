package com.garganttua.api.core.integ;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.api.core.service.RequestBuilder;
import com.garganttua.core.reflection.IClass;

/**
 * Pins mon général's 2026-05-19 directive: when a request arrives with no
 * caller information, {@code Domain.invoke} auto-creates an anonymous
 * caller and lets the security pipeline decide whether the operation
 * accepts anonymous traffic. No more "No caller provided" 400 for the
 * "user just didn't set .caller(...)" case.
 */
@DisplayName("Anonymous caller is auto-created when none is provided")
class AnonymousCallerAutoCreatedTest extends AbstractCrudIntegrationTest {

    private IApi buildUnsecuredDomain(StubDao dao) throws ApiException {
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

    private IApi buildSecuredDomain(StubDao dao, Access readAllAccess) throws ApiException {
        IApiBuilder builder = newBuilder();  // multi-tenant + superTenantId="SUPER_TENANT"
        builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity().id("id").uuid("uuid").tenantId("tenantId").up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(dao)
                .up()
                .security()
                    .readAllAccess(readAllAccess)
                    .up()
            .up();
        IApi api = builder.build();
        api.onInit();
        api.onStart();
        return api;
    }

    @Nested
    @DisplayName("Caller.createAnonymousCaller() factory")
    class Factory {

        @Test
        @DisplayName("every field is null and both super flags are false")
        void shapeIsTotallyNull() {
            ICaller anon = Caller.createAnonymousCaller();
            assertEquals(null, anon.tenantId(), "tenantId must be null on anonymous caller");
            assertEquals(null, anon.requestedTenantId(), "requestedTenantId must be null");
            assertEquals(null, anon.callerId(), "callerId must be null");
            assertEquals(null, anon.ownerId(), "ownerId must be null");
            assertFalse(anon.superTenant(), "anonymous caller must NOT carry the super-tenant flag");
            assertFalse(anon.superOwner(), "anonymous caller must NOT carry the super-owner flag");
            assertEquals(null, anon.authorities(), "authorities must be null");
        }
    }

    @Nested
    @DisplayName("Domain.invoke materializes the anonymous caller")
    class AutoMaterialization {

        @Test
        @DisplayName("no caller + unsecured domain -> OK (anonymous flows through the no-security-pipeline path)")
        void unsecuredDomainAcceptsAnonymous() throws ApiException {
            IApi api = buildUnsecuredDomain(new StubDao());
            IDomain<?> domain = api.getDomain("products").orElseThrow();

            IOperationResponse response = RequestBuilder.builder(domain)
                    .readAll()
                    .build()
                    .execute();

            // Pre-fix this used to return CLIENT_ERROR "No caller provided".
            // The auto-anonymous swap lets it through cleanly now.
            assertEquals(OperationResponseCode.OK, response.getResponseCode(),
                    "no caller on unsecured domain must auto-create anonymous and succeed; got " + response);
            assertEquals(java.util.Optional.empty(), response.getException(),
                    "success path must not expose an exception");
        }

        @Test
        @DisplayName("the 'No caller provided' message is gone from the rejection path")
        void noCallerProvidedMessageIsGone() throws ApiException {
            // Whatever happens downstream on a secured tenant domain (the
            // pipeline currently rejects anonymous traffic at the
            // business-rules / tenant-rules stage with 400 — see the
            // garganttua-core variable-naming gap), the response MUST NOT
            // carry the legacy "No caller provided" wording. That string
            // was the marker mon général asked us to retire.
            IApi api = buildSecuredDomain(new StubDao(), Access.authenticated);
            IDomain<?> domain = api.getDomain("users").orElseThrow();

            IOperationResponse response = RequestBuilder.builder(domain)
                    .readAll()
                    .build()
                    .execute();

            Throwable cause = response.getException().orElseThrow(
                    () -> new AssertionError("failure path must carry an exception; got: " + response));
            assertFalse(cause.getMessage().contains("No caller provided"),
                    "the legacy 'No caller provided' wording must be gone; got: " + cause.getMessage());
        }

        @Test
        @DisplayName("an explicit Caller.createAnonymousCaller() takes the SAME branch as 'no caller'")
        void explicitAnonymousIsEquivalent() throws ApiException {
            // Equivalence test: caller=null and caller=anonymous() must
            // produce the same response. Doesn't depend on whether the
            // downstream pipeline accepts the request — it just pins that
            // the empty-caller detection treats both as anonymous.
            IApi api = buildSecuredDomain(new StubDao(), Access.authenticated);
            IDomain<?> domain = api.getDomain("users").orElseThrow();

            IOperationResponse implicit = RequestBuilder.builder(domain)
                    .readAll()
                    .build()
                    .execute();
            IOperationResponse explicit = RequestBuilder.builder(domain)
                    .caller(Caller.createAnonymousCaller())
                    .readAll()
                    .build()
                    .execute();

            assertEquals(implicit.getResponseCode(), explicit.getResponseCode(),
                    "implicit no-caller and explicit anonymous caller must produce the same code; "
                            + "got implicit=" + implicit + " explicit=" + explicit);
        }
    }

    // isEmptyCaller(...) detection is package-private on Domain; the
    // exhaustive unit tests for it live in DomainErrorMessageTest in
    // com.garganttua.api.core.context (same package as Domain).
}
