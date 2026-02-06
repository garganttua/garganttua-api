package com.garganttua.api.core.legacy.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.context.IEngine;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.service.IService;
import com.garganttua.api.spec.service.IServicesRegistry;

public class ServicesRegistry implements IServicesRegistry {

	private Map<String, IService> services = new HashMap<String, IService>();

	public ServicesRegistry(Map<String, IService> services) {
		this.services = services;
	}

	@Override
	public IService getService(String domain) {
		return this.services.get(domain);
	}
	
	@Override
	public List<IService> getServices() {
		ArrayList<IService> servicesList = new ArrayList<IService>();
		this.services.values().parallelStream().forEach(service -> {
			servicesList.add(service);
		});

		return servicesList;
	}

	@Override
	public void setDomain(IDomain domain) {
	}

	@Override
	public void setEngine(IEngine engine) {
		this.services.values().parallelStream().forEach(service -> {
			service.setEngine(engine);
		});
	}
}
