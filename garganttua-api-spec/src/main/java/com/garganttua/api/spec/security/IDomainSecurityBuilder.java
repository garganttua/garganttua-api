package com.garganttua.api.spec.security;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IAutomaticLinkedBuilder;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.service.ServiceAccess;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;

public interface IDomainSecurityBuilder extends IAutomaticLinkedBuilder<Object, IDomainBuilder, IDomainSecurityBuilder> {

	IDomainSecurityBuilder injector(IGGInjector injector);

	ISecurityEngine build() throws CoreException;

	IDomainSecurityBuilder loader(IGGBeanLoader loader);

	IDomainSecurityBuilder engine(IEngine engine);

	IDomainSecurityBuilder creationAccess(ServiceAccess access);

	IDomainSecurityBuilder readAllAccess(ServiceAccess tenant);

	IDomainSecurityBuilder readOneAccess(ServiceAccess tenant);

	IDomainSecurityBuilder updateAccess(ServiceAccess tenant);

	IDomainSecurityBuilder deleteAllAccess(ServiceAccess tenant);

	IDomainSecurityBuilder deleteOneAccess(ServiceAccess tenant);

	IDomainSecurityBuilder creationAuthority(boolean b);

	IDomainSecurityBuilder readAllAuthority(boolean b);

	IDomainSecurityBuilder readOneAuthority(boolean b);

	IDomainSecurityBuilder updateAuthority(boolean b);

	IDomainSecurityBuilder deleteAllAuthority(boolean b);

	IDomainSecurityBuilder deleteOneAuthority(boolean b);

	IDomainSecurityAuthorizationBuilder authorization(IDomainBuilder authorizationDomain);
}
