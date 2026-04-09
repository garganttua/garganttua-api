package com.garganttua.api.core.integ.crud;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.context.IApi;
import com.garganttua.api.spec.context.IDomain;
import com.garganttua.api.spec.operation.OperationDefinition;
import com.garganttua.api.spec.context.dsl.IApiBuilder;
import com.garganttua.api.spec.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.workflow.WorkflowResult;

@DisplayName("CreateOne Script Tests")
class CreateOneIntegrationTest extends AbstractCrudScriptTest {

    private IApi context;
    private IDomain<?> userCtx;
    private CapturingDao userDao;

    @BeforeEach
    void setUp() throws ApiException {
        userDao = new CapturingDao();

        IApiBuilder builder = newBuilder();
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
        userCtx = context.getDomain("users").orElseThrow();
    }

    @Test
    @DisplayName("createOne persists entity and returns it")
    void createOnePersistsEntity() throws ApiException {
        User user = new User();
        user.setName("Alice");
        user.setEmail("alice@example.com");

        OperationDefinition createOp = OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(createOp);
        request.arg("entity", user);

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess(), () -> {
            var workflow = (com.garganttua.core.workflow.Workflow) userCtx.getWorkflow();
            return "Workflow failed with code " + result.code()
                    + "\nGenerated script:\n" + workflow.getGeneratedScript();
        });
        assertNotNull(result.output());
        assertTrue(result.output() instanceof User);

        User output = (User) result.output();
        assertEquals("Alice", output.getName());
        assertNotNull(output.getUuid(), "UUID should have been generated");
        assertEquals("SUPER_TENANT", output.getTenantId(), "TenantId should be set from caller");

        assertNotNull(userDao.getLastSaved());
    }

    @Test
    @DisplayName("createOne generates UUID if not set")
    void createOneGeneratesUuid() throws ApiException {
        User user = new User();
        user.setName("Bob");

        OperationDefinition createOp = OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(createOp);
        request.arg("entity", user);

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess());
        User output = (User) result.output();
        assertNotNull(output.getUuid());
        assertFalse(output.getUuid().isEmpty());
    }

    @Test
    @DisplayName("createOne preserves existing UUID")
    void createOnePreservesExistingUuid() throws ApiException {
        User user = new User();
        user.setUuid("my-custom-uuid");
        user.setName("Charlie");

        OperationDefinition createOp = OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(createOp);
        request.arg("entity", user);

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess());
        User output = (User) result.output();
        assertEquals("my-custom-uuid", output.getUuid());
    }

    @Test
    @DisplayName("createOne sets tenantId from caller")
    void createOneSetsTenantIdFromCaller() throws ApiException {
        User user = new User();
        user.setName("Diana");

        OperationDefinition createOp = OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(createOp);
        request.arg("entity", user);

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess());
        User output = (User) result.output();
        assertEquals("SUPER_TENANT", output.getTenantId());
    }

    @Test
    @DisplayName("createOne returns 400 when mandatory field is null")
    void createOneReturns400WhenMandatoryNull() throws ApiException {
        User user = new User();
        user.setEmail("nobody@example.com");

        OperationDefinition createOp = OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(createOp);
        request.arg("entity", user);

        WorkflowResult result = executeScript(userCtx, request);

        assertFalse(result.isSuccess());
        assertEquals(400, result.code());
    }

    @Test
    @DisplayName("createOne returns 400 when no caller is provided")
    void createOneReturns400WhenNoCaller() throws ApiException {
        User user = new User();
        user.setName("Eve");

        OperationDefinition createOp = OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = new OperationRequest(new HashMap<>());
        request.arg(IOperationRequest.OPERATION, createOp);
        request.arg("entity", user);

        WorkflowResult result = executeScript(userCtx, request);

        assertFalse(result.isSuccess());
        assertEquals(400, result.code());
    }

    @Test
    @DisplayName("createOne returns 400 when no entity is provided")
    void createOneReturns400WhenNoEntity() throws ApiException {
        OperationDefinition createOp = OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(createOp);

        WorkflowResult result = executeScript(userCtx, request);

        assertFalse(result.isSuccess());
        assertEquals(400, result.code());
    }

    @Test
    @DisplayName("createOne returns 500 when repository throws an exception")
    void createOneReturns500OnRepositoryException() throws ApiException {
        IApiBuilder failingBuilder = newBuilder();

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

        IApi failingContext = buildAndStart(failingBuilder);
        IDomain<?> failingUserCtx = failingContext.getDomain("users").orElseThrow();

        User user = new User();
        user.setName("Frank");

        OperationDefinition createOp = OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(createOp);
        request.arg("entity", user);

        WorkflowResult result = executeScript(failingUserCtx, request);

        assertFalse(result.isSuccess());
        assertEquals(500, result.code());
    }
}
