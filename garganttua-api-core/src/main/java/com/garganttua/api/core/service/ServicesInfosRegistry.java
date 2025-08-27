package com.garganttua.api.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.service.IServiceInfos;
import com.garganttua.api.spec.service.IServicesInfosRegistry;

public class ServicesInfosRegistry implements IServicesInfosRegistry {

	private Map<String, List<IServiceInfos>> servicesInfos;

	public ServicesInfosRegistry(Map<String, List<IServiceInfos>> servicesInfos) {
		this.servicesInfos = servicesInfos;
	}

	@Override
	public List<IServiceInfos> getServiceInfos(String domainName) {
		return this.servicesInfos.get(domainName);
	}

	@Override
	public List<IServiceInfos> getServicesInfos() {
		List<IServiceInfos> list = new ArrayList<IServiceInfos>();
		
		this.servicesInfos.forEach((domainName, l)-> {
			l.forEach(infos -> {
				list.add(infos);
			});
		});
		
		return list;
	}

	@Override
	public void addServicesInfos(IDomain domain, List<IServiceInfos> authenticationServiceInfos) {
		this.servicesInfos.get(domain.getDomain()).addAll(authenticationServiceInfos);
	}

}
