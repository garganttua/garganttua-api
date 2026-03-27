package com.garganttua.api.core.integ.crud;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.api.spec.operation.OperationDefinition;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.workflow.WorkflowResult;

@DisplayName("CRUD operation toggle tests")
class CrudToggleIntegrationTest extends AbstractCrudScriptTest {

    private IDomainContext<?> buildDomain(DomainConfigurator configurator) throws ApiException {
        CapturingDao dao = new CapturingDao();
        IApiContextBuilder builder = newBuilder();
        var domainBuilder = builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(dao)
                .up();

        configurator.configure(domainBuilder);
        domainBuilder.up();

        IApiContext context = buildAndStart(builder);
        return context.getDomainContext("users").orElseThrow();
    }

    @FunctionalInterface
    interface DomainConfigurator {
        void configure(com.garganttua.api.spec.context.dsl.IDomainBuilder<?> builder);
    }

    private WorkflowResult executeOperation(IDomainContext<?> ctx, OperationDefinition op) {
        var request = superTenantScriptRequest(op);
        User user = new User();
        user.setName("test");
        request.arg("entity", user);
        return executeScript(ctx, request);
    }

    @Nested
    @DisplayName("All CRUD enabled by default")
    class AllEnabledByDefault {

        @Test
        @DisplayName("createOne is enabled by default")
        void createEnabled() throws ApiException {
            IDomainContext<?> ctx = buildDomain(b -> {});
            WorkflowResult result = executeOperation(ctx,
                    OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class)));
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("readAll is enabled by default")
        void readAllEnabled() throws ApiException {
            IDomainContext<?> ctx = buildDomain(b -> {});
            WorkflowResult result = executeOperation(ctx,
                    OperationDefinition.readAllWithStandardSecurity("users", IClass.getClass(User.class)));
            assertTrue(result.isSuccess());
        }
    }

    @Nested
    @DisplayName("Disabling individual operations")
    class DisablingOperations {

        @Test
        @DisplayName("creation(false) disables createOne")
        void disableCreation() throws ApiException {
            IDomainContext<?> ctx = buildDomain(b -> b.creation(false));
            WorkflowResult result = executeOperation(ctx,
                    OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class)));
            assertFalse(result.isSuccess());
            assertEquals(405, result.code(), "disabled operation should return 405 Method Not Allowed");
        }

        @Test
        @DisplayName("readAll(false) disables readAll")
        void disableReadAll() throws ApiException {
            IDomainContext<?> ctx = buildDomain(b -> b.readAll(false));
            WorkflowResult result = executeOperation(ctx,
                    OperationDefinition.readAllWithStandardSecurity("users", IClass.getClass(User.class)));
            assertFalse(result.isSuccess());
            assertEquals(405, result.code());
        }

        @Test
        @DisplayName("readOne(false) disables readOne")
        void disableReadOne() throws ApiException {
            IDomainContext<?> ctx = buildDomain(b -> b.readOne(false));
            WorkflowResult result = executeOperation(ctx,
                    OperationDefinition.readOneWithStandardSecurity("users", IClass.getClass(User.class)));
            assertFalse(result.isSuccess());
            assertEquals(405, result.code());
        }

        @Test
        @DisplayName("update(false) disables updateOne")
        void disableUpdate() throws ApiException {
            IDomainContext<?> ctx = buildDomain(b -> b.update(false));
            WorkflowResult result = executeOperation(ctx,
                    OperationDefinition.updateOneWithStandardSecurity("users", IClass.getClass(User.class)));
            assertFalse(result.isSuccess());
            assertEquals(405, result.code());
        }

        @Test
        @DisplayName("deleteOne(false) disables deleteOne")
        void disableDeleteOne() throws ApiException {
            IDomainContext<?> ctx = buildDomain(b -> b.deleteOne(false));
            WorkflowResult result = executeOperation(ctx,
                    OperationDefinition.deleteOneWithStandardSecurity("users", IClass.getClass(User.class)));
            assertFalse(result.isSuccess());
            assertEquals(405, result.code());
        }

        @Test
        @DisplayName("deleteAll(false) disables deleteAll")
        void disableDeleteAll() throws ApiException {
            IDomainContext<?> ctx = buildDomain(b -> b.deleteAll(false));
            WorkflowResult result = executeOperation(ctx,
                    OperationDefinition.deleteAllWithStandardSecurity("users", IClass.getClass(User.class)));
            assertFalse(result.isSuccess());
            assertEquals(405, result.code());
        }
    }

    @Nested
    @DisplayName("Partial disable")
    class PartialDisable {

        @Test
        @DisplayName("disabling creation does not affect readAll")
        void disableCreationKeepsRead() throws ApiException {
            IDomainContext<?> ctx = buildDomain(b -> b.creation(false));
            WorkflowResult result = executeOperation(ctx,
                    OperationDefinition.readAllWithStandardSecurity("users", IClass.getClass(User.class)));
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("disabling deleteAll does not affect createOne")
        void disableDeleteKeepsCreate() throws ApiException {
            IDomainContext<?> ctx = buildDomain(b -> b.deleteAll(false));
            WorkflowResult result = executeOperation(ctx,
                    OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class)));
            assertTrue(result.isSuccess());
        }
    }
}
