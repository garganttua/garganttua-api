package com.garganttua.api.core.builder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.context.application.ApiContext;
import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.dao.IDao;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.core.CoreException;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.dsl.IObservableBuilder;
import com.garganttua.core.dsl.dependency.IDependentBuilder;
import com.garganttua.core.expression.context.IExpressionContext;
import com.garganttua.core.expression.dsl.IExpressionContextBuilder;
import com.garganttua.core.injection.IInjectionContext;
import com.garganttua.core.injection.context.dsl.IInjectionContextBuilder;

@DisplayName("ApiContextBuilder Tests")
class ApiContextBuilderTest {

    // Test entity class
    public static class TestEntity {
        private String id;
        private String uuid;
        private String tenantId;
        private String name;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    // Test DTO class
    public static class TestDto {
        private String id;
        private String uuid;
        private String tenantId;
        private String name;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    // Simple in-memory DAO for testing
    public static class TestDao implements IDao {
        private final List<Object> storage = new ArrayList<>();
        private Class<?> dtoClass;

        @Override
        public void setDtoClass(Class<?> dtoClass) { this.dtoClass = dtoClass; }

        @Override
        public List<Object> find(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort)
                throws CoreException {
            return new ArrayList<>(storage);
        }

        @Override
        public Object save(Object object) throws CoreException {
            storage.add(object);
            return object;
        }

        @Override
        public void delete(Object object) throws CoreException {
            storage.remove(object);
        }

        @Override
        public long count(IFilter filter) throws CoreException {
            return storage.size();
        }
    }

    private IApiContextBuilder builder;

    @BeforeEach
    void setUp() {
        builder = ApiContextBuilder.builder();
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
        @DisplayName("builder() returns IApiContextBuilder instance")
        void builderReturnsCorrectType() {
            assertTrue(builder instanceof IApiContextBuilder);
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
        void superTenantAutoCreateAcceptsBoolean() throws DslException {
            assertDoesNotThrow(() -> builder.superTenantAutoCreate(true));
            assertDoesNotThrow(() -> builder.superTenantAutoCreate(false));
        }

        @Test
        @DisplayName("superTenantId() returns builder for chaining")
        void superTenantIdReturnsBuilder() {
            IApiContextBuilder result = builder.superTenantId("TENANT");
            assertSame(builder, result);
        }
    }

    @Nested
    @DisplayName("Domain Builder")
    class DomainBuilderTests {

        @Test
        @DisplayName("domain() returns IDomainBuilder")
        void domainReturnsDomainBuilder() throws DslException {
            IDomainBuilder<TestEntity> domainBuilder = builder.domain(TestEntity.class);
            assertNotNull(domainBuilder);
        }

        @Test
        @DisplayName("domain() rejects null class")
        void domainRejectsNullClass() {
            assertThrows(NullPointerException.class, () -> builder.domain(null));
        }

        @Test
        @DisplayName("domain() returns same builder for same class")
        void domainReturnsSameBuilderForSameClass() throws DslException {
            IDomainBuilder<TestEntity> first = builder.domain(TestEntity.class);
            IDomainBuilder<TestEntity> second = builder.domain(TestEntity.class);
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
    @DisplayName("Injection Builder")
    class InjectionBuilderTests {

        // TODO: Re-enable when injection() method is added to IApiContextBuilder
        // @Test
        // @DisplayName("injection() returns null when not provided")
        // void injectionReturnsNullWhenNotProvided() {
        //     assertNull(builder.injection());
        // }

        // @Test
        // @DisplayName("injection() returns non-null when provided")
        // @SuppressWarnings("unchecked")
        // void injectionReturnsNonNullWhenProvided() throws DslException {
        //     IInjectionContextBuilder mockBuilder = mock(IInjectionContextBuilder.class);
        //     ((IDependentBuilder<IApiContextBuilder, IApiContext>) builder).provide(mockBuilder);
        //     assertNotNull(builder.injection());
        // }
    }

    @Nested
    @DisplayName("Full Context Build")
    class FullContextBuild {

        private IInjectionContextBuilder mockInjectionContextBuilder;
        private IInjectionContext mockInjectionContext;
        private IExpressionContextBuilder mockExpressionContextBuilder;
        private IExpressionContext mockExpressionContext;

        @BeforeEach
        @SuppressWarnings("unchecked")
        void setUpInjection() throws DslException {
            mockInjectionContextBuilder = mock(IInjectionContextBuilder.class);
            mockInjectionContext = mock(IInjectionContext.class);
            when(mockInjectionContextBuilder.build()).thenReturn(mockInjectionContext);

            mockExpressionContextBuilder = mock(IExpressionContextBuilder.class);
            mockExpressionContext = mock(IExpressionContext.class);
            when(mockExpressionContextBuilder.build()).thenReturn(mockExpressionContext);

            ((IDependentBuilder<IApiContextBuilder, IApiContext>) builder).provide(mockInjectionContextBuilder);
            ((IDependentBuilder<IApiContextBuilder, IApiContext>) builder).provide(mockExpressionContextBuilder);
        }

        @Test
        @DisplayName("build() creates valid context with domain")
        void buildCreatesValidContext() throws DslException {
            builder.superTenantId("SUPER")
                   .superTenantAutoCreate(true)
                   .domain(TestEntity.class)
                       .entity()
                           .id("id")
                           .uuid("uuid")
                           .tenantId("tenantId")
                       .up()
                       .dto(TestDto.class)
                           .id("id")
                           .uuid("uuid")
                           .tenantId("tenantId")
                           .db(new TestDao())
                       .up()
                   .up();

            IApiContext context = builder.build();

            assertNotNull(context);
            assertNotNull(((ApiContext) context).getInjectionContext());
        }

        @Test
        @DisplayName("build() includes domain context")
        void buildIncludesDomainContext() throws DslException {
            builder.domain(TestEntity.class)
                   .entity()
                       .id("id")
                       .uuid("uuid")
                       .tenantId("tenantId")
                   .up()
                   .dto(TestDto.class)
                       .id("id")
                       .uuid("uuid")
                       .tenantId("tenantId")
                       .db(new TestDao())
                   .up()
               .up();

            IApiContext context = builder.build();

            Optional<IDomainContext<?>> domainCtx = context.getDomainContext("testentities");
            assertTrue(domainCtx.isPresent());
            assertEquals(TestEntity.class, domainCtx.get().getEntityClass());
        }

        @Test
        @DisplayName("build() returns domain name based on entity class")
        void buildReturnsDomainNameBasedOnEntityClass() throws DslException {
            builder.domain(TestEntity.class)
                   .entity()
                       .id("id")
                       .uuid("uuid")
                       .tenantId("tenantId")
                   .up()
                   .dto(TestDto.class)
                       .id("id")
                       .uuid("uuid")
                       .tenantId("tenantId")
                       .db(new TestDao())
                   .up()
               .up();

            IApiContext context = builder.build();
            Optional<IDomainContext<?>> domainCtx = context.getDomainContext("testentities");

            assertTrue(domainCtx.isPresent());
            assertEquals("testentities", domainCtx.get().getDomain());
        }
    }
}
