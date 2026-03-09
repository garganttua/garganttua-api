package com.garganttua.api.core.integ;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.context.OperationRequest;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.context.Operation;
import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.service.IOperationRequest;
import com.garganttua.api.spec.service.IOperationResponse;
import com.garganttua.api.spec.service.OperationResponseCode;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.api.spec.sort.Sort;
import com.garganttua.api.spec.sort.SortDirection;
import com.garganttua.core.reflection.runtime.RuntimeClass;

@DisplayName("ReadAll Integration Tests")
class ReadAllIntegrationTest extends AbstractCrudIntegrationTest {

    private IApiContext context;
    private IDomainContext<?> userCtx;
    private StubDao userDao;

    @BeforeEach
    void setUp() throws ApiException {
        userDao = new StubDao();

        IApiContextBuilder builder = newBuilder();
        builder.domain(RuntimeClass.of(User.class))
                .tenant(true)
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(RuntimeClass.of(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(userDao)
                .up()
            .up();

        context = buildAndStart(builder);
        userCtx = context.getDomainContext("users").orElseThrow();
    }

    @Test
    @DisplayName("invoke readAll workflow returns a successful response")
    void invokeReadAllWorkflow() throws ApiException {
        UserDto alice = new UserDto();
        alice.setId("1");
        alice.setUuid("uuid-alice");
        alice.setTenantId("SUPER_TENANT");
        alice.setName("Alice");
        alice.setEmail("alice@example.com");

        UserDto bob = new UserDto();
        bob.setId("2");
        bob.setUuid("uuid-bob");
        bob.setTenantId("SUPER_TENANT");
        bob.setName("Bob");
        bob.setEmail("bob@example.com");

        UserDto charlie = new UserDto();
        charlie.setId("3");
        charlie.setUuid("uuid-charlie");
        charlie.setTenantId("SUPER_TENANT");
        charlie.setName("Charlie");
        charlie.setEmail("charlie@example.com");

        userDao.getStorage().add(alice);
        userDao.getStorage().add(bob);
        userDao.getStorage().add(charlie);

        Operation readAllOp = Operation.readAllWithStandardSecurity("users", RuntimeClass.of(User.class));
        OperationRequest request = superTenantRequest(readAllOp);

        IOperationResponse response = userCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.OK, response.getResponseCode());

        assertNotNull(response.getResponse());
        assertTrue(response.getResponse() instanceof List);
        List<Object> entities = (List<Object>) response.getResponse();
        assertEquals(3, entities.size());
    }

    @Test
    @DisplayName("readAll returns SERVER_ERROR when repository throws an exception")
    void readAllReturnsServerErrorOnRepositoryException() throws ApiException {
        IApiContextBuilder failingBuilder = newBuilder();

        failingBuilder.domain(RuntimeClass.of(User.class))
                .tenant(true)
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(RuntimeClass.of(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(new FailingDao())
                .up()
            .up();

        IApiContext failingContext = buildAndStart(failingBuilder);
        IDomainContext<?> failingUserCtx = failingContext.getDomainContext("users").orElseThrow();

        Operation readAllOp = Operation.readAllWithStandardSecurity("users", RuntimeClass.of(User.class));
        OperationRequest request = superTenantRequest(readAllOp);

        IOperationResponse response = failingUserCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.SERVER_ERROR, response.getResponseCode());
    }

    @Test
    @DisplayName("readAll returns CLIENT_ERROR when no caller is provided")
    void readAllReturnsBadRequestWhenNoCaller() throws ApiException {
        Operation readAllOp = Operation.readAllWithStandardSecurity("users", RuntimeClass.of(User.class));
        OperationRequest request = new OperationRequest(new HashMap<>());
        request.arg(IOperationRequest.OPERATION, readAllOp);
        // No tenant/caller args provided

        IOperationResponse response = userCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.CLIENT_ERROR, response.getResponseCode());
    }

    @Test
    @DisplayName("readAll passes sort and pageable to the repository DAO")
    void readAllPassesSortAndPageableToDao() throws ApiException {
        CapturingDao capturingDao = new CapturingDao();

        IApiContextBuilder capBuilder = newBuilder();
        capBuilder.domain(RuntimeClass.of(User.class))
                .tenant(true)
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(RuntimeClass.of(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(capturingDao)
                .up()
            .up();

        IApiContext capContext = buildAndStart(capBuilder);
        IDomainContext<?> capUserCtx = capContext.getDomainContext("users").orElseThrow();

        UserDto alice = new UserDto();
        alice.setId("1");
        alice.setUuid("uuid-alice");
        alice.setTenantId("SUPER_TENANT");
        alice.setName("Alice");
        alice.setEmail("alice@example.com");
        capturingDao.getStorage().add(alice);

        ISort sort = new Sort("name", SortDirection.asc);
        IPageable pageable = new IPageable() {
            @Override public int getPageIndex() { return 0; }
            @Override public int getPageSize() { return 10; }
        };

        Operation readAllOp = Operation.readAllWithStandardSecurity("users", RuntimeClass.of(User.class));
        OperationRequest request = superTenantRequest(readAllOp);
        request.arg("sort", sort);
        request.arg("pageable", pageable);

        IOperationResponse response = capUserCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.OK, response.getResponseCode());

        assertNotNull(capturingDao.getLastSort());
        assertTrue(capturingDao.getLastSort().isPresent());
        ISort receivedSort = capturingDao.getLastSort().get();
        assertEquals("name", receivedSort.getFieldName());
        assertEquals(SortDirection.asc, receivedSort.getDirection());

        assertNotNull(capturingDao.getLastPageable());
        assertTrue(capturingDao.getLastPageable().isPresent());
        IPageable receivedPageable = capturingDao.getLastPageable().get();
        assertEquals(0, receivedPageable.getPageIndex());
        assertEquals(10, receivedPageable.getPageSize());
    }
}
