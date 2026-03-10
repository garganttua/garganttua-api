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

@DisplayName("ReadOne Integration Tests")
class ReadOneIntegrationTest extends AbstractCrudIntegrationTest {

    private IApiContext context;
    private IDomainContext<?> userCtx;
    private StubDao userDao;

    @BeforeEach
    void setUp() throws ApiException {
        userDao = new StubDao();

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
            .up();

        context = buildAndStart(builder);
        userCtx = context.getDomainContext("users").orElseThrow();
    }

    @Test
    @DisplayName("readOne returns a single entity")
    void readOneReturnsEntity() throws ApiException {
        seedOneUser();

        Operation readOneOp = Operation.readOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(readOneOp);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        IOperationResponse response = userCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.OK, response.getResponseCode());
        assertNotNull(response.getResponse());
        assertTrue(response.getResponse() instanceof User);

        User user = (User) response.getResponse();
        assertEquals("Alice", user.getName());
    }

    @Test
    @DisplayName("readOne returns NOT_FOUND when no entities exist")
    void readOneReturnsNotFoundWhenEmpty() throws ApiException {
        // No entities seeded

        Operation readOneOp = Operation.readOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(readOneOp);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-nonexistent");

        IOperationResponse response = userCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.NOT_FOUND, response.getResponseCode());
    }

    @Test
    @DisplayName("readOne returns CLIENT_ERROR when no caller is provided")
    void readOneReturnsBadRequestWhenNoCaller() throws ApiException {
        seedOneUser();

        Operation readOneOp = Operation.readOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = new OperationRequest(new HashMap<>());
        request.arg(IOperationRequest.OPERATION, readOneOp);

        IOperationResponse response = userCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.CLIENT_ERROR, response.getResponseCode());
        assertEquals("No caller provided", response.getResponse());
    }

    @Test
    @DisplayName("readOne returns SERVER_ERROR when repository throws an exception")
    void readOneReturnsServerErrorOnRepositoryException() throws ApiException {
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

        Operation readOneOp = Operation.readOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(readOneOp);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        IOperationResponse response = failingUserCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.SERVER_ERROR, response.getResponseCode());
    }

    @Test
    @DisplayName("readOne with type=id uses id-based lookup")
    void readOneByIdType() throws ApiException {
        seedOneUser();

        Operation readOneOp = Operation.readOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(readOneOp);
        request.arg("type", "id");
        request.arg("identifier", "1");

        IOperationResponse response = userCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.OK, response.getResponseCode());
        assertNotNull(response.getResponse());
        assertTrue(response.getResponse() instanceof User);
    }

    @Test
    @DisplayName("readOne defaults to uuid type when type is not specified")
    void readOneDefaultsToUuidType() throws ApiException {
        seedOneUser();

        Operation readOneOp = Operation.readOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(readOneOp);
        request.arg("identifier", "uuid-alice");
        // No type arg — should default to uuid

        IOperationResponse response = userCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.OK, response.getResponseCode());
        assertNotNull(response.getResponse());
        assertTrue(response.getResponse() instanceof User);
    }

    private void seedOneUser() {
        UserDto alice = new UserDto();
        alice.setId("1");
        alice.setUuid("uuid-alice");
        alice.setTenantId("SUPER_TENANT");
        alice.setName("Alice");
        alice.setEmail("alice@example.com");
        userDao.getStorage().add(alice);
    }
}
