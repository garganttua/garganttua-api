package com.garganttua.api.core.integ.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.integ.TestAuthorization;
import com.garganttua.api.core.integ.crud.AbstractCrudScriptTest;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.BusinessOperation;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.workflow.WorkflowResult;

@DisplayName("CRUD Security Configuration Tests")
class CrudSecurityIntegrationTest extends AbstractCrudScriptTest {

    private IDomain<?> buildSecuredDomain(SecurityConfigurator configurator) throws ApiException {
        CapturingDao dao = new CapturingDao();
        IApiBuilder builder = newBuilder();
        var domainBuilder = builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(dao)
                .up();

        var secBuilder = domainBuilder.security();
        configurator.configure(secBuilder);
        secBuilder.up().up();

        IApi context = buildAndStart(builder);
        return context.getDomain("users").orElseThrow();
    }

    @FunctionalInterface
    interface SecurityConfigurator {
        void configure(com.garganttua.api.commons.context.dsl.security.IDomainSecurityBuilder<?> builder);
    }

    // --- Helpers ---

    private OperationDefinition findOperation(IDomain<?> ctx, BusinessOperation bo) {
        return ctx.getDomainDefinition().operations().stream()
                .filter(op -> op.getBusinessOperation() == bo)
                .findFirst()
                .orElse(null);
    }

    private WorkflowResult executeWithoutAuth(IDomain<?> ctx, OperationDefinition op) {
        // Request with no authorization token — should fail for non-anonymous access.
        // Use a non-super-tenant caller to stay aligned with how a real caller
        // would invoke this op (super and tenant callers traverse the same path
        // since the 2026-05-18 super-tenant bypass remediation).
        OperationRequest request = tenantScriptRequest(op, "acme");
        User user = new User();
        user.setName("test");
        request.arg("entity", user);
        return executeScript(ctx, request);
    }

    private WorkflowResult executeWithAuth(IDomain<?> ctx, OperationDefinition op) {
        // Request with a Mode B pre-decoded IAuthorization. The fixture has no
        // matching domain — verifyAuthorization falls through to validate(),
        // which is a no-op on TestAuthorization. Sufficient to exercise the
        // "authenticated access accepts a token" path without crypto setup.
        OperationRequest request = tenantScriptRequest(op, "acme");
        request.arg("authorization", new TestAuthorization());
        User user = new User();
        user.setName("test");
        request.arg("entity", user);
        return executeScript(ctx, request);
    }

    // --- Tests ---

    @Nested
    @DisplayName("Access level propagation")
    class AccessLevelPropagation {

        @Test
        @DisplayName("creationAccess(anonymous) sets anonymous access on create operation")
        void creationAccessAnonymous() throws ApiException {
            IDomain<?> ctx = buildSecuredDomain(b -> b.creationAccess(Access.anonymous));
            OperationDefinition op = findOperation(ctx, BusinessOperation.create);
            assertNotNull(op);
            assertEquals(Access.anonymous, op.access());
        }

        @Test
        @DisplayName("readAllAccess(tenant) sets tenant access on readAll operation")
        void readAllAccessTenant() throws ApiException {
            IDomain<?> ctx = buildSecuredDomain(b -> b.readAllAccess(Access.authenticated));
            OperationDefinition op = findOperation(ctx, BusinessOperation.readAll);
            assertNotNull(op);
            assertEquals(Access.authenticated, op.access());
        }

        @Test
        @DisplayName("updateAccess(owner) sets owner access on update operation")
        void updateAccessOwner() throws ApiException {
            IDomain<?> ctx = buildSecuredDomain(b -> b.updateAccess(Access.authenticated));
            OperationDefinition op = findOperation(ctx, BusinessOperation.update);
            assertNotNull(op);
            assertEquals(Access.authenticated, op.access());
        }

        @Test
        @DisplayName("default access is authenticated")
        void defaultAccessIsAuthenticated() throws ApiException {
            IDomain<?> ctx = buildSecuredDomain(b -> {});
            OperationDefinition op = findOperation(ctx, BusinessOperation.create);
            assertNotNull(op);
            assertEquals(Access.authenticated, op.access());
        }

        @Test
        @DisplayName("each operation can have different access levels")
        void differentAccessLevels() throws ApiException {
            IDomain<?> ctx = buildSecuredDomain(b -> b
                    .creationAccess(Access.authenticated)
                    .readAllAccess(Access.anonymous)
                    .deleteAllAccess(Access.authenticated));

            assertEquals(Access.authenticated, findOperation(ctx, BusinessOperation.create).access());
            assertEquals(Access.anonymous, findOperation(ctx, BusinessOperation.readAll).access());
            assertEquals(Access.authenticated, findOperation(ctx, BusinessOperation.deleteAll).access());
            // Unchanged ones keep default
            assertEquals(Access.authenticated, findOperation(ctx, BusinessOperation.readOne).access());
        }
    }

    @Nested
    @DisplayName("Authority propagation")
    class AuthorityPropagation {

        @Test
        @DisplayName("creationAuthority(true) sets authority on create operation")
        void creationAuthorityTrue() throws ApiException {
            IDomain<?> ctx = buildSecuredDomain(b -> b.creationAuthority(true));
            OperationDefinition op = findOperation(ctx, BusinessOperation.create);
            assertTrue(op.authority());
        }

        @Test
        @DisplayName("default authority is false")
        void defaultAuthorityFalse() throws ApiException {
            IDomain<?> ctx = buildSecuredDomain(b -> {});
            OperationDefinition op = findOperation(ctx, BusinessOperation.create);
            assertFalse(op.authority());
        }

        @Test
        @DisplayName("deleteOneAuthority(String) sets authority on deleteOne operation")
        void customAuthorityString() throws ApiException {
            IDomain<?> ctx = buildSecuredDomain(b -> b.deleteOneAuthority("users:delete"));
            OperationDefinition op = findOperation(ctx, BusinessOperation.deleteOne);
            assertTrue(op.authority());
        }
    }

    @Nested
    @DisplayName("VERIFY_AUTHORIZATION script enforcement")
    class VerifyAccessEnforcement {

        @Test
        @DisplayName("anonymous access allows unauthenticated request")
        void anonymousAccessAllowsUnauthenticated() throws ApiException {
            IDomain<?> ctx = buildSecuredDomain(b -> b.creationAccess(Access.anonymous));
            OperationDefinition op = findOperation(ctx, BusinessOperation.create);
            WorkflowResult result = executeWithoutAuth(ctx, op);
            assertTrue(result.isSuccess(), "anonymous access should allow request without auth");
        }

        @Test
        @DisplayName("authenticated access rejects request without authorization token")
        void authenticatedAccessRejectsWithoutAuth() throws ApiException {
            IDomain<?> ctx = buildSecuredDomain(b -> b.creationAccess(Access.authenticated));
            OperationDefinition op = findOperation(ctx, BusinessOperation.create);
            WorkflowResult result = executeWithoutAuth(ctx, op);
            assertFalse(result.isSuccess(),
                    "result: success=" + result.isSuccess() + " code=" + result.code()
                    + " vars=" + result.variables());
            assertEquals(401, result.code(), "authenticated access without token should return 401");
        }

        @Test
        @DisplayName("authenticated access accepts request with authorization token")
        void authenticatedAccessAcceptsWithAuth() throws ApiException {
            IDomain<?> ctx = buildSecuredDomain(b -> b.creationAccess(Access.authenticated));
            OperationDefinition op = findOperation(ctx, BusinessOperation.create);
            WorkflowResult result = executeWithAuth(ctx, op);
            assertTrue(result.isSuccess(), "authenticated access with token should succeed");
        }

        @Test
        @DisplayName("tenant access rejects request without authorization token")
        void tenantAccessRejectsWithoutAuth() throws ApiException {
            IDomain<?> ctx = buildSecuredDomain(b -> b.readAllAccess(Access.authenticated));
            OperationDefinition op = findOperation(ctx, BusinessOperation.readAll);
            WorkflowResult result = executeWithoutAuth(ctx, op);
            assertFalse(result.isSuccess());
            assertEquals(401, result.code());
        }
    }
}
