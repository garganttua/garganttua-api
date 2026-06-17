package com.garganttua.api.core.integ.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.context.dsl.IEntityBuilder;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.security.injection.DomainContext;
import com.garganttua.api.core.integ.crud.AbstractCrudScriptTest;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IMethod;
import com.garganttua.core.workflow.WorkflowResult;

/**
 * Proves {@code entity().beforeCreate(IMethod)} (and the other hook overloads) bind the EXACT method
 * provided — its external declaring class, its static nature, and its signature — feeding it the
 * current entity (and injected framework context), rather than re-resolving a no-arg method by name
 * on the entity. The reported bug was {@code ReflectionException: No overload of method … on entity}.
 */
@DisplayName("Free lifecycle-hook binding (beforeCreate(IMethod) → external method)")
class EntityHookFreeBindingIntegrationTest extends AbstractCrudScriptTest {

    /** An EXTERNAL validator — its logic lives off the entity (here a static method taking the entity). */
    public static final class UserValidator {
        static volatile Object lastSeen;
        static volatile String lastDomainName;

        public static void requireName(User user) {
            lastSeen = user;
            if (user == null || user.getName() == null) {
                throw new ApiException("name is required (external validator)");
            }
        }

        public static void requireNameWithDomain(User user, @DomainContext IDomain<?> domain) {
            lastDomainName = (domain != null) ? domain.getDomainName() : null;
            if (user == null || user.getName() == null) {
                throw new ApiException("name is required");
            }
        }
    }

    @BeforeEach
    void resetCaptures() {
        UserValidator.lastSeen = null;
        UserValidator.lastDomainName = null;
    }

    private static IMethod method(String name, IClass<?>... params) throws Exception {
        return IClass.getClass(UserValidator.class).getMethod(name, params);
    }

    /** Builds a users domain whose entity carries the given beforeCreate free hook, security off. */
    private IDomain<?> userDomainWithBeforeCreate(IMethod hook) throws Exception {
        IApiBuilder builder = newBuilder();
        IEntityBuilder<User> entity = (IEntityBuilder<User>) (IEntityBuilder<?>) builder
                .domain(IClass.getClass(User.class))
                    .tenant(true)
                    .superTenant("superTenant")
                    .entity()
                        .id("id").uuid("uuid").tenantId("tenantId");
        entity.beforeCreate(hook);
        entity.up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(new CapturingDao())
                .up()
                .security().disable(true).up()
            .up();
        IApi context = buildAndStart(builder);
        return context.getDomain("users").orElseThrow();
    }

    private WorkflowResult create(IDomain<?> userCtx, User user) {
        OperationDefinition createOp = OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(createOp);
        request.arg("entity", user);
        return executeScript(userCtx, request);
    }

    @Test
    @DisplayName("the external static validator is invoked and its rejection fails the create (vs ReflectionException)")
    void externalValidatorRejectsInvalidEntity() throws Exception {
        IDomain<?> userCtx = userDomainWithBeforeCreate(method("requireName", IClass.getClass(User.class)));

        User invalid = new User(); // no name
        invalid.setEmail("x@y.z");
        WorkflowResult result = create(userCtx, invalid);

        assertFalse(result.isSuccess(), "the external validator's rejection must fail the create");
        assertEquals(500, result.code(), "a thrown hook maps to the runBeforeCreate route (500, kept)");
        assertSame(invalid, UserValidator.lastSeen,
                "the external validator must have run on the invalid entity (vs Reflection: No overload on entity)");
    }

    @Test
    @DisplayName("a valid entity passes the external validator and is created")
    void externalValidatorAcceptsValidEntity() throws Exception {
        IDomain<?> userCtx = userDomainWithBeforeCreate(method("requireName", IClass.getClass(User.class)));

        User valid = new User();
        valid.setName("Alice");
        WorkflowResult result = create(userCtx, valid);

        assertTrue(result.isSuccess(), () -> "valid create must pass; code=" + result.code());
        User output = assertInstanceOf(User.class, result.output());
        assertEquals("Alice", output.getName());
    }

    @Test
    @DisplayName("the hook receives the CURRENT entity (post uuid/tenant stamping), passed as its argument")
    void hookReceivesTheCurrentEntity() throws Exception {
        IDomain<?> userCtx = userDomainWithBeforeCreate(method("requireName", IClass.getClass(User.class)));

        User valid = new User();
        valid.setName("Bob");
        WorkflowResult result = create(userCtx, valid);

        assertTrue(result.isSuccess());
        assertSame(result.output(), UserValidator.lastSeen, "the validator saw the very entity the pipeline produced");
        User seen = assertInstanceOf(User.class, UserValidator.lastSeen);
        assertEquals("Bob", seen.getName());
        assertNotNull(seen.getUuid(), "the entity passed to the hook is post-stamping (uuid assigned)");
    }

    @Test
    @DisplayName("a hook method can also declare an injected @DomainContext parameter (resolved like a use case)")
    void hookInjectsDomainContext() throws Exception {
        IDomain<?> userCtx = userDomainWithBeforeCreate(
                method("requireNameWithDomain", IClass.getClass(User.class), IClass.getClass(IDomain.class)));

        User valid = new User();
        valid.setName("Carol");
        WorkflowResult result = create(userCtx, valid);

        assertTrue(result.isSuccess(), () -> "code=" + result.code());
        assertEquals("users", UserValidator.lastDomainName, "the @DomainContext parameter must be injected");
    }
}
