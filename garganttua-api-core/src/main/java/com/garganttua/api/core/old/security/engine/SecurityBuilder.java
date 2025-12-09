package com.garganttua.api.core.security.engine;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.core.engine.EngineException;
import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;
import com.garganttua.api.spec.context.IEngine;
import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.security.ISecurityBuilder;
import com.garganttua.api.spec.security.ISecurityEngine;
import com.garganttua.api.spec.service.ServiceAccess;
import com.garganttua.core.reflection.beans.IGGBeanLoader;
import com.garganttua.core.reflection.injection.IGGInjector;

public class SecurityBuilder implements ISecurityBuilder {

	private List<String> packages;
	private IGGInjector injector;
	private IGGBeanLoader loader;
	private IEngine engine;

	@Override
	public ISecurityEngine build() throws CoreException {
		if (this.packages == null) {
			throw new EngineException(CoreExceptionCode.CORE_GENERIC_CODE, "Packages cannnot be null");
		}
		if (this.loader == null) {
			throw new EngineException(CoreExceptionCode.CORE_GENERIC_CODE, "BeanLoader cannnot be null");
		}
		if (this.engine == null) {
			throw new EngineException(CoreExceptionCode.CORE_GENERIC_CODE, "Engine cannnot be null");
		}

		return new SecurityEngine(
				this.engine,
				this.packages,
				Optional.ofNullable(this.injector),
				this.loader);
	}

	@Override
	public ISecurityBuilder scanPackages(List<String> packages) {
		this.packages = packages;
		return this;
	}

	@Override
	public ISecurityBuilder injector(IGGInjector injector) {
		this.injector = injector;
		return this;
	}

	@Override
	public ISecurityBuilder loader(IGGBeanLoader loader) {
		this.loader = loader;
		return this;
	}

	@Override
	public ISecurityBuilder engine(IEngine engine) {
		this.engine = engine;
		return this;
	}

	@Override
	public ISecurityBuilder creationAccess(ServiceAccess access) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'creationAccess'");
	}

	@Override
	public ISecurityBuilder readAllAccess(ServiceAccess tenant) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'readAllAccess'");
	}

	@Override
	public ISecurityBuilder readOneAccess(ServiceAccess tenant) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'readOneAccess'");
	}

	@Override
	public ISecurityBuilder updateAccess(ServiceAccess tenant) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'updateAccess'");
	}

	@Override
	public ISecurityBuilder deleteAllAccess(ServiceAccess tenant) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'deleteAllAccess'");
	}

	@Override
	public ISecurityBuilder deleteOneAccess(ServiceAccess tenant) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'deleteOneAccess'");
	}

	@Override
	public ISecurityBuilder creationAuthority(boolean b) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'creationAuthority'");
	}

	@Override
	public ISecurityBuilder readAllAuthority(boolean b) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'readAllAuthority'");
	}

	@Override
	public ISecurityBuilder readOneAuthority(boolean b) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'readOneAuthority'");
	}

	@Override
	public ISecurityBuilder updateAuthority(boolean b) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'updateAuthority'");
	}

	@Override
	public ISecurityBuilder deleteAllAuthority(boolean b) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'deleteAllAuthority'");
	}

	@Override
	public ISecurityBuilder deleteOneAuthority(boolean b) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'deleteOneAuthority'");
	}

	@Override
	public ISecurityBuilder authorization(Class<Object> class1) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'authorization'");
	}

	@Override
	public ISecurityBuilder authorizationProtocol(Class<Object> class1) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'authorizationProtocol'");
	}

	@Override
	public IDomainBuilder up() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'up'");
	}

	@Override
	public ISecurityBuilder autoDetect(boolean b) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'autoDetect'");
	}

}
