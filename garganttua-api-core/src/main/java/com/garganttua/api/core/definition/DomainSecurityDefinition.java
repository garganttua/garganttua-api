package com.garganttua.api.core.definition;

import com.garganttua.api.spec.context.Access;

public record DomainSecurityDefinition(
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
    boolean disabled) {

}
