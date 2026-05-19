package com.garganttua.api.core.context;

import com.garganttua.api.commons.context.IDomainKeyContext;
import com.garganttua.api.commons.definition.IDomainKeyDefinition;

public record DomainKeyContext(IDomainKeyDefinition getKeyDefinition) implements IDomainKeyContext {

}
