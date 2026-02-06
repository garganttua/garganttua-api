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

import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.context.dsl.IEntityBuilder;
import com.garganttua.api.spec.dao.IDao;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.core.CoreException;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.dsl.dependency.IDependentBuilder;
import com.garganttua.core.expression.context.IExpressionContext;
import com.garganttua.core.expression.dsl.IExpressionContextBuilder;
import com.garganttua.core.injection.IInjectionContext;
import com.garganttua.core.injection.context.dsl.IInjectionContextBuilder;

@DisplayName("DomainBuilder Tests")
class DomainBuilderTest {

    // Test entity class
    public static class TestEntity {
        private String id;
        private String uuid;
        private String tenantId;
        private String ownerId;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getOwnerId() { return ownerId; }
        public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    }

    // Test DTO class
    public static class TestDto {
        private String id;
        private String uuid;
        private String tenantId;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    }

    // Simple test DAO
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

    private IDomainBuilder<TestEntity> domainBuilder;
    private IApiContextBuilder contextBuilder;

    @BeforeEach
    void setUp() throws DslException {
        contextBuilder = ApiContextBuilder.builder();
        domainBuilder = contextBuilder.domain(TestEntity.class);
    }

    @Nested
    @DisplayName("Entity Builder Access")
    class EntityBuilderAccess {

        @Test
        @DisplayName("entity() returns entity builder")
        void entityReturnsEntityBuilder() throws DslException {
            IEntityBuilder<TestEntity> entityBuilder = domainBuilder.entity();
            assertNotNull(entityBuilder);
        }

        @Test
        @DisplayName("entity() returns same builder on multiple calls")
        void entityReturnsSameBuilder() throws DslException {
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
        void dtoReturnsDtoBuilder() throws DslException {
            var dtoBuilder = domainBuilder.dto(TestDto.class);
            assertNotNull(dtoBuilder);
        }

        @Test
        @DisplayName("dto() returns same builder for same class")
        void dtoReturnsSameBuilderForSameClass() throws DslException {
            var first = domainBuilder.dto(TestDto.class);
            var second = domainBuilder.dto(TestDto.class);
            assertSame(first, second);
        }
    }

    @Nested
    @DisplayName("CRUD Operation Configuration")
    class CrudOperationConfiguration {

        @Test
        @DisplayName("creation() configures create operation")
        void creationConfiguresCreateOperation() {
            IDomainBuilder<TestEntity> result = domainBuilder.creation(true);
            assertSame(domainBuilder, result);
        }

        @Test
        @DisplayName("readAll() configures read all operation")
        void readAllConfiguresReadAllOperation() {
            IDomainBuilder<TestEntity> result = domainBuilder.readAll(true);
            assertSame(domainBuilder, result);
        }

        @Test
        @DisplayName("readOne() configures read one operation")
        void readOneConfiguresReadOneOperation() {
            IDomainBuilder<TestEntity> result = domainBuilder.readOne(true);
            assertSame(domainBuilder, result);
        }

        @Test
        @DisplayName("update() configures update operation")
        void updateConfiguresUpdateOperation() {
            IDomainBuilder<TestEntity> result = domainBuilder.update(true);
            assertSame(domainBuilder, result);
        }

        @Test
        @DisplayName("deleteOne() configures delete one operation")
        void deleteOneConfiguresDeleteOneOperation() {
            IDomainBuilder<TestEntity> result = domainBuilder.deleteOne(true);
            assertSame(domainBuilder, result);
        }

        @Test
        @DisplayName("deleteAll() configures delete all operation")
        void deleteAllConfiguresDeleteAllOperation() {
            IDomainBuilder<TestEntity> result = domainBuilder.deleteAll(true);
            assertSame(domainBuilder, result);
        }
    }

    @Nested
    @DisplayName("Tenancy Configuration")
    class TenancyConfiguration {

        @Test
        @DisplayName("tenant() configures tenant flag")
        void tenantConfiguresTenantFlag() throws DslException {
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
        void ownerConfiguresOwnerField() throws DslException {
            IDomainBuilder<TestEntity> result = domainBuilder.owner("ownerId");
            assertSame(domainBuilder, result);
        }
    }

    @Nested
    @DisplayName("Security Builder Access")
    class SecurityBuilderAccess {

        @Test
        @DisplayName("security() returns security builder")
        void securityReturnsSecurityBuilder() throws DslException {
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
            IApiContextBuilder parent = domainBuilder.up();
            assertSame(contextBuilder, parent);
        }

        @Test
        @DisplayName("getEntityClass() returns configured class")
        void getEntityClassReturnsConfiguredClass() throws DslException {
            assertEquals(TestEntity.class, domainBuilder.getEntityClass());
        }
    }

    @Nested
    @DisplayName("Domain Build")
    class DomainBuild {

        private IInjectionContextBuilder mockInjectionContextBuilder;
        private IExpressionContextBuilder mockExpressionContextBuilder;

        @BeforeEach
        @SuppressWarnings("unchecked")
        void setUpDependencies() throws DslException {
            mockInjectionContextBuilder = mock(IInjectionContextBuilder.class);
            IInjectionContext mockInjectionContext = mock(IInjectionContext.class);
            when(mockInjectionContextBuilder.build()).thenReturn(mockInjectionContext);

            mockExpressionContextBuilder = mock(IExpressionContextBuilder.class);
            IExpressionContext mockExpressionContext = mock(IExpressionContext.class);
            when(mockExpressionContextBuilder.build()).thenReturn(mockExpressionContext);

            ((IDependentBuilder<IApiContextBuilder, IApiContext>) contextBuilder).provide(mockInjectionContextBuilder);
            ((IDependentBuilder<IApiContextBuilder, IApiContext>) contextBuilder).provide(mockExpressionContextBuilder);
        }

        @Test
        @DisplayName("build() fails without DTO")
        void buildFailsWithoutDto() throws DslException {
            domainBuilder.entity()
                    .id("id")
                    .uuid("uuid")
                    .tenantId("tenantId");

            assertThrows(DslException.class, () -> domainBuilder.build());
        }

        @Test
        @DisplayName("build() succeeds with complete configuration")
        void buildSucceedsWithCompleteConfiguration() throws DslException {
            domainBuilder.entity()
                    .id("id")
                    .uuid("uuid")
                    .tenantId("tenantId")
                .up()
                .dto(TestDto.class)
                    .id("id")
                    .uuid("uuid")
                    .tenantId("tenantId")
                    .db(new TestDao())
                .up();

            // Set dependency builders for workflow building
            ((DomainBuilder<TestEntity>) domainBuilder).setDependencyBuilders(
                    mockInjectionContextBuilder, mockExpressionContextBuilder);

            IDomainContext<TestEntity> context = domainBuilder.build();

            assertNotNull(context);
            assertEquals("testentities", context.getDomain());
            assertEquals(TestEntity.class, context.getEntityClass());
        }
    }
}
