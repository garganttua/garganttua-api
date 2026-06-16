package com.garganttua.api.core.integ.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.usecase.injection.UseCaseInput;
import com.garganttua.api.core.integ.crud.AbstractCrudScriptTest;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.workflow.WorkflowResult;

/**
 * Regression: declaring a use case on a domain must NOT break that domain's CRUD operations.
 *
 * <p>The use-case business stage is guarded by {@code businessOperation == useCase}; for a plain
 * CRUD request (e.g. the create of an admin seed) the use-case stage is skipped. The exit-code
 * aggregation must still receive a non-null code for the skipped stage (pass-through 405 from
 * {@code init-codes}), otherwise the create surfaces a {@code SupplyException} / SERVER_ERROR.
 */
@DisplayName("CRUD on a use-case-bearing domain")
class CrudOnUseCaseDomainIntegrationTest extends AbstractCrudScriptTest {

    public static class GreetingInput {
        private String name;
        public GreetingInput() {}
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class GreetingOutput {
        private String message;
        public GreetingOutput() {}
        public GreetingOutput(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class GreetingService {
        public GreetingOutput greet(@UseCaseInput GreetingInput input) {
            return new GreetingOutput("Hello, " + (input != null ? input.getName() : "stranger") + "!");
        }
    }

    private IApi context;
    private IDomain<?> userCtx;
    private CapturingDao userDao;

    @BeforeEach
    void setUp() throws ApiException {
        userDao = new CapturingDao();
        IApiBuilder builder = newBuilder();
        builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .mandatory("name")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(userDao)
                .up()
                .useCase("greet", IClass.getClass(GreetingInput.class), IClass.getClass(GreetingOutput.class))
                    .bind(new GreetingService())
                        .method("greet", IClass.getClass(GreetingOutput.class), IClass.getClass(GreetingInput.class))
                    .up()
                .up()
                .security().disable(true).up()
            .up();

        context = buildAndStart(builder);
        userCtx = context.getDomain("users").orElseThrow();
    }

    @Test
    @DisplayName("createOne succeeds even though the domain also declares a use case")
    void createOneSucceedsOnUseCaseDomain() throws ApiException {
        User user = new User();
        user.setName("Alice");

        OperationDefinition createOp =
                OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(createOp);
        request.arg("entity", user);

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess(), () -> "Workflow failed with code " + result.code()
                + " exception=" + result.exception().map(Throwable::toString).orElse("none")
                + " message=" + result.exceptionMessage().orElse("none"));
        assertEquals(0, result.code());
        assertNotNull(result.output());
        User output = (User) result.output();
        assertEquals("Alice", output.getName());
        assertNotNull(output.getUuid(), "UUID should have been generated");
        assertNotNull(userDao.getLastSaved(), "entity must be persisted");
    }
}
