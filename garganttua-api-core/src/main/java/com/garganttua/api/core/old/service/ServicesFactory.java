package com.garganttua.api.core.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.core.runtime.Service;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.event.IEventPublisher;
import com.garganttua.api.spec.service.IService;
import com.garganttua.api.spec.service.IServicesRegistry;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.beans.IGGBeanLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServicesFactory {

	private Collection<IDomain> domains;
	private Map<String, IService> services = new HashMap<String, IService>();
	private IGGBeanLoader loader;

	public ServicesFactory(Collection<IDomain> domains, IGGBeanLoader loader) throws EngineException {
		this.domains = domains;
		this.loader = loader;
		try {
			this.collectService();
		} catch (GGReflectionException e) {
			throw new EngineException(e);
		}
	}

	private void collectService() throws GGReflectionException {
		log.info("*** Creating Services ...");
		for( IDomain domain: this.domains ) {
			
			Service service = new Service(domain);
			this.services.put(domain.getDomain(), service);
			
			if( domain.getEvent() != null && !domain.getEvent().isEmpty() ) {
				IEventPublisher event = (IEventPublisher) this.loader.getBeanNamed(domain.getEvent());
				service.setEventPublisher(Optional.ofNullable(event));
			}

			log.info("	Service added [domain {}, service {}]", domain.getDomain(), service);
		}
	}

	public IServicesRegistry getRegistry() {
		return new ServicesRegistry(this.services);
	}
}
