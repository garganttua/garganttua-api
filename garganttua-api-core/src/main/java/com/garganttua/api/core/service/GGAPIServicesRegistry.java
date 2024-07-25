package com.garganttua.api.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;
import com.garganttua.api.spec.service.IGGAPIServicesRegistry;

public class GGAPIServicesRegistry implements IGGAPIServicesRegistry {

	private Map<String, Pair<IGGAPIService, List<IGGAPIServiceInfos>>> services = new HashMap<String, Pair<IGGAPIService, List<IGGAPIServiceInfos>>>();

	public GGAPIServicesRegistry(Map<String, Pair<IGGAPIService, List<IGGAPIServiceInfos>>> services) {
		this.services = services;
	}

	@Override
	public IGGAPIService getService(String domain) {
		return this.services.get(domain).getValue0();
	}
	
	@Override
	public List<IGGAPIServiceInfos> getServiceInfos(String domain){
		return this.services.get(domain).getValue1();
	}

	@Override
	public List<IGGAPIService> getServices() {
		ArrayList<IGGAPIService> servicesList = new ArrayList<IGGAPIService>();
		this.services.values().parallelStream().forEach(pair -> {
			servicesList.add(pair.getValue0());
		});

		return servicesList;
	}

	@Override
	public void setDomain(IGGAPIDomain domain) {
	}

	@Override
	public void setEngine(IGGAPIEngine engine) {
		this.services.values().parallelStream().forEach(pair -> {
			pair.getValue0().setEngine(engine);
		});
	}
}
