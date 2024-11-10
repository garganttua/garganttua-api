package com.garganttua.api.core.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServicesRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIServicesFactory {

	private Collection<IGGAPIDomain> domains;
	private Map<String, IGGAPIService> services = new HashMap<String, IGGAPIService>();

	public GGAPIServicesFactory(Collection<IGGAPIDomain> domains) {
		this.domains = domains;
		this.collectService();
	}

	private void collectService() {
		log.info("*** Creating Services ...");
		for( IGGAPIDomain domain: this.domains ) {
			
			GGAPIService service = new GGAPIService(domain);
			this.services.put(domain.getDomain(), service);
			
			log.info("	Service added [domain {}, service {}]", domain.getEntity().getValue1().domain(), service);
		}
	}

	public IGGAPIServicesRegistry getRegistry() {
		return new GGAPIServicesRegistry(this.services);
	}
}
