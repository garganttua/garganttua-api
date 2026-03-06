package com.garganttua.api.core.builder;

import com.garganttua.api.spec.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.api.spec.context.dsl.security.IKeyBuilder;
import com.garganttua.api.spec.security.IKeyContext;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IClass;

public class KeyBuilder<E> extends AbstractAutomaticLinkedBuilder<IKeyBuilder<E>, IDomainSecurityBuilder<E>, IKeyContext>
        implements IKeyBuilder<E> {

    public KeyBuilder(DomainSecurityBuilder<E> domainSecurityBuilder, IClass<?> entityClass) {
        super(domainSecurityBuilder);
    }

    @Override
    protected synchronized IKeyContext doBuild() throws ApiException {
        return null;
    }

    @Override
    protected void doAutoDetection() throws ApiException {

    }

}
