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
import com.garganttua.api.spec.service.IOperationResponse;
import com.garganttua.api.spec.service.OperationResponseCode;
import com.garganttua.core.reflection.IClass;

@DisplayName("DeleteOne Integration Tests")
class DeleteOneIntegrationTest extends AbstractCrudIntegrationTest {

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
            .up();

        context = buildAndStart(builder);
        userCtx = context.getDomainContext("users").orElseThrow();
    }

    @Test
    @DisplayName("deleteOne deletes entity by uuid and returns it")
    void deleteOneByUuid() throws ApiException {
        seedUsers("Alice", "Bob");
        assertEquals(2, userDao.getStorage().size());

        OperationDefinition deleteOneOp = OperationDefinition.deleteOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(deleteOneOp);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        IOperationResponse response = userCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.OK, response.getResponseCode());
        assertTrue(response.getResponse() instanceof User);

        User deleted = (User) response.getResponse();
        assertEquals("Alice", deleted.getName());

        assertEquals(1, userDao.getStorage().size(), "Only one entity should remain");
    }

    @Test
    @DisplayName("deleteOne deletes entity by id")
    void deleteOneById() throws ApiException {
        seedUsers("Alice");

        OperationDefinition deleteOneOp = OperationDefinition.deleteOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(deleteOneOp);
        request.arg("type", "id");
        request.arg("identifier", "1");

        IOperationResponse response = userCtx.invoke(request);

        assertEquals(OperationResponseCode.OK, response.getResponseCode());
        assertTrue(response.getResponse() instanceof User);
        assertEquals(0, userDao.getStorage().size());
    }

    @Test
    @DisplayName("deleteOne defaults to uuid type when not specified")
    void deleteOneDefaultsToUuid() throws ApiException {
        seedUsers("Alice");

        OperationDefinition deleteOneOp = OperationDefinition.deleteOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(deleteOneOp);
        request.arg("identifier", "uuid-alice");

        IOperationResponse response = userCtx.invoke(request);

        assertEquals(OperationResponseCode.OK, response.getResponseCode());
        assertTrue(response.getResponse() instanceof User);
        assertEquals(0, userDao.getStorage().size());
    }

    @Test
    @DisplayName("deleteOne returns NOT_FOUND when entity does not exist")
    void deleteOneReturnsNotFound() throws ApiException {
        OperationDefinition deleteOneOp = OperationDefinition.deleteOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(deleteOneOp);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-nonexistent");

        IOperationResponse response = userCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.NOT_FOUND, response.getResponseCode());
    }

    @Test
    @DisplayName("deleteOne returns CLIENT_ERROR when no caller is provided")
    void deleteOneReturnsBadRequestWhenNoCaller() throws ApiException {
        seedUsers("Alice");

        OperationDefinition deleteOneOp = OperationDefinition.deleteOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = new OperationRequest(new HashMap<>());
        request.arg(IOperationRequest.OPERATION, deleteOneOp);

        IOperationResponse response = userCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.CLIENT_ERROR, response.getResponseCode());
        assertEquals("No caller provided", response.getResponse());

        assertEquals(1, userDao.getStorage().size(), "Entity should not have been deleted");
    }

    @Test
    @DisplayName("deleteOne returns SERVER_ERROR when repository throws an exception")
    void deleteOneReturnsServerErrorOnRepositoryException() throws ApiException {
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

        OperationDefinition deleteOneOp = OperationDefinition.deleteOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantRequest(deleteOneOp);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        IOperationResponse response = failingUserCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.SERVER_ERROR, response.getResponseCode());
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
