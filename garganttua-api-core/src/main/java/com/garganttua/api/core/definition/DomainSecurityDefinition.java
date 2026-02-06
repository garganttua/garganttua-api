package com.garganttua.api.core.definition;

import com.garganttua.api.spec.context.Access;
import com.garganttua.api.spec.definition.IDomainSecurityDefinition;

public record DomainSecurityDefinition (
    Access creationAccess,
    Access readAllAccess,
    Access readOneAccess,
    Access updateAccess,
    Access deleteAllAccess,
    Access deleteOneAccess,
    Boolean deleteOneAuthority,
    Boolean creationAuthority,
    Boolean readAllAuthority,
    Boolean readOneAuthority,
    Boolean updateAuthority,
    Boolean deleteAllAuthority,
    boolean disabled) implements IDomainSecurityDefinition {

}
