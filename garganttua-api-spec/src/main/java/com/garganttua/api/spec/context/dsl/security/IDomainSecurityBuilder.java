package com.garganttua.api.spec.context.dsl.security;

import com.garganttua.api.spec.context.Access;
import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.context.dsl.IUseCaseBuilder;
import com.garganttua.api.spec.security.IDomainSecurityContext;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;

public interface IDomainSecurityBuilder<E>
		extends IAutomaticLinkedBuilder<IDomainSecurityBuilder<E>, IDomainBuilder<E>, IDomainSecurityContext> {

	IAuthorizationBuilder<E> authorization();

	IDomainSecurityBuilder<E> disable(boolean b);

	IDomainSecurityBuilder<E> authorizationProtocol(Class<?> class1, IAuthorizationProtocolBuilder protocole) throws ApiException;

	IKeyBuilder<E> key();

	IAuthenticatorBuilder<E> authenticator();

	IDomainSecurityBuilder<E> useCase(IUseCaseBuilder<?, ?, ?> useCaseBuilder, boolean authority, Access access);

}
