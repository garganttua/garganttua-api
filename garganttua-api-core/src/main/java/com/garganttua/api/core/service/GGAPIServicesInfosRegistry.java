package com.garganttua.api.core.service;

import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.service.IGGAPIServiceInfos;
import com.garganttua.api.spec.service.IGGAPIServicesInfosRegistry;

public class GGAPIServicesInfosRegistry implements IGGAPIServicesInfosRegistry {

	private Map<String, List<IGGAPIServiceInfos>> servicesInfos;

	public GGAPIServicesInfosRegistry(Map<String, List<IGGAPIServiceInfos>> servicesInfos) {
		this.servicesInfos = servicesInfos;
	}

	@Override
	public List<IGGAPIServiceInfos> getServiceInfos(String domainName) {
		return this.servicesInfos.get(domainName);
	}

}
