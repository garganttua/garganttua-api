package com.garganttua.api.core.integ.crud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.pageable.Pageable;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.Page;
import com.garganttua.api.core.expression.CrudExpressions;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.workflow.WorkflowResult;

/**
 * Field-projection ("select") on reads through the pipeline: a read carrying a {@code PROJECTION}
 * arg yields sparse maps with ONLY the requested entity fields. Exercised with the in-memory
 * {@link CapturingDao} (3-arg find only — so the DB-pushdown is a no-op and this isolates the
 * post-fetch output shaping, layer a), proving the {@code .gs} wiring
 * (effectiveDaoProjection / getEntitiesProjected / applyProjection).
 */
@DisplayName("Read projection (select) — sparse field output")
class ReadProjectionIntegrationTest extends AbstractCrudScriptTest {

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
        seedUsers();
    }

    private OperationRequest readAll() {
        return superTenantScriptRequest(
                OperationDefinition.readAllWithStandardSecurity("users", IClass.getClass(User.class)));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("readAll with select=[name] yields one sparse map per row carrying only 'name'")
    void readAllProjectsSingleField() throws ApiException {
        OperationRequest request = readAll();
        request.arg("projection", List.of("name"));

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess(), () -> "failed with code " + result.code());
        List<Object> rows = (List<Object>) result.output();
        assertEquals(3, rows.size());
        for (Object row : rows) {
            Map<String, Object> map = assertInstanceOf(Map.class, row);
            assertEquals(Set.of("name"), map.keySet(), "only the requested field is present");
            assertFalse(map.containsKey("email"), "email must NOT leak");
            assertFalse(map.containsKey("uuid"), "uuid must NOT leak (it was not requested)");
        }
        Set<Object> names = rows.stream().map(r -> ((Map<String, Object>) r).get("name")).collect(Collectors.toSet());
        assertEquals(Set.of("Alice", "Bob", "Charlie"), names, "the actual field values are projected");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("readAll with select=[name,email] yields maps carrying exactly those two keys with the right values")
    void readAllProjectsTwoFields() throws ApiException {
        OperationRequest request = readAll();
        request.arg("projection", List.of("name", "email"));

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess(), () -> "failed with code " + result.code());
        List<Object> rows = (List<Object>) result.output();
        Map<Object, Object> byName = rows.stream()
                .map(r -> (Map<String, Object>) r)
                .collect(Collectors.toMap(m -> m.get("name"), m -> m.get("email")));
        assertEquals(Set.of("name", "email"), ((Map<String, Object>) rows.get(0)).keySet());
        assertEquals("alice@example.com", byName.get("Alice"));
        assertEquals("bob@example.com", byName.get("Bob"));
        assertEquals("charlie@example.com", byName.get("Charlie"));
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("projection composes with pagination: the Page wraps sparse maps, totalCount intact")
    void projectionWithPagination() throws ApiException {
        OperationRequest request = readAll();
        request.arg("projection", List.of("name"));
        request.arg(IOperationRequest.PAGE, new Pageable(0, 2));

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess(), () -> "failed with code " + result.code());
        Page page = assertInstanceOf(Page.class, result.output());
        assertEquals(3, page.totalCount(), "totalCount reflects the full match count, not the page size");
        assertEquals(2, page.entities().size(), "the page holds pageSize rows");
        assertEquals(Set.of("name"), ((Map<String, Object>) page.entities().get(0)).keySet());
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("mode=uuid wins over projection: output stays a list of uuid strings")
    void uuidModeIgnoresProjection() throws ApiException {
        OperationRequest request = readAll();
        request.arg("projection", List.of("name"));
        request.arg(IOperationRequest.MODE, "uuid");

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess(), () -> "failed with code " + result.code());
        List<Object> rows = (List<Object>) result.output();
        assertEquals(Set.of("uuid-alice", "uuid-bob", "uuid-charlie"),
                rows.stream().collect(Collectors.toSet()), "uuid mode reduces to uuid strings, projection ignored");
    }

    @Test
    @DisplayName("an unknown projected field is rejected with 400")
    void unknownFieldIs400() throws ApiException {
        OperationRequest request = readAll();
        request.arg("projection", List.of("nope"));

        WorkflowResult result = executeScript(userCtx, request);

        assertFalse(result.isSuccess());
        assertEquals(400, result.code(), "an unknown projected field is a client error");
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("no projection → full entities (regression: default behavior preserved)")
    void noProjectionReturnsFullEntities() throws ApiException {
        WorkflowResult result = executeScript(userCtx, readAll());

        assertTrue(result.isSuccess());
        List<Object> rows = (List<Object>) result.output();
        assertEquals(3, rows.size());
        for (Object row : rows) {
            assertInstanceOf(User.class, row, "without a projection the full entity is returned");
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("readOne with select=[email] yields a single sparse map carrying only 'email'")
    void readOneProjectsSingleField() throws ApiException {
        OperationRequest request = superTenantScriptRequest(
                OperationDefinition.readOneWithStandardSecurity("users", IClass.getClass(User.class)));
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");
        request.arg("projection", List.of("email"));

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess(), () -> "failed with code " + result.code());
        Map<String, Object> map = assertInstanceOf(Map.class, result.output());
        assertEquals(Set.of("email"), map.keySet());
        assertEquals("alice@example.com", map.get("email"));
    }

    @Test
    @DisplayName("DB-pushdown gate: pushed for a clean domain, skipped when a server hook could need other fields")
    void daoProjectionGate() throws ApiException {
        // Clean domain (no afterGet / injection / compositions): the projection IS pushed to the DAO.
        assertEquals(List.of("name", "email"),
                CrudExpressions.effectiveDaoProjection(userCtx, List.of("name", "email")),
                "a clean domain pushes the requested fields down to the DAO");

        // Injection ON: a server-side injector could read a non-requested field, so DB-level projection
        // must be skipped (full fetch); the output shaping (sparse maps) still applies downstream.
        IApiBuilder b = newBuilder();
        b.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .doInjection(true)
                .entity().id("id").uuid("uuid").tenantId("tenantId").up()
                .dto(IClass.getClass(UserDto.class)).id("id").uuid("uuid").tenantId("tenantId").db(new CapturingDao()).up()
                .security().disable(true).up()
            .up();
        IDomain<?> injecting = buildAndStart(b).getDomain("users").orElseThrow();

        assertTrue(((List<?>) CrudExpressions.effectiveDaoProjection(injecting, List.of("name"))).isEmpty(),
                "injection on → DB-level projection is skipped, so the DAO is never starved of hook inputs");
    }

    private void seedUsers() {
        userDao.getStorage().add(dto("1", "uuid-alice", "Alice", "alice@example.com"));
        userDao.getStorage().add(dto("2", "uuid-bob", "Bob", "bob@example.com"));
        userDao.getStorage().add(dto("3", "uuid-charlie", "Charlie", "charlie@example.com"));
    }

    private static UserDto dto(String id, String uuid, String name, String email) {
        UserDto d = new UserDto();
        d.setId(id);
        d.setUuid(uuid);
        d.setTenantId("SUPER_TENANT");
        d.setName(name);
        d.setEmail(email);
        return d;
    }
}
