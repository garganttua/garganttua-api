package com.garganttua.api.core.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.factory.IEntityFactory;
import com.garganttua.api.spec.factory.IFactoriesRegistry;

public class FactoriesRegistry implements IFactoriesRegistry {

	private Map<String, IEntityFactory<?>> factories;
	
	public FactoriesRegistry(Map<String, IEntityFactory<?>> factories) {
		this.factories = factories;
	}

	@Override
	public List<IEntityFactory<?>> getFactories() {
		return new ArrayList<IEntityFactory<?>>(this.factories.values());
	}

	@Override
	public IEntityFactory<?> getFactory(String domain) {
		return this.factories.get(domain);
	}

	@Override
	public void setDomain(IDomain domain) {
	}

	@Override
	public void setEngine(IEngine engine) {
		this.factories.values().parallelStream().forEach(factory -> {
			factory.setEngine(engine);
		});
	}
}