package com.garganttua.api.core.unit.builder;

import com.garganttua.api.core.api.ApiBuilder;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.context.IEntityContext;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.IEntityBuilder;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.dsl.ReflectionBuilder;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.core.reflections.ReflectionsAnnotationScanner;

@DisplayName("EntityBuilder Tests")
class EntityBuilderTest {

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
        private String name;
        private String optionalField;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getOptionalField() { return optionalField; }
        public void setOptionalField(String optionalField) { this.optionalField = optionalField; }
    }

    private IEntityBuilder<TestEntity> entityBuilder;
    private IDomainBuilder<TestEntity> domainBuilder;

    @BeforeEach
    void setUp() throws ApiException {
        domainBuilder = ApiBuilder.builder().domain(IClass.getClass(TestEntity.class));
        entityBuilder = domainBuilder.entity();
    }

    @Nested
    @DisplayName("Field Configuration")
    class FieldConfiguration {

        @Test
        @DisplayName("id() accepts valid field name")
        void idAcceptsValidFieldName() throws ApiException {
            assertDoesNotThrow(() -> entityBuilder.id("id"));
        }

        @Test
        @DisplayName("id() rejects null")
        void idRejectsNull() {
            assertThrows(NullPointerException.class, () -> entityBuilder.id((String) null));
        }

        @Test
        @DisplayName("uuid() accepts valid field name")
        void uuidAcceptsValidFieldName() throws ApiException {
            assertDoesNotThrow(() -> entityBuilder.uuid("uuid"));
        }

        @Test
        @DisplayName("uuid() rejects null")
        void uuidRejectsNull() {
            assertThrows(NullPointerException.class, () -> entityBuilder.uuid((String) null));
        }

        @Test
        @DisplayName("tenantId() accepts valid field name")
        void tenantIdAcceptsValidFieldName() throws ApiException {
            assertDoesNotThrow(() -> entityBuilder.tenantId("tenantId"));
        }

        @Test
        @DisplayName("tenantId() rejects null")
        void tenantIdRejectsNull() {
            assertThrows(NullPointerException.class, () -> entityBuilder.tenantId((String) null));
        }

        @Test
        @DisplayName("Field methods return builder for chaining")
        void fieldMethodsReturnBuilder() throws ApiException {
            IEntityBuilder<TestEntity> result = entityBuilder.id("id");
            assertSame(entityBuilder, result);

            result = entityBuilder.uuid("uuid");
            assertSame(entityBuilder, result);

            result = entityBuilder.tenantId("tenantId");
            assertSame(entityBuilder, result);
        }
    }

    @Nested
    @DisplayName("Mandatory Fields")
    class MandatoryFields {

        @Test
        @DisplayName("mandatory() accepts valid field name")
        void mandatoryAcceptsValidFieldName() throws ApiException {
            assertDoesNotThrow(() -> entityBuilder.mandatory("name"));
        }

        @Test
        @DisplayName("mandatory() can be called multiple times")
        void mandatoryCanBeCalledMultipleTimes() throws ApiException {
            entityBuilder.mandatory("name");
            assertDoesNotThrow(() -> entityBuilder.mandatory("optionalField"));
        }
    }

    @Nested
    @DisplayName("Unicity Fields")
    class UnicityFields {

        @Test
        @DisplayName("unicity() accepts valid field name")
        void unicityAcceptsValidFieldName() throws ApiException {
            assertDoesNotThrow(() -> entityBuilder.unicity("name"));
        }

        @Test
        @DisplayName("unicity() can be called multiple times")
        void unicityCanBeCalledMultipleTimes() throws ApiException {
            entityBuilder.unicity("name");
            assertDoesNotThrow(() -> entityBuilder.unicity("uuid"));
        }
    }

    @Nested
    @DisplayName("Update Fields")
    class UpdateFields {

        @Test
        @DisplayName("update() accepts valid field name")
        void updateAcceptsValidFieldName() throws ApiException {
            assertDoesNotThrow(() -> entityBuilder.update("name"));
        }

        @Test
        @DisplayName("update() with authority accepts valid parameters")
        void updateWithAuthorityAcceptsValidParams() throws ApiException {
            assertDoesNotThrow(() -> entityBuilder.update("name", "users:update"));
        }
    }

    @Nested
    @DisplayName("Builder Navigation")
    class BuilderNavigation {

        @Test
        @DisplayName("up() returns parent domain builder")
        void upReturnsParentDomainBuilder() throws ApiException {
            IDomainBuilder<TestEntity> parent = entityBuilder
                    .id("id")
                    .uuid("uuid")
                    .tenantId("tenantId")
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
            entityBuilder.uuid("uuid").tenantId("tenantId");
            assertThrows(ApiException.class, () -> entityBuilder.build());
        }

        @Test
        @DisplayName("build() fails without uuid configured")
        void buildFailsWithoutUuid() throws ApiException {
            entityBuilder.id("id").tenantId("tenantId");
            assertThrows(ApiException.class, () -> entityBuilder.build());
        }

        @Test
        @DisplayName("build() fails without tenantId configured")
        void buildFailsWithoutTenantId() throws ApiException {
            entityBuilder.id("id").uuid("uuid");
            assertThrows(ApiException.class, () -> entityBuilder.build());
        }

        @Test
        @DisplayName("build() succeeds with all required fields")
        void buildSucceedsWithAllRequiredFields() throws ApiException {
            entityBuilder.id("id").uuid("uuid").tenantId("tenantId");
            IEntityContext<TestEntity> context = entityBuilder.build();

            assertNotNull(context);
            assertEquals(IClass.getClass(TestEntity.class), context.getEntityClass());
        }
    }
}
