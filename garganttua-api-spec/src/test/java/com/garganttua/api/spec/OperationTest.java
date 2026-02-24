package com.garganttua.api.spec;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.spec.context.Access;
import com.garganttua.api.spec.context.BusinessOperation;
import com.garganttua.api.spec.context.Operation;
import com.garganttua.api.spec.context.OperationType;
import com.garganttua.api.spec.context.Scope;
import com.garganttua.api.spec.context.TechnicalOperation;

class OperationTest {

    static class DummyEntity {}

    @Nested
    @DisplayName("Factory methods")
    class FactoryMethods {

        @Test
        void testReadOne() {
            Operation op = Operation.readOne("test", DummyEntity.class, false, Access.authenticated);

            assertEquals("test", op.domainName());
            assertEquals(TechnicalOperation.read, op.operation());
            assertEquals(DummyEntity.class, op.entity());
            assertEquals(Scope.oneEntity, op.scope());
            assertEquals(OperationType.standard, op.type());
        }

        @Test
        void testCreateOne() {
            Operation op = Operation.createOne("domain", DummyEntity.class, false, Access.authenticated);

            assertEquals(TechnicalOperation.create, op.operation());
            assertEquals(Scope.oneEntity, op.scope());
        }

        @Test
        void testDeleteAll() {
            Operation op = Operation.deleteAll("x", DummyEntity.class, false, Access.authenticated);

            assertEquals(TechnicalOperation.delete, op.operation());
            assertEquals(Scope.allEntities, op.scope());
        }

        @Test
        void testUseCase() {
            Operation op = Operation.useCase("domain", TechnicalOperation.read, DummyEntity.class, Scope.oneEntity, false, Access.authenticated);

            assertEquals(OperationType.usesCase, op.type());
        }

        @Test
        void testAuthenticate() {
            Operation op = Operation.authenticate("sec", DummyEntity.class);

            assertEquals(OperationType.authentication, op.type());
            assertEquals(TechnicalOperation.create, op.operation());
        }
    }

    @Nested
    @DisplayName("Path generation")
    class PathGeneration {

        @Test
        void testPathOneEntity() {
            Operation op = Operation.readOne("d", DummyEntity.class, false, Access.authenticated);
            assertEquals("/dummyentities/${uuid}", op.getPath().path());
            assertEquals("dummyentities", op.getPath().domain());
            assertEquals("${uuid}", op.getPath().suffix());
        }

        @Test
        void testPathAllEntities() {
            Operation op = Operation.readAll("d", DummyEntity.class, false, Access.authenticated);
            assertEquals("/dummyentities", op.getPath().path());
            assertEquals("dummyentities", op.getPath().domain());
            assertNull(op.getPath().suffix());
        }

        @Test
        void testPathAuthenticate() {
            Operation op = Operation.authenticate("d", DummyEntity.class);
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
            Operation op = Operation.readOne("d", DummyEntity.class, false, Access.authenticated);
            assertEquals("read-one-dummyentity", op.getOperationName());
        }

        @Test
        void testOperationNameReadAll() {
            Operation op = Operation.readAll("d", DummyEntity.class, false, Access.authenticated);
            assertEquals("read-all-dummyentities", op.getOperationName());
        }

        @Test
        void testOperationNameAuthenticate() {
            Operation op = Operation.authenticate("d", DummyEntity.class);
            assertEquals("authenticate-one-dummyentity", op.getOperationName());
        }
    }

    @Nested
    @DisplayName("BusinessOperation selection")
    class BusinessOperationSelection {

        @Test
        void testAuthentication() {
            Operation op = Operation.authenticate("x", DummyEntity.class);
            assertEquals(BusinessOperation.authenticate, op.getBusinessOperation());
        }

        @Test
        void testUseCase() {
            Operation op = Operation.useCase("x", TechnicalOperation.read, DummyEntity.class, Scope.oneEntity, false, Access.authenticated);
            assertEquals(BusinessOperation.useCase, op.getBusinessOperation());
        }

        @Test
        void testCreate() {
            Operation op = Operation.createOne("x", DummyEntity.class, false, Access.authenticated);
            assertEquals(BusinessOperation.create, op.getBusinessOperation());
        }

        @Test
        void testDeleteOne() {
            Operation op = Operation.deleteOne("x", DummyEntity.class, false, Access.authenticated);
            assertEquals(BusinessOperation.deleteOne, op.getBusinessOperation());
        }

        @Test
        void testDeleteAll() {
            Operation op = Operation.deleteAll("x", DummyEntity.class, false, Access.authenticated);
            assertEquals(BusinessOperation.deleteAll, op.getBusinessOperation());
        }

        @Test
        void testReadAll() {
            Operation op = Operation.readAll("x", DummyEntity.class, false, Access.authenticated);
            assertEquals(BusinessOperation.readAll, op.getBusinessOperation());
        }
    }

    @Nested
    @DisplayName("Equality and hashCode")
    class EqualityTests {

        @Test
        void testEqualsAndHashCode() {
            Operation op1 = Operation.readOne("d", DummyEntity.class, false, Access.authenticated);
            Operation op2 = Operation.readOne("d", DummyEntity.class, false, Access.authenticated);

            assertEquals(op1, op2);
            assertEquals(op1.hashCode(), op2.hashCode());
        }

        @Test
        void testNotEqualsDifferentOperation() {
            Operation op1 = Operation.readOne("d", DummyEntity.class, false, Access.authenticated);
            Operation op2 = Operation.createOne("d", DummyEntity.class, false, Access.authenticated);

            assertNotEquals(op1, op2);
        }
    }

    @Test
    void testToString() {
        Operation op = Operation.readOne("domain", DummyEntity.class, false, Access.authenticated);

        assertEquals(
                "domain-read-one-dummyentity",
                op.toString()
        );
    }

}
