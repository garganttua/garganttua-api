package com.garganttua.api.spec.context.dsl.security;

import com.garganttua.api.spec.context.Access;
import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.context.dsl.IUseCaseBuilder;
import com.garganttua.api.spec.security.IDomainSecurityContext;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;

public interface IDomainSecurityBuilder<E>
		extends IAutomaticLinkedBuilder<IDomainSecurityBuilder<E>, IDomainBuilder<E>, IDomainSecurityContext> {

	IDomainSecurityBuilder<E> creationAccess(Access access);

	IDomainSecurityBuilder<E> readAllAccess(Access tenant);

	IDomainSecurityBuilder<E> readOneAccess(Access tenant);

	IDomainSecurityBuilder<E> updateAccess(Access tenant);

	IDomainSecurityBuilder<E> deleteAllAccess(Access tenant);

	IDomainSecurityBuilder<E> deleteOneAccess(Access tenant);

	IDomainSecurityBuilder<E> creationAuthority(boolean b);

	IDomainSecurityBuilder<E> readAllAuthority(boolean b);

	IDomainSecurityBuilder<E> readOneAuthority(boolean b);

	IDomainSecurityBuilder<E> updateAuthority(boolean b);

	IDomainSecurityBuilder<E> deleteAllAuthority(boolean b);

	IDomainSecurityBuilder<E> deleteOneAuthority(boolean b);

	IAuthorizationBuilder<E> authorization();

	IDomainSecurityBuilder<E> disable(boolean b);

	IDomainSecurityBuilder<E> authorizationProtocol(Class<?> class1, IAuthorizationProtocolBuilder protocole) throws DslException;

	IKeyBuilder<E> key();

	IAuthenticatorBuilder<E> authenticator();

	IDomainSecurityBuilder<E> useCase(IUseCaseBuilder<?, ?, ?> useCaseBuilder, boolean authority, Access access);

}
