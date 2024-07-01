package com.garganttua.api.core.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.engine.IGGAPIFactoriesRegistry;
import com.garganttua.api.spec.factory.IGGAPIEntityFactory;

public class GGAPIFactoriesRegistry implements IGGAPIFactoriesRegistry {

	private Map<String, IGGAPIEntityFactory<?>> factories;

	public GGAPIFactoriesRegistry(Map<String, IGGAPIEntityFactory<?>> factories) {
		this.factories = factories;
	}

	@Override
	public List<IGGAPIEntityFactory<?>> getFactories() {
		return new ArrayList<IGGAPIEntityFactory<?>>(this.factories.values());
	}

	@Override
	public IGGAPIEntityFactory<?> getFactory(String domain) {
		return this.factories.get(domain);
	}
}