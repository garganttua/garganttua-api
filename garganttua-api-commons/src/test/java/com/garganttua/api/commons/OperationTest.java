package com.garganttua.api.commons;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.BusinessOperation;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.operation.OperationType;
import com.garganttua.api.commons.operation.Scope;
import com.garganttua.api.commons.operation.TechnicalOperation;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.dsl.ReflectionBuilder;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;

class OperationTest {

    @BeforeAll
    static void initReflection() throws Exception {
        IClass.setReflection(ReflectionBuilder.builder()
                .withProvider(new RuntimeReflectionProvider())
                .build());
    }

    static class DummyEntity {}

    /** Minimal use-case definition for building a use-case OperationDefinition in tests. */
    private static com.garganttua.api.commons.definition.IUseCaseDefinition uc(String name,
            TechnicalOperation verb, Scope scope) {
        return new com.garganttua.api.commons.definition.IUseCaseDefinition() {
            public String name() { return name; }
            public com.garganttua.api.commons.operation.OperationPath path() { return null; }
            public IClass<?> inputType() { return null; }
            public IClass<?> outputType() { return null; }
            public com.garganttua.core.reflection.binders.IMethodBinder<?> binder() { return null; }
            public Scope scope() { return scope; }
            public TechnicalOperation operation() { return verb; }
            public Access access() { return Access.authenticated; }
            public boolean authority() { return false; }
            public String authorityName() { return null; }
        };
    }

    @Nested
    @DisplayName("Factory methods")
    class FactoryMethods {

        @Test
        void testReadOne() {
            OperationDefinition op = OperationDefinition.readOne("test", IClass.getClass(DummyEntity.class), false, null, Access.authenticated);

            assertEquals("test", op.domainName());
            assertEquals(TechnicalOperation.read, op.technicalOperation());
            assertEquals(IClass.getClass(DummyEntity.class), op.entity());
            assertEquals(Scope.oneEntity, op.scope());
            assertEquals(OperationType.standard, op.type());
        }

        @Test
        void testCreateOne() {
            OperationDefinition op = OperationDefinition.createOne("domain", IClass.getClass(DummyEntity.class), false, null, Access.authenticated);

            assertEquals(TechnicalOperation.create, op.technicalOperation());
            assertEquals(Scope.oneEntity, op.scope());
        }

        @Test
        void testDeleteAll() {
            OperationDefinition op = OperationDefinition.deleteAll("x", IClass.getClass(DummyEntity.class), false, null, Access.authenticated);

            assertEquals(TechnicalOperation.delete, op.technicalOperation());
            assertEquals(Scope.allEntities, op.scope());
        }

        @Test
        void testUseCase() {
            OperationDefinition op = OperationDefinition.useCase("domain", IClass.getClass(DummyEntity.class),
                    uc("myUseCase", TechnicalOperation.read, Scope.oneEntity));

            assertEquals(OperationType.usesCase, op.type());
            assertEquals("myUseCase", op.getOperationName(), "a use case's operation name is its own name");
            assertEquals("myUseCase", op.useCaseName());
        }

        @Test
        void testAuthenticate() {
            OperationDefinition op = OperationDefinition.authenticate("sec", IClass.getClass(DummyEntity.class));

            assertEquals(OperationType.authentication, op.type());
            assertEquals(TechnicalOperation.create, op.technicalOperation());
        }
    }

    @Nested
    @DisplayName("Path generation")
    class PathGeneration {

        @Test
        void testPathOneEntity() {
            OperationDefinition op = OperationDefinition.readOne("d", IClass.getClass(DummyEntity.class), false, null, Access.authenticated);
            assertEquals("/dummyentities/${uuid}", op.getPath().path());
            assertEquals("dummyentities", op.getPath().domain());
            assertEquals("${uuid}", op.getPath().suffix());
        }

        @Test
        void testPathAllEntities() {
            OperationDefinition op = OperationDefinition.readAll("d", IClass.getClass(DummyEntity.class), false, null, Access.authenticated);
            assertEquals("/dummyentities", op.getPath().path());
            assertEquals("dummyentities", op.getPath().domain());
            assertNull(op.getPath().suffix());
        }

        @Test
        void testPathAuthenticate() {
            OperationDefinition op = OperationDefinition.authenticate("d", IClass.getClass(DummyEntity.class));
            assertEquals("/dummyentities/authenticate", op.getPath().path());
            assertEquals("dummyentities", op.getPath().domain());
            assertEquals("authenticate", op.getPath().suffix());
        }
    }

    @Nested
    @DisplayName("Operation names")
    class OperationNames {

        @Test
        void testOperationNameReadOne() {
            OperationDefinition op = OperationDefinition.readOne("d", IClass.getClass(DummyEntity.class), false, null, Access.authenticated);
            assertEquals("read-one-dummyentity", op.getOperationName());
        }

        @Test
        void testOperationNameReadAll() {
            OperationDefinition op = OperationDefinition.readAll("d", IClass.getClass(DummyEntity.class), false, null, Access.authenticated);
            assertEquals("read-all-dummyentities", op.getOperationName());
        }

        @Test
        void testOperationNameAuthenticate() {
            OperationDefinition op = OperationDefinition.authenticate("d", IClass.getClass(DummyEntity.class));
            assertEquals("authenticate-one-dummyentity", op.getOperationName());
        }
    }

    @Nested
    @DisplayName("BusinessOperation selection")
    class BusinessOperationSelection {

        @Test
        void testAuthentication() {
            OperationDefinition op = OperationDefinition.authenticate("x", IClass.getClass(DummyEntity.class));
            assertEquals(BusinessOperation.authenticate, op.getBusinessOperation());
        }

        @Test
        void testUseCase() {
            OperationDefinition op = OperationDefinition.useCase("x", IClass.getClass(DummyEntity.class),
                    uc("myUseCase", TechnicalOperation.read, Scope.oneEntity));
            assertEquals(BusinessOperation.useCase, op.getBusinessOperation());
        }

        @Test
        void testCreate() {
            OperationDefinition op = OperationDefinition.createOne("x", IClass.getClass(DummyEntity.class), false, null, Access.authenticated);
            assertEquals(BusinessOperation.create, op.getBusinessOperation());
        }

        @Test
        void testDeleteOne() {
            OperationDefinition op = OperationDefinition.deleteOne("x", IClass.getClass(DummyEntity.class), false, null, Access.authenticated);
            assertEquals(BusinessOperation.deleteOne, op.getBusinessOperation());
        }

        @Test
        void testDeleteAll() {
            OperationDefinition op = OperationDefinition.deleteAll("x", IClass.getClass(DummyEntity.class), false, null, Access.authenticated);
            assertEquals(BusinessOperation.deleteAll, op.getBusinessOperation());
        }

        @Test
        void testReadAll() {
            OperationDefinition op = OperationDefinition.readAll("x", IClass.getClass(DummyEntity.class), false, null, Access.authenticated);
            assertEquals(BusinessOperation.readAll, op.getBusinessOperation());
        }
    }

    @Nested
    @DisplayName("Equality and hashCode")
    class EqualityTests {

        @Test
        void testEqualsAndHashCode() {
            OperationDefinition op1 = OperationDefinition.readOne("d", IClass.getClass(DummyEntity.class), false, null, Access.authenticated);
            OperationDefinition op2 = OperationDefinition.readOne("d", IClass.getClass(DummyEntity.class), false, null, Access.authenticated);

            assertEquals(op1, op2);
            assertEquals(op1.hashCode(), op2.hashCode());
        }

        @Test
        void testNotEqualsDifferentOperation() {
            OperationDefinition op1 = OperationDefinition.readOne("d", IClass.getClass(DummyEntity.class), false, null, Access.authenticated);
            OperationDefinition op2 = OperationDefinition.createOne("d", IClass.getClass(DummyEntity.class), false, null, Access.authenticated);

            assertNotEquals(op1, op2);
        }
    }

    @Test
    void testToString() {
        OperationDefinition op = OperationDefinition.readOne("domain", IClass.getClass(DummyEntity.class), false, null, Access.authenticated);

        assertEquals(
                "domain-read-one-dummyentity",
                op.toString()
        );
    }

}
