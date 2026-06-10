package com.garganttua.api.core.integ;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.BusinessOperation;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.api.core.integ.crud.AbstractCrudScriptTest;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.core.reflection.IClass;

/**
 * Fail-safe security default (2026-06-10): a domain with no security mention still
 * installs the authorization gate; its operations default to {@code authenticated}, so
 * a request without a token is rejected. Opening a domain is now EXPLICIT — either
 * {@code .security().disable(true)} (the whole gate off) or {@code Access.anonymous}
 * (a specific operation public). Replaces the previous fail-open behaviour where an
 * unconfigured domain (e.g. a {@code .security().key()}-only keys domain) was wide open.
 */
@DisplayName("Security default is fail-safe (no config ⇒ authenticated, not open)")
class SecurityDefaultFailSafeTest extends AbstractCrudScriptTest {

    private IDomain<?> buildUsers(java.util.function.Consumer<com.garganttua.api.commons.context.dsl.IDomainBuilder<User>> securityTweak)
            throws ApiException {
        IApiBuilder builder = newBuilder();
        @SuppressWarnings("unchecked")
        com.garganttua.api.commons.context.dsl.IDomainBuilder<User> domain =
                (com.garganttua.api.commons.context.dsl.IDomainBuilder<User>) builder
                        .domain(IClass.getClass(User.class))
                            .tenant(true)
                            .superTenant("superTenant")
                            .entity().id("id").uuid("uuid").tenantId("tenantId").up()
                            .dto(IClass.getClass(UserDto.class))
                                .id("id").uuid("uuid").tenantId("tenantId").db(new CapturingDao())
                            .up()
                            .readAll(true);
        securityTweak.accept(domain);
        domain.up();
        IApi api = buildAndStart(builder);
        return api.getDomain("users").orElseThrow();
    }

    private static OperationDefinition readAllOp(IDomain<?> domain) {
        return domain.getDomainDefinition().operations().stream()
                .filter(o -> o.getBusinessOperation() == BusinessOperation.readAll)
                .findFirst().orElseThrow(() -> new AssertionError("readAll op must be exposed"));
    }

    /** A readAll request carrying NO authorization token and no caller (anonymous). */
    private static OperationRequest tokenlessReadAll(OperationDefinition op) {
        OperationRequest req = new OperationRequest(new HashMap<>());
        req.arg(IOperationRequest.OPERATION, op);
        return req;
    }

    @Test
    @DisplayName("no security config → readAll defaults to authenticated → a tokenless call is 401")
    void noConfigRequiresAuthentication() throws ApiException {
        IDomain<?> users = buildUsers(d -> { /* no security mention */ });

        OperationDefinition readAll = readAllOp(users);
        assertEquals(Access.authenticated, readAll.access(),
                "an unconfigured operation must default to authenticated");

        IOperationResponse response = users.invoke(tokenlessReadAll(readAll));
        assertEquals(OperationResponseCode.UNAUTHORIZED, response.getResponseCode(),
                "a tokenless call on an unconfigured domain must be rejected 401, not served; got "
                        + response.getResponseCode() + " / " + response.getResponse());
    }

    @Test
    @DisplayName(".security().disable(true) → the gate is off → a tokenless call is served (200)")
    void explicitDisableOpensTheDomain() throws ApiException {
        IDomain<?> users = buildUsers(d -> d.security().disable(true).up());

        IOperationResponse response = users.invoke(tokenlessReadAll(readAllOp(users)));
        assertEquals(OperationResponseCode.OK, response.getResponseCode(),
                "disable(true) must turn the gate off so anonymous traffic is served; got "
                        + response.getResponseCode() + " / " + response.getResponse());
    }

    @Test
    @DisplayName(".readAllAccess(Access.anonymous) → that operation is public → a tokenless call is served (200)")
    void perOperationAnonymousStaysOpen() throws ApiException {
        IDomain<?> users = buildUsers(d -> d.security().readAllAccess(Access.anonymous).up());

        OperationDefinition readAll = readAllOp(users);
        assertEquals(Access.anonymous, readAll.access(),
                "the operation must carry the declared anonymous access");

        IOperationResponse response = users.invoke(tokenlessReadAll(readAll));
        assertEquals(OperationResponseCode.OK, response.getResponseCode(),
                "an explicitly anonymous operation must remain open; got "
                        + response.getResponseCode() + " / " + response.getResponse());
    }
}
