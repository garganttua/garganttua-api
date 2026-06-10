package com.garganttua.api.core.integ;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.api.core.service.RequestBuilder;
import com.garganttua.core.mapper.annotations.FieldMappingRule;
import com.garganttua.core.reflection.IClass;

/**
 * A tenant entity (domain marked {@code .tenant(true)}) IS the tenant — its
 * uuid identifies it. It must not be required to also carry a redundant
 * {@code tenantId} field; downstream filtering must instead match the
 * caller's tenantId against the tenant entity's uuid so a non-super caller
 * only sees its own tenant row (data isolation).
 */
@DisplayName("Tenant entity without tenantId field")
class TenantEntityWithoutTenantIdTest extends AbstractCrudIntegrationTest {

    // ─── Local fixtures: a Tenant entity that does NOT carry a tenantId field ───

    public static class TenantEntity {
        private String id;
        private String uuid;
        private String name;
        private Boolean superTenant = false;

        public TenantEntity() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
    }

    public static class TenantEntityDto {
        @FieldMappingRule(sourceFieldAddress = "id")
        private String id;
        @FieldMappingRule(sourceFieldAddress = "uuid")
        private String uuid;
        @FieldMappingRule(sourceFieldAddress = "name")
        private String name;
        @FieldMappingRule(sourceFieldAddress = "superTenant")
        private Boolean superTenant;

        public TenantEntityDto() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
    }

    private static final String MASTER = "MASTER";

    private CapturingDao buildAndStart(IApi[] apiHolder, boolean autoCreate) throws ApiException {
        CapturingDao dao = new CapturingDao();
        IApiBuilder builder = newBaseBuilder();
        builder.multiTenant(true)
               .superTenantId(MASTER)
               .superTenantAutoCreate(autoCreate);

        builder.domain(IClass.getClass(TenantEntity.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity()
                    .id("id").uuid("uuid")            // <-- no .tenantId(...)
                .up()
                .dto(IClass.getClass(TenantEntityDto.class))
                    .id("id").uuid("uuid")            // <-- no .tenantId(...)
                    .db(dao)
                .up()
                .security().disable(true).up()
                .readAll(true).readOne(true)
            .up();

        IApi api = builder.build();
        api.onInit();
        api.onStart();
        apiHolder[0] = api;
        return dao;
    }

    private static String uuidOf(Object o) {
        try {
            Field f = o.getClass().getDeclaredField("uuid");
            f.setAccessible(true);
            Object v = f.get(o);
            return v == null ? null : v.toString();
        } catch (Exception e) {
            throw new AssertionError("Could not read uuid field on " + o.getClass().getName(), e);
        }
    }

    private TenantEntityDto seedTenant(CapturingDao dao, String uuid, String name) {
        TenantEntityDto dto = new TenantEntityDto();
        dto.setId(uuid);
        dto.setUuid(uuid);
        dto.setName(name);
        dao.getStorage().add(dto);
        return dto;
    }

    @Nested
    @DisplayName("Build-time validation")
    class BuildValidation {

        @Test
        @DisplayName("build succeeds for a tenant domain configured without tenantId on entity/dto")
        void buildAcceptsMissingTenantId() throws ApiException {
            IApi[] holder = new IApi[1];
            buildAndStart(holder, /* autoCreate */ false);
            IDomain<?> domain = holder[0].getDomain("tenantentities").orElseThrow();
            assertNull(domain.getEntityDefinition().tenantId(),
                    "tenant entity should have no tenantId address — it is the tenant");
            assertNotNull(domain.getEntityDefinition().uuid(),
                    "tenant entity must still have a uuid field — that's the identity");
        }

        @Test
        @DisplayName("non-tenant domain without tenantId still throws (regression: validation only relaxed for tenant entities)")
        void nonTenantStillRequiresTenantId() throws ApiException {
            IApiBuilder builder = newBuilder();
            builder.domain(IClass.getClass(User.class))
                    .entity().id("id").uuid("uuid").up()   // no .tenantId(...) and NOT marked tenant
                    .dto(IClass.getClass(UserDto.class)).id("id").uuid("uuid").db(new CapturingDao()).up()
                .up();

            // Validation runs at api.build() time (the entity/dto builders' doBuild()
            // is invoked when the parent api builds the domain). Triggering build here.
            try {
                builder.build();
                fail("Building a non-tenant entity without tenantId in multi-tenant mode must still throw");
            } catch (ApiException ok) {
                assertTrue(ok.getMessage().contains("tenantId"),
                        "Validation should reference the missing tenantId; got: " + ok.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("Auto-create flow on a tenant entity without tenantId")
    class AutoCreate {

        @Test
        @DisplayName("auto-create populates the DAO with a row whose uuid equals superTenantId")
        void autoCreateWorksWithoutTenantId() throws ApiException {
            IApi[] holder = new IApi[1];
            CapturingDao dao = buildAndStart(holder, /* autoCreate */ true);
            assertEquals(1, dao.getStorage().size(),
                    "auto-create must still seed exactly one master tenant; got " + dao.getStorage());
            assertEquals(MASTER, uuidOf(dao.getStorage().get(0)),
                    "the auto-created tenant's uuid must equal the configured superTenantId");
        }
    }

    @Nested
    @DisplayName("Data isolation via uuid-based tenant filter")
    class TenantIsolation {

        @Test
        @DisplayName("super-tenant caller bound to MASTER sees only the MASTER tenant (uuid-based filter still applies)")
        void superCallerBoundSeesOnlyItsTenant() throws ApiException {
            IApi[] holder = new IApi[1];
            CapturingDao dao = buildAndStart(holder, /* autoCreate */ true);
            seedTenant(dao, "acme", "Acme Corp");
            seedTenant(dao, "globex", "Globex Inc");
            IDomain<?> domain = holder[0].getDomain("tenantentities").orElseThrow();

            IOperationResponse response = RequestBuilder.builder(domain)
                    .caller(Caller.createSuperCaller(MASTER))
                    .readAll()
                    .build()
                    .execute();

            assertEquals(OperationResponseCode.OK, response.getResponseCode(),
                    "super-tenant readAll should succeed; got " + response.getResponse());
            List<?> payload = (List<?>) response.getResponse();
            // The framework treats a super-tenant *bound to a specific tenantId* as
            // "act as that tenant" (sees only its own row). The "see-all" path is
            // reserved for super-tenant with a null tenantId — that path is
            // currently incompatible with Domain.invoke's caller guard and out of
            // scope for this fix.
            assertEquals(1, payload.size(),
                    "super-tenant bound to MASTER must see only the MASTER row; got " + payload);
            assertEquals(MASTER, uuidOf(payload.get(0)));
        }

        @Test
        @DisplayName("non-super tenant caller sees ONLY its own tenant row (uuid-based filter, data isolation)")
        void tenantCallerSeesOnlyItself() throws ApiException {
            IApi[] holder = new IApi[1];
            CapturingDao dao = buildAndStart(holder, /* autoCreate */ false);
            seedTenant(dao, "acme", "Acme Corp");
            seedTenant(dao, "globex", "Globex Inc");
            IDomain<?> domain = holder[0].getDomain("tenantentities").orElseThrow();

            IOperationResponse response = RequestBuilder.builder(domain)
                    .caller(Caller.createTenantCaller("acme"))
                    .readAll()
                    .build()
                    .execute();

            assertEquals(OperationResponseCode.OK, response.getResponseCode(),
                    "tenant readAll should succeed; got " + response.getResponse());
            List<?> payload = (List<?>) response.getResponse();
            assertEquals(1, payload.size(),
                    "tenant 'acme' must see exactly its own tenant row; got " + payload);
            assertEquals("acme", uuidOf(payload.get(0)),
                    "the visible row must be the caller's own tenant entity (uuid=acme)");
            // Negative check: globex must not leak.
            for (Object row : payload) {
                assertFalse("globex".equals(uuidOf(row)),
                        "globex must NOT appear in acme's view — that's the data-leak gap we're closing");
            }
        }
    }
}
