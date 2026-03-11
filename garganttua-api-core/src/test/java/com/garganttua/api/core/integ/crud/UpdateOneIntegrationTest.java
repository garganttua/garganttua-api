package com.garganttua.api.core.integ.crud;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.operation.OperationDefinition;
import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.api.spec.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.workflow.WorkflowResult;

@DisplayName("UpdateOne Script Tests")
class UpdateOneIntegrationTest extends AbstractCrudScriptTest {

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
                    .update("name")
                    .update("email")
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
    @DisplayName("updateOne updates entity by uuid and returns it")
    void updateOneByUuid() throws ApiException {
        seedUser("1", "uuid-alice", "Alice", "alice@example.com");

        User updatedUser = new User();
        updatedUser.setName("Alice Updated");
        updatedUser.setEmail("alice.updated@example.com");

        OperationDefinition updateOp = OperationDefinition.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(updateOp);
        request.arg("entity", updatedUser);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        WorkflowResult result = executeScript(userCtx, "update", request);

        assertTrue(result.isSuccess());
        assertTrue(result.output() instanceof User);

        User output = (User) result.output();
        assertEquals("Alice Updated", output.getName());
        assertEquals("alice.updated@example.com", output.getEmail());
        assertEquals("uuid-alice", output.getUuid(), "UUID should not change");
    }

    @Test
    @DisplayName("updateOne updates entity by id")
    void updateOneById() throws ApiException {
        seedUser("1", "uuid-alice", "Alice", "alice@example.com");

        User updatedUser = new User();
        updatedUser.setName("Alice By Id");

        OperationDefinition updateOp = OperationDefinition.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(updateOp);
        request.arg("entity", updatedUser);
        request.arg("type", "id");
        request.arg("identifier", "1");

        WorkflowResult result = executeScript(userCtx, "update", request);

        assertTrue(result.isSuccess());
        User output = (User) result.output();
        assertEquals("Alice By Id", output.getName());
    }

    @Test
    @DisplayName("updateOne defaults to uuid type when not specified")
    void updateOneDefaultsToUuid() throws ApiException {
        seedUser("1", "uuid-alice", "Alice", "alice@example.com");

        User updatedUser = new User();
        updatedUser.setName("Alice Default");

        OperationDefinition updateOp = OperationDefinition.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(updateOp);
        request.arg("entity", updatedUser);
        request.arg("identifier", "uuid-alice");

        WorkflowResult result = executeScript(userCtx, "update", request);

        assertTrue(result.isSuccess());
        User output = (User) result.output();
        assertEquals("Alice Default", output.getName());
    }

    @Test
    @DisplayName("updateOne returns 404 when entity does not exist")
    void updateOneReturns404() throws ApiException {
        User updatedUser = new User();
        updatedUser.setName("Ghost");

        OperationDefinition updateOp = OperationDefinition.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(updateOp);
        request.arg("entity", updatedUser);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-nonexistent");

        WorkflowResult result = executeScript(userCtx, "update", request);

        assertFalse(result.isSuccess());
        assertEquals(404, result.code());
    }

    @Test
    @DisplayName("updateOne returns 400 when no caller is provided")
    void updateOneReturns400WhenNoCaller() throws ApiException {
        seedUser("1", "uuid-alice", "Alice", "alice@example.com");

        User updatedUser = new User();
        updatedUser.setName("Alice Updated");

        OperationDefinition updateOp = OperationDefinition.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = new OperationRequest(new HashMap<>());
        request.arg(IOperationRequest.OPERATION, updateOp);
        request.arg("entity", updatedUser);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        WorkflowResult result = executeScript(userCtx, "update", request);

        assertFalse(result.isSuccess());
        assertEquals(400, result.code());
    }

    @Test
    @DisplayName("updateOne returns 400 when no entity is provided")
    void updateOneReturns400WhenNoEntity() throws ApiException {
        seedUser("1", "uuid-alice", "Alice", "alice@example.com");

        OperationDefinition updateOp = OperationDefinition.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(updateOp);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        WorkflowResult result = executeScript(userCtx, "update", request);

        assertFalse(result.isSuccess());
        assertEquals(400, result.code());
    }

    @Test
    @DisplayName("updateOne returns 500 when repository throws an exception")
    void updateOneReturns500OnRepositoryException() throws ApiException {
        IApiContextBuilder failingBuilder = newBuilder();

        failingBuilder.domain(IClass.getClass(User.class))
                .tenant(true)
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .update("name")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(new FailingDao())
                .up()
            .up();

        IApiContext failingContext = buildAndStart(failingBuilder);
        IDomainContext<?> failingUserCtx = failingContext.getDomainContext("users").orElseThrow();

        User updatedUser = new User();
        updatedUser.setName("Fail");

        OperationDefinition updateOp = OperationDefinition.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(updateOp);
        request.arg("entity", updatedUser);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        WorkflowResult result = executeScript(failingUserCtx, "update", request);

        assertFalse(result.isSuccess());
        assertEquals(500, result.code());
    }

    @Test
    @DisplayName("updateOne persists the updated entity to the DAO")
    void updateOnePersistsUpdatedEntity() throws ApiException {
        seedUser("1", "uuid-alice", "Alice", "alice@example.com");

        User updatedUser = new User();
        updatedUser.setName("Alice Persisted");
        updatedUser.setEmail("persisted@example.com");

        OperationDefinition updateOp = OperationDefinition.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(updateOp);
        request.arg("entity", updatedUser);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        WorkflowResult result = executeScript(userCtx, "update", request);

        assertTrue(result.isSuccess());
        assertNotNull(userDao.getLastSaved(), "Updated entity should have been saved to DAO");
    }

    private void seedUser(String id, String uuid, String name, String email) {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setUuid(uuid);
        dto.setTenantId("SUPER_TENANT");
        dto.setName(name);
        dto.setEmail(email);
        userDao.getStorage().add(dto);
    }
}
