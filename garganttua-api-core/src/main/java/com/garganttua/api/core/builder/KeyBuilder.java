package com.garganttua.api.core.builder;

import com.garganttua.api.spec.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.api.spec.context.dsl.security.IKeyBuilder;
import com.garganttua.api.spec.security.IKeyContext;
import com.garganttua.core.CoreException;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IObjectQuery;

public class KeyBuilder extends AbstractAutomaticLinkedBuilder<IKeyBuilder, IDomainSecurityBuilder, IKeyContext>
        implements IKeyBuilder {

    public KeyBuilder(DomainSecurityBuilder domainSecurityBuilder, IObjectQuery objectQuery, Class<?> entityClass) {
        super(domainSecurityBuilder);
    }

    @Override
    protected IKeyContext doBuild() throws CoreException {
        return null;
    }

    @Override
    protected void doAutoDetection() throws CoreException {

    }

}
