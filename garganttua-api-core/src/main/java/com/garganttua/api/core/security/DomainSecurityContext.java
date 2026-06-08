package com.garganttua.api.core.security;

import java.util.Objects;

import com.garganttua.api.core.security.DomainSecurityDefinition;
import com.garganttua.api.commons.definition.IDomainSecurityDefinition;
import com.garganttua.api.commons.security.IDomainSecurityContext;

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
