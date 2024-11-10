package com.garganttua.api.core.interfasse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.interfasse.IGGAPIInterface;
import com.garganttua.api.spec.interfasse.IGGAPIInterfacesRegistry;

public class GGAPIInterfacesRegistry implements IGGAPIInterfacesRegistry {

	private Map<String, List<IGGAPIInterface>> interfaces;

	public GGAPIInterfacesRegistry(Map<String, List<IGGAPIInterface>> interfaces) {
		this.interfaces = interfaces;
	}

	@Override
	public List<IGGAPIInterface> getInterfaces(String domainName) {
		return this.interfaces.get(domainName);
	}

	@Override
	public List<IGGAPIInterface> getInterfaces() {
		List<IGGAPIInterface> list = new ArrayList<IGGAPIInterface>();
		this.interfaces.values().parallelStream().forEach(interfaces -> {
			list.addAll(interfaces);
		});	
		return list ;
	}

	@Override
	public void setDomain(IGGAPIDomain domain) {
	}

	@Override
	public void setEngine(IGGAPIEngine engine) {
		this.interfaces.values().parallelStream().forEach(list -> {
			list.parallelStream().forEach(inter -> {
				inter.setEngine(engine);
			});
		});
	}
}
