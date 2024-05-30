package com.garganttua.api.spec.engine;

import java.util.List;

import com.garganttua.api.spec.factory.IGGAPIEntityFactory;

public interface IGGAPIFactoriesRegistry {

	List<IGGAPIEntityFactory<?>> getFactories();
	
	IGGAPIEntityFactory<?> getFactory(String domain);

}
