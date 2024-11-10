package com.garganttua.api.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServicesRegistry;

public class GGAPIServicesRegistry implements IGGAPIServicesRegistry {

	private Map<String, IGGAPIService> services = new HashMap<String, IGGAPIService>();

	public GGAPIServicesRegistry(Map<String, IGGAPIService> services) {
		this.services = services;
	}

	@Override
	public IGGAPIService getService(String domain) {
		return this.services.get(domain);
	}
	
	@Override
	public List<IGGAPIService> getServices() {
		ArrayList<IGGAPIService> servicesList = new ArrayList<IGGAPIService>();
		this.services.values().parallelStream().forEach(service -> {
			servicesList.add(service);
		});

		return servicesList;
	}

	@Override
	public void setDomain(IGGAPIDomain domain) {
	}

	@Override
	public void setEngine(IGGAPIEngine engine) {
		this.services.values().parallelStream().forEach(service -> {
			service.setEngine(engine);
		});
	}
}
