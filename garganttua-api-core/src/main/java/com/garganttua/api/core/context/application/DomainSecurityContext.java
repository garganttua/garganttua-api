package com.garganttua.api.core.context.application;

import java.util.Objects;

import com.garganttua.api.core.definition.DomainSecurityDefinition;
import com.garganttua.api.spec.definition.IDomainSecurityDefinition;
import com.garganttua.api.spec.security.IDomainSecurityContext;

public class DomainSecurityContext implements IDomainSecurityContext {

    private DomainSecurityDefinition domainSecurityDefinition;

    public DomainSecurityContext(DomainSecurityDefinition domainSecurityDefinition) {
        this.domainSecurityDefinition = Objects.requireNonNull(domainSecurityDefinition,"Domain security definition cannot be null");
    }
    
    @Override
    public IDomainSecurityDefinition getDomainSecurityDefinition() {
        return this.domainSecurityDefinition;
    }

}
