package com.garganttua.api.core.usecase;
import com.garganttua.core.reflection.annotations.Reflected;

import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.context.dsl.IUseCaseBuilder;
import com.garganttua.api.commons.context.dsl.security.IUseCaseSecurityBuilder;
import com.garganttua.api.commons.security.IUseCaseSecurity;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.api.commons.ApiException;

@Reflected
public class UseCaseSecurityBuilder<I, O, E>
        extends AbstractAutomaticLinkedBuilder<IUseCaseSecurityBuilder<I, O, E>, IUseCaseBuilder<I, O, E>, IUseCaseSecurity>
        implements IUseCaseSecurityBuilder<I, O, E> {

    private boolean disabled = false;
    private boolean authority = false;
    private String customAuthority;
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
        this.customAuthority = null;
        return this;
    }

    @Override
    public IUseCaseSecurityBuilder<I, O, E> authority(String customAuthority) {
        this.authority = true;
        this.customAuthority = java.util.Objects.requireNonNull(customAuthority, "Custom authority cannot be null");
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

    String getCustomAuthority() {
        return this.customAuthority;
    }

    Access getAccess() {
        return this.access;
    }

    @Override
    protected synchronized IUseCaseSecurity doBuild() throws ApiException {
        return new UseCaseSecurityImpl(this.disabled, this.authority, this.customAuthority, this.access);
    }

    @Override
    protected void doAutoDetection() throws ApiException {
    }

    private static class UseCaseSecurityImpl implements IUseCaseSecurity {
        private final boolean disabled;
        private final boolean authority;
        private final String customAuthority;
        private final Access access;

        public UseCaseSecurityImpl(boolean disabled, boolean authority, String customAuthority, Access access) {
            this.disabled = disabled;
            this.authority = authority;
            this.customAuthority = customAuthority;
            this.access = access;
        }

        @Override
        public boolean isDisabled() {
            return disabled;
        }

        @Override
        public boolean hasAuthority() {
            return authority;
        }

        @Override
        public String customAuthority() {
            return customAuthority;
        }

        @Override
        public Access getAccess() {
            return access;
        }
    }
}
