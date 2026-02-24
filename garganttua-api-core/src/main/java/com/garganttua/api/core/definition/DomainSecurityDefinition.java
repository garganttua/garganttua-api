package com.garganttua.api.core.definition;

import com.garganttua.api.spec.definition.IDomainSecurityDefinition;

public record DomainSecurityDefinition(
    boolean disabled) implements IDomainSecurityDefinition {

}
