package com.garganttua.api.core.unit.builder;

import com.garganttua.api.core.api.ApiBuilder;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.context.IDtoContext;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.IDtoBuilder;
import com.garganttua.api.commons.dao.IDao;
import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.pageable.IPageable;
import com.garganttua.api.commons.sort.ISort;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.dsl.ReflectionBuilder;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.core.reflections.ReflectionsAnnotationScanner;

@DisplayName("DtoBuilder Tests")
class DtoBuilderTest {

    @BeforeAll
    static void initReflection() {
        IClass.setReflection(ReflectionBuilder.builder()
                .withProvider(new RuntimeReflectionProvider())
                .withScanner(new ReflectionsAnnotationScanner())
                .build());
    }

    // Test entity class
    public static class TestEntity {
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

    private IDtoBuilder<TestEntity, TestDto> dtoBuilder;
    private IDomainBuilder<TestEntity> domainBuilder;

    @BeforeEach
    void setUp() throws ApiException {
        domainBuilder = ApiBuilder.builder().domain(IClass.getClass(TestEntity.class));
        dtoBuilder = domainBuilder.dto(IClass.getClass(TestDto.class));
    }

    @Nested
    @DisplayName("Field Configuration")
    class FieldConfiguration {

        @Test
        @DisplayName("id() accepts valid field name")
        void idAcceptsValidFieldName() throws ApiException {
            assertDoesNotThrow(() -> dtoBuilder.id("id"));
        }

        @Test
        @DisplayName("id() rejects null")
        void idRejectsNull() {
            assertThrows(NullPointerException.class, () -> dtoBuilder.id((String) null));
        }

        @Test
        @DisplayName("uuid() accepts valid field name")
        void uuidAcceptsValidFieldName() throws ApiException {
            assertDoesNotThrow(() -> dtoBuilder.uuid("uuid"));
        }

        @Test
        @DisplayName("tenantId() accepts valid field name")
        void tenantIdAcceptsValidFieldName() throws ApiException {
            assertDoesNotThrow(() -> dtoBuilder.tenantId("tenantId"));
        }
    }

    @Nested
    @DisplayName("DAO Configuration")
    class DaoConfiguration {

        @Test
        @DisplayName("db() accepts IDao instance")
        void dbAcceptsIDaoInstance() {
            assertDoesNotThrow(() -> dtoBuilder.db(new TestDao()));
        }

        @Test
        @DisplayName("db() rejects null")
        void dbRejectsNull() {
            assertThrows(NullPointerException.class, () -> dtoBuilder.db((IDao) null));
        }
    }

    @Nested
    @DisplayName("Builder Navigation")
    class BuilderNavigation {

        @Test
        @DisplayName("up() returns parent domain builder")
        void upReturnsParentDomainBuilder() throws ApiException {
            IDomainBuilder<TestEntity> parent = dtoBuilder
                    .id("id")
                    .uuid("uuid")
                    .tenantId("tenantId")
                    .db(new TestDao())
                    .up();

            assertSame(domainBuilder, parent);
        }
    }

    @Nested
    @DisplayName("Build Validation")
    class BuildValidation {

        @Test
        @DisplayName("build() fails without id configured")
        void buildFailsWithoutId() throws ApiException {
            dtoBuilder.uuid("uuid").tenantId("tenantId").db(new TestDao());
            assertThrows(ApiException.class, () -> dtoBuilder.build());
        }

        @Test
        @DisplayName("build() fails without uuid configured")
        void buildFailsWithoutUuid() throws ApiException {
            dtoBuilder.id("id").tenantId("tenantId").db(new TestDao());
            assertThrows(ApiException.class, () -> dtoBuilder.build());
        }

        @Test
        @DisplayName("build() fails without tenantId configured")
        void buildFailsWithoutTenantId() throws ApiException {
            dtoBuilder.id("id").uuid("uuid").db(new TestDao());
            assertThrows(ApiException.class, () -> dtoBuilder.build());
        }

        @Test
        @DisplayName("build() succeeds with all required fields and DAO")
        void buildSucceedsWithAllRequiredFieldsAndDao() throws ApiException {
            dtoBuilder.id("id").uuid("uuid").tenantId("tenantId").db(new TestDao());
            IDtoContext<TestDto> context = dtoBuilder.build();

            assertNotNull(context);
        }
    }
}
