package com.garganttua.api.core.integ.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.integ.crud.AbstractCrudScriptTest;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.BusinessOperation;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.workflow.WorkflowResult;

/**
 * End-to-end tests for the VERIFY_AUTHORITY pipeline stage and the wider
 * authority chain (DSL → OperationDefinition → script → 403).
 */
@DisplayName("Authority Enforcement Integration Tests")
class AuthorityIntegrationTest extends AbstractCrudScriptTest {

    // --- Test fixture helpers ---

    private IDomain<?> buildDomain(SecurityConfigurator configurator) throws ApiException {
        CapturingDao dao = new CapturingDao();
        IApiBuilder builder = newBuilder();
        var domainBuilder = builder.domain(IClass.getClass(User.class))
                .tenant(true)
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

        IApi api = buildAndStart(builder);
        return api.getDomain("users").orElseThrow();
    }

    @FunctionalInterface
    private interface SecurityConfigurator {
        void configure(com.garganttua.api.commons.context.dsl.security.IDomainSecurityBuilder<?> builder);
    }

    private OperationDefinition op(IDomain<?> ctx, BusinessOperation bo) {
        return ctx.getDomainDefinition().operations().stream()
                .filter(o -> o.getBusinessOperation() == bo)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Operation " + bo + " not registered"));
    }

    /**
     * Builds a Mode-B request with a non-super-tenant caller carrying the
     * supplied authorities. Mode B means we bypass EXTRACT (no rawRequest)
     * and a pre-populated authorization token (so VERIFY_AUTHORIZATION
     * short-circuits to success).
     */
    private OperationRequest authenticatedRequest(OperationDefinition operation, List<String> authorities) {
        OperationRequest request = new OperationRequest(new HashMap<>());
        request.arg(IOperationRequest.OPERATION, operation);
        request.arg(IOperationRequest.TENANT_ID, "TENANT_A");
        request.arg(IOperationRequest.REQUESTED_TENANT_ID, "TENANT_A");
        request.arg(IOperationRequest.CALLER_ID, "user-1");
        request.arg(IOperationRequest.OWNER_ID, "user-1");
        request.arg(IOperationRequest.SUPER_TENANT, false);
        request.arg(IOperationRequest.SUPER_OWNER, false);
        request.arg(IOperationRequest.AUTHORITIES, authorities);
        request.arg("authorization", new Object()); // Mode B: pre-resolved authorization
        // AbstractCrudScriptTest.executeScript materializes the caller from these args
        request.arg("caller", new Caller("TENANT_A", "TENANT_A", "user-1", "user-1", false, false, authorities));
        User entity = new User();
        entity.setName("test");
        entity.setTenantId("TENANT_A");
        request.arg("entity", entity);
        return request;
    }

    // --- Tests ---

    @Nested
    @DisplayName("DSL → OperationDefinition propagation")
    class DslPropagation {

        @Test
        @DisplayName("creationAuthority(true) is exposed on the operation with effective name <technicalOp>-<scope>-<entity>")
        void booleanAuthorityProducesAutoName() throws ApiException {
            IDomain<?> ctx = buildDomain(b -> b.creationAuthority(true));
            OperationDefinition op = op(ctx, BusinessOperation.create);
            assertTrue(op.authority(), "operation should require authority");
            assertNull(op.authorityName(), "no explicit authority name was configured");
            assertEquals("create-one-user", op.effectiveAuthorityName(),
                    "default name should be <technicalOperation>-<scope>-<entity-singular-or-plural>");
        }

        @Test
        @DisplayName("creationAuthority(\"admin\") exposes the explicit name on the operation")
        void customAuthorityNameIsPropagated() throws ApiException {
            IDomain<?> ctx = buildDomain(b -> b.creationAuthority("admin"));
            OperationDefinition op = op(ctx, BusinessOperation.create);
            assertTrue(op.authority());
            assertEquals("admin", op.authorityName());
            assertEquals("admin", op.effectiveAuthorityName(),
                    "explicit name should win over the auto-generated default");
        }

        @Test
        @DisplayName("no authority configured leaves the operation with no authority requirement")
        void noAuthorityConfigured() throws ApiException {
            IDomain<?> ctx = buildDomain(b -> {});
            OperationDefinition op = op(ctx, BusinessOperation.create);
            assertFalse(op.authority());
            assertNull(op.authorityName());
            assertNull(op.effectiveAuthorityName(),
                    "no authority required → effective name is null");
        }

        @Test
        @DisplayName("each CRUD slot stores its own authority name independently")
        void independentNamesPerOperation() throws ApiException {
            IDomain<?> ctx = buildDomain(b -> b
                    .creationAuthority("write")
                    .readAllAuthority("read")
                    .deleteAllAuthority(true));
            assertEquals("write", op(ctx, BusinessOperation.create).effectiveAuthorityName());
            assertEquals("read", op(ctx, BusinessOperation.readAll).effectiveAuthorityName());
            // boolean only → auto-generated using <technicalOp>-<scope>-<entity-plural-for-all>
            assertEquals("delete-all-users", op(ctx, BusinessOperation.deleteAll).effectiveAuthorityName());
            // unconfigured operations stay unauthorized
            assertNull(op(ctx, BusinessOperation.readOne).effectiveAuthorityName());
        }
    }

    @Nested
    @DisplayName("VERIFY_AUTHORITY pipeline enforcement")
    class PipelineEnforcement {

        @Test
        @DisplayName("operation without authority requirement passes regardless of caller authorities")
        void noAuthorityRequiredPasses() throws ApiException {
            IDomain<?> ctx = buildDomain(b -> b.creationAccess(Access.authenticated));
            OperationDefinition op = op(ctx, BusinessOperation.create);
            WorkflowResult result = executeScript(ctx, authenticatedRequest(op, List.of()));
            assertTrue(result.isSuccess(),
                    "no authority configured → empty authorities list is fine. code=" + result.code()
                            + " vars=" + result.variables());
        }

        @Test
        @DisplayName("403 when caller is missing the auto-generated authority")
        void autoNameMissingReturns403() throws ApiException {
            IDomain<?> ctx = buildDomain(b -> b
                    .creationAccess(Access.authenticated)
                    .creationAuthority(true));
            OperationDefinition op = op(ctx, BusinessOperation.create);
            WorkflowResult result = executeScript(ctx, authenticatedRequest(op, List.of("something-else")));
            assertFalse(result.isSuccess(), "missing authority must fail");
            assertEquals(403, result.code(),
                    "missing auto-generated authority → 403. vars=" + result.variables());
        }

        @Test
        @DisplayName("happy path when caller carries the auto-generated authority")
        void autoNamePresentSucceeds() throws ApiException {
            IDomain<?> ctx = buildDomain(b -> b
                    .creationAccess(Access.authenticated)
                    .creationAuthority(true));
            OperationDefinition op = op(ctx, BusinessOperation.create);
            WorkflowResult result = executeScript(ctx,
                    authenticatedRequest(op, List.of("create-one-user")));
            assertTrue(result.isSuccess(),
                    "caller has the expected authority but the request failed. code=" + result.code()
                            + " vars=" + result.variables());
        }

        @Test
        @DisplayName("403 when caller is missing the custom-named authority")
        void customNameMissingReturns403() throws ApiException {
            IDomain<?> ctx = buildDomain(b -> b
                    .creationAccess(Access.authenticated)
                    .creationAuthority("admin"));
            OperationDefinition op = op(ctx, BusinessOperation.create);
            WorkflowResult result = executeScript(ctx, authenticatedRequest(op, List.of("user")));
            assertFalse(result.isSuccess());
            assertEquals(403, result.code(),
                    "missing custom authority → 403. vars=" + result.variables());
        }

        @Test
        @DisplayName("happy path when caller carries the custom-named authority")
        void customNamePresentSucceeds() throws ApiException {
            IDomain<?> ctx = buildDomain(b -> b
                    .creationAccess(Access.authenticated)
                    .creationAuthority("admin"));
            OperationDefinition op = op(ctx, BusinessOperation.create);
            WorkflowResult result = executeScript(ctx, authenticatedRequest(op, List.of("admin")));
            assertTrue(result.isSuccess(),
                    "code=" + result.code() + " vars=" + result.variables());
        }

        @Test
        @DisplayName("caller with multiple authorities passes when one matches")
        void anyMatchingAuthorityPasses() throws ApiException {
            IDomain<?> ctx = buildDomain(b -> b
                    .creationAccess(Access.authenticated)
                    .creationAuthority("admin"));
            OperationDefinition op = op(ctx, BusinessOperation.create);
            WorkflowResult result = executeScript(ctx,
                    authenticatedRequest(op, List.of("user", "admin", "auditor")));
            assertTrue(result.isSuccess(),
                    "code=" + result.code() + " vars=" + result.variables());
        }

        @Test
        @DisplayName("authority requirement is per-operation: readAll passes while create is blocked")
        void requirementIsPerOperation() throws ApiException {
            IDomain<?> ctx = buildDomain(b -> b
                    .creationAccess(Access.authenticated)
                    .creationAuthority("admin")
                    .readAllAccess(Access.authenticated));
            OperationDefinition createOp = op(ctx, BusinessOperation.create);
            OperationDefinition readAllOp = op(ctx, BusinessOperation.readAll);

            // Caller has no authorities — readAll requires none and should pass
            WorkflowResult readResult = executeScript(ctx,
                    authenticatedRequest(readAllOp, List.of()));
            assertTrue(readResult.isSuccess(),
                    "readAll without authority configured should pass. code=" + readResult.code());

            // Same caller hits create → blocked
            WorkflowResult createResult = executeScript(ctx,
                    authenticatedRequest(createOp, List.of()));
            assertEquals(403, createResult.code(),
                    "create with authority configured should require it. vars=" + createResult.variables());
        }

        @Test
        @DisplayName("super-tenant caller bypasses the authority check")
        void superTenantBypasses() throws ApiException {
            IDomain<?> ctx = buildDomain(b -> b
                    .creationAccess(Access.authenticated)
                    .creationAuthority("admin"));
            OperationDefinition op = op(ctx, BusinessOperation.create);
            // Super-tenant caller with NO authorities should still pass
            OperationRequest request = superTenantScriptRequest(op);
            request.arg("authorization", new Object());
            request.arg(IOperationRequest.AUTHORITIES, List.<String>of());
            Caller superCaller = new Caller("SUPER_TENANT", "SUPER_TENANT", "super-user", "super-user",
                    true, true, List.of());
            request.arg("caller", superCaller);
            User entity = new User();
            entity.setName("test");
            entity.setTenantId("SUPER_TENANT");
            request.arg("entity", entity);
            WorkflowResult result = executeScript(ctx, request);
            assertTrue(result.isSuccess(),
                    "super-tenant should bypass authority. code=" + result.code()
                            + " vars=" + result.variables());
        }
    }

    @Nested
    @DisplayName("Workflow stage registration")
    class StageRegistration {

        @Test
        @DisplayName("verify-authority stage is wired when security is configured")
        void stageIsWiredOnSecuredDomain() throws ApiException {
            IDomain<?> ctx = buildDomain(b -> b.creationAuthority(true));
            assertNotNull(ctx.getWorkflow(), "secured domain should have a workflow");
            // Stage names are not introspectable from IWorkflow directly, but we can
            // observe the side effect: an authority-protected op with no caller fails
            // with 403, not 405.
            OperationDefinition op = op(ctx, BusinessOperation.create);
            WorkflowResult result = executeScript(ctx, authenticatedRequest(op, List.of("nope")));
            assertEquals(403, result.code(),
                    "expected 403 from verify-authority. got code=" + result.code()
                            + " vars=" + result.variables());
        }
    }
}
