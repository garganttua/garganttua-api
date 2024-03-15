package com.garganttua.api.engine.registries;

import java.util.List;

import com.garganttua.api.core.entity.factory.IGGAPIEntityFactory;

public interface IGGAPIFactoriesRegistry {

	List<IGGAPIEntityFactory<?>> getFactories();
	
	IGGAPIEntityFactory<?> getFactory(String domain);

}
