package com.garganttua.api.core.integ.entityscan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.dto.annotations.Dto;
import com.garganttua.api.commons.dto.annotations.DtoId;
import com.garganttua.api.commons.dto.annotations.DtoTenantId;
import com.garganttua.api.commons.dto.annotations.DtoUuid;
import com.garganttua.api.commons.entity.annotations.Entity;
import com.garganttua.api.commons.entity.annotations.EntityHiddenable;
import com.garganttua.api.commons.entity.annotations.EntityId;
import com.garganttua.api.commons.entity.annotations.EntityPublic;
import com.garganttua.api.commons.entity.annotations.EntitySuperTenant;
import com.garganttua.api.commons.entity.annotations.EntityTenant;
import com.garganttua.api.commons.entity.annotations.EntityTenantId;
import com.garganttua.api.commons.entity.annotations.EntityUuid;
import com.garganttua.api.commons.operation.BusinessOperation;
import com.garganttua.api.core.api.Api;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.core.reflection.IClass;

@DisplayName("@Entity / @Dto auto-detection wires the domain via the DSL")
class EntityAnnotationScanTest extends AbstractCrudIntegrationTest {

    // ── Tenant entity (auto-detected) ─────────────────────────────────────
    @Entity
    @EntityTenant
    public static class AutoTenant {
        @EntityId private String id;
        @EntityUuid private String uuid;
        @EntityTenantId private String tenantId;
        @EntitySuperTenant private Boolean superTenant;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
    }

    @Dto(entityClass = AutoTenant.class)
    public static class AutoTenantDto {
        @DtoId private String id;
        @DtoUuid private String uuid;
        @DtoTenantId private String tenantId;
        private Boolean superTenant;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
    }

    // ── Public hiddenable entity (auto-detected, with type-level marker) ──
    @Entity(creation = false, deleteAll = false)
    @EntityPublic
    @EntityHiddenable(hidden = "hidden")
    public static class AutoPublicHiddenable {
        @EntityId private String id;
        @EntityUuid private String uuid;
        @EntityTenantId private String tenantId;
        private Boolean hidden = false;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public Boolean getHidden() { return hidden; }
        public void setHidden(Boolean hidden) { this.hidden = hidden; }
    }

    @Dto(entityClass = AutoPublicHiddenable.class)
    public static class AutoPublicHiddenableDto {
        @DtoId private String id;
        @DtoUuid private String uuid;
        @DtoTenantId private String tenantId;
        private Boolean hidden = false;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public Boolean getHidden() { return hidden; }
        public void setHidden(Boolean hidden) { this.hidden = hidden; }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IDomain<?> findDomain(IApi api, Class<?> entityClass) {
        Map<String, IDomain<?>> domains = ((Api) api).getDomains();
        return domains.values().stream()
                .filter(d -> d.getEntityClass().represents(entityClass))
                .findFirst().orElse(null);
    }

    @Nested
    @DisplayName("happy path")
    class HappyPath {

        @Test
        @DisplayName("scanner registers both auto-detected entities as domains with the correct DTOs")
        void registersDomains() throws ApiException {
            IApiBuilder builder = newBuilder();
            ((com.garganttua.api.core.api.ApiBuilder) builder).withPackage("com.garganttua.api.core.integ.entityscan");
            ((com.garganttua.core.dsl.IAutomaticBuilder<?, ?>) builder).autoDetect(true);

            // The scanner builds the domain shape but doesn't supply a DAO; we
            // re-enter each domain after auto-detect to set the DAO via the same
            // DTO builder.
            builder.domain(IClass.getClass(AutoTenant.class))
                    .dto(IClass.getClass(AutoTenantDto.class))
                        .db(new CapturingDao())
                    .up()
                .up();
            builder.domain(IClass.getClass(AutoPublicHiddenable.class))
                    .dto(IClass.getClass(AutoPublicHiddenableDto.class))
                        .db(new CapturingDao())
                    .up()
                .up();

            IApi api = buildAndStart(builder);

            IDomain<?> tenantDomain = findDomain(api, AutoTenant.class);
            assertNotNull(tenantDomain, "AutoTenant domain should be registered");
            assertTrue(tenantDomain.isTenantEntity(), "AutoTenant must be marked tenant via @EntityTenant");

            IDomain<?> publicHiddenableDomain = findDomain(api, AutoPublicHiddenable.class);
            assertNotNull(publicHiddenableDomain, "AutoPublicHiddenable domain should be registered");
            assertTrue(publicHiddenableDomain.isPublicEntity(), "AutoPublicHiddenable must be marked public via @EntityPublic");

            // CRUD flags from @Entity(creation=false, deleteAll=false): inspect operation labels
            var ops = publicHiddenableDomain.getDomainDefinition().operations().stream()
                    .map(o -> {
                        BusinessOperation bo = o.getBusinessOperation();
                        return bo != null ? bo.getLabel() : "";
                    })
                    .toList();
            assertTrue(!ops.contains(BusinessOperation.create.getLabel()),
                    "create op must NOT be registered when creation=false; ops=" + ops);
            assertTrue(!ops.contains(BusinessOperation.deleteAll.getLabel()),
                    "deleteAll op must NOT be registered when deleteAll=false; ops=" + ops);
            assertTrue(ops.contains(BusinessOperation.readOne.getLabel()),
                    "readOne op should be registered (default true); ops=" + ops);
        }

        @Test
        @DisplayName("hiddenable field address is captured from @EntityHiddenable(hidden=...)")
        void hiddenableWired() throws ApiException {
            IApiBuilder builder = newBuilder();
            ((com.garganttua.api.core.api.ApiBuilder) builder).withPackage("com.garganttua.api.core.integ.entityscan");
            ((com.garganttua.core.dsl.IAutomaticBuilder<?, ?>) builder).autoDetect(true);
            builder.domain(IClass.getClass(AutoTenant.class))
                    .dto(IClass.getClass(AutoTenantDto.class)).db(new CapturingDao()).up().up();
            builder.domain(IClass.getClass(AutoPublicHiddenable.class))
                    .dto(IClass.getClass(AutoPublicHiddenableDto.class)).db(new CapturingDao()).up().up();

            IApi api = buildAndStart(builder);
            IDomain<?> domain = findDomain(api, AutoPublicHiddenable.class);
            assertNotNull(domain);
            var hiddenable = domain.getDomainDefinition().hiddenable();
            assertNotNull(hiddenable, "hiddenable field should have been captured");
            assertEquals("hidden", hiddenable.getElement(0),
                    "hiddenable address should target 'hidden', got: " + hiddenable);
        }
    }

    @Nested
    @DisplayName("guards")
    class Guards {

        @Test
        @DisplayName("autoDetect without packages: scanner is a no-op, no domains auto-registered")
        void autoDetectNoPackagesIsNoOp() throws ApiException {
            IApiBuilder builder = newBuilder();
            ((com.garganttua.core.dsl.IAutomaticBuilder<?, ?>) builder).autoDetect(true);
            builder.domain(IClass.getClass(User.class))
                    .tenant(true)
                    .superTenant("superTenant")
                    .entity().id("id").uuid("uuid").tenantId("tenantId").up()
                    .dto(IClass.getClass(UserDto.class))
                        .id("id").uuid("uuid").tenantId("tenantId")
                        .db(new CapturingDao())
                    .up()
                .up();

            IApi api = buildAndStart(builder);
            assertEquals(1, ((Api) api).getDomains().size(),
                    "Without packages the scanner must not auto-register anything");
        }

        @Test
        @DisplayName("withPackage but no autoDetect: scanner is not invoked")
        void noAutoDetectIsNoOp() throws ApiException {
            IApiBuilder builder = newBuilder();
            ((com.garganttua.api.core.api.ApiBuilder) builder).withPackage("com.garganttua.api.core.integ.entityscan");
            builder.domain(IClass.getClass(User.class))
                    .tenant(true)
                    .superTenant("superTenant")
                    .entity().id("id").uuid("uuid").tenantId("tenantId").up()
                    .dto(IClass.getClass(UserDto.class))
                        .id("id").uuid("uuid").tenantId("tenantId")
                        .db(new CapturingDao())
                    .up()
                .up();

            IApi api = buildAndStart(builder);
            assertEquals(1, ((Api) api).getDomains().size(),
                    "Without autoDetect the scanner must not run");
        }
    }
}
