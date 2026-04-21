package com.garganttua.api.commons.factory;

import java.util.List;

public interface IFactoriesRegistry {

	List<IFactory> getFactories();
	
	IFactory getFactory(String domain);
}
