package com.garganttua.api.core.unit.builder;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.api.ApiBuilder;
import com.garganttua.api.core.domain.DomainBuilder;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.IEntityBuilder;
import com.garganttua.api.commons.dao.IDao;
import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.pageable.IPageable;
import com.garganttua.api.commons.sort.ISort;
import com.garganttua.core.dsl.dependency.IDependentBuilder;
import com.garganttua.core.expression.dsl.ExpressionContextBuilder;
import com.garganttua.core.expression.dsl.IExpressionContextBuilder;
import com.garganttua.core.injection.context.dsl.IInjectionContextBuilder;
import com.garganttua.core.injection.context.dsl.InjectionContextBuilder;
import com.garganttua.core.mapper.annotations.FieldMappingRule;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.dsl.ReflectionBuilder;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.core.reflections.ReflectionsAnnotationScanner;
import com.garganttua.core.runtime.RuntimeContextFactory;

@DisplayName("DomainBuilder Tests")
class DomainBuilderTest {

    // Test entity class
    public static class TestEntity {
        private String id;
        private String uuid;
        private String tenantId;
        private String ownerId;
        private Boolean superTenant = false;
        private Boolean superOwner = false;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getOwnerId() { return ownerId; }
        public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
        public Boolean getSuperOwner() { return superOwner; }
        public void setSuperOwner(Boolean superOwner) { this.superOwner = superOwner; }
    }

    // Test DTO class
    public static class TestDto {
        @FieldMappingRule(sourceFieldAddress = "id")
        private String id;
        @FieldMappingRule(sourceFieldAddress = "uuid")
        private String uuid;
        @FieldMappingRule(sourceFieldAddress = "tenantId")
        private String tenantId;
        @FieldMappingRule(sourceFieldAddress = "superTenant")
        private Boolean superTenant;
        @FieldMappingRule(sourceFieldAddress = "superOwner")
        private Boolean superOwner;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
        public Boolean getSuperOwner() { return superOwner; }
        public void setSuperOwner(Boolean superOwner) { this.superOwner = superOwner; }
    }

    // Simple test DAO
    public static class TestDao implements IDao {
        private final List<Object> storage = new ArrayList<>();

        @Override
        public void registerDomain(IDomainDefinition domainDefinition) {}

        @Override
        public List<Object> find(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort)
                throws ApiException {
            return new ArrayList<>(storage);
        }

        @Override
        public Object save(Object object) throws ApiException {
            storage.add(object);
            return object;
        }

        @Override
        public void delete(Object object) throws ApiException {
            storage.remove(object);
        }

        @Override
        public long count(IFilter filter) throws ApiException {
            return storage.size();
        }
    }

    private IDomainBuilder<TestEntity> domainBuilder;
    private IApiBuilder contextBuilder;

    @BeforeEach
    void setUp() throws ApiException {
        contextBuilder = ApiBuilder.builder();
        domainBuilder = contextBuilder.domain(IClass.getClass(TestEntity.class));
    }

    @Nested
    @DisplayName("Entity Builder Access")
    class EntityBuilderAccess {

        @Test
        @DisplayName("entity() returns entity builder")
        void entityReturnsEntityBuilder() throws ApiException {
            IEntityBuilder<TestEntity> entityBuilder = domainBuilder.entity();
            assertNotNull(entityBuilder);
        }

        @Test
        @DisplayName("entity() returns same builder on multiple calls")
        void entityReturnsSameBuilder() throws ApiException {
            IEntityBuilder<TestEntity> first = domainBuilder.entity();
            IEntityBuilder<TestEntity> second = domainBuilder.entity();
            assertSame(first, second);
        }
    }

    @Nested
    @DisplayName("DTO Builder Access")
    class DtoBuilderAccess {

        @Test
        @DisplayName("dto() returns DTO builder")
        void dtoReturnsDtoBuilder() throws ApiException {
            var dtoBuilder = domainBuilder.dto(IClass.getClass(TestDto.class));
            assertNotNull(dtoBuilder);
        }

        @Test
        @DisplayName("dto() returns same builder for same class")
        void dtoReturnsSameBuilderForSameClass() throws ApiException {
            var first = domainBuilder.dto(IClass.getClass(TestDto.class));
            var second = domainBuilder.dto(IClass.getClass(TestDto.class));
            assertSame(first, second);
        }
    }

    @Nested
    @DisplayName("Tenancy Configuration")
    class TenancyConfiguration {

        @Test
        @DisplayName("tenant() configures tenant flag")
        void tenantConfiguresTenantFlag() throws ApiException {
            IDomainBuilder<TestEntity> result = domainBuilder.tenant(true);
            assertSame(domainBuilder, result);
        }

        @Test
        @DisplayName("publik() configures public flag")
        void publikConfiguresPublicFlag() {
            IDomainBuilder<TestEntity> result = domainBuilder.publik();
            assertSame(domainBuilder, result);
        }

        @Test
        @DisplayName("owner() configures owner field")
        void ownerConfiguresOwnerField() throws ApiException {
            IDomainBuilder<TestEntity> result = domainBuilder.owner("ownerId");
            assertSame(domainBuilder, result);
        }
    }

    @Nested
    @DisplayName("Security Builder Access")
    class SecurityBuilderAccess {

        @Test
        @DisplayName("security() returns security builder")
        void securityReturnsSecurityBuilder() throws ApiException {
            var securityBuilder = domainBuilder.security();
            assertNotNull(securityBuilder);
        }
    }

    @Nested
    @DisplayName("Builder Navigation")
    class BuilderNavigation {

        @Test
        @DisplayName("up() returns parent context builder")
        void upReturnsParentContextBuilder() {
            IApiBuilder parent = domainBuilder.up();
            assertSame(contextBuilder, parent);
        }

        @Test
        @DisplayName("getEntityClass() returns configured class")
        void getEntityClassReturnsConfiguredClass() throws ApiException {
            assertEquals(IClass.getClass(TestEntity.class), domainBuilder.getEntityClass());
        }
    }

    @Nested
    @DisplayName("Domain Build")
    class DomainBuild {

        private IInjectionContextBuilder injectionContextBuilder;
        private IExpressionContextBuilder expressionContextBuilder;

        @BeforeEach
        @SuppressWarnings("unchecked")
        void setUpDependencies() throws ApiException {
            com.garganttua.core.reflection.dsl.IReflectionBuilder reflectionBuilder = ReflectionBuilder.builder()
                    .withProvider(new RuntimeReflectionProvider())
                    .withScanner(new ReflectionsAnnotationScanner());
            reflectionBuilder.build();
            IClass.setReflection(reflectionBuilder.build());

            injectionContextBuilder = InjectionContextBuilder.builder()
                    .childContextFactory(new RuntimeContextFactory());
            expressionContextBuilder = ExpressionContextBuilder.builder();

            ((IDependentBuilder<IInjectionContextBuilder, ?>) injectionContextBuilder).provide(reflectionBuilder);

            injectionContextBuilder.build();
            // Do NOT pre-build expressionContextBuilder — ApiBuilder.provide() adds
            // required packages before triggering the build via handle()

            ((IDependentBuilder<IApiBuilder, IApi>) contextBuilder).provide(reflectionBuilder);
            ((IDependentBuilder<IApiBuilder, IApi>) contextBuilder).provide(injectionContextBuilder);
            ((IDependentBuilder<IApiBuilder, IApi>) contextBuilder).provide(expressionContextBuilder);

            // ApiBuilder requires IWorkflowsBuilder since core 2.0.0-ALPHA02.
            com.garganttua.core.runtime.dsl.IRuntimesBuilder runtimesBuilder =
                    com.garganttua.core.runtime.dsl.RuntimesBuilder.builder();
            ((IDependentBuilder<com.garganttua.core.runtime.dsl.IRuntimesBuilder, ?>) runtimesBuilder)
                    .provide(injectionContextBuilder);
            com.garganttua.core.script.dsl.IScriptsBuilder scriptsBuilder =
                    com.garganttua.core.script.dsl.ScriptsBuilder.builder();
            ((IDependentBuilder<com.garganttua.core.script.dsl.IScriptsBuilder, ?>) scriptsBuilder)
                    .provide(injectionContextBuilder);
            ((IDependentBuilder<com.garganttua.core.script.dsl.IScriptsBuilder, ?>) scriptsBuilder)
                    .provide(expressionContextBuilder);
            ((IDependentBuilder<com.garganttua.core.script.dsl.IScriptsBuilder, ?>) scriptsBuilder)
                    .provide(runtimesBuilder);
            com.garganttua.core.workflow.dsl.IWorkflowsBuilder workflowsBuilder =
                    com.garganttua.core.workflow.dsl.WorkflowsBuilder.builder();
            ((IDependentBuilder<com.garganttua.core.workflow.dsl.IWorkflowsBuilder, ?>) workflowsBuilder)
                    .provide(injectionContextBuilder);
            ((IDependentBuilder<com.garganttua.core.workflow.dsl.IWorkflowsBuilder, ?>) workflowsBuilder)
                    .provide(scriptsBuilder);
            ((IDependentBuilder<IApiBuilder, IApi>) contextBuilder).provide(workflowsBuilder);
        }

        @Test
        @DisplayName("build() fails without DTO")
        void buildFailsWithoutDto() throws ApiException {
            domainBuilder.entity()
                    .id("id")
                    .uuid("uuid")
                    .tenantId("tenantId");

            assertThrows(ApiException.class, () -> domainBuilder.build());
        }

        @Test
        @DisplayName("build() succeeds with complete configuration")
        void buildSucceedsWithCompleteConfiguration() throws ApiException {
            domainBuilder.entity()
                    .id("id")
                    .uuid("uuid")
                    .tenantId("tenantId")
                .up()
                .dto(IClass.getClass(TestDto.class))
                    .id("id")
                    .uuid("uuid")
                    .tenantId("tenantId")
                    .db(new TestDao())
                .up();

            ((DomainBuilder<TestEntity>) domainBuilder).setDependencyBuilders(
                    injectionContextBuilder, expressionContextBuilder);

            IDomain<TestEntity> context = domainBuilder.build();

            assertNotNull(context);
            assertEquals("testentities", context.getDomain());
            assertEquals(IClass.getClass(TestEntity.class), context.getEntityClass());
        }

        private void wireEntityAndDto() throws ApiException {
            domainBuilder.entity()
                    .id("id")
                    .uuid("uuid")
                    .tenantId("tenantId")
                .up()
                .dto(IClass.getClass(TestDto.class))
                    .id("id")
                    .uuid("uuid")
                    .tenantId("tenantId")
                    .db(new TestDao())
                .up();
            ((DomainBuilder<TestEntity>) domainBuilder).setDependencyBuilders(
                    injectionContextBuilder, expressionContextBuilder);
        }

        @Test
        @DisplayName("build() refuses a tenant domain that declares no superTenant field")
        void buildFailsWhenTenantWithoutSuperTenant() throws ApiException {
            domainBuilder.tenant(true);
            wireEntityAndDto();

            ApiException ex = assertThrows(ApiException.class, () -> domainBuilder.build(),
                    "a tenant domain without a superTenant field must be rejected at build time");
            String msg = rootMessage(ex);
            assertTrue(msg.contains("superTenant field"),
                    "rejection must name the missing superTenant field; got: " + msg);
            assertTrue(msg.contains(".superTenant(") || msg.contains("@EntitySuperTenant"),
                    "rejection must point at the way to declare it; got: " + msg);
        }

        @Test
        @DisplayName("build() refuses an owner domain that declares no superOwner field")
        void buildFailsWhenOwnerWithoutSuperOwner() throws ApiException {
            domainBuilder.owner("ownerId");
            wireEntityAndDto();

            ApiException ex = assertThrows(ApiException.class, () -> domainBuilder.build(),
                    "an owner domain without a superOwner field must be rejected at build time");
            String msg = rootMessage(ex);
            assertTrue(msg.contains("superOwner field"),
                    "rejection must name the missing superOwner field; got: " + msg);
            assertTrue(msg.contains(".superOwner(") || msg.contains("@EntitySuperOwner"),
                    "rejection must point at the way to declare it; got: " + msg);
        }

        @Test
        @DisplayName("build() succeeds when a tenant domain declares its superTenant field")
        void buildSucceedsWhenTenantDeclaresSuperTenant() throws ApiException {
            domainBuilder.tenant(true).superTenant("superTenant");
            wireEntityAndDto();

            IDomain<TestEntity> context = domainBuilder.build();
            assertNotNull(context, "a conformant tenant domain must build");
            assertTrue(context.isTenantEntity(), "the built domain must carry the tenant role");
        }

        @Test
        @DisplayName("build() succeeds when an owner domain declares its superOwner field")
        void buildSucceedsWhenOwnerDeclaresSuperOwner() throws ApiException {
            domainBuilder.owner("ownerId").superOwner("superOwner");
            wireEntityAndDto();

            IDomain<TestEntity> context = domainBuilder.build();
            assertNotNull(context, "a conformant owner domain must build");
            assertEquals(IClass.getClass(TestEntity.class), context.getEntityClass());
        }

        /** Unwraps the ApiException chain to the deepest message (the validation throws the parlant cause). */
        private String rootMessage(Throwable t) {
            Throwable cur = t;
            StringBuilder all = new StringBuilder();
            while (cur != null) {
                if (cur.getMessage() != null) all.append(cur.getMessage()).append('\n');
                cur = cur.getCause();
            }
            return all.toString();
        }
    }
}
