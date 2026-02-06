package com.garganttua.api.core.legacy.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.context.IEngine;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.factory.IFactoriesRegistry;
import com.garganttua.api.spec.factory.IFactory;

public class FactoriesRegistry implements IFactoriesRegistry {

	private Map<String, IFactory> factories;
	
	public FactoriesRegistry(Map<String, IFactory> factories) {
		this.factories = factories;
	}

	@Override
	public List<IFactory> getFactories() {
		return new ArrayList<IFactory>(this.factories.values());
	}

	@Override
	public IFactory getFactory(String domain) {
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