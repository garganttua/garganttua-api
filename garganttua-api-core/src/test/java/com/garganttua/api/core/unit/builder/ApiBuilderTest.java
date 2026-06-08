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
import com.garganttua.api.core.api.Api;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
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

@DisplayName("ApiBuilder Tests")
class ApiBuilderTest {

    // Test entity class
    public static class TestEntity {
        private String id;
        private String uuid;
        private String tenantId;
        private String name;
        private Boolean superTenant = false;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
    }

    // Test DTO class
    public static class TestDto {
        @FieldMappingRule(sourceFieldAddress = "id")
        private String id;
        @FieldMappingRule(sourceFieldAddress = "uuid")
        private String uuid;
        @FieldMappingRule(sourceFieldAddress = "tenantId")
        private String tenantId;
        @FieldMappingRule(sourceFieldAddress = "name")
        private String name;
        @FieldMappingRule(sourceFieldAddress = "superTenant")
        private Boolean superTenant;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
    }

    // Simple in-memory DAO for testing
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

    private IApiBuilder builder;

    @BeforeEach
    void setUp() {
        builder = ApiBuilder.builder();
    }

    @Nested
    @DisplayName("Builder Factory")
    class BuilderFactory {

        @Test
        @DisplayName("builder() returns non-null builder")
        void builderReturnsNonNull() {
            assertNotNull(builder);
        }

        @Test
        @DisplayName("builder() returns IApiBuilder instance")
        void builderReturnsCorrectType() {
            assertTrue(builder instanceof IApiBuilder);
        }
    }

    @Nested
    @DisplayName("Super Tenant Configuration")
    class SuperTenantConfiguration {

        @Test
        @DisplayName("superTenantId() accepts valid ID")
        void superTenantIdAcceptsValidId() {
            assertDoesNotThrow(() -> builder.superTenantId("SUPER_TENANT"));
        }

        @Test
        @DisplayName("superTenantId() rejects null")
        void superTenantIdRejectsNull() {
            assertThrows(NullPointerException.class, () -> builder.superTenantId(null));
        }

        @Test
        @DisplayName("superTenantAutoCreate() accepts boolean")
        void superTenantAutoCreateAcceptsBoolean() throws ApiException {
            assertDoesNotThrow(() -> builder.superTenantAutoCreate(true));
            assertDoesNotThrow(() -> builder.superTenantAutoCreate(false));
        }

        @Test
        @DisplayName("superTenantId() returns builder for chaining")
        void superTenantIdReturnsBuilder() {
            IApiBuilder result = builder.superTenantId("TENANT");
            assertSame(builder, result);
        }
    }

    @Nested
    @DisplayName("Domain Builder")
    class DomainBuilderTests {

        @Test
        @DisplayName("domain() returns IDomainBuilder")
        void domainReturnsDomainBuilder() throws ApiException {
            IDomainBuilder<TestEntity> domainBuilder = builder.domain(IClass.getClass(TestEntity.class));
            assertNotNull(domainBuilder);
        }

        @Test
        @DisplayName("domain() rejects null class")
        void domainRejectsNullClass() {
            assertThrows(NullPointerException.class, () -> builder.domain(null));
        }

        @Test
        @DisplayName("domain() returns same builder for same class")
        void domainReturnsSameBuilderForSameClass() throws ApiException {
            IDomainBuilder<TestEntity> first = builder.domain(IClass.getClass(TestEntity.class));
            IDomainBuilder<TestEntity> second = builder.domain(IClass.getClass(TestEntity.class));
            assertSame(first, second);
        }
    }

    @Nested
    @DisplayName("Security Builder")
    class SecurityBuilderTests {

        @Test
        @DisplayName("security() returns non-null")
        void securityReturnsNonNull() {
            assertNotNull(builder.security());
        }

        @Test
        @DisplayName("security() returns same instance on multiple calls")
        void securityReturnsSameInstance() {
            var first = builder.security();
            var second = builder.security();
            assertSame(first, second);
        }
    }

    @Nested
    @DisplayName("Full Context Build")
    class FullContextBuild {

        @BeforeEach
        @SuppressWarnings("unchecked")
        void setUpDependencies() throws ApiException {
            com.garganttua.core.reflection.dsl.IReflectionBuilder reflectionBuilder = ReflectionBuilder.builder()
                    .withProvider(new RuntimeReflectionProvider())
                    .withScanner(new ReflectionsAnnotationScanner());
            reflectionBuilder.build();
            IClass.setReflection(reflectionBuilder.build());

            IInjectionContextBuilder injectionContextBuilder = InjectionContextBuilder.builder()
                    .childContextFactory(new RuntimeContextFactory());
            IExpressionContextBuilder expressionContextBuilder = ExpressionContextBuilder.builder();

            ((IDependentBuilder<IInjectionContextBuilder, ?>) injectionContextBuilder).provide(reflectionBuilder);

            injectionContextBuilder.build();
            // Do NOT pre-build expressionContextBuilder — ApiBuilder.provide() adds
            // required packages before triggering the build via handle()

            ((IDependentBuilder<IApiBuilder, IApi>) builder).provide(reflectionBuilder);
            ((IDependentBuilder<IApiBuilder, IApi>) builder).provide(injectionContextBuilder);
            ((IDependentBuilder<IApiBuilder, IApi>) builder).provide(expressionContextBuilder);

            // Provide the IWorkflowsBuilder ApiBuilder now requires (core
            // 2.0.0-ALPHA02 dropped WorkflowBuilder.create()).
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
            ((IDependentBuilder<IApiBuilder, IApi>) builder).provide(workflowsBuilder);
        }

        @Test
        @DisplayName("build() creates valid context with domain")
        void buildCreatesValidContext() throws ApiException {
            builder.superTenantId("SUPER")
                   .superTenantAutoCreate(true)
                   .domain(IClass.getClass(TestEntity.class))
                       .tenant(true)
                       .superTenant("superTenant")
                       .entity()
                           .id("id")
                           .uuid("uuid")
                           .tenantId("tenantId")
                       .up()
                       .dto(IClass.getClass(TestDto.class))
                           .id("id")
                           .uuid("uuid")
                           .tenantId("tenantId")
                           .db(new TestDao())
                       .up()
                   .up();

            IApi context = builder.build();

            assertNotNull(context);
            assertNotNull(((Api) context).getInjectionContext());
        }

        @Test
        @DisplayName("build() includes domain context")
        void buildIncludesDomain() throws ApiException {
            builder.multiTenant(false)
                   .domain(IClass.getClass(TestEntity.class))
                       .entity()
                           .id("id")
                           .uuid("uuid")
                           .tenantId("tenantId")
                       .up()
                       .dto(IClass.getClass(TestDto.class))
                           .id("id")
                           .uuid("uuid")
                           .tenantId("tenantId")
                           .db(new TestDao())
                       .up()
                   .up();

            IApi context = builder.build();

            Optional<IDomain<?>> domainCtx = context.getDomain("testentities");
            assertTrue(domainCtx.isPresent());
            assertEquals(IClass.getClass(TestEntity.class), domainCtx.get().getEntityClass());
        }

        @Test
        @DisplayName("build() returns domain name based on entity class")
        void buildReturnsDomainNameBasedOnEntityClass() throws ApiException {
            builder.multiTenant(false)
                   .domain(IClass.getClass(TestEntity.class))
                       .entity()
                           .id("id")
                           .uuid("uuid")
                           .tenantId("tenantId")
                       .up()
                       .dto(IClass.getClass(TestDto.class))
                           .id("id")
                           .uuid("uuid")
                           .tenantId("tenantId")
                           .db(new TestDao())
                       .up()
                   .up();

            IApi context = builder.build();
            Optional<IDomain<?>> domainCtx = context.getDomain("testentities");

            assertTrue(domainCtx.isPresent());
            assertEquals("testentities", domainCtx.get().getDomain());
        }

        @Test
        @DisplayName("build() throws when multi-tenancy enabled but no tenant domain")
        void buildThrowsWhenMultiTenantWithoutTenantDomain() throws ApiException {
            builder.superTenantId("SUPER")
                   .domain(IClass.getClass(TestEntity.class))
                       .entity()
                           .id("id")
                           .uuid("uuid")
                           .tenantId("tenantId")
                       .up()
                       .dto(IClass.getClass(TestDto.class))
                           .id("id")
                           .uuid("uuid")
                           .tenantId("tenantId")
                           .db(new TestDao())
                       .up()
                   .up();

            ApiException ex = assertThrows(ApiException.class, () -> builder.build());
            assertTrue(ex.getMessage().contains("no domain is marked as tenant"));
        }

        @Test
        @DisplayName("build() succeeds when multi-tenancy disabled and no tenant domain")
        void buildSucceedsWhenMultiTenantDisabledWithoutTenantDomain() throws ApiException {
            builder.multiTenant(false)
                   .domain(IClass.getClass(TestEntity.class))
                       .entity()
                           .id("id")
                           .uuid("uuid")
                           .tenantId("tenantId")
                       .up()
                       .dto(IClass.getClass(TestDto.class))
                           .id("id")
                           .uuid("uuid")
                           .tenantId("tenantId")
                           .db(new TestDao())
                       .up()
                   .up();

            IApi context = builder.build();
            assertNotNull(context);
        }

        @Test
        @DisplayName("build() succeeds when multi-tenancy enabled and tenant domain exists")
        void buildSucceedsWhenMultiTenantWithTenantDomain() throws ApiException {
            builder.superTenantId("SUPER")
                   .domain(IClass.getClass(TestEntity.class))
                       .tenant(true)
                       .superTenant("superTenant")
                       .entity()
                           .id("id")
                           .uuid("uuid")
                           .tenantId("tenantId")
                       .up()
                       .dto(IClass.getClass(TestDto.class))
                           .id("id")
                           .uuid("uuid")
                           .tenantId("tenantId")
                           .db(new TestDao())
                       .up()
                   .up();

            IApi context = builder.build();
            assertNotNull(context);
        }
    }

    @Nested
    @DisplayName("Framework package auto-inclusion")
    class FrameworkPackageAutoInclusion {

        @BeforeEach
        @SuppressWarnings("unchecked")
        void setUpDependencies() throws ApiException {
            com.garganttua.core.reflection.dsl.IReflectionBuilder reflectionBuilder = ReflectionBuilder.builder()
                    .withProvider(new RuntimeReflectionProvider())
                    .withScanner(new ReflectionsAnnotationScanner());
            reflectionBuilder.build();
            IClass.setReflection(reflectionBuilder.build());

            IInjectionContextBuilder injectionContextBuilder = InjectionContextBuilder.builder()
                    .childContextFactory(new RuntimeContextFactory());
            IExpressionContextBuilder expressionContextBuilder = ExpressionContextBuilder.builder();

            ((IDependentBuilder<IInjectionContextBuilder, ?>) injectionContextBuilder).provide(reflectionBuilder);
            injectionContextBuilder.build();

            ((IDependentBuilder<IApiBuilder, IApi>) builder).provide(reflectionBuilder);
            ((IDependentBuilder<IApiBuilder, IApi>) builder).provide(injectionContextBuilder);
            ((IDependentBuilder<IApiBuilder, IApi>) builder).provide(expressionContextBuilder);

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
            ((IDependentBuilder<IApiBuilder, IApi>) builder).provide(workflowsBuilder);
        }

        /**
         * The framework injection only affects the *asset* scan surface
         * (@Serializer, @Protocol, @AuthorizationProtocol). The user-declared
         * package set returned by getPackages() stays clean. We assert
         * directly on the internal computed surface to avoid running the
         * full build cycle (which would also pick up the framework's own
         * test fixtures and clash with our minimal wiring).
         */
        @SuppressWarnings("unchecked")
        private java.util.Set<String> resolveAssetScanSurface() throws Exception {
            java.lang.reflect.Method m = ApiBuilder.class.getDeclaredMethod("assetScanSurface");
            m.setAccessible(true);
            return (java.util.Set<String>) m.invoke(builder);
        }

        @Test
        @DisplayName("default asset scan surface includes com.garganttua.api and com.garganttua.core")
        void defaultIncludesFrameworkPackages() throws Exception {
            java.util.Set<String> surface = resolveAssetScanSurface();
            assertTrue(surface.contains("com.garganttua.api"),
                    "com.garganttua.api should be auto-injected, got: " + surface);
            assertTrue(surface.contains("com.garganttua.core"),
                    "com.garganttua.core should be auto-injected, got: " + surface);
        }

        @Test
        @DisplayName("includeFrameworkPackages(false) keeps both framework packages out of the asset scan surface")
        void optOutExcludesFrameworkPackages() throws Exception {
            builder.includeFrameworkPackages(false);
            java.util.Set<String> surface = resolveAssetScanSurface();
            assertFalse(surface.contains("com.garganttua.api"),
                    "com.garganttua.api must be absent after opt-out, got: " + surface);
            assertFalse(surface.contains("com.garganttua.core"),
                    "com.garganttua.core must be absent after opt-out, got: " + surface);
        }

        @Test
        @DisplayName("user packages and framework packages coexist on the asset surface; getPackages() stays user-only")
        void userPackagesCoexist() throws Exception {
            builder.packages("com.myapp.entities");
            java.util.Set<String> surface = resolveAssetScanSurface();
            assertTrue(surface.contains("com.myapp.entities"),
                    "user package must be in the scan surface, got: " + surface);
            assertTrue(surface.contains("com.garganttua.api"),
                    "framework package must be present alongside user packages, got: " + surface);
            assertTrue(surface.contains("com.garganttua.core"),
                    "framework package must be present alongside user packages, got: " + surface);

            // getPackages() reports the *user-declared* surface — the framework
            // injection is an internal detail of the asset scanners, not a
            // mutation of the user's view.
            ApiBuilder concrete = (ApiBuilder) builder;
            List<String> userPackages = List.of(concrete.getPackages());
            assertEquals(List.of("com.myapp.entities"), userPackages,
                    "getPackages() must reflect only what the user declared, got: " + userPackages);
        }

        @Test
        @DisplayName("auto-injection is idempotent — explicitly declaring com.garganttua.api does not cause a duplicate")
        void noDuplicateOnExplicitDeclaration() throws Exception {
            builder.packages("com.garganttua.api");
            java.util.Set<String> surface = resolveAssetScanSurface();
            long count = surface.stream().filter("com.garganttua.api"::equals).count();
            assertEquals(1L, count,
                    "com.garganttua.api should appear exactly once on the asset surface, got "
                            + count + " in " + surface);
        }

        @Test
        @DisplayName("framework packages do NOT leak into entity/security scans — getPackages() (used by those scanners) stays user-only even when nothing was declared")
        void entityScanStaysUserOnly() {
            // No user package, default includeFrameworkPackages(true).
            ApiBuilder concrete = (ApiBuilder) builder;
            List<String> userPackages = List.of(concrete.getPackages());
            assertFalse(userPackages.contains("com.garganttua.api"),
                    "framework package must NOT pollute getPackages() — entity / security "
                            + "scanners look at it and would pick up framework test fixtures, got: "
                            + userPackages);
        }
    }
}
