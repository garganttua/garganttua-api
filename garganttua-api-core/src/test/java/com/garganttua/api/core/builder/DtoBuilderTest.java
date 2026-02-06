package com.garganttua.api.core.builder;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.spec.context.IDtoContext;
import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.context.dsl.IDtoBuilder;
import com.garganttua.api.spec.dao.IDao;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.core.CoreException;
import com.garganttua.core.dsl.DslException;

@DisplayName("DtoBuilder Tests")
class DtoBuilderTest {

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

    private IDtoBuilder<TestEntity, TestDto> dtoBuilder;
    private IDomainBuilder<TestEntity> domainBuilder;

    @BeforeEach
    void setUp() throws DslException {
        domainBuilder = ApiContextBuilder.builder().domain(TestEntity.class);
        dtoBuilder = domainBuilder.dto(TestDto.class);
    }

    @Nested
    @DisplayName("Field Configuration")
    class FieldConfiguration {

        @Test
        @DisplayName("id() accepts valid field name")
        void idAcceptsValidFieldName() throws DslException {
            assertDoesNotThrow(() -> dtoBuilder.id("id"));
        }

        @Test
        @DisplayName("id() rejects null")
        void idRejectsNull() {
            assertThrows(NullPointerException.class, () -> dtoBuilder.id((String) null));
        }

        @Test
        @DisplayName("uuid() accepts valid field name")
        void uuidAcceptsValidFieldName() throws DslException {
            assertDoesNotThrow(() -> dtoBuilder.uuid("uuid"));
        }

        @Test
        @DisplayName("tenantId() accepts valid field name")
        void tenantIdAcceptsValidFieldName() throws DslException {
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
        void upReturnsParentDomainBuilder() throws DslException {
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
        void buildFailsWithoutId() throws DslException {
            dtoBuilder.uuid("uuid").tenantId("tenantId").db(new TestDao());
            assertThrows(DslException.class, () -> dtoBuilder.build());
        }

        @Test
        @DisplayName("build() fails without uuid configured")
        void buildFailsWithoutUuid() throws DslException {
            dtoBuilder.id("id").tenantId("tenantId").db(new TestDao());
            assertThrows(DslException.class, () -> dtoBuilder.build());
        }

        @Test
        @DisplayName("build() fails without tenantId configured")
        void buildFailsWithoutTenantId() throws DslException {
            dtoBuilder.id("id").uuid("uuid").db(new TestDao());
            assertThrows(DslException.class, () -> dtoBuilder.build());
        }

        @Test
        @DisplayName("build() succeeds with all required fields and DAO")
        void buildSucceedsWithAllRequiredFieldsAndDao() throws DslException {
            dtoBuilder.id("id").uuid("uuid").tenantId("tenantId").db(new TestDao());
            IDtoContext<TestDto> context = dtoBuilder.build();

            assertNotNull(context);
        }
    }
}
