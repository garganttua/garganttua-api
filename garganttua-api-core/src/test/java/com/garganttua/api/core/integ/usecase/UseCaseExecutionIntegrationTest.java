package com.garganttua.api.core.integ.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.BusinessOperation;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.usecase.injection.UseCaseInput;
import com.garganttua.api.core.integ.crud.AbstractCrudScriptTest;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.workflow.WorkflowResult;

/**
 * End-to-end execution of a domain use case through the assembled pipeline (increments 2 & 3):
 * a use case bound to a free method whose {@code @UseCaseInput} parameter receives the request body,
 * routed by the {@code USE_CASE.gs} business stage ({@code invokeUseCase}) and returning its result.
 */
@DisplayName("Use Case Execution (pipeline)")
class UseCaseExecutionIntegrationTest extends AbstractCrudScriptTest {

    // ───── Use-case input / output POJOs ─────

    public static class GreetingInput {
        private String name;
        public GreetingInput() {}
        public GreetingInput(String name) { this.name = name; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class GreetingOutput {
        private String message;
        private int length;
        public GreetingOutput() {}
        public GreetingOutput(String message, int length) { this.message = message; this.length = length; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public int getLength() { return length; }
        public void setLength(int length) { this.length = length; }
    }

    /** The bound service — a "completely free" method: it declares only its @UseCaseInput parameter. */
    public static class GreetingService {
        public GreetingOutput greet(@UseCaseInput GreetingInput input) {
            String name = (input != null && input.getName() != null) ? input.getName() : "stranger";
            String message = "Hello, " + name + "!";
            return new GreetingOutput(message, message.length());
        }

        /** A second, equally free method: it declares @UseCaseInput AND @DomainContext, both auto-wired. */
        public GreetingOutput describe(@UseCaseInput GreetingInput input,
                @com.garganttua.api.commons.security.injection.DomainContext IDomain<?> domain) {
            String name = (input != null && input.getName() != null) ? input.getName() : "stranger";
            String message = name + "@" + domain.getDomainName();
            return new GreetingOutput(message, message.length());
        }
    }

    private IApi context;
    private IDomain<?> userCtx;

    @BeforeEach
    void setUp() throws ApiException {
        IApiBuilder builder = newBuilder();
        builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(new CapturingDao())
                .up()
                .useCase("greet", IClass.getClass(GreetingInput.class), IClass.getClass(GreetingOutput.class))
                    .bind(new GreetingService())
                        .method("greet", IClass.getClass(GreetingOutput.class), IClass.getClass(GreetingInput.class))
                    .up()
                .up()
                .useCase("describe", IClass.getClass(GreetingInput.class), IClass.getClass(GreetingOutput.class))
                    .bind(new GreetingService())
                        .method("describe", IClass.getClass(GreetingOutput.class),
                                IClass.getClass(GreetingInput.class),
                                IClass.getClass(IDomain.class))
                    .up()
                .up()
                .security().disable(true).up()
            .up();

        context = buildAndStart(builder);
        userCtx = context.getDomain("users").orElseThrow();
    }

    private OperationDefinition greetOperation() {
        return useCaseOperation("greet");
    }

    private OperationDefinition useCaseOperation(String name) {
        return userCtx.getDomainDefinition().operations().stream()
                .filter(op -> op.getBusinessOperation() == BusinessOperation.useCase)
                .filter(op -> name.equals(op.useCaseName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(name + " use-case operation not exposed in operations()"));
    }

    @Test
    @DisplayName("a free method declaring @UseCaseInput AND @DomainContext gets both auto-wired")
    void useCaseAutowiresSeveralFrameworkParams() throws ApiException {
        OperationDefinition describeOp = useCaseOperation("describe");
        OperationRequest request = superTenantScriptRequest(describeOp);
        request.arg("entity", new GreetingInput("Alice"));

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess(), () -> {
            var workflow = (com.garganttua.core.workflow.Workflow) userCtx.getWorkflow();
            return "Workflow failed with code " + result.code()
                    + "\nGenerated script:\n" + workflow.getGeneratedScript();
        });
        GreetingOutput output = assertInstanceOf(GreetingOutput.class, result.output());
        // The body fed @UseCaseInput (Alice) AND the @DomainContext was injected (the "users" domain).
        assertEquals("Alice@users", output.getMessage());
    }

    @Test
    @DisplayName("the use-case business stage invokes the bound method with the request body and returns its result")
    void useCaseInvokesBoundMethodWithBody() throws ApiException {
        OperationDefinition greetOp = greetOperation();
        OperationRequest request = superTenantScriptRequest(greetOp);
        request.arg("entity", new GreetingInput("Alice"));

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess(), () -> {
            var workflow = (com.garganttua.core.workflow.Workflow) userCtx.getWorkflow();
            return "Workflow failed with code " + result.code()
                    + "\nGenerated script:\n" + workflow.getGeneratedScript();
        });
        assertEquals(0, result.code());
        assertNotNull(result.output(), "use case must produce an output");
        GreetingOutput output = assertInstanceOf(GreetingOutput.class, result.output());
        assertEquals("Hello, Alice!", output.getMessage());
        assertEquals("Hello, Alice!".length(), output.getLength());
    }

    @Test
    @DisplayName("a different body produces a different result through the same bound method")
    void useCaseHonoursTheBody() throws ApiException {
        OperationDefinition greetOp = greetOperation();
        OperationRequest request = superTenantScriptRequest(greetOp);
        request.arg("entity", new GreetingInput("Versailles"));

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess(), () -> "Workflow failed with code " + result.code());
        GreetingOutput output = assertInstanceOf(GreetingOutput.class, result.output());
        assertEquals("Hello, Versailles!", output.getMessage());
        assertEquals(18, output.getLength());
    }

    @Test
    @DisplayName("the greet use case is exposed as a routable operation carrying its rich definition")
    void greetOperationCarriesRichDefinition() {
        OperationDefinition greetOp = greetOperation();
        assertNotNull(greetOp.useCase(), "the operation must carry the use-case definition");
        assertEquals("greet", greetOp.useCase().name());
        assertEquals(IClass.getClass(GreetingInput.class), greetOp.useCase().inputType());
        assertEquals(IClass.getClass(GreetingOutput.class), greetOp.useCase().outputType());
        assertNotNull(greetOp.useCase().binder(), "the built method binder must be carried on the definition");
        // Default route: GET /users/greet (read verb, allEntities scope → no ${uuid} segment).
        assertEquals("/users/greet", greetOp.getPath().path());
    }
}
