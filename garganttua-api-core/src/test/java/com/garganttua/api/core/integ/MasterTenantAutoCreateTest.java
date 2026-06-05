package com.garganttua.api.core.integ;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.core.reflection.IClass;

/**
 * End-to-end verification of {@code Api.autoCreateMasterTenant}. The class is
 * non-trivial — it spans the workflow pipeline, repository, and reflection —
 * but previously had zero integration coverage, which is exactly why the
 * misleading "Workflow returned code 404" WARN log went unnoticed.
 */
@DisplayName("Master tenant auto-create")
class MasterTenantAutoCreateTest extends AbstractCrudIntegrationTest {

    private static final String MASTER = "MASTER";

    private IApiBuilder buildBuilderWithTenantDomain(CapturingDao dao, boolean autoCreate) throws ApiException {
        IApiBuilder builder = newBaseBuilder();
        builder.multiTenant(true)
               .superTenantId(MASTER)
               .superTenantAutoCreate(autoCreate);

        builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(dao)
                .up()
            .up();

        return builder;
    }

    private static String uuid(Object dto) {
        try {
            Field f = dto.getClass().getDeclaredField("uuid");
            f.setAccessible(true);
            Object value = f.get(dto);
            return value == null ? null : value.toString();
        } catch (Exception e) {
            throw new AssertionError("Could not read uuid field on " + dto.getClass().getName(), e);
        }
    }

    @Nested
    @DisplayName("Empty DAO")
    class EmptyDao {

        @Test
        @DisplayName("auto-creates the master tenant in the repository after onStart()")
        void masterTenantLandsInDao() throws ApiException {
            CapturingDao dao = new CapturingDao();
            IApi api = buildBuilderWithTenantDomain(dao, true).build();
            api.onInit();
            api.onStart();

            List<Object> storage = dao.getStorage();
            assertEquals(1, storage.size(),
                    "exactly one master tenant should have been persisted; got " + storage.size()
                            + " entries: " + storage);
            Object created = storage.get(0);
            assertNotNull(created, "stored entity must not be null");
            assertEquals(MASTER, uuid(created),
                    "stored entity uuid should equal the configured superTenantId");
        }
    }

    @Nested
    @DisplayName("DAO already has the master tenant")
    class PrePopulated {

        @Test
        @DisplayName("skips creation — no duplicate, storage stays at one")
        void doesNotDuplicate() throws ApiException {
            CapturingDao dao = new CapturingDao();
            // Pre-seed with a dto whose uuid matches MASTER. doesExist queries the
            // DAO via the DTO's uuid field, so we use UserDto here.
            UserDto existing = new UserDto();
            existing.setId(MASTER);
            existing.setUuid(MASTER);
            existing.setTenantId(MASTER);
            existing.setName("Pre-existing master");
            dao.save(existing);

            IApi api = buildBuilderWithTenantDomain(dao, true).build();
            api.onInit();
            api.onStart();

            List<Object> storage = dao.getStorage();
            assertEquals(1, storage.size(),
                    "auto-create should have detected the existing master tenant and skipped; "
                            + "storage now has " + storage.size() + " entries: " + storage);
            assertTrue(storage.contains(existing),
                    "the pre-seeded master tenant must still be the one in storage");
        }
    }

    @Nested
    @DisplayName("Auto-create disabled")
    class AutoCreateOff {

        @Test
        @DisplayName("does nothing — storage stays empty")
        void noTenantCreated() throws ApiException {
            CapturingDao dao = new CapturingDao();
            IApi api = buildBuilderWithTenantDomain(dao, false).build();
            api.onInit();
            api.onStart();

            assertEquals(0, dao.getStorage().size(),
                    "with superTenantAutoCreate(false) the DAO must stay empty; got " + dao.getStorage());
        }
    }

    @Nested
    @DisplayName("Tenant domain with a .security() block")
    class SecurityEnabledTenantDomain {

        @Test
        @DisplayName("auto-create still succeeds when the tenant domain has .security() configured")
        void autoCreateWorksOnSecuredDomain() throws ApiException {
            // The user-facing trap from the tenant-domain-security bug report:
            // simply calling .security() on the tenant domain enables the full
            // security pipeline (VERIFY_AUTHORIZATION) on every CRUD op,
            // including the CREATE used by autoCreateMasterTenant — which
            // would otherwise fail with 401 because there is no authorization
            // at bootstrap time.
            //
            // The fix (2026-05-18 security flaw remediation):
            // autoCreateMasterTenant writes directly via repository.save(...),
            // bypassing Domain.invoke and therefore the workflow pipeline
            // entirely. The security stage never runs. This intentionally also
            // means @EntityBeforeCreate / @EntityAfterCreate hooks do NOT fire
            // for the master tenant — it is a system bootstrap, not a normal
            // create.
            //
            // This test pins that contract: a tenant domain configured with
            // .security() still receives its auto-created master row.
            CapturingDao dao = new CapturingDao();
            IApiBuilder builder = newBaseBuilder();
            builder.multiTenant(true)
                   .superTenantId(MASTER)
                   .superTenantAutoCreate(true);

            builder.domain(IClass.getClass(User.class))
                    .tenant(true)
                    .superTenant("superTenant")
                    .entity().id("id").uuid("uuid").tenantId("tenantId").up()
                    .dto(IClass.getClass(UserDto.class))
                        .id("id").uuid("uuid").tenantId("tenantId")
                        .db(dao)
                    .up()
                    .security()
                        .readAllAccess(Access.anonymous)   // enables the security pipeline
                        .up()
                .up();

            IApi api = builder.build();
            api.onInit();
            api.onStart();

            assertEquals(1, dao.getStorage().size(),
                    "auto-create must succeed on a security-enabled tenant domain — bootstrap "
                            + "writes via repository.save() and never touches the workflow; "
                            + "got " + dao.getStorage());
            assertEquals(MASTER, uuid(dao.getStorage().get(0)));
        }
    }
}
