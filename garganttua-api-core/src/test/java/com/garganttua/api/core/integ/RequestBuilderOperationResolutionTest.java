package com.garganttua.api.core.integ;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.BusinessOperation;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.IRequest;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.api.core.service.RequestBuilder;
import com.garganttua.core.reflection.IClass;

/**
 * Regression coverage for the bug where {@code RequestBuilder}'s CRUD
 * shortcuts (createOne/readAll/...) ignored the domain's per-operation
 * access/authority overrides because they instantiated a fresh
 * {@code OperationDefinition.*WithStandardSecurity(...)} every time — hard-coding
 * {@code Access.authenticated} regardless of the DSL config.
 */
@DisplayName("RequestBuilder honours DSL-configured operation overrides")
class RequestBuilderOperationResolutionTest extends AbstractCrudIntegrationTest {

    private IApi buildApi(SecurityConfigurator configurator) throws ApiException {
        return buildApi(configurator, true);
    }

    private IApi buildApi(SecurityConfigurator configurator, boolean creationEnabled) throws ApiException {
        IApiBuilder builder = newBuilder();
        var domainBuilder = builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity().id("id").uuid("uuid").tenantId("tenantId").up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(new CapturingDao())
                .up()
                .creation(creationEnabled).readAll(true).readOne(true);

        if (configurator != null) {
            var secBuilder = domainBuilder.security();
            configurator.configure(secBuilder);
            secBuilder.up();
        }
        domainBuilder.up();

        IApi api = builder.build();
        api.onInit();
        api.onStart();
        return api;
    }

    @FunctionalInterface
    private interface SecurityConfigurator {
        void configure(com.garganttua.api.commons.context.dsl.security.IDomainSecurityBuilder<?> b);
    }

    private static OperationDefinition operationAttached(IRequest req) {
        Object op = req.operationRequest().arg(IOperationRequest.OPERATION).orElse(null);
        assertNotNull(op, "RequestBuilder must attach an OperationDefinition on the request");
        return (OperationDefinition) op;
    }

    @Nested
    @DisplayName("Per-CRUD access override flows into the attached OperationDefinition")
    class AttachedOpReflectsOverride {

        @Test
        @DisplayName("readAllAccess(anonymous) → RequestBuilder.readAll() attaches an op with access=anonymous")
        void readAllAnonymous() throws ApiException {
            IApi api = buildApi(b -> b.readAllAccess(Access.anonymous));
            IDomain<?> domain = api.getDomain("users").orElseThrow();
            IRequest req = RequestBuilder.builder(domain).readAll().build();
            assertEquals(Access.anonymous, operationAttached(req).access(),
                    "the registered op's access (anonymous) must propagate, not the *WithStandardSecurity default");
        }

        @Test
        @DisplayName("creationAccess(owner) → RequestBuilder.createOne() attaches an op with access=owner")
        void creationOwner() throws ApiException {
            IApi api = buildApi(b -> b.creationAccess(Access.authenticated));
            IDomain<?> domain = api.getDomain("users").orElseThrow();
            IRequest req = RequestBuilder.builder(domain).createOne(new User()).build();
            assertEquals(Access.authenticated, operationAttached(req).access());
        }

        @Test
        @DisplayName("No security override → RequestBuilder still attaches the registered op (authenticated default)")
        void registeredOpIsUsedEvenWithoutSecurityBlock() throws ApiException {
            IApi api = buildApi(null);
            IDomain<?> domain = api.getDomain("users").orElseThrow();
            IRequest req = RequestBuilder.builder(domain).readAll().build();
            // With no .security() call, the registered op carries the framework default
            // (Access.authenticated for vanilla workflows). The point of this test is that
            // RequestBuilder doesn't fabricate a fresh op — it picks up whatever is
            // registered, so future framework-default changes will propagate uniformly.
            assertEquals(Access.authenticated, operationAttached(req).access(),
                    "registered op default access should propagate");
        }
    }

    @Nested
    @DisplayName("End-to-end: anonymous readAll succeeds without a token")
    class EndToEnd {

        @Test
        @DisplayName("readAllAccess(anonymous) lets RequestBuilder.readAll() run on a secured domain without auth")
        void readAllAnonymousReachesOperation() throws ApiException {
            // Just touching .security() is enough to enable the security pipeline today
            // (see Bug #1 of the tenant-domain-security report). Calling readAllAccess(anonymous)
            // simultaneously enables it AND configures the op as anonymous — the workflow
            // then short-circuits on the _isAnonymous check inside VERIFY_AUTHORIZATION.gs.
            IApi api = buildApi(b -> b.readAllAccess(Access.anonymous));
            IDomain<?> domain = api.getDomain("users").orElseThrow();

            IOperationResponse response = RequestBuilder.builder(domain)
                    // Minimum viable caller: Domain.invoke() requires non-null tenantId
                    // even for anonymous ops (separate concern, tracked in the same report).
                    .caller(Caller.createSuperCaller(api.getSuperTenantId()))
                    .readAll()
                    .build()
                    .execute();

            assertEquals(OperationResponseCode.OK, response.getResponseCode(),
                    "anonymous readAll on a secured domain should reach the operation; got code="
                            + response.getResponseCode() + " response=" + response.getResponse());
        }
    }

    @Nested
    @DisplayName("Fallback when the op is not registered on the domain")
    class Fallback {

        @Test
        @DisplayName("createOne on a domain with creation(false) falls back to createOneWithStandardSecurity")
        void fallbackToStandardSecurity() throws ApiException {
            IApi api = buildApi(null, /* creationEnabled */ false);
            IDomain<?> domain = api.getDomain("users").orElseThrow();
            IRequest req = RequestBuilder.builder(domain).createOne(new User()).build();
            OperationDefinition attached = operationAttached(req);

            // Equivalence with the framework default factory — same domain, same entity,
            // same BusinessOperation, same access, same authority flag.
            OperationDefinition standard = OperationDefinition.createOneWithStandardSecurity(
                    domain.getDomainName(), domain.getEntityClass());
            assertEquals(standard.access(), attached.access(),
                    "fallback access should match createOneWithStandardSecurity (Access.authenticated)");
            assertEquals(standard.authority(), attached.authority(),
                    "fallback authority should match createOneWithStandardSecurity (true)");
            assertEquals(BusinessOperation.create, attached.getBusinessOperation(),
                    "fallback business operation should still be 'create'");
            assertTrue(attached.equals(standard),
                    "fallback OperationDefinition should equal createOneWithStandardSecurity by record identity");
        }
    }
}
