package com.garganttua.api.core.integ.crud;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;

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

@DisplayName("DeleteAll Script Tests")
class DeleteAllIntegrationTest extends AbstractCrudScriptTest {

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
    @DisplayName("deleteAll deletes all entities and returns them")
    void deleteAllDeletesAllEntities() throws ApiException {
        seedUsers("Alice", "Bob", "Charlie");
        assertEquals(3, userDao.getStorage().size());

        OperationDefinition deleteAllOp = OperationDefinition.deleteAllWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(deleteAllOp);

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess());
        assertNotNull(result.output());
        assertTrue(result.output() instanceof List);

        List<?> deleted = (List<?>) result.output();
        assertEquals(3, deleted.size());

        assertEquals(0, userDao.getStorage().size(), "All entities should have been deleted from DAO");
    }

    @Test
    @DisplayName("deleteAll returns empty list when no entities exist")
    void deleteAllReturnsEmptyWhenNoEntities() throws ApiException {
        OperationDefinition deleteAllOp = OperationDefinition.deleteAllWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(deleteAllOp);

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess());
        assertTrue(result.output() instanceof List);

        List<?> deleted = (List<?>) result.output();
        assertTrue(deleted.isEmpty());
    }

    @Test
    @DisplayName("deleteAll returns 400 when no caller is provided")
    void deleteAllReturns400WhenNoCaller() throws ApiException {
        seedUsers("Alice");

        OperationDefinition deleteAllOp = OperationDefinition.deleteAllWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = new OperationRequest(new HashMap<>());
        request.arg(IOperationRequest.OPERATION, deleteAllOp);

        WorkflowResult result = executeScript(userCtx, request);

        assertFalse(result.isSuccess());
        assertEquals(400, result.code());

        assertEquals(1, userDao.getStorage().size(), "Entity should not have been deleted");
    }

    @Test
    @DisplayName("deleteAll returns 500 when repository throws an exception")
    void deleteAllReturns500OnRepositoryException() throws ApiException {
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

        OperationDefinition deleteAllOp = OperationDefinition.deleteAllWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(deleteAllOp);

        WorkflowResult result = executeScript(failingUserCtx, request);

        assertFalse(result.isSuccess());
        assertEquals(500, result.code());
    }

    @Test
    @DisplayName("deleteAll with single entity deletes it and returns it")
    void deleteAllSingleEntity() throws ApiException {
        seedUsers("Alice");
        assertEquals(1, userDao.getStorage().size());

        OperationDefinition deleteAllOp = OperationDefinition.deleteAllWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(deleteAllOp);

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess());
        List<?> deleted = (List<?>) result.output();
        assertEquals(1, deleted.size());
        assertEquals(0, userDao.getStorage().size());
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
