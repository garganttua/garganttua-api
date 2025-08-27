package com.garganttua.api.core.engine;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.engine.IBuilder;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;
import com.garganttua.reflection.properties.IGGPropertyLoader;

public class Builder implements IBuilder {

	public static IBuilder builder() {
		return new Builder();
	}
	private IGGBeanLoader loader;
	private List<String> packages;
	private IGGPropertyLoader propLoader;
	private IGGInjector injector;
	
	@Override
	public IBuilder beanLoader(IGGBeanLoader loader) {
		this.loader = loader;
		return this;
	}

	@Override
	public IEngine build() throws CoreException {
		if( this.loader == null ) {
			throw new EngineException(CoreExceptionCode.CORE_GENERIC_CODE, "BeanLoader cannnot be null");
		}
		if( this.packages == null ) {
			throw new EngineException(CoreExceptionCode.CORE_GENERIC_CODE, "Packages cannnot be null");
		}
		if( this.propLoader == null ) {
			throw new EngineException(CoreExceptionCode.CORE_GENERIC_CODE, "PropLoader cannnot be null");
		}
		return new Engine(Optional.ofNullable(this.injector), this.packages, this.propLoader, this.loader);
	}

	@Override
	public IBuilder packages(List<String> packages) {
		this.packages = packages;
		return this;
	}

	@Override
	public IBuilder propertyLoader(IGGPropertyLoader loader) {
		propLoader = loader;
		return this;
	}

	@Override
	public IBuilder injector(IGGInjector injector) {
		this.injector = injector;
		return this;
	}

}
