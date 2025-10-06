package com.garganttua.api.core.interfasse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.api.spec.interfasse.IInterfacesRegistry;

public class InterfacesRegistry implements IInterfacesRegistry {

	private Map<String, List<IInterface>> interfaces;

	public InterfacesRegistry(Map<String, List<IInterface>> interfaces) {
		this.interfaces = interfaces;
	}

	@Override
	public List<IInterface> getInterfaces(String domainName) {
		return this.interfaces.get(domainName);
	}

	@Override
	public List<IInterface> getInterfaces() {
		List<IInterface> list = new ArrayList<IInterface>();
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
