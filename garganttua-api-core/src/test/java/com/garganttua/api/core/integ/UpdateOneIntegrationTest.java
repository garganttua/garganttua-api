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

@DisplayName("UpdateOne Integration Tests")
class UpdateOneIntegrationTest extends AbstractCrudIntegrationTest {

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

        Operation updateOp = Operation.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(updateOp);
        request.arg("entity", updatedUser);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        IOperationResponse response = userCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.OK, response.getResponseCode());
        assertTrue(response.getResponse() instanceof User);

        User result = (User) response.getResponse();
        assertEquals("Alice Updated", result.getName());
        assertEquals("alice.updated@example.com", result.getEmail());
        assertEquals("uuid-alice", result.getUuid(), "UUID should not change");
    }

    @Test
    @DisplayName("updateOne updates entity by id")
    void updateOneById() throws ApiException {
        seedUser("1", "uuid-alice", "Alice", "alice@example.com");

        User updatedUser = new User();
        updatedUser.setName("Alice By Id");

        Operation updateOp = Operation.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(updateOp);
        request.arg("entity", updatedUser);
        request.arg("type", "id");
        request.arg("identifier", "1");

        IOperationResponse response = userCtx.invoke(request);

        assertEquals(OperationResponseCode.OK, response.getResponseCode());
        User result = (User) response.getResponse();
        assertEquals("Alice By Id", result.getName());
    }

    @Test
    @DisplayName("updateOne defaults to uuid type when not specified")
    void updateOneDefaultsToUuid() throws ApiException {
        seedUser("1", "uuid-alice", "Alice", "alice@example.com");

        User updatedUser = new User();
        updatedUser.setName("Alice Default");

        Operation updateOp = Operation.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(updateOp);
        request.arg("entity", updatedUser);
        request.arg("identifier", "uuid-alice");

        IOperationResponse response = userCtx.invoke(request);

        assertEquals(OperationResponseCode.OK, response.getResponseCode());
        User result = (User) response.getResponse();
        assertEquals("Alice Default", result.getName());
    }

    @Test
    @DisplayName("updateOne returns NOT_FOUND when entity does not exist")
    void updateOneReturnsNotFound() throws ApiException {
        User updatedUser = new User();
        updatedUser.setName("Ghost");

        Operation updateOp = Operation.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(updateOp);
        request.arg("entity", updatedUser);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-nonexistent");

        IOperationResponse response = userCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.NOT_FOUND, response.getResponseCode());
    }

    @Test
    @DisplayName("updateOne returns CLIENT_ERROR when no caller is provided")
    void updateOneReturnsBadRequestWhenNoCaller() throws ApiException {
        seedUser("1", "uuid-alice", "Alice", "alice@example.com");

        User updatedUser = new User();
        updatedUser.setName("Alice Updated");

        Operation updateOp = Operation.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = new OperationRequest(new HashMap<>());
        request.arg(IOperationRequest.OPERATION, updateOp);
        request.arg("entity", updatedUser);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        IOperationResponse response = userCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.CLIENT_ERROR, response.getResponseCode());
        assertEquals("No caller provided", response.getResponse());
    }

    @Test
    @DisplayName("updateOne returns CLIENT_ERROR when no entity is provided")
    void updateOneReturnsBadRequestWhenNoEntity() throws ApiException {
        seedUser("1", "uuid-alice", "Alice", "alice@example.com");

        Operation updateOp = Operation.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(updateOp);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        IOperationResponse response = userCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.CLIENT_ERROR, response.getResponseCode());
    }

    @Test
    @DisplayName("updateOne returns SERVER_ERROR when repository throws an exception")
    void updateOneReturnsServerErrorOnRepositoryException() throws ApiException {
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

        Operation updateOp = Operation.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(updateOp);
        request.arg("entity", updatedUser);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        IOperationResponse response = failingUserCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.SERVER_ERROR, response.getResponseCode());
    }

    @Test
    @DisplayName("updateOne persists the updated entity to the DAO")
    void updateOnePersistsUpdatedEntity() throws ApiException {
        seedUser("1", "uuid-alice", "Alice", "alice@example.com");

        User updatedUser = new User();
        updatedUser.setName("Alice Persisted");
        updatedUser.setEmail("persisted@example.com");

        Operation updateOp = Operation.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(updateOp);
        request.arg("entity", updatedUser);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        IOperationResponse response = userCtx.invoke(request);

        assertEquals(OperationResponseCode.OK, response.getResponseCode());
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
