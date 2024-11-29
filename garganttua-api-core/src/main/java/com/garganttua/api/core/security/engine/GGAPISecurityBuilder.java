package com.garganttua.api.core.security.engine;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.security.IGGAPISecurityBuilder;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;
import com.garganttua.api.spec.service.IGGAPIServicesRegistry;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;

public class GGAPISecurityBuilder implements IGGAPISecurityBuilder {

	private List<String> packages;
	private IGGAPIServicesRegistry servicesRegistry;
	private IGGInjector injector;
	private IGGBeanLoader loader;
	private IGGAPIEngine engine;

	@Override
	public IGGAPISecurityEngine build() throws GGAPIException {
		if( this.servicesRegistry == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.CORE_GENERIC_CODE, "ServicesRegistry cannnot be null");
		}
		if( this.packages == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.CORE_GENERIC_CODE, "Packages cannnot be null");
		}
		if( this.loader == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.CORE_GENERIC_CODE, "BeanLoader cannnot be null");
		}
		if( this.engine == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.CORE_GENERIC_CODE, "Engine cannnot be null");
		}
		
		return new GGAPISecurityEngine(
				this.engine,
				this.servicesRegistry, 
				this.packages, 
				Optional.ofNullable(this.injector), 
				this.loader
		);
	}

	@Override
	public IGGAPISecurityBuilder scanPackages(List<String> packages) {
		this.packages = packages;
		return this;
	}

	@Override
	public IGGAPISecurityBuilder injector(IGGInjector injector) {
		this.injector = injector;
		return this;
	}

	@Override
	public IGGAPISecurityBuilder servicesRegistry(IGGAPIServicesRegistry registry) {
		this.servicesRegistry = registry;
		return this;
	}

	@Override
	public IGGAPISecurityBuilder loader(IGGBeanLoader loader) {
		this.loader = loader;
		return this;
	}

	@Override
	public IGGAPISecurityBuilder engine(IGGAPIEngine engine) {
		this.engine = engine;
		return this;
	}
}
