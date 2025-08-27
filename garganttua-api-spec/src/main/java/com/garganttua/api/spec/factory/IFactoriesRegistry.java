package com.garganttua.api.spec.factory;

import java.util.List;

import com.garganttua.api.spec.engine.IEngineObject;

public interface IFactoriesRegistry extends IEngineObject {

	List<IEntityFactory<?>> getFactories();
	
	IEntityFactory<?> getFactory(String domain);
}
