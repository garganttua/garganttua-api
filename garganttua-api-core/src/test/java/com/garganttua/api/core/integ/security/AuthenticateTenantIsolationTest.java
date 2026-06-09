package com.garganttua.api.core.integ.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.integ.crud.AbstractCrudScriptTest;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.core.security.authentication.AuthenticateCredentialsSupplierBuilder;
import com.garganttua.api.core.security.authentication.AuthenticationRequest;
import com.garganttua.api.core.security.authentication.AuthenticatorDefinitionSupplierBuilder;
import com.garganttua.api.core.security.authentication.PrincipalSupplier;
import com.garganttua.api.core.security.authentication.PrincipalSupplierBuilder;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.security.authenticator.AuthenticatorScope;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.SupplyException;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;

/**
 * Tenant isolation at authentication for a {@code scope=tenant} authenticator,
 * exercised directly on {@link PrincipalSupplier} (the one place that resolves the
 * principal).
 * <p>
 * The principal lookup queries the repository with no caller, so it does not
 * tenant-scope on its own — a same-login user from <em>any</em> tenant would be
 * resolved. {@code PrincipalSupplier} now AND-s a {@code tenantId == caller's
 * tenant} predicate into the login filter for tenant scope — the caller's tenant
 * being the one {@code requireCallerTenantForScope} already mandates (the request
 * TENANT_ID arg, set over HTTP from the X-Tenant-Id header). For
 * {@code scope=system} the lookup stays global.
 * <p>
 * The supplier is driven through a mocked {@link IRuntimeContext} so the assertion
 * is on the resolved principal itself, not on the assembled-workflow output.
 */
@DisplayName("Authentication tenant isolation (PrincipalSupplier, scope=tenant)")
class AuthenticateTenantIsolationTest extends AbstractCrudScriptTest {

    /** Builds a User domain whose authenticator uses the given scope; login is the {@code id} field. */
    private IDomain<?> buildUsersApi(AuthenticatorScope scope, CapturingDao dao) throws ApiException {
        StubAuthentication stubAuth = new StubAuthentication();

        IApiBuilder builder = newBuilder();
        var authBuilder = builder.security()
                .authentication(new FixedSupplierBuilder<>(stubAuth, IClass.getClass(StubAuthentication.class)));
        authBuilder.authenticate("authenticate")
                .withParam(0, new PrincipalSupplierBuilder())
                .withParam(1, new AuthenticateCredentialsSupplierBuilder())
                .withParam(2, new AuthenticatorDefinitionSupplierBuilder());
        authBuilder.up();

        builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(dao)
                .up()
                .security()
                    .authenticator()
                        .login("id")
                        .scope(scope)
                        .alwaysEnabled(true)
                        .authentication(authBuilder)
                    .up()
                .up()
            .up();

        IApi api = buildAndStart(builder);
        return api.getDomain("users").orElseThrow();
    }

    /** Seeds a user DTO (login = id) in the given tenant. */
    private static void seed(CapturingDao dao, String login, String uuid, String tenantId) {
        UserDto u = new UserDto();
        u.setId(login);
        u.setUuid(uuid);
        u.setTenantId(tenantId);
        u.setName("user-" + uuid);
        dao.save(u);
    }

    /** Drives PrincipalSupplier with a request carrying the given caller tenant. */
    private static Optional<Object> resolvePrincipal(IDomain<?> users, String callerTenant) throws SupplyException {
        OperationDefinition authOp = OperationDefinition.authenticate("users", IClass.getClass(User.class));
        OperationRequest request = new OperationRequest(new HashMap<>());
        request.arg(IOperationRequest.OPERATION, authOp);
        request.arg(IOperationRequest.TENANT_ID, callerTenant);
        request.arg("entity", new AuthenticationRequest(
                "john@example.com", "valid-password".getBytes(StandardCharsets.UTF_8)));

        IRuntimeContext ctx = mock(IRuntimeContext.class);
        doReturn(Optional.of(request)).when(ctx).getVariable(eq("request"), any());
        doReturn(Optional.of(users)).when(ctx).getVariable(eq("domainContext"), any());

        return new PrincipalSupplier().supply(ctx);
    }

    private static String uuidOf(Object principal) {
        return (String) DefaultMapper.reflection().getFieldValue(principal, "uuid");
    }

    @Test
    @DisplayName("scope=tenant: the caller's tenant scopes the lookup — resolves the user OF THAT TENANT")
    void resolvesTheCallerTenantUser() throws Exception {
        CapturingDao dao = new CapturingDao();
        IDomain<?> users = buildUsersApi(AuthenticatorScope.tenant, dao);
        // SAME login in two different tenants.
        seed(dao, "john@example.com", "john-acme", "acme");
        seed(dao, "john@example.com", "john-globex", "globex");

        Object principal = resolvePrincipal(users, "acme").orElseThrow();

        assertEquals("john-acme", uuidOf(principal),
                "must resolve the 'acme' user, never the same-login user of 'globex'");
    }

    @Test
    @DisplayName("scope=tenant: the SAME login resolves independently in each of its tenants")
    void eachTenantResolvesItsOwnUser() throws Exception {
        CapturingDao dao = new CapturingDao();
        IDomain<?> users = buildUsersApi(AuthenticatorScope.tenant, dao);
        seed(dao, "john@example.com", "john-acme", "acme");
        seed(dao, "john@example.com", "john-globex", "globex");

        assertEquals("john-acme", uuidOf(resolvePrincipal(users, "acme").orElseThrow()),
                "caller tenant 'acme' → the acme user");
        assertEquals("john-globex", uuidOf(resolvePrincipal(users, "globex").orElseThrow()),
                "caller tenant 'globex' → the globex user");
    }

    @Test
    @DisplayName("scope=tenant: a user that exists ONLY in another tenant is NOT resolved (isolation)")
    void rejectsCrossTenantLogin() throws Exception {
        CapturingDao dao = new CapturingDao();
        IDomain<?> users = buildUsersApi(AuthenticatorScope.tenant, dao);
        // john exists ONLY in 'acme'.
        seed(dao, "john@example.com", "john-acme", "acme");

        // Caller carries 'globex' — before the fix the super lookup found john regardless.
        SupplyException ex = assertThrows(SupplyException.class, () -> resolvePrincipal(users, "globex"),
                "a user of tenant 'acme' must NOT be resolvable against tenant 'globex'");
        assertTrue(ex.getMessage().contains("User not found"),
                "must fail because the login is absent in the caller's tenant (scoped lookup); got: "
                        + ex.getMessage());
    }

    @Test
    @DisplayName("scope=system: the lookup is NOT tenant-scoped — a user of any tenant is resolved")
    void systemScopeIsNotTenantIsolated() throws Exception {
        CapturingDao dao = new CapturingDao();
        IDomain<?> users = buildUsersApi(AuthenticatorScope.system, dao);
        seed(dao, "john@example.com", "john-acme", "acme");

        // Caller carries a DIFFERENT tenant; with system scope there is no tenant predicate.
        Object principal = resolvePrincipal(users, "globex").orElseThrow();

        assertEquals("john-acme", uuidOf(principal),
                "system scope resolves the login regardless of the caller's tenant");
    }
}
