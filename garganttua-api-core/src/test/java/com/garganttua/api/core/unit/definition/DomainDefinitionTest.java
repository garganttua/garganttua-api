package com.garganttua.api.core.unit.definition;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.domain.DomainDefinition;
import com.garganttua.api.core.security.DomainSecurityDefinition;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.BusinessOperation;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.operation.OperationType;
import com.garganttua.api.commons.operation.Scope;
import com.garganttua.api.commons.operation.TechnicalOperation;
import com.garganttua.api.commons.definition.IEntityDefinition;
import com.garganttua.api.commons.definition.IUseCaseDefinition;
import com.garganttua.api.commons.definition.IWorkflowDefinition;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.dsl.ReflectionBuilder;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.core.reflections.ReflectionsAnnotationScanner;

@DisplayName("DomainDefinition Tests")
class DomainDefinitionTest {

    @BeforeAll
    static void initReflection() {
        IClass.setReflection(ReflectionBuilder.builder()
                .withProvider(new RuntimeReflectionProvider())
                .withScanner(new ReflectionsAnnotationScanner())
                .build());
    }

    public static class TestEntity {
        private String id;
        private String uuid;
        public String getId() { return id; }
        public String getUuid() { return uuid; }
    }

    private IEntityDefinition<TestEntity> mockEntityDefinition;

    @BeforeEach
    void setUp() {
        mockEntityDefinition = mock(IEntityDefinition.class);
        when(mockEntityDefinition.entityClass()).thenReturn(IClass.getClass(TestEntity.class));
    }

    private DomainDefinition<TestEntity> createDefinition(
            DomainSecurityDefinition securityDef,
            Map<String, IWorkflowDefinition> workflows) {
        return createDefinition(securityDef, Map.of(), workflows);
    }

    private DomainDefinition<TestEntity> createDefinition(
            DomainSecurityDefinition securityDef,
            Map<String, IUseCaseDefinition> useCases,
            Map<String, IWorkflowDefinition> workflows) {
        return new DomainDefinition<TestEntity>(
                "testentities",
                mockEntityDefinition,
                List.of(),
                List.of(),
                false, false,
                List.of(), List.of(),
                null, null, null, null, null, null, null,
                useCases,
                workflows,
                securityDef,
                null);
    }

    private DomainSecurityDefinition defaultSecurityDef() {
        return new DomainSecurityDefinition(false, null, null);
    }

    private IWorkflowDefinition crudWorkflow(Access access, boolean authority) {
        IWorkflowDefinition wf = mock(IWorkflowDefinition.class);
        when(wf.access()).thenReturn(access);
        when(wf.authority()).thenReturn(authority);
        when(wf.custom()).thenReturn(false);
        return wf;
    }

    private IWorkflowDefinition defaultCrudWorkflow() {
        return crudWorkflow(Access.authenticated, false);
    }

    private Map<String, IWorkflowDefinition> allCrudWorkflows() {
        return Map.of(
                "create", defaultCrudWorkflow(),
                "readAll", defaultCrudWorkflow(),
                "readOne", defaultCrudWorkflow(),
                "update", defaultCrudWorkflow(),
                "deleteOne", defaultCrudWorkflow(),
                "deleteAll", defaultCrudWorkflow());
    }

    @Nested
    @DisplayName("operations() CRUD generation")
    class CrudOperationsGeneration {

        @Test
        @DisplayName("generates operations only for workflows present in the map")
        void generatesOperationsForPresentWorkflows() {
            Map<String, IWorkflowDefinition> workflows = Map.of(
                    "create", defaultCrudWorkflow(),
                    "readAll", defaultCrudWorkflow());

            DomainDefinition<TestEntity> def = createDefinition(
                    defaultSecurityDef(), workflows);

            List<OperationDefinition> ops = def.operations();

            assertEquals(2, ops.size());
            assertTrue(ops.stream().anyMatch(o -> o.getBusinessOperation() == BusinessOperation.create));
            assertTrue(ops.stream().anyMatch(o -> o.getBusinessOperation() == BusinessOperation.readAll));
        }

        @Test
        @DisplayName("generates all 6 operations when all CRUD workflows present")
        void generatesAllOperationsWhenAllPresent() {
            DomainDefinition<TestEntity> def = createDefinition(
                    defaultSecurityDef(), allCrudWorkflows());

            List<OperationDefinition> ops = def.operations();

            assertEquals(6, ops.size());
        }

        @Test
        @DisplayName("generates no operations when no workflows present")
        void generatesNoOperationsWhenNonePresent() {
            DomainDefinition<TestEntity> def = createDefinition(
                    defaultSecurityDef(), Map.of());

            List<OperationDefinition> ops = def.operations();

            assertTrue(ops.isEmpty());
        }

        @Test
        @DisplayName("operations have correct domain name and entity class")
        void operationsHaveCorrectDomainAndEntity() {
            Map<String, IWorkflowDefinition> workflows = Map.of(
                    "create", defaultCrudWorkflow());

            DomainDefinition<TestEntity> def = createDefinition(
                    defaultSecurityDef(), workflows);

            List<OperationDefinition> ops = def.operations();

            assertEquals("testentities", ops.get(0).domainName());
            assertEquals(IClass.getClass(TestEntity.class), ops.get(0).entity());
        }
    }

    @Nested
    @DisplayName("operations() security configuration")
    class SecurityConfiguration {

        @Test
        @DisplayName("uses access levels from workflow definitions")
        void usesAccessFromWorkflowDefinitions() {
            Map<String, IWorkflowDefinition> workflows = Map.of(
                    "create", crudWorkflow(Access.authenticated, false),
                    "readAll", crudWorkflow(Access.authenticated, false),
                    "readOne", crudWorkflow(Access.anonymous, false));

            DomainDefinition<TestEntity> def = createDefinition(
                    defaultSecurityDef(), workflows);

            List<OperationDefinition> ops = def.operations();

            OperationDefinition createOp = ops.stream()
                    .filter(o -> o.getBusinessOperation() == BusinessOperation.create)
                    .findFirst().orElseThrow();
            OperationDefinition readAllOp = ops.stream()
                    .filter(o -> o.getBusinessOperation() == BusinessOperation.readAll)
                    .findFirst().orElseThrow();
            OperationDefinition readOneOp = ops.stream()
                    .filter(o -> o.getBusinessOperation() == BusinessOperation.readOne)
                    .findFirst().orElseThrow();

            assertEquals(Access.authenticated, createOp.access());
            assertEquals(Access.authenticated, readAllOp.access());
            assertEquals(Access.anonymous, readOneOp.access());
        }

        @Test
        @DisplayName("uses authority flags from workflow definitions")
        void usesAuthorityFromWorkflowDefinitions() {
            Map<String, IWorkflowDefinition> workflows = Map.of(
                    "create", crudWorkflow(Access.authenticated, true),
                    "readAll", crudWorkflow(Access.authenticated, false),
                    "readOne", crudWorkflow(Access.authenticated, true));

            DomainDefinition<TestEntity> def = createDefinition(
                    defaultSecurityDef(), workflows);

            List<OperationDefinition> ops = def.operations();

            OperationDefinition createOp = ops.stream()
                    .filter(o -> o.getBusinessOperation() == BusinessOperation.create)
                    .findFirst().orElseThrow();
            OperationDefinition readAllOp = ops.stream()
                    .filter(o -> o.getBusinessOperation() == BusinessOperation.readAll)
                    .findFirst().orElseThrow();
            OperationDefinition readOneOp = ops.stream()
                    .filter(o -> o.getBusinessOperation() == BusinessOperation.readOne)
                    .findFirst().orElseThrow();

            assertTrue(createOp.authority());
            assertFalse(readAllOp.authority());
            assertTrue(readOneOp.authority());
        }
    }

    @Nested
    @DisplayName("operations() workflow generation")
    class WorkflowOperationsGeneration {

        @Test
        @DisplayName("includes custom workflow operations")
        void includesWorkflowOperations() {
            IWorkflowDefinition wfDef = mock(IWorkflowDefinition.class);
            when(wfDef.operation()).thenReturn(TechnicalOperation.update);
            when(wfDef.scope()).thenReturn(Scope.oneEntity);
            when(wfDef.authority()).thenReturn(false);
            when(wfDef.access()).thenReturn(Access.authenticated);
            when(wfDef.custom()).thenReturn(true);

            DomainDefinition<TestEntity> def = createDefinition(
                    defaultSecurityDef(), Map.of(), Map.of("myWorkflow", wfDef));

            List<OperationDefinition> ops = def.operations();

            assertEquals(1, ops.size());
            assertEquals(OperationType.workflow, ops.get(0).type());
            assertEquals(TechnicalOperation.update, ops.get(0).technicalOperation());
            assertEquals(Scope.oneEntity, ops.get(0).scope());
        }

        @Test
        @DisplayName("CRUD workflow generates standard operation, not workflow operation")
        void crudWorkflowGeneratesStandardOperation() {
            DomainDefinition<TestEntity> def = createDefinition(
                    defaultSecurityDef(), Map.of("create", defaultCrudWorkflow()));

            List<OperationDefinition> ops = def.operations();

            assertEquals(1, ops.size());
            assertEquals(OperationType.standard, ops.get(0).type());
            assertEquals(BusinessOperation.create, ops.get(0).getBusinessOperation());
        }

        @Test
        @DisplayName("defaults workflow operation to read and scope to allEntities when null")
        void defaultsWorkflowNullFields() {
            IWorkflowDefinition wfDef = mock(IWorkflowDefinition.class);
            when(wfDef.operation()).thenReturn(null);
            when(wfDef.scope()).thenReturn(null);
            when(wfDef.authority()).thenReturn(false);
            when(wfDef.access()).thenReturn(Access.anonymous);
            when(wfDef.custom()).thenReturn(true);

            DomainDefinition<TestEntity> def = createDefinition(
                    defaultSecurityDef(), Map.of(), Map.of("wf", wfDef));

            List<OperationDefinition> ops = def.operations();

            assertEquals(TechnicalOperation.read, ops.get(0).technicalOperation());
            assertEquals(Scope.allEntities, ops.get(0).scope());
        }
    }

    @Nested
    @DisplayName("operations() use case generation")
    class UseCaseOperationsGeneration {

        @Test
        @DisplayName("includes use case operations")
        void includesUseCaseOperations() {
            IUseCaseDefinition ucDef = mock(IUseCaseDefinition.class);
            when(ucDef.operation()).thenReturn(TechnicalOperation.create);
            when(ucDef.scope()).thenReturn(Scope.oneEntity);
            when(ucDef.authority()).thenReturn(true);
            when(ucDef.access()).thenReturn(Access.authenticated);

            DomainDefinition<TestEntity> def = createDefinition(
                    defaultSecurityDef(), Map.of("myUseCase", ucDef), Map.of());

            List<OperationDefinition> ops = def.operations();

            assertEquals(1, ops.size());
            assertEquals(OperationType.usesCase, ops.get(0).type());
            assertEquals(TechnicalOperation.create, ops.get(0).technicalOperation());
            assertTrue(ops.get(0).authority());
            assertEquals(Access.authenticated, ops.get(0).access());
        }

        @Test
        @DisplayName("defaults use case operation to read and scope to allEntities when null")
        void defaultsUseCaseNullFields() {
            IUseCaseDefinition ucDef = mock(IUseCaseDefinition.class);
            when(ucDef.operation()).thenReturn(null);
            when(ucDef.scope()).thenReturn(null);
            when(ucDef.authority()).thenReturn(false);
            when(ucDef.access()).thenReturn(Access.authenticated);

            DomainDefinition<TestEntity> def = createDefinition(
                    defaultSecurityDef(), Map.of("uc", ucDef), Map.of());

            List<OperationDefinition> ops = def.operations();

            assertEquals(TechnicalOperation.read, ops.get(0).technicalOperation());
            assertEquals(Scope.allEntities, ops.get(0).scope());
        }
    }

    @Nested
    @DisplayName("operations() combined")
    class CombinedOperations {

        @Test
        @DisplayName("combines CRUD, workflow and use case operations")
        void combinesCrudWorkflowAndUseCaseOperations() {
            IWorkflowDefinition customWfDef = mock(IWorkflowDefinition.class);
            when(customWfDef.operation()).thenReturn(TechnicalOperation.read);
            when(customWfDef.scope()).thenReturn(Scope.allEntities);
            when(customWfDef.authority()).thenReturn(false);
            when(customWfDef.access()).thenReturn(Access.authenticated);
            when(customWfDef.custom()).thenReturn(true);

            IUseCaseDefinition ucDef = mock(IUseCaseDefinition.class);
            when(ucDef.operation()).thenReturn(TechnicalOperation.create);
            when(ucDef.scope()).thenReturn(Scope.oneEntity);
            when(ucDef.authority()).thenReturn(false);
            when(ucDef.access()).thenReturn(Access.authenticated);

            // 2 CRUD workflows + 1 custom workflow
            Map<String, IWorkflowDefinition> workflows = new java.util.HashMap<>();
            workflows.put("create", defaultCrudWorkflow());
            workflows.put("readAll", defaultCrudWorkflow());
            workflows.put("wf1", customWfDef);

            DomainDefinition<TestEntity> def = createDefinition(
                    defaultSecurityDef(), Map.of("uc1", ucDef), workflows);

            List<OperationDefinition> ops = def.operations();

            // 2 CRUD + 1 custom workflow + 1 use case
            assertEquals(4, ops.size());
            assertEquals(2, ops.stream().filter(o -> o.type() == OperationType.standard).count());
            assertEquals(1, ops.stream().filter(o -> o.type() == OperationType.workflow).count());
            assertEquals(1, ops.stream().filter(o -> o.type() == OperationType.usesCase).count());
        }
    }
}
