package com.garganttua.api.spec.security;

import java.util.List;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.service.ServiceAccess;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;

public interface ISecurityBuilder {

	ISecurityBuilder scanPackages(List<String> packages);
	
	ISecurityBuilder injector(IGGInjector injector); 

	ISecurityEngine build() throws CoreException;

	ISecurityBuilder loader(IGGBeanLoader loader);

	ISecurityBuilder engine(IEngine engine);

	ISecurityBuilder creationAccess(ServiceAccess access);

	ISecurityBuilder readAllAccess(ServiceAccess tenant);

	ISecurityBuilder readOneAccess(ServiceAccess tenant);

	ISecurityBuilder updateAccess(ServiceAccess tenant);

    ISecurityBuilder deleteAllAccess(ServiceAccess tenant);

	ISecurityBuilder deleteOneAccess(ServiceAccess tenant);

	ISecurityBuilder creationAuthority(boolean b);

    ISecurityBuilder readAllAuthority(boolean b);

    ISecurityBuilder readOneAuthority(boolean b);

    ISecurityBuilder updateAuthority(boolean b);

    ISecurityBuilder deleteAllAuthority(boolean b);

    ISecurityBuilder deleteOneAuthority(boolean b);

	ISecurityBuilder authorization(Class<Object> class1);

	ISecurityBuilder authorizationProtocol(Class<Object> class1);

	IDomainBuilder up();

	ISecurityBuilder autoDetect(boolean b);
}
