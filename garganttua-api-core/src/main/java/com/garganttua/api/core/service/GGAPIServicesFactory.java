package com.garganttua.api.core.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIServicesRegistry;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIServicesFactory {

	private Collection<IGGAPIDomain> domains;
	private Map<String, Pair<IGGAPIService, List<IGGAPIServiceInfos>>> services = new HashMap<String, Pair<IGGAPIService, List<IGGAPIServiceInfos>>>();

	public GGAPIServicesFactory(Collection<IGGAPIDomain> domains) {
		this.domains = domains;
		this.collectService();
	}

	private void collectService() {
		log.info("*** Creating Services ...");
		for( IGGAPIDomain domain: this.domains ) {
			
			GGAPIService service = new GGAPIService(domain);
			List<IGGAPIServiceInfos> infos = GGAPIServicesInfosBuilder.buildGGAPIServices(domain, service.getClass());
			this.services.put(domain.getDomain(), new Pair<IGGAPIService, List<IGGAPIServiceInfos>>(service, infos));
			
			log.info("	Service added [domain {}, service {}]", domain.getEntity().getValue1().domain(), service);
			
			infos.forEach(info -> {
				log.info("		Method added [domain {}, service {}]", domain.getEntity().getValue1().domain(), info);				
			});
		}
	}

	public IGGAPIServicesRegistry getRegistry() {
		return new GGAPIServicesRegistry(this.services);
	}
}
