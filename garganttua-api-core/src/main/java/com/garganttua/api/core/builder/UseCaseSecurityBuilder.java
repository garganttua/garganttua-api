package com.garganttua.api.core.builder;

import com.garganttua.api.spec.operation.Access;
import com.garganttua.api.spec.context.dsl.IUseCaseBuilder;
import com.garganttua.api.spec.context.dsl.security.IUseCaseSecurityBuilder;
import com.garganttua.api.spec.security.IUseCaseSecurity;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.api.spec.ApiException;

public class UseCaseSecurityBuilder<I, O, E>
        extends AbstractAutomaticLinkedBuilder<IUseCaseSecurityBuilder<I, O, E>, IUseCaseBuilder<I, O, E>, IUseCaseSecurity>
        implements IUseCaseSecurityBuilder<I, O, E> {

    private boolean disabled = false;
    private boolean authority = false;
    private Access access = Access.authenticated;

    public UseCaseSecurityBuilder(IUseCaseBuilder<I, O, E> up) {
        super(up);
    }

    @Override
    public IUseCaseSecurityBuilder<I, O, E> disable(boolean b) {
        this.disabled = b;
        return this;
    }

    @Override
    public IUseCaseSecurityBuilder<I, O, E> authority(boolean authority) {
        this.authority = authority;
        return this;
    }

    @Override
    public IUseCaseSecurityBuilder<I, O, E> access(Access access) {
        this.access = access;
        return this;
    }

    boolean hasAuthority() {
        return this.authority;
    }

    Access getAccess() {
        return this.access;
    }

    @Override
    protected synchronized IUseCaseSecurity doBuild() throws ApiException {
        // Return a simple implementation of IUseCaseSecurity
        return new UseCaseSecurityImpl(this.disabled, this.authority, this.access);
    }

    @Override
    protected void doAutoDetection() throws ApiException {
        // No auto-detection for use case security - all configuration is explicit
    }

    private static class UseCaseSecurityImpl implements IUseCaseSecurity {
        private final boolean disabled;
        private final boolean authority;
        private final Access access;

        public UseCaseSecurityImpl(boolean disabled, boolean authority, Access access) {
            this.disabled = disabled;
            this.authority = authority;
            this.access = access;
        }

        public boolean isDisabled() {
            return disabled;
        }

        public boolean hasAuthority() {
            return authority;
        }

        public Access getAccess() {
            return access;
        }
    }
}
