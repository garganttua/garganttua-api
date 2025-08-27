package com.garganttua.api.core.security.engine;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.security.ISecurityBuilder;
import com.garganttua.api.spec.security.ISecurityEngine;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;

public class SecurityBuilder implements ISecurityBuilder {

	private List<String> packages;
	private IGGInjector injector;
	private IGGBeanLoader loader;
	private IEngine engine;

	@Override
	public ISecurityEngine build() throws CoreException {
		if( this.packages == null ) {
			throw new EngineException(CoreExceptionCode.CORE_GENERIC_CODE, "Packages cannnot be null");
		}
		if( this.loader == null ) {
			throw new EngineException(CoreExceptionCode.CORE_GENERIC_CODE, "BeanLoader cannnot be null");
		}
		if( this.engine == null ) {
			throw new EngineException(CoreExceptionCode.CORE_GENERIC_CODE, "Engine cannnot be null");
		}
		
		return new SecurityEngine(
				this.engine,
				this.packages, 
				Optional.ofNullable(this.injector), 
				this.loader
		);
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

}
