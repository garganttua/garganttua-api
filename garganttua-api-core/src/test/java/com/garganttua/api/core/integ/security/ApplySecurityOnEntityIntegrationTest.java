package com.garganttua.api.core.integ.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.integ.crud.AbstractCrudScriptTest;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.BusinessOperation;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.security.authentication.Authentication;
import com.garganttua.api.commons.security.authentication.IAuthentication;
import com.garganttua.api.commons.security.authenticator.AuthenticatorScope;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;
import com.garganttua.core.workflow.WorkflowResult;

/**
 * The custom {@code .authentication(...).applySecurityOnEntity(method)} runs on CREATE and
 * UPDATE of an authenticator entity (after validation, before persist) — here it marks the
 * {@code name} field, standing in for e.g. hashing a password. Proves the entity actually
 * persisted carries the secured value.
 */
@DisplayName("applySecurityOnEntity runs on create/update of an authenticator entity")
class ApplySecurityOnEntityIntegrationTest extends AbstractCrudScriptTest {

    /** Authentication strategy carrying a no-op authenticate + the entity-securing method. */
    public static class SecuringStrategy {
        public IAuthentication authenticate() {
            return new Authentication(true, null, null, null, List.of(), null, null, false, false, true, true, true, true);
        }

        /** Secures the entity in place: prefixes the name (stands in for hashing a password). */
        public void secure(User entity) {
            if (entity.getName() != null && !entity.getName().startsWith("secured:")) {
                entity.setName("secured:" + entity.getName());
            }
        }
    }

    private IApi context;
    private IDomain<?> userCtx;
    private CapturingDao userDao;

    @BeforeEach
    void setUp() throws ApiException {
        userDao = new CapturingDao();
        IApiBuilder builder = newBuilder();

        // Single fluent chain across TWO binder segments (authenticate then applySecurityOnEntity),
        // each ending in up(). This compiles thanks to the parameterized Link on
        // IAuthenticationMethodBinderBuilder — up() no longer returns a RAW IAuthenticationBuilder
        // (which erased the generics and degraded the next withParam to Object).
        var authBuilder = builder.security()
                .authentication(new FixedSupplierBuilder<>(new SecuringStrategy(),
                        IClass.getClass(SecuringStrategy.class)))
                .authenticate("authenticate").up()
                .applySecurityOnEntity("secure")
                        .withParam(0, new com.garganttua.api.core.security.authentication.SecuredEntitySupplierBuilder())
                        .up();

        builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .update("name")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(userDao)
                .up()
                .security()
                    // create/update are anonymous here so the test exercises the apply step
                    // without minting a token — the redesign requires a token for authenticated ops.
                    .creationAccess(Access.anonymous)
                    .updateAccess(Access.anonymous)
                    .authenticator()
                        .login("id")
                        .scope(AuthenticatorScope.tenant)
                        .alwaysEnabled(true)
                        .authentication(authBuilder)
                    .up()
                .up()
            .up();

        context = buildAndStart(builder);
        userCtx = context.getDomain("users").orElseThrow();
    }

    private OperationDefinition op(BusinessOperation bo) {
        return userCtx.getDomainDefinition().operations().stream()
                .filter(o -> o.getBusinessOperation() == bo)
                .findFirst().orElseThrow();
    }

    private String createUser(String name) throws ApiException {
        OperationRequest req = superTenantScriptRequest(op(BusinessOperation.create));
        User u = new User();
        u.setName(name);
        req.arg("entity", u);
        WorkflowResult result = executeScript(userCtx, req);
        assertEquals(0, result.code(), () -> "create failed; vars=" + result.variables());
        return ((User) result.output()).getUuid();
    }

    private UserDto stored(String uuid) {
        // CapturingDao appends on save, so the LAST record for a uuid is the current state.
        return userDao.getStorage().stream()
                .map(o -> (UserDto) o)
                .filter(d -> uuid.equals(d.getUuid()))
                .reduce((a, b) -> b).orElseThrow();
    }

    @Test
    @DisplayName("create: the persisted entity carries the secured field, not the raw input")
    void createSecuresTheEntity() throws ApiException {
        String uuid = createUser("alice");
        assertEquals("secured:alice", stored(uuid).getName(),
                "the apply method must have secured the name before persistence");
    }

    @Test
    @DisplayName("update: the persisted merged entity is re-secured")
    void updateSecuresTheEntity() throws ApiException {
        String uuid = createUser("alice");

        OperationRequest req = superTenantScriptRequest(op(BusinessOperation.update));
        req.arg("type", "uuid");
        req.arg("identifier", uuid);
        User patch = new User();
        patch.setName("bob");
        req.arg("entity", patch);
        WorkflowResult result = executeScript(userCtx, req);
        assertEquals(0, result.code(), () -> "update failed; vars=" + result.variables());

        assertEquals("secured:bob", stored(uuid).getName(),
                "the apply method must have re-secured the updated name before persistence");
    }
}
