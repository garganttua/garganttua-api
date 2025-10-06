package com.garganttua.api.core.builder;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.security.IDomainSecurityBuilder;
import com.garganttua.api.spec.security.IKeyBuilder;
import com.garganttua.api.spec.security.IKeyContext;
import com.garganttua.reflection.query.IGGObjectQuery;

public class KeyBuilder extends AbstractAutomaticLinkedBuilder<IKeyContext, IKeyBuilder, IDomainSecurityBuilder> implements IKeyBuilder {

    public KeyBuilder(DomainSecurityBuilder domainSecurityBuilder, IGGObjectQuery objectQuery, Class<?> entityClass) {
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
