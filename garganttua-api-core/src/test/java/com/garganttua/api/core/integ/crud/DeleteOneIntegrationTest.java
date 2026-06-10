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

@DisplayName("DeleteOne Script Tests")
class DeleteOneIntegrationTest extends AbstractCrudScriptTest {

    private IApi context;
    private IDomain<?> userCtx;
    private CapturingDao userDao;

    @BeforeEach
    void setUp() throws ApiException {
        userDao = new CapturingDao();

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
    @DisplayName("deleteOne deletes entity by uuid and returns it")
    void deleteOneByUuid() throws ApiException {
        seedUsers("Alice", "Bob");
        assertEquals(2, userDao.getStorage().size());

        OperationDefinition deleteOneOp = OperationDefinition.deleteOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(deleteOneOp);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess());
        assertTrue(result.output() instanceof User);

        User deleted = (User) result.output();
        assertEquals("Alice", deleted.getName());

        assertEquals(1, userDao.getStorage().size(), "Only one entity should remain");
    }

    @Test
    @DisplayName("deleteOne deletes entity by id")
    void deleteOneById() throws ApiException {
        seedUsers("Alice");

        OperationDefinition deleteOneOp = OperationDefinition.deleteOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(deleteOneOp);
        request.arg("type", "id");
        request.arg("identifier", "1");

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess());
        assertTrue(result.output() instanceof User);
        assertEquals(0, userDao.getStorage().size());
    }

    @Test
    @DisplayName("deleteOne defaults to uuid type when not specified")
    void deleteOneDefaultsToUuid() throws ApiException {
        seedUsers("Alice");

        OperationDefinition deleteOneOp = OperationDefinition.deleteOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(deleteOneOp);
        request.arg("identifier", "uuid-alice");

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess());
        assertTrue(result.output() instanceof User);
        assertEquals(0, userDao.getStorage().size());
    }

    @Test
    @DisplayName("deleteOne returns 404 when entity does not exist")
    void deleteOneReturns404() throws ApiException {
        OperationDefinition deleteOneOp = OperationDefinition.deleteOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(deleteOneOp);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-nonexistent");

        WorkflowResult result = executeScript(userCtx, request);

        assertFalse(result.isSuccess());
        assertEquals(404, result.code());
    }

    @Test
    @DisplayName("deleteOne returns 400 when no caller is provided")
    void deleteOneReturns400WhenNoCaller() throws ApiException {
        seedUsers("Alice");

        OperationDefinition deleteOneOp = OperationDefinition.deleteOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = new OperationRequest(new HashMap<>());
        request.arg(IOperationRequest.OPERATION, deleteOneOp);

        WorkflowResult result = executeScript(userCtx, request);

        assertFalse(result.isSuccess());
        assertEquals(400, result.code());

        assertEquals(1, userDao.getStorage().size(), "Entity should not have been deleted");
    }

    @Test
    @DisplayName("deleteOne returns 500 when repository throws an exception")
    void deleteOneReturns500OnRepositoryException() throws ApiException {
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

        OperationDefinition deleteOneOp = OperationDefinition.deleteOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(deleteOneOp);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        WorkflowResult result = executeScript(failingUserCtx, request);

        assertFalse(result.isSuccess());
        assertEquals(500, result.code());
    }

    private void seedUsers(String... names) {
        int i = 1;
        for (String name : names) {
            UserDto dto = new UserDto();
            dto.setId(String.valueOf(i));
            dto.setUuid("uuid-" + name.toLowerCase());
            dto.setTenantId("SUPER_TENANT");
            dto.setName(name);
            dto.setEmail(name.toLowerCase() + "@example.com");
            userDao.getStorage().add(dto);
            i++;
        }
    }
}
