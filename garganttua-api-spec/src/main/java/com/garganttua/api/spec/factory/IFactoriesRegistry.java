package com.garganttua.api.spec.factory;

import java.util.List;

import com.garganttua.api.spec.engine.IEngineObject;

public interface IFactoriesRegistry extends IEngineObject {

	List<IFactory> getFactories();
	
	IFactory getFactory(String domain);
}
