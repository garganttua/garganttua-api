package com.garganttua.api.core.integ.crud;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.IEntityBuilder;
import com.garganttua.core.reflection.IClass;

/**
 * Proves that declared startup entities ({@code .create(...)} / {@code .upsert(...)})
 * are materialized <strong>through the create pipeline</strong> at {@code onStart()},
 * not via a blind repository write:
 *
 *  <ul>
 *    <li>a missing uuid is filled by {@code ensureUuid} (v7, or the domain's custom generator);</li>
 *    <li>a mandatory-field violation is rejected by the pipeline (the row never lands);</li>
 *    <li>{@code upsert(...)} with no uuid no longer throws "has no UUID" — it is simply created;</li>
 *    <li>{@code upsert(...)} of an already-present uuid replaces the existing row.</li>
 *  </ul>
 */
@DisplayName("Startup entities go through the pipeline")
class StartupEntitiesIntegrationTest extends AbstractCrudScriptTest {

    @FunctionalInterface
    private interface EntityCfg {
        void apply(IEntityBuilder<User> entity) throws ApiException;
    }

    @FunctionalInterface
    private interface DomainCfg {
        void apply(IDomainBuilder<User> domain) throws ApiException;
    }

    @SuppressWarnings("unchecked")
    private IApi buildTenantApi(CapturingDao dao, EntityCfg entityCfg, DomainCfg domainCfg) throws ApiException {
        IApiBuilder builder = newBuilder();
        IEntityBuilder<User> entity = (IEntityBuilder<User>) builder
                .domain(IClass.getClass(User.class))
                    .tenant(true)
                    .superTenant("superTenant")
                    .entity()
                        .id("id").uuid("uuid").tenantId("tenantId")
                        .mandatory("name");
        entityCfg.apply(entity);
        IDomainBuilder<User> domain = entity.up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(dao)
                .up()
                .creation(true).readAll(true);
        domainCfg.apply(domain);
        domain.up();
        return buildAndStart(builder);
    }

    private static User user(String uuid, String name) {
        User u = new User();
        u.setUuid(uuid);
        u.setName(name);
        u.setTenantId("SUPER_TENANT");
        return u;
    }

    private static List<UserDto> rows(CapturingDao dao) {
        return dao.getStorage().stream()
                .filter(UserDto.class::isInstance)
                .map(UserDto.class::cast)
                .toList();
    }

    @Test
    @DisplayName("create(...) with no uuid: the pipeline fills a time-ordered UUID v7")
    void createStartupEntityGetsV7() throws ApiException {
        CapturingDao dao = new CapturingDao();
        User declared = user(null, "Alice");

        buildTenantApi(dao, e -> {}, d -> d.create(declared));

        List<UserDto> rows = rows(dao);
        assertEquals(1, rows.size(), "the declared startup entity must have been persisted");
        UserDto persisted = rows.get(0);
        assertEquals("Alice", persisted.getName());
        assertNotNull(persisted.getUuid(), "the pipeline must have assigned a uuid");
        UUID parsed = assertDoesNotThrow(() -> UUID.fromString(persisted.getUuid()));
        assertEquals(7, parsed.version(), "startup create must use the same v7 ensureUuid as a client create");
    }

    @Test
    @DisplayName("create(...) honors the domain's custom uuidGenerator (proves the pipeline ran)")
    void createStartupEntityUsesCustomGenerator() throws ApiException {
        CapturingDao dao = new CapturingDao();
        User declared = user(null, "Bob");

        buildTenantApi(dao, e -> e.uuidGenerator(entity -> "STARTUP-GEN"), d -> d.create(declared));

        List<UserDto> rows = rows(dao);
        assertEquals(1, rows.size());
        assertEquals("STARTUP-GEN", rows.get(0).getUuid(),
                "the startup path must traverse ensureUuid, which honors the domain uuidGenerator");
    }

    @Test
    @DisplayName("create(...) violating a mandatory field is rejected — the row never lands")
    void createStartupEntityRejectedOnMandatoryViolation() throws ApiException {
        CapturingDao dao = new CapturingDao();
        User declared = user(null, null); // name is @mandatory → must be rejected by the pipeline

        // best-effort create: onStart must NOT fail, but the invalid row must be refused.
        assertDoesNotThrow(() -> buildTenantApi(dao, e -> {}, d -> d.create(declared)));

        assertTrue(rows(dao).isEmpty(),
                "a blind repository.save would have stored the invalid row; the pipeline rejects it");
    }

    @Test
    @DisplayName("upsert(...) with no uuid no longer throws — it is created with a generated v7")
    void upsertStartupEntityWithoutUuidIsCreated() throws ApiException {
        CapturingDao dao = new CapturingDao();
        User declared = user(null, "Charlie");

        // Previously this threw ApiException("Upsert startup entity has no UUID ...").
        assertDoesNotThrow(() -> buildTenantApi(dao, e -> {}, d -> d.upsert(declared)));

        List<UserDto> rows = rows(dao);
        assertEquals(1, rows.size());
        assertEquals("Charlie", rows.get(0).getName());
        UUID parsed = assertDoesNotThrow(() -> UUID.fromString(rows.get(0).getUuid()));
        assertEquals(7, parsed.version());
    }

    @Test
    @DisplayName("upsert(...) of an already-present uuid replaces the existing row through the pipeline")
    void upsertStartupEntityReplacesExistingRow() throws ApiException {
        CapturingDao dao = new CapturingDao();

        // Pre-seed a persisted row at the declared uuid (the "previous run" state).
        UserDto existing = new UserDto();
        existing.setUuid("FIXED-X");
        existing.setName("old-name");
        existing.setTenantId("SUPER_TENANT");
        dao.getStorage().add(existing);

        User declared = user("FIXED-X", "new-name");

        buildTenantApi(dao, e -> {}, d -> d.upsert(declared));

        List<UserDto> rows = rows(dao);
        assertEquals(1, rows.size(), "replace must leave exactly one row at the declared uuid");
        assertEquals("FIXED-X", rows.get(0).getUuid());
        assertEquals("new-name", rows.get(0).getName(),
                "the row must now hold the declared (upserted) value, not the stale one");
    }
}
