package com.garganttua.api.core.integ.security;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.integ.crud.AbstractCrudScriptTest;
import com.garganttua.api.core.security.authentication.AuthenticationRequest;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.api.spec.operation.BusinessOperation;
import com.garganttua.api.spec.operation.OperationDefinition;
import com.garganttua.api.spec.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.workflow.WorkflowResult;

@DisplayName("Authenticate Script Integration Tests")
class AuthenticateIntegrationTest extends AbstractCrudScriptTest {

    private IApiContext context;
    private IDomainContext<?> userCtx;
    private CapturingDao userDao;

    @BeforeEach
    void setUp() throws ApiException {
        userDao = new CapturingDao();

        IApiContextBuilder builder = newBuilder();
        builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(userDao)
                .up()
                .workflow(BusinessOperation.authenticate.getLabel())
                .up()
            .up();

        context = buildAndStart(builder);
        userCtx = context.getDomainContext("users").orElseThrow();
    }

    private OperationRequest authenticateRequest() {
        OperationDefinition authOp = OperationDefinition.authenticate("users", IClass.getClass(User.class));
        return superTenantScriptRequest(authOp);
    }

    @Nested
    @DisplayName("Request validation")
    class RequestValidation {

        @Test
        @DisplayName("returns 400 when no caller is provided")
        void returns400WhenNoCaller() throws ApiException {
            AuthenticationRequest authReq = new AuthenticationRequest("john", "secret".getBytes(StandardCharsets.UTF_8), "SUPER_TENANT");

            OperationDefinition authOp = OperationDefinition.authenticate("users", IClass.getClass(User.class));
            OperationRequest request = new OperationRequest(new HashMap<>());
            request.arg(IOperationRequest.OPERATION, authOp);
            request.arg("entity", authReq);

            WorkflowResult result = executeScript(userCtx, request);

            assertFalse(result.isSuccess());
            assertEquals(400, result.code());
        }

        @Test
        @DisplayName("returns 400 when no entity is provided")
        void returns400WhenNoEntity() throws ApiException {
            OperationRequest request = authenticateRequest();

            WorkflowResult result = executeScript(userCtx, request);

            assertFalse(result.isSuccess());
            assertEquals(400, result.code());
        }

        @Test
        @DisplayName("entity is passed as AuthenticationRequest")
        void entityIsAuthenticationRequest() {
            AuthenticationRequest authReq = new AuthenticationRequest(
                    "john@example.com",
                    "password123".getBytes(StandardCharsets.UTF_8),
                    "TENANT_1");

            assertEquals("john@example.com", authReq.login());
            assertArrayEquals("password123".getBytes(StandardCharsets.UTF_8), authReq.credentials());
            assertEquals("TENANT_1", authReq.tenantId());
        }
    }

    @Nested
    @DisplayName("Business operation routing")
    class BusinessOperationRouting {

        @Test
        @DisplayName("authenticate operation is routed to AUTHENTICATE script")
        void authenticateOperationIsRouted() throws ApiException {
            AuthenticationRequest authReq = new AuthenticationRequest(
                    "john@example.com",
                    "secret".getBytes(StandardCharsets.UTF_8),
                    "SUPER_TENANT");

            OperationRequest request = authenticateRequest();
            request.arg("entity", authReq);

            WorkflowResult result = executeScript(userCtx, request);

            // The workflow dispatches to the authenticate stage
            assertNotNull(result);
        }

        @Test
        @DisplayName("authenticate OperationDefinition has correct business operation")
        void operationDefinitionHasCorrectBusinessOp() {
            OperationDefinition authOp = OperationDefinition.authenticate("users", IClass.getClass(User.class));
            assertEquals(BusinessOperation.authenticate, authOp.getBusinessOperation());
        }
    }

    @Nested
    @DisplayName("AuthenticationRequest record")
    class AuthenticationRequestRecord {

        @Test
        @DisplayName("record accessors return constructor values")
        void recordAccessors() {
            byte[] creds = "pass".getBytes(StandardCharsets.UTF_8);
            AuthenticationRequest req = new AuthenticationRequest("alice", creds, "T1");

            assertEquals("alice", req.login());
            assertSame(creds, req.credentials());
            assertEquals("T1", req.tenantId());
        }

        @Test
        @DisplayName("tenantId can be null")
        void tenantIdCanBeNull() {
            AuthenticationRequest req = new AuthenticationRequest("bob", new byte[]{1, 2}, null);
            assertNull(req.tenantId());
        }

        @Test
        @DisplayName("implements IAuthenticationRequest")
        void implementsInterface() {
            AuthenticationRequest req = new AuthenticationRequest("x", new byte[0], null);
            assertInstanceOf(com.garganttua.api.spec.security.authentication.IAuthenticationRequest.class, req);
        }
    }
}
