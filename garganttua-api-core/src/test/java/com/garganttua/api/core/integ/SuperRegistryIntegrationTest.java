package com.garganttua.api.core.integ;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.core.integ.crud.AbstractCrudScriptTest;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.workflow.WorkflowResult;

/**
 * Exercises the server-side super-tenant / super-owner registry doctrine
 * (Versant B): startup scan, estampillage of the auto-created master tenant,
 * the independent creation locks (locked by default → runtime promotion
 * rejected), demotion (always allowed, removes from the registry), and the
 * registry's own contract. The per-request server-authoritative override is
 * covered end-to-end by {@code AuthorityIntegrationTest.superTenantBypasses}
 * (positive) and by {@link OverrideStripsForgedClaim} here (negative).
 */
@DisplayName("Super-status registry (scan, estampillage, locks, demotion)")
class SuperRegistryIntegrationTest extends AbstractCrudScriptTest {

    /** Builds a tenant (User) domain. autoCreate seeds the master; lock toggles runtime promotion. */
    private IApi buildTenantApi(boolean autoCreate, boolean lockSuperTenant,
            boolean superTenantUpdatable, CapturingDao dao) throws ApiException {
        IApiBuilder builder = newBuilder();
        builder.superTenantAutoCreate(autoCreate)
               .lockSuperTenantCreation(lockSuperTenant);

        var entity = builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId");
        if (superTenantUpdatable) {
            entity.update("superTenant");
        }
        entity.up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(dao)
                .up()
                .security().disable(true).up()
                .creation(true).readOne(true).readAll(true).update(true)
            .up();

        return buildAndStart(builder);
    }

    private static User user(String uuid, boolean superTenant) {
        User u = new User();
        u.setUuid(uuid);
        u.setName("n-" + uuid);
        u.setTenantId("SUPER_TENANT");
        u.setSuperTenant(superTenant);
        return u;
    }

    private WorkflowResult create(IDomain<?> ctx, User entity) {
        OperationDefinition op = OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(op);
        request.arg("entity", entity);
        return executeScript(ctx, request);
    }

    private WorkflowResult update(IDomain<?> ctx, String uuid, User entity) {
        OperationDefinition op = OperationDefinition.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(op);
        request.arg("type", "uuid");
        request.arg("identifier", uuid);
        request.arg("entity", entity);
        return executeScript(ctx, request);
    }

    @Nested
    @DisplayName("Estampillage + startup scan")
    class ScanAndStamp {

        @Test
        @DisplayName("auto-created master tenant is stamped superTenant=true and registered")
        void masterIsStampedAndRegistered() throws ApiException {
            CapturingDao dao = new CapturingDao();
            IApi api = buildTenantApi(true, true, false, dao);

            assertTrue(api.isSuperTenant("SUPER_TENANT"),
                    "the auto-created master tenant must be registered as a super-tenant");
            assertTrue(api.getSuperTenantIds().contains("SUPER_TENANT"),
                    "registry snapshot must list the master id");

            // The persisted master row carries the stamped flag (self-describing).
            Object persisted = dao.getStorage().stream()
                    .filter(o -> o instanceof UserDto && "SUPER_TENANT".equals(((UserDto) o).getUuid()))
                    .findFirst().orElse(null);
            assertNotNull(persisted, "master tenant must have been persisted");
            assertEquals(Boolean.TRUE, ((UserDto) persisted).getSuperTenant(),
                    "persisted master must be stamped superTenant=true");
        }

        @Test
        @DisplayName("startup scan registers a pre-persisted super-tenant from its field")
        void scanRegistersPrePersistedSuper() throws ApiException {
            CapturingDao dao = new CapturingDao();
            // Build + init (registers repositories) but seed BEFORE start so the
            // scan that runs in onStart() discovers the seeded super-tenant.
            IApiBuilder builder = newBuilder();
            builder.lockSuperTenantCreation(true);
            builder.domain(IClass.getClass(User.class))
                    .tenant(true).superTenant("superTenant")
                    .entity().id("id").uuid("uuid").tenantId("tenantId").up()
                    .dto(IClass.getClass(UserDto.class))
                        .id("id").uuid("uuid").tenantId("tenantId").db(dao)
                    .up()
                    .security().disable(true).up()
                    .readAll(true)
                .up();
            IApi api = builder.build();
            api.onInit();

            UserDto seeded = new UserDto();
            seeded.setUuid("SCANNED_REALM");
            seeded.setTenantId("SCANNED_REALM");
            seeded.setName("seed");
            seeded.setSuperTenant(true);
            dao.getStorage().add(seeded);

            UserDto plain = new UserDto();
            plain.setUuid("PLAIN_REALM");
            plain.setTenantId("PLAIN_REALM");
            plain.setName("plain");
            plain.setSuperTenant(false);
            dao.getStorage().add(plain);

            api.onStart();

            assertTrue(api.isSuperTenant("SCANNED_REALM"),
                    "scan must register a tenant whose superTenant field is true");
            assertFalse(api.isSuperTenant("PLAIN_REALM"),
                    "scan must NOT register a tenant whose superTenant field is false");
        }
    }

    @Nested
    @DisplayName("Creation lock")
    class CreationLock {

        @Test
        @DisplayName("locked by default: creating a super-tenant at runtime is rejected, registry untouched")
        void lockedRejectsPromotion() throws ApiException {
            CapturingDao dao = new CapturingDao();
            IApi api = buildTenantApi(false, true, false, dao);
            IDomain<?> ctx = api.getDomain("users").orElseThrow();

            WorkflowResult result = create(ctx, user("LOCKED_PROMO", true));

            assertFalse(result.isSuccess(),
                    "a locked super-tenant promotion must be rejected; code=" + result.code());
            assertEquals(403, result.code(),
                    "lock rejection must surface as 403; vars=" + result.variables());
            assertFalse(api.isSuperTenant("LOCKED_PROMO"),
                    "a rejected promotion must NOT enter the registry");
        }

        @Test
        @DisplayName("a normal (non-super) tenant is created fine while locked and is not registered")
        void lockedAllowsNormalTenant() throws ApiException {
            CapturingDao dao = new CapturingDao();
            IApi api = buildTenantApi(false, true, false, dao);
            IDomain<?> ctx = api.getDomain("users").orElseThrow();

            WorkflowResult result = create(ctx, user("NORMAL_TENANT", false));

            assertTrue(result.isSuccess(), "a normal tenant must be creatable while the lock is on");
            assertFalse(api.isSuperTenant("NORMAL_TENANT"), "a non-super tenant must not be registered");
        }

        @Test
        @DisplayName("unlocked: creating a super-tenant succeeds and registers it")
        void unlockedRegistersPromotion() throws ApiException {
            CapturingDao dao = new CapturingDao();
            IApi api = buildTenantApi(false, false, false, dao);
            IDomain<?> ctx = api.getDomain("users").orElseThrow();

            assertFalse(api.isSuperTenant("OPEN_PROMO"), "precondition: not yet registered");

            WorkflowResult result = create(ctx, user("OPEN_PROMO", true));

            assertTrue(result.isSuccess(),
                    "unlocked super-tenant creation must succeed; code=" + result.code()
                            + " vars=" + result.variables());
            assertTrue(api.isSuperTenant("OPEN_PROMO"),
                    "a successful super-tenant creation must register the id (sync)");
        }
    }

    @Nested
    @DisplayName("Demotion")
    class Demotion {

        @Test
        @DisplayName("updating a super-tenant to superTenant=false removes it from the registry even when locked")
        void demotionRemovesEvenWhenLocked() throws ApiException {
            CapturingDao dao = new CapturingDao();
            // Locked + master auto-created (registered) + superTenant updatable.
            IApi api = buildTenantApi(true, true, true, dao);
            IDomain<?> ctx = api.getDomain("users").orElseThrow();
            assertTrue(api.isSuperTenant("SUPER_TENANT"), "precondition: master is registered");

            WorkflowResult result = update(ctx, "SUPER_TENANT", user("SUPER_TENANT", false));

            assertTrue(result.isSuccess(),
                    "demotion (super→normal) must be allowed even while the lock is on; code=" + result.code()
                            + " vars=" + result.variables());
            assertFalse(api.isSuperTenant("SUPER_TENANT"),
                    "demotion must remove the id from the super-tenant registry (sync)");
        }
    }

    @Nested
    @DisplayName("Registry contract + locks")
    class RegistryContract {

        @Test
        @DisplayName("register / unregister / membership work for tenants and owners independently")
        void directRegistryApi() throws ApiException {
            IApi api = buildTenantApi(false, true, false, new CapturingDao());

            api.registerSuperOwner("O1");
            assertTrue(api.isSuperOwner("O1"));
            assertFalse(api.isSuperTenant("O1"), "owner registry must be independent of the tenant registry");
            assertTrue(api.getSuperOwnerIds().contains("O1"));

            api.unregisterSuperOwner("O1");
            assertFalse(api.isSuperOwner("O1"), "unregister must drop the id");

            // null-safety
            assertFalse(api.isSuperTenant(null));
            assertFalse(api.isSuperOwner(null));
            api.registerSuperTenant(null); // no-op, must not throw
            api.registerSuperTenant("   "); // blank no-op
            assertFalse(api.getSuperTenantIds().contains("   "));
        }

        @Test
        @DisplayName("both creation locks are on by default")
        void locksLockedByDefault() throws ApiException {
            IApi api = buildTenantApi(false, true, false, new CapturingDao());
            assertTrue(api.isSuperTenantCreationLocked(), "super-tenant creation must be locked by default");
            assertTrue(api.isSuperOwnerCreationLocked(), "super-owner creation must be locked by default");
        }
    }
}
