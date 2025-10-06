package com.garganttua.api.spec.security;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IAuthenticatorBuilder;
import com.garganttua.api.spec.engine.IAuthorizationBuilder;
import com.garganttua.api.spec.engine.IAuthorizationProtocolBuilder;
import com.garganttua.api.spec.engine.IAutomaticLinkedBuilder;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IUseCaseBuilder;
import com.garganttua.api.spec.service.Access;

public interface IDomainSecurityBuilder
		extends IAutomaticLinkedBuilder<IDomainSecurityContext, IDomainBuilder, IDomainSecurityBuilder> {

	IDomainSecurityBuilder creationAccess(Access access);

	IDomainSecurityBuilder readAllAccess(Access tenant);

	IDomainSecurityBuilder readOneAccess(Access tenant);

	IDomainSecurityBuilder updateAccess(Access tenant);

	IDomainSecurityBuilder deleteAllAccess(Access tenant);

	IDomainSecurityBuilder deleteOneAccess(Access tenant);

	IDomainSecurityBuilder creationAuthority(boolean b);

	IDomainSecurityBuilder readAllAuthority(boolean b);

	IDomainSecurityBuilder readOneAuthority(boolean b);

	IDomainSecurityBuilder updateAuthority(boolean b);

	IDomainSecurityBuilder deleteAllAuthority(boolean b);

	IDomainSecurityBuilder deleteOneAuthority(boolean b);

	IAuthorizationBuilder authorization();

	IDomainSecurityBuilder disable(boolean b);

	IDomainSecurityBuilder authorizationProtocol(Class<?> class1, IAuthorizationProtocolBuilder protocole) throws CoreException;

	IKeyBuilder key();

	IAuthenticatorBuilder authenticator();

    IDomainSecurityBuilder useCase(IUseCaseBuilder<?> useCaseBuilder, boolean authority, Access acceess);
}
