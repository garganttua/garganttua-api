package com.garganttua.api.core.legacy.interfasse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.context.IEngine;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.endpoint.IEndpoint;
import com.garganttua.api.spec.endpoint.IEndpointsRegistry;

public class InterfacesRegistry implements IEndpointsRegistry {

	private Map<String, List<IEndpoint>> interfaces;

	public InterfacesRegistry(Map<String, List<IEndpoint>> interfaces) {
		this.interfaces = interfaces;
	}

	@Override
	public List<IEndpoint> getInterfaces(String domainName) {
		return this.interfaces.get(domainName);
	}

	@Override
	public List<IEndpoint> getInterfaces() {
		List<IEndpoint> list = new ArrayList<IEndpoint>();
		this.interfaces.values().parallelStream().forEach(interfaces -> {
			list.addAll(interfaces);
		});	
		return list ;
	}

	@Override
	public void setDomain(IDomain domain) {
	}

	@Override
	public void setEngine(IEngine engine) {
		this.interfaces.values().parallelStream().forEach(list -> {
			list.parallelStream().forEach(inter -> {
				inter.setEngine(engine);
			});
		});
	}
}
