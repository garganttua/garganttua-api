package com.garganttua.api.core.integ.crud;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;

import com.garganttua.api.commons.service.Page;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.pageable.IPageable;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.sort.ISort;
import com.garganttua.api.commons.sort.Sort;
import com.garganttua.api.commons.sort.SortDirection;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.workflow.WorkflowResult;

@DisplayName("ReadAll Script Tests")
class ReadAllIntegrationTest extends AbstractCrudScriptTest {

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
    @DisplayName("readAll returns all entities")
    void readAllReturnsEntities() throws ApiException {
        seedUsers();

        OperationDefinition readAllOp = OperationDefinition.readAllWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(readAllOp);

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess());
        assertNotNull(result.output());
        assertTrue(result.output() instanceof List);
        List<Object> entities = (List<Object>) result.output();
        assertEquals(3, entities.size());
    }

    @Test
    @DisplayName("readAll returns 500 when repository throws an exception")
    void readAllReturns500OnRepositoryException() throws ApiException {
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

        OperationDefinition readAllOp = OperationDefinition.readAllWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(readAllOp);

        WorkflowResult result = executeScript(failingUserCtx, request);

        assertFalse(result.isSuccess());
        assertEquals(500, result.code());
    }

    @Test
    @DisplayName("readAll returns 400 when no caller is provided")
    void readAllReturns400WhenNoCaller() throws ApiException {
        OperationDefinition readAllOp = OperationDefinition.readAllWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = new OperationRequest(new HashMap<>());
        request.arg(IOperationRequest.OPERATION, readAllOp);

        WorkflowResult result = executeScript(userCtx, request);

        assertFalse(result.isSuccess());
        assertEquals(400, result.code());
    }

    @Test
    @DisplayName("readAll with mode=uuid returns only uuid values")
    void readAllModeUuidReturnsUuids() throws ApiException {
        seedUsers();

        OperationDefinition readAllOp = OperationDefinition.readAllWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(readAllOp);
        request.arg("mode", "uuid");

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess());
        List<Object> output = (List<Object>) result.output();
        assertEquals(3, output.size());
        assertTrue(output.contains("uuid-alice"));
        assertTrue(output.contains("uuid-bob"));
        assertTrue(output.contains("uuid-charlie"));
    }

    @Test
    @DisplayName("readAll with mode=id returns only id values")
    void readAllModeIdReturnsIds() throws ApiException {
        seedUsers();

        OperationDefinition readAllOp = OperationDefinition.readAllWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(readAllOp);
        request.arg("mode", "id");

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess());
        List<Object> output = (List<Object>) result.output();
        assertEquals(3, output.size());
        assertTrue(output.contains("1"));
        assertTrue(output.contains("2"));
        assertTrue(output.contains("3"));
    }

    @Test
    @DisplayName("readAll with mode=full returns full entity objects")
    void readAllModeFullReturnsEntities() throws ApiException {
        seedUsers();

        OperationDefinition readAllOp = OperationDefinition.readAllWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(readAllOp);
        request.arg("mode", "full");

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess());
        List<Object> output = (List<Object>) result.output();
        assertEquals(3, output.size());
        assertTrue(output.get(0) instanceof User);
    }

    @Test
    @DisplayName("readAll with pageable wraps result in a Page")
    void readAllWithPageableReturnsPage() throws ApiException {
        seedUsers();

        IPageable pageable = new IPageable() {
            @Override public int getPageIndex() { return 0; }
            @Override public int getPageSize() { return 10; }
        };

        OperationDefinition readAllOp = OperationDefinition.readAllWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(readAllOp);
        request.arg("pageable", pageable);

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess());
        assertTrue(result.output() instanceof Page);

        Page page = (Page) result.output();
        assertEquals(3L, page.totalCount());
        assertEquals(3, page.entities().size());
    }

    @Test
    @DisplayName("readAll with pageable actually SLICES (page 1 / size 2 over 5 → entities 3-4), totalCount=5, via the canonical PAGE key")
    void readAllPageableSlices() throws ApiException {
        for (int i = 0; i < 5; i++) {
            UserDto u = new UserDto();
            u.setId(String.valueOf(i));
            u.setUuid("uuid-" + i);
            u.setTenantId("SUPER_TENANT");
            u.setName("User" + i);
            userDao.getStorage().add(u);
        }

        IPageable pageable = new IPageable() {
            @Override public int getPageIndex() { return 1; } // second page (0-based)
            @Override public int getPageSize() { return 2; }
        };

        OperationDefinition readAllOp = OperationDefinition.readAllWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(readAllOp);
        // The CANONICAL key (now "pageable") — proves the DSL/IDomain page path reaches the pipeline.
        request.arg(IOperationRequest.PAGE, pageable);

        WorkflowResult result = executeScript(userCtx, request);
        assertTrue(result.isSuccess(), () -> "readAll failed; vars=" + result.variables());

        Page page = (Page) result.output();
        assertEquals(5L, page.totalCount(), "totalCount is the UNPAGINATED total");
        assertEquals(2, page.entities().size(), "page size 2 -> exactly 2 entities");
        assertEquals("uuid-2", ((User) page.entities().get(0)).getUuid(), "page 1 starts at index 2 (the 3rd entity)");
        assertEquals("uuid-3", ((User) page.entities().get(1)).getUuid(), "and ends at index 3 (the 4th entity)");
    }

    @Test
    @DisplayName("readAll without pageable returns a plain list")
    void readAllWithoutPageableReturnsPlainList() throws ApiException {
        seedUsers();

        OperationDefinition readAllOp = OperationDefinition.readAllWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(readAllOp);

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess());
        assertTrue(result.output() instanceof List);
    }

    @Test
    @DisplayName("readAll with pageable and mode=uuid wraps uuids in a Page")
    void readAllPageableWithModeUuid() throws ApiException {
        seedUsers();

        IPageable pageable = new IPageable() {
            @Override public int getPageIndex() { return 0; } // first page; size 5 holds all 3
            @Override public int getPageSize() { return 5; }
        };

        OperationDefinition readAllOp = OperationDefinition.readAllWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(readAllOp);
        request.arg("pageable", pageable);
        request.arg("mode", "uuid");

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess());
        assertTrue(result.output() instanceof Page);

        Page page = (Page) result.output();
        assertEquals(3L, page.totalCount());
        assertEquals(3, page.entities().size());
        assertTrue(page.entities().contains("uuid-alice"));
        assertTrue(page.entities().contains("uuid-bob"));
        assertTrue(page.entities().contains("uuid-charlie"));
    }

    @Test
    @DisplayName("readAll passes sort and pageable to the repository DAO")
    void readAllPassesSortAndPageableToDao() throws ApiException {
        CapturingDao capturingDao = new CapturingDao();

        IApiBuilder capBuilder = newBuilder();
        capBuilder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(capturingDao)
                .up()
                .security().disable(true).up()
            .up();

        IApi capContext = buildAndStart(capBuilder);
        IDomain<?> capUserCtx = capContext.getDomain("users").orElseThrow();

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

        OperationDefinition readAllOp = OperationDefinition.readAllWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(readAllOp);
        request.arg("sort", sort);
        request.arg("pageable", pageable);

        WorkflowResult result = executeScript(capUserCtx, request);

        assertTrue(result.isSuccess());

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

    private void seedUsers() {
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
    }
}
