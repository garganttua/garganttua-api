package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import com.garganttua.api.core.domain.DomainDefinition;
import com.garganttua.api.core.expression.SecurityExpressions;
import com.garganttua.api.core.filter.Filter;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.definition.IAuthenticatorDefinition;
import com.garganttua.api.commons.security.authentication.IAuthenticationRequest;
import com.garganttua.api.commons.security.authenticator.AuthenticatorScope;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.SupplyException;

/**
 * Supplies the principal (authenticator entity) for the authenticate method.
 *
 * Performs the full principal resolution pipeline:
 * 1. Reads the AuthenticationRequest from the operation request
 * 2. Finds the authenticator entity by login via the domain pipeline (readAll + login filter)
 * 3. Checks account status (enabled, non-locked, non-expired) unless alwaysEnabled
 * 4. Returns the verified principal
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class PrincipalSupplier implements IContextualSupplier<Object, IRuntimeContext> {

    private static final IClass<Object> SUPPLIED_CLASS = IClass.getClass(Object.class);
    private static final IClass<IRuntimeContext> CONTEXT_CLASS = IClass.getClass(IRuntimeContext.class);

    @Override
    public Type getSuppliedType() { return SUPPLIED_CLASS.getType(); }

    @Override
    public IClass<Object> getSuppliedClass() { return SUPPLIED_CLASS; }

    @Override
    public IClass<IRuntimeContext> getOwnerContextType() { return CONTEXT_CLASS; }

    @Override
    public Optional<Object> supply(IRuntimeContext context, Object... otherContexts) throws SupplyException {
        if (context == null) {
            throw new SupplyException("IRuntimeContext cannot be null");
        }

        // 1. Get the operation request from runtime context
        Optional<?> requestOpt = context.getVariable("request", IClass.getClass(IOperationRequest.class));
        if (requestOpt.isEmpty()) {
            throw new SupplyException("Variable 'request' not found in runtime context");
        }
        IOperationRequest request = (IOperationRequest) requestOpt.get();

        // 2. Get the domain context from runtime context
        Optional<?> domainCtxOpt = context.getVariable("domainContext", IClass.getClass(IDomain.class));
        if (domainCtxOpt.isEmpty()) {
            throw new SupplyException("Variable 'domainContext' not found in runtime context");
        }
        IDomain<?> domainContext = (IDomain<?>) domainCtxOpt.get();

        // 3. Get the authenticator definition
        DomainDefinition<?> domDef = (DomainDefinition<?>) domainContext.getDomainDefinition();
        var secDef = domDef.domainSecurityDefinition();
        if (secDef == null) {
            throw new SupplyException("No security definition found on domain");
        }
        IAuthenticatorDefinition authDef = secDef.authenticatorDefinition();
        if (authDef == null) {
            throw new SupplyException("No authenticator definition found on domain");
        }

        // 4. Get the AuthenticationRequest entity from the request
        Optional<?> entityOpt = request.arg("entity");
        if (entityOpt.isEmpty()) {
            throw new SupplyException("No 'entity' (AuthenticationRequest) found in operation request");
        }
        IAuthenticationRequest authRequest = (IAuthenticationRequest) entityOpt.get();

        // 5. Find the authenticator entity by login in the repository
        String login = authRequest.login();
        ObjectAddress loginField = authDef.login();
        if (loginField == null) {
            throw new SupplyException("No login field configured on authenticator");
        }

        // Match by login and, for a tenant-scoped authenticator, ALSO by the caller's
        // tenant. The lookup routes through invokeReadAll, whose super lookup-caller
        // bypasses tenant filtering (requestedTenantId=null →
        // RepositoryFilterTools.isSuperTenantWithoutTenant), so a same-login user of ANY
        // tenant would otherwise match — letting any X-Tenant-Id authenticate a user of
        // another tenant. The caller's tenant is the one requireCallerTenantForScope
        // already mandates (the request TENANT_ID arg, set over HTTP from the X-Tenant-Id
        // header), never the AuthenticationRequest body. The super lookup-caller routes
        // this explicit filter as-is, so the tenantId predicate enforces the isolation.
        Filter filter = Filter.eq(loginField.toString(), login);
        if (authDef.scope() == AuthenticatorScope.tenant) {
            ObjectAddress tenantField = domDef.entityDefinition().tenantId();
            String callerTenant = request.arg(IOperationRequest.TENANT_ID).orElse(null);
            if (tenantField != null && callerTenant != null && !callerTenant.isBlank()) {
                filter = Filter.and(filter, Filter.eq(tenantField.toString(), callerTenant));
            }
        }

        List<Object> results;
        try {
            results = SecurityExpressions.invokeReadAll(domainContext, filter);
        } catch (Exception e) {
            throw new SupplyException("PrincipalSupplier: failed to query authenticator domain '"
                    + domainContext.getDomainName() + "' for login '" + login + "': " + e.getMessage(), e);
        }
        if (results == null || results.isEmpty()) {
            throw new SupplyException("User not found for login: " + login);
        }
        Object principal = results.get(0);

        // 6. Check account status (enabled, non-locked, non-expired) unless alwaysEnabled
        checkAccountStatus(authDef, principal);

        return Optional.of(principal);
    }

    private void checkAccountStatus(IAuthenticatorDefinition authDef, Object entity) throws SupplyException {
        if (authDef.alwaysEnabled()) {
            return;
        }

        IReflection reflection = DefaultMapper.reflection();

        if (authDef.enabled() != null) {
            Object value = reflection.getFieldValue(entity, authDef.enabled().toString());
            if (!Boolean.TRUE.equals(value)) {
                throw new SupplyException("Account is disabled");
            }
        }

        if (authDef.accountNonLocked() != null) {
            Object value = reflection.getFieldValue(entity, authDef.accountNonLocked().toString());
            if (!Boolean.TRUE.equals(value)) {
                throw new SupplyException("Account is locked");
            }
        }

        if (authDef.accountNonExpired() != null) {
            Object value = reflection.getFieldValue(entity, authDef.accountNonExpired().toString());
            if (!Boolean.TRUE.equals(value)) {
                throw new SupplyException("Account is expired");
            }
        }

        if (authDef.credentialsNonExpired() != null) {
            Object value = reflection.getFieldValue(entity, authDef.credentialsNonExpired().toString());
            if (!Boolean.TRUE.equals(value)) {
                throw new SupplyException("Credentials are expired");
            }
        }
    }
}
