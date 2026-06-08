package com.garganttua.api.core.security.key;

import com.garganttua.api.commons.context.IDomainKeyContext;
import com.garganttua.api.commons.definition.IDomainKeyDefinition;

public record DomainKeyContext(IDomainKeyDefinition getKeyDefinition) implements IDomainKeyContext {

}
