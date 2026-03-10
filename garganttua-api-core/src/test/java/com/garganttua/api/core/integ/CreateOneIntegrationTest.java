package com.garganttua.api.core.integ;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.operation.Operation;
import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.api.spec.service.IOperationRequest;
import com.garganttua.api.spec.service.IOperationResponse;
import com.garganttua.api.spec.service.OperationResponseCode;
import com.garganttua.core.reflection.IClass;

@DisplayName("CreateOne Integration Tests")
class CreateOneIntegrationTest extends AbstractCrudIntegrationTest {

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
                    .mandatory("name")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(userDao)
                .up()
            .up();

        context = buildAndStart(builder);
        userCtx = context.getDomainContext("users").orElseThrow();
    }

    @Test
    @DisplayName("createOne persists entity and returns it")
    void createOnePersistsEntity() throws ApiException {
        User user = new User();
        user.setName("Alice");
        user.setEmail("alice@example.com");

        Operation createOp = Operation.createOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(createOp);
        request.arg("entity", user);

        IOperationResponse response = userCtx.invoke(request);

        assertNotNull(response);
        assertNotNull(response);
        assertEquals(OperationResponseCode.OK, response.getResponseCode());
        assertNotNull(response.getResponse());
        assertTrue(response.getResponse() instanceof User);

        User result = (User) response.getResponse();
        assertEquals("Alice", result.getName());
        assertNotNull(result.getUuid(), "UUID should have been generated");
        assertEquals("SUPER_TENANT", result.getTenantId(), "TenantId should be set from caller");

        // Verify entity was saved to DAO
        assertNotNull(userDao.getLastSaved());
    }

    @Test
    @DisplayName("createOne generates UUID if not set")
    void createOneGeneratesUuid() throws ApiException {
        User user = new User();
        user.setName("Bob");

        Operation createOp = Operation.createOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(createOp);
        request.arg("entity", user);

        IOperationResponse response = userCtx.invoke(request);

        assertEquals(OperationResponseCode.OK, response.getResponseCode());
        User result = (User) response.getResponse();
        assertNotNull(result.getUuid());
        assertFalse(result.getUuid().isEmpty());
    }

    @Test
    @DisplayName("createOne preserves existing UUID")
    void createOnePreservesExistingUuid() throws ApiException {
        User user = new User();
        user.setUuid("my-custom-uuid");
        user.setName("Charlie");

        Operation createOp = Operation.createOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(createOp);
        request.arg("entity", user);

        IOperationResponse response = userCtx.invoke(request);

        assertEquals(OperationResponseCode.OK, response.getResponseCode());
        User result = (User) response.getResponse();
        assertEquals("my-custom-uuid", result.getUuid());
    }

    @Test
    @DisplayName("createOne sets tenantId from caller")
    void createOneSetsTenantIdFromCaller() throws ApiException {
        User user = new User();
        user.setName("Diana");

        Operation createOp = Operation.createOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(createOp);
        request.arg("entity", user);

        IOperationResponse response = userCtx.invoke(request);

        assertEquals(OperationResponseCode.OK, response.getResponseCode());
        User result = (User) response.getResponse();
        assertEquals("SUPER_TENANT", result.getTenantId());
    }

    @Test
    @DisplayName("createOne returns CLIENT_ERROR when mandatory field is null")
    void createOneReturnsBadRequestWhenMandatoryNull() throws ApiException {
        User user = new User();
        // name is mandatory but not set
        user.setEmail("nobody@example.com");

        Operation createOp = Operation.createOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(createOp);
        request.arg("entity", user);

        IOperationResponse response = userCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.CLIENT_ERROR, response.getResponseCode());
    }

    @Test
    @DisplayName("createOne returns CLIENT_ERROR when no caller is provided")
    void createOneReturnsBadRequestWhenNoCaller() throws ApiException {
        User user = new User();
        user.setName("Eve");

        Operation createOp = Operation.createOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = new OperationRequest(new HashMap<>());
        request.arg(IOperationRequest.OPERATION, createOp);
        request.arg("entity", user);

        IOperationResponse response = userCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.CLIENT_ERROR, response.getResponseCode());
        assertEquals("No caller provided", response.getResponse());
    }

    @Test
    @DisplayName("createOne returns CLIENT_ERROR when no entity is provided")
    void createOneReturnsBadRequestWhenNoEntity() throws ApiException {
        Operation createOp = Operation.createOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(createOp);
        // No entity arg

        IOperationResponse response = userCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.CLIENT_ERROR, response.getResponseCode());
    }

    @Test
    @DisplayName("createOne returns SERVER_ERROR when repository throws an exception")
    void createOneReturnsServerErrorOnRepositoryException() throws ApiException {
        IApiContextBuilder failingBuilder = newBuilder();

        failingBuilder.domain(IClass.getClass(User.class))
                .tenant(true)
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(new FailingDao())
                .up()
            .up();

        IApiContext failingContext = buildAndStart(failingBuilder);
        IDomainContext<?> failingUserCtx = failingContext.getDomainContext("users").orElseThrow();

        User user = new User();
        user.setName("Frank");

        Operation createOp = Operation.createOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(createOp);
        request.arg("entity", user);

        IOperationResponse response = failingUserCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.SERVER_ERROR, response.getResponseCode());
    }
}
