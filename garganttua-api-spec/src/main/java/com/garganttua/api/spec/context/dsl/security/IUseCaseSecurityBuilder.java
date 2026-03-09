package com.garganttua.api.spec.context.dsl.security;

import com.garganttua.api.spec.operation.Access;
import com.garganttua.api.spec.context.dsl.IUseCaseBuilder;
import com.garganttua.api.spec.security.IUseCaseSecurity;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;

public interface IUseCaseSecurityBuilder<I, O, E>
        extends IAutomaticLinkedBuilder<IUseCaseSecurityBuilder<I, O, E>, IUseCaseBuilder<I, O, E>, IUseCaseSecurity> {

    IUseCaseSecurityBuilder<I, O, E> disable(boolean b);

    IUseCaseSecurityBuilder<I, O, E> authority(boolean authority);

    IUseCaseSecurityBuilder<I, O, E> access(Access acceess);

}
