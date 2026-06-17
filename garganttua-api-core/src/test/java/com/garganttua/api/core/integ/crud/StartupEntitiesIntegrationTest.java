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

    /** CapturingDao whose save() REPLACES any row with the same uuid — mirrors MongoDB replaceOne by _id (a real upsert), so an in-place update leaves one row, not a duplicate. */
    static class UpsertingDao extends CapturingDao {
        @Override
        public Object save(Object object) throws ApiException {
            String uuid = uuidOf(object);
            if (uuid != null) {
                getStorage().removeIf(row -> uuid.equals(uuidOf(row)));
            }
            return super.save(object);
        }

        private static String uuidOf(Object o) {
            try {
                java.lang.reflect.Field f = o.getClass().getDeclaredField("uuid");
                f.setAccessible(true);
                Object v = f.get(o);
                return v != null ? v.toString() : null;
            } catch (Exception e) {
                return null;
            }
        }
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
    @DisplayName("upsert(...) UPDATES the existing row in place — non-declared/non-updatable data survives (no delete-then-create)")
    void upsertUpdatesInPlacePreservingData() throws ApiException {
        UpsertingDao dao = new UpsertingDao();

        // Previous-run row carrying data the new declaration does NOT touch (email is not declared
        // updatable below — it stands for UI curation a delete-then-create would wipe).
        UserDto existing = new UserDto();
        existing.setUuid("FIXED-X");
        existing.setName("old-name");
        existing.setEmail("curated@example.com");
        existing.setTenantId("SUPER_TENANT");
        dao.getStorage().add(existing);

        User declared = user("FIXED-X", "new-name"); // email left unset

        // Only "name" is updatable.
        buildTenantApi(dao, e -> e.update("name"), d -> d.upsert(declared));

        List<UserDto> rows = rows(dao);
        assertEquals(1, rows.size(), "an in-place update must not duplicate the row");
        assertEquals("FIXED-X", rows.get(0).getUuid(), "the row keeps its identity (no delete-then-create)");
        assertEquals("new-name", rows.get(0).getName(), "the declared updatable field is refreshed");
        assertEquals("curated@example.com", rows.get(0).getEmail(),
                "a field the upsert did not declare updatable must SURVIVE — proving update-in-place, not delete+create");
    }

    @Test
    @DisplayName("upsert(...) matched by a unicity key updates that row in place (was: 409 on the unicity)")
    void upsertMatchedByUnicityUpdatesInPlace() throws ApiException {
        UpsertingDao dao = new UpsertingDao();

        // Existing row keyed by a unique email, under a uuid the new declaration does not carry.
        UserDto existing = new UserDto();
        existing.setUuid("OLD-UUID");
        existing.setName("old-name");
        existing.setEmail("ref@example.com");
        existing.setTenantId("SUPER_TENANT");
        dao.getStorage().add(existing);

        // Re-declared keyed by the unique email, no (matching) uuid.
        User declared = user(null, "new-name");
        declared.setEmail("ref@example.com");

        assertDoesNotThrow(() ->
                buildTenantApi(dao, e -> {
                    e.unicity("email", com.garganttua.api.commons.entity.annotations.UnicityScope.system);
                    e.update("name");
                }, d -> d.upsert(declared)),
                "an upsert matched by a unique field must update that row, not 409 against it");

        List<UserDto> rows = rows(dao);
        assertEquals(1, rows.size(), "the reference datum stays a single row");
        assertEquals("OLD-UUID", rows.get(0).getUuid(), "the existing row's identity is preserved");
        assertEquals("new-name", rows.get(0).getName(), "the declared value is merged");
        assertEquals("ref@example.com", rows.get(0).getEmail());
    }

    @Test
    @DisplayName("upsert(...) with a stable uuid AND system-scoped unicity updates cleanly (the palliad admin shape)")
    void upsertStableUuidSystemUnicity() throws ApiException {
        UpsertingDao dao = new UpsertingDao();

        // The "admin": a stable uuid, a system-unique id and email (here all = the login value).
        UserDto existing = new UserDto();
        existing.setUuid("ADMIN-UUID");
        existing.setId("admin@palliad.care");
        existing.setName("old-name");
        existing.setEmail("admin@palliad.care");
        existing.setTenantId("SUPER_TENANT");
        dao.getStorage().add(existing);

        User declared = user("ADMIN-UUID", "new-name");
        declared.setId("admin@palliad.care");
        declared.setEmail("admin@palliad.care");

        assertDoesNotThrow(() ->
                buildTenantApi(dao, e -> {
                    e.unicity("id", com.garganttua.api.commons.entity.annotations.UnicityScope.system);
                    e.unicity("email", com.garganttua.api.commons.entity.annotations.UnicityScope.system);
                    e.update("name");
                }, d -> d.upsert(declared)),
                "re-upserting the admin must refresh it in place, not 409 on its own id/email unicity");

        List<UserDto> rows = rows(dao);
        assertEquals(1, rows.size());
        assertEquals("ADMIN-UUID", rows.get(0).getUuid());
        assertEquals("new-name", rows.get(0).getName());
    }
}
