package com.garganttua.api.core.service;

import java.util.ArrayList;
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

	@Override
	public List<IGGAPIServiceInfos> getServicesInfos() {
		List<IGGAPIServiceInfos> list = new ArrayList<IGGAPIServiceInfos>();
		
		this.servicesInfos.forEach((domainName, l)-> {
			l.forEach(infos -> {
				list.add(infos);
			});
		});
		
		return list;
	}

}
