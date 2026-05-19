package com.garganttua.api.commons.context;

import com.garganttua.api.commons.definition.IDomainKeyDefinition;

/**
 * Runtime view of a domain marked as key domain (entity carries
 * {@code @Key} or DSL declared {@code .key()}). Exposes the
 * {@link IDomainKeyDefinition} consumed by the key auto-create /
 * lookup path.
 */
public interface IDomainKeyContext {

    IDomainKeyDefinition getKeyDefinition();

}
