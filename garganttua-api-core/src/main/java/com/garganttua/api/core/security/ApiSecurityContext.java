package com.garganttua.api.core.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.security.IApiSecurityContext;
import com.garganttua.api.commons.security.authentication.IAuthenticationRequest;
import com.garganttua.api.commons.security.context.IAuthenticationContext;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.lifecycle.ILifecycle;
import com.garganttua.core.lifecycle.LifecycleException;
import com.garganttua.core.lifecycle.LifecycleStatus;

public class ApiSecurityContext implements IApiSecurityContext {

    private final boolean disabled;
    private final Map<String, List<IAuthenticationContext>> authenticationContextsByDomain = new HashMap<>();

    private volatile LifecycleStatus status = LifecycleStatus.NEW;

    public ApiSecurityContext(boolean disabled) {
        this.disabled = disabled;
    }

    public void registerAuthenticationContexts(String domainName, List<IAuthenticationContext> contexts) {
        this.authenticationContextsByDomain.put(domainName, contexts);
    }

    @Override
    public void verifyTenant(ICaller caller, Object authentication) throws ApiException {
        if (disabled) return;
        // Stub implementation
    }

    @Override
    public void verifyOwner(ICaller caller, Object authentication) throws ApiException {
        if (disabled) return;
        // Stub implementation
    }

    @Override
    public byte[] decodeAuthorizationFromRequest(Object request, ICaller caller) throws ApiException {
        // Stub implementation
        return new byte[0];
    }

    @Override
    public Object decodeRawAuthorization(byte[] authorizationRaw, ICaller caller) {
        // Stub implementation
        return null;
    }

    @Override
    public boolean isStorableAuthorization(Object authorization) {
        // Stub implementation
        return false;
    }

    @Override
    public void authenticatorEntitySecurityPreProcessing(ICaller caller, Object entity,
            Map<String, String> params) throws ApiException {
        if (disabled) return;
        // Stub implementation
    }

    @Override
    public void authenticatorEntitySecurityPostProcessing(ICaller caller, Object entity,
            Map<String, String> params) throws ApiException {
        if (disabled) return;
        // Stub implementation
    }

    @Override
    public IAuthenticationRequest createAuthenticationRequestFromAuthorization(ICaller caller, Object authorization)
            throws ApiException {
        // Stub implementation
        return null;
    }

    @Override
    public Optional<Object> getAuthorizationFromRequest(ICaller caller, Object request) throws ApiException {
        // Stub implementation
        return Optional.empty();
    }

    @Override
    public ILifecycle onStart() throws LifecycleException {
        this.status = LifecycleStatus.STARTED;
        return this;
    }

    @Override
    public ILifecycle onStop() throws LifecycleException {
        this.status = LifecycleStatus.STOPPED;
        return this;
    }

    @Override
    public ILifecycle onFlush() throws LifecycleException {
        this.status = LifecycleStatus.FLUSHED;
        return this;
    }

    @Override
    public ILifecycle onInit() throws LifecycleException {
        this.status = LifecycleStatus.INITIALIZED;
        return this;
    }

    @Override
    public ILifecycle onReload() throws LifecycleException {
        return this;
    }

    @Override
    public LifecycleStatus status() {
        return this.status;
    }
}
