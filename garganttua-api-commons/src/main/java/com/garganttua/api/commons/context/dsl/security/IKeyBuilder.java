package com.garganttua.api.commons.context.dsl.security;

import com.garganttua.api.commons.security.IKeyContext;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;

public interface IKeyBuilder<E> extends IAutomaticLinkedBuilder<IKeyBuilder<E>, IDomainSecurityBuilder<E>, IKeyContext>{

}
