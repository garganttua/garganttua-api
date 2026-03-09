package com.garganttua.api.core.legacy.interfasse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.core.legacy.engine.EngineException;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.endpoint.IEndpoint;
import com.garganttua.api.spec.endpoint.IEndpointsRegistry;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.beans.GGBeanRefValidator;
import com.garganttua.core.reflection.beans.IGGBeanLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InterfacesFactory {

	private Collection<IDomain> domains;
	private IGGBeanLoader beanLoader;
	private Map<String, List<IEndpoint>> interfaces = new HashMap<String, List<IEndpoint>>();

	public InterfacesFactory(Collection<IDomain> domains, IGGBeanLoader beanLoader) throws EngineException {
		this.domains = domains;
		this.beanLoader = beanLoader;
		try {
			this.collectInterfaces();
		} catch (ReflectionException e) {
			throw new EngineException(e);
		}
	}

	private void collectInterfaces() throws ReflectionException {
		log.info("*** Collecting Interfaces ...");
		
		for( IDomain domain: this.domains ) {
			List<IEndpoint> listOfInterfaces = new ArrayList<IEndpoint>();
			this.interfaces.put(domain.getDomain(), listOfInterfaces ) ;

			for( String interfasse: domain.getInterfaces()) {
			
				Pair<String, String> ref = GGBeanRefValidator.validate(interfasse);
				
				IEndpoint interfasseObject = (IEndpoint) this.beanLoader.getBeanNamed(ref.getValue0(), ref.getValue1());
				interfasseObject.setDomain(domain);
				listOfInterfaces.add(interfasseObject);

				log.info("	Interface added [domain {}, interface {}]", domain.getDomain(), interfasseObject);
			}
		}
	}

	public IEndpointsRegistry getRegistry() {
		return new InterfacesRegistry(this.interfaces);
	}
}
