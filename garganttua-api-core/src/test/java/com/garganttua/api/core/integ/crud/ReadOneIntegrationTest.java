package com.garganttua.api.core.integ.crud;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.workflow.WorkflowResult;

@DisplayName("ReadOne Script Tests")
class ReadOneIntegrationTest extends AbstractCrudScriptTest {

    private IApi context;
    private IDomain<?> userCtx;
    private StubDao userDao;

    @BeforeEach
    void setUp() throws ApiException {
        userDao = new StubDao();

        IApiBuilder builder = newBuilder();
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
                .security().disable(true).up()
            .up();

        context = buildAndStart(builder);
        userCtx = context.getDomain("users").orElseThrow();
    }

    @Test
    @DisplayName("readOne returns a single entity")
    void readOneReturnsEntity() throws ApiException {
        seedOneUser();

        OperationDefinition readOneOp = OperationDefinition.readOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(readOneOp);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess(), "code=" + result.code() + " msg=" + result.exceptionMessage() + " output=" + result.output());
        assertNotNull(result.output());
        assertTrue(result.output() instanceof User);

        User user = (User) result.output();
        assertEquals("Alice", user.getName());
    }

    @Test
    @DisplayName("readOne returns 404 when no entities exist")
    void readOneReturns404WhenEmpty() throws ApiException {
        OperationDefinition readOneOp = OperationDefinition.readOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(readOneOp);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-nonexistent");

        WorkflowResult result = executeScript(userCtx, request);

        assertFalse(result.isSuccess());
        assertEquals(404, result.code());
    }

    @Test
    @DisplayName("readOne returns 400 when no caller is provided")
    void readOneReturns400WhenNoCaller() throws ApiException {
        seedOneUser();

        OperationDefinition readOneOp = OperationDefinition.readOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = new OperationRequest(new HashMap<>());
        request.arg(IOperationRequest.OPERATION, readOneOp);

        WorkflowResult result = executeScript(userCtx, request);

        assertFalse(result.isSuccess());
        assertEquals(400, result.code());
    }

    @Test
    @DisplayName("readOne returns 500 when repository throws an exception")
    void readOneReturns500OnRepositoryException() throws ApiException {
        IApiBuilder failingBuilder = newBuilder();

        failingBuilder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(new FailingDao())
                .up()
                .security().disable(true).up()
            .up();

        IApi failingContext = buildAndStart(failingBuilder);
        IDomain<?> failingUserCtx = failingContext.getDomain("users").orElseThrow();

        OperationDefinition readOneOp = OperationDefinition.readOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(readOneOp);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        WorkflowResult result = executeScript(failingUserCtx, request);

        assertFalse(result.isSuccess());
        assertEquals(500, result.code());
    }

    @Test
    @DisplayName("readOne with type=id uses id-based lookup")
    void readOneByIdType() throws ApiException {
        seedOneUser();

        OperationDefinition readOneOp = OperationDefinition.readOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(readOneOp);
        request.arg("type", "id");
        request.arg("identifier", "1");

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess());
        assertNotNull(result.output());
        assertTrue(result.output() instanceof User);
    }

    @Test
    @DisplayName("readOne defaults to uuid type when type is not specified")
    void readOneDefaultsToUuidType() throws ApiException {
        seedOneUser();

        OperationDefinition readOneOp = OperationDefinition.readOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(readOneOp);
        request.arg("identifier", "uuid-alice");

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess());
        assertNotNull(result.output());
        assertTrue(result.output() instanceof User);
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
