package com.garganttua.api.spec.factory;

import java.util.List;

import com.garganttua.api.spec.engine.IGGAPIEngineObject;

public interface IGGAPIFactoriesRegistry extends IGGAPIEngineObject {

	List<IGGAPIEntityFactory<?>> getFactories();
	
	IGGAPIEntityFactory<?> getFactory(String domain);
}
