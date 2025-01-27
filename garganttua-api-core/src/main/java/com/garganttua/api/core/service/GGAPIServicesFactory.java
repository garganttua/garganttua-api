package com.garganttua.api.core.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.event.IGGAPIEventPublisher;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServicesRegistry;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.beans.IGGBeanLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIServicesFactory {

	private Collection<IGGAPIDomain> domains;
	private Map<String, IGGAPIService> services = new HashMap<String, IGGAPIService>();
	private IGGBeanLoader loader;

	public GGAPIServicesFactory(Collection<IGGAPIDomain> domains, IGGBeanLoader loader) throws GGAPIEngineException {
		this.domains = domains;
		this.loader = loader;
		try {
			this.collectService();
		} catch (GGReflectionException e) {
			throw new GGAPIEngineException(e);
		}
	}

	private void collectService() throws GGReflectionException {
		log.info("*** Creating Services ...");
		for( IGGAPIDomain domain: this.domains ) {
			
			GGAPIService service = new GGAPIService(domain);
			this.services.put(domain.getDomain(), service);
			
			if( domain.getEvent() != null && !domain.getEvent().isEmpty() ) {
				IGGAPIEventPublisher event = (IGGAPIEventPublisher) this.loader.getBeanNamed(domain.getEvent());
				service.setEventPublisher(Optional.ofNullable(event));
			}

			log.info("	Service added [domain {}, service {}]", domain.getDomain(), service);
		}
	}

	public IGGAPIServicesRegistry getRegistry() {
		return new GGAPIServicesRegistry(this.services);
	}
}
