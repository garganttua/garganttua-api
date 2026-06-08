package com.garganttua.api.core.integ.security;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.integ.crud.AbstractCrudScriptTest;
import com.garganttua.api.core.security.authentication.AuthenticationRequest;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.definition.IAuthenticatorDefinition;
import com.garganttua.api.commons.operation.BusinessOperation;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.security.authentication.Authentication;
import com.garganttua.api.commons.security.authentication.IAuthentication;
import com.garganttua.api.commons.security.authenticator.AuthenticatorScope;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;
import com.garganttua.core.workflow.WorkflowResult;

@DisplayName("Authenticate Script Integration Tests")
class AuthenticateIntegrationTest extends AbstractCrudScriptTest {

    // --- Stub authentication method ---

    /**
     * Simulates an authentication method (like LoginPasswordAuthentication).
     * Compares credentials against a hardcoded password.
     */

    /**
     * Always-failing authentication method.
     */

    private IApi context;
    private IDomain<?> userCtx;
    private CapturingDao userDao;

    @BeforeEach
    void setUp() throws ApiException {
        userDao = new CapturingDao();

        StubAuthentication stubAuth = new StubAuthentication();

        IApiBuilder builder = newBuilder();

        // Register the authentication method at API level with parameter suppliers
        var authBuilder = builder.security()
                .authentication(new FixedSupplierBuilder<>(stubAuth, IClass.getClass(StubAuthentication.class)));
        authBuilder.authenticate("authenticate")
                .withParam(0, new com.garganttua.api.core.security.authentication.PrincipalSupplierBuilder())
                .withParam(1, new com.garganttua.api.core.security.authentication.AuthenticateCredentialsSupplierBuilder())
                .withParam(2, new com.garganttua.api.core.security.authentication.AuthenticatorDefinitionSupplierBuilder());
        authBuilder.up();

        // Register domain with authenticator referencing the authentication method
        builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(userDao)
                .up()
                .security()
                    .authenticator()
                        .login("id")
                        .scope(AuthenticatorScope.tenant)
                        .enabled("enabled")
                        .accountNonLocked("accountNonLocked")
                        .accountNonExpired("accountNonExpired")
                        .credentialsNonExpired("credentialsNonExpired")
                        .alwaysEnabled(true)
                        .authentication(authBuilder)
                    .up()
                .up()
            .up();

        context = buildAndStart(builder);
        userCtx = context.getDomain("users").orElseThrow();

        // Pre-populate a user DTO in the DAO (the pipeline works with DTOs, not entities)
        UserDto existingUser = new UserDto();
        existingUser.setId("john@example.com");
        existingUser.setUuid("user-uuid-1");
        existingUser.setTenantId("SUPER_TENANT");
        existingUser.setName("John");
        userDao.save(existingUser);
    }

    private OperationRequest authenticateRequest() {
        OperationDefinition authOp = OperationDefinition.authenticate("users", IClass.getClass(User.class));
        return superTenantScriptRequest(authOp);
    }

    @Nested
    @DisplayName("Automatic workflow registration")
    class AutomaticWorkflowRegistration {

        @Test
        @DisplayName("authenticate workflow is automatically registered when authenticator is configured")
        void authenticateWorkflowAutoRegistered() {
            assertNotNull(userCtx.getWorkflow(), "workflow should exist");

            AuthenticationRequest authReq = new AuthenticationRequest(
                    "john@example.com",
                    "valid-password".getBytes(StandardCharsets.UTF_8));

            OperationRequest request = authenticateRequest();
            request.arg("entity", authReq);

            WorkflowResult result = executeScript(userCtx, request);
            assertNotEquals(-1, result.code(), "authenticate stage should be registered");
        }

        @Test
        @DisplayName("authenticate OperationDefinition has correct business operation")
        void operationDefinitionHasCorrectBusinessOp() {
            OperationDefinition authOp = OperationDefinition.authenticate("users", IClass.getClass(User.class));
            assertEquals(BusinessOperation.authenticate, authOp.getBusinessOperation());
        }
    }

    @Nested
    @DisplayName("Request validation")
    class RequestValidation {

        @Test
        @DisplayName("does not require an authenticated caller — only the tenant context for tenant scope")
        void doesNotRequireCaller() throws ApiException {
            AuthenticationRequest authReq = new AuthenticationRequest(
                    "john@example.com", "valid-password".getBytes(StandardCharsets.UTF_8));

            OperationDefinition authOp = OperationDefinition.authenticate("users", IClass.getClass(User.class));
            OperationRequest request = new OperationRequest(new HashMap<>());
            request.arg(IOperationRequest.OPERATION, authOp);
            // No caller IDENTITY (no callerId / authorities) — authentication is the
            // anonymous entry point — but a tenant-scoped authenticator still needs the
            // caller's tenant context (over HTTP, the X-Tenant-Id header).
            request.arg(IOperationRequest.TENANT_ID, "SUPER_TENANT");
            request.arg("entity", authReq);

            WorkflowResult result = executeScript(userCtx, request);

            assertNotEquals(400, result.code(), "authenticate should not require an identified caller");
            assertNotEquals(-1, result.code(), "script should not abort");
        }

        @Test
        @DisplayName("returns 400 when no entity is provided")
        void returns400WhenNoEntity() throws ApiException {
            OperationRequest request = authenticateRequest();

            WorkflowResult result = executeScript(userCtx, request);

            assertFalse(result.isSuccess());
            assertEquals(400, result.code());
        }
    }

    @Nested
    @DisplayName("Authenticator scope")
    class AuthenticatorScopeTests {

        @Test
        @DisplayName("tenant-scoped authenticator passes when entity has tenantId")
        void tenantScopePassesWithTenantId() throws ApiException {
            AuthenticationRequest authReq = new AuthenticationRequest(
                    "john@example.com",
                    "valid-password".getBytes(StandardCharsets.UTF_8));

            OperationRequest request = authenticateRequest();
            request.arg("entity", authReq);

            WorkflowResult result = executeScript(userCtx, request);

            assertNotNull(result);
            assertNotEquals(400, result.code(), "should not fail on tenant scope check");
            assertNotEquals(-1, result.code(), "script should not abort");
        }

        @Test
        @DisplayName("tenant-scoped authenticator returns 400 when the caller carries no tenant")
        void tenantScopeFailsWithoutTenantId() throws ApiException {
            AuthenticationRequest authReq = new AuthenticationRequest(
                    "john@example.com",
                    "valid-password".getBytes(StandardCharsets.UTF_8));

            // No tenant on the caller (no TENANT_ID arg) — tenant-scoped auth must reject.
            OperationRequest request = new OperationRequest(new HashMap<>());
            request.arg(IOperationRequest.OPERATION,
                    OperationDefinition.authenticate("users", IClass.getClass(User.class)));
            request.arg("entity", authReq);

            WorkflowResult result = executeScript(userCtx, request);

            assertEquals(400, result.code(), "tenant scope without the caller's tenant should return 400");
            // The parlant message is stashed on the request by recordCaughtException
            // (the `! -> 400` pattern resets the WorkflowResult's own message).
            Object recorded = request.arg(com.garganttua.api.commons.service.ArgKey.of(
                    "_lastException", IClass.getClass(Object.class))).orElse(null);
            String message = (recorded instanceof Throwable t && t.getMessage() != null) ? t.getMessage() : "";
            assertTrue(message.contains("caller's tenant"),
                    "the 400 must name what is required (the caller's tenant), not a generic "
                            + "'Required value is null'; got: " + message);
        }
    }

    @Nested
    @DisplayName("Authorization generation gate")
    class AuthorizationGenerationGate {

        @Test
        @DisplayName("no authorization defined: authentication succeeds but NO token is generated (output is the auth result)")
        void noAuthorizationDefinedNoTokenGenerated() throws ApiException {
            // The authenticator in setUp() declares NO .authorization(...) — so on a
            // successful authentication CREATE_AUTHORIZATION must not run, and the
            // output is the raw IAuthentication result rather than a minted token.
            AuthenticationRequest authReq = new AuthenticationRequest(
                    "john@example.com",
                    "valid-password".getBytes(StandardCharsets.UTF_8));

            OperationRequest request = authenticateRequest();
            request.arg("entity", authReq);

            WorkflowResult result = executeScript(userCtx, request);

            assertEquals(0, result.code(), "authentication must succeed; vars=" + result.variables());
            assertInstanceOf(IAuthentication.class, result.output(),
                    "with no authorization defined, the output must be the authentication result, not a token");
            assertTrue(((IAuthentication) result.output()).authenticated(),
                    "the authentication result must be successful");
        }
    }

    @Nested
    @DisplayName("AuthenticationRequest record")
    class AuthenticationRequestRecord {

        @Test
        @DisplayName("record accessors return constructor values")
        void recordAccessors() {
            byte[] creds = "pass".getBytes(StandardCharsets.UTF_8);
            AuthenticationRequest req = new AuthenticationRequest("alice", creds);

            assertEquals("alice", req.login());
            assertSame(creds, req.credentials());
        }

        @Test
        @DisplayName("implements IAuthenticationRequest")
        void implementsInterface() {
            AuthenticationRequest req = new AuthenticationRequest("x", new byte[0]);
            assertInstanceOf(com.garganttua.api.commons.security.authentication.IAuthenticationRequest.class, req);
        }
    }
}
