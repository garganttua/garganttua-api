package com.garganttua.api.core.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.factory.IGGAPIEntityFactory;
import com.garganttua.api.spec.factory.IGGAPIFactoriesRegistry;

import lombok.Setter;

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

	@Override
	public void setDomain(IGGAPIDomain domain) {
	}

	@Override
	public void setEngine(IGGAPIEngine engine) {
		this.factories.values().parallelStream().forEach(factory -> {
			factory.setEngine(engine);
		});
	}
}