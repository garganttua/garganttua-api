package com.garganttua.api.core.interfasse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.interfasse.IGGAPIInterface;
import com.garganttua.api.spec.interfasse.IGGAPIInterfacesRegistry;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.beans.GGBeanRefValidator;
import com.garganttua.reflection.beans.IGGBeanLoader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIInterfacesFactory {

	private Collection<IGGAPIDomain> domains;
	private IGGBeanLoader beanLoader;
	private Map<String, List<IGGAPIInterface>> interfaces = new HashMap<String, List<IGGAPIInterface>>();

	public GGAPIInterfacesFactory(Collection<IGGAPIDomain> domains, IGGBeanLoader beanLoader) throws GGAPIEngineException {
		this.domains = domains;
		this.beanLoader = beanLoader;
		try {
			this.collectInterfaces();
		} catch (GGReflectionException e) {
			throw new GGAPIEngineException(e);
		}
	}

	private void collectInterfaces() throws GGReflectionException {
		log.info("*** Collecting Interfaces ...");
		
		for( IGGAPIDomain domain: this.domains ) {
			List<IGGAPIInterface> listOfInterfaces = new ArrayList<IGGAPIInterface>();
			this.interfaces.put(domain.getEntity().getValue1().domain(), listOfInterfaces ) ;

			for( String interfasse: domain.getInterfaces()) {
			
				Pair<String, String> ref = GGBeanRefValidator.validate(interfasse);
				
				IGGAPIInterface interfasseObject = (IGGAPIInterface) this.beanLoader.getBeanNamed(ref.getValue0(), ref.getValue1());
				interfasseObject.setDomain(domain);
				listOfInterfaces.add(interfasseObject);

				log.info("	Interface added [domain {}, interface {}]", domain.getEntity().getValue1().domain(), interfasseObject);
			}
		}
	}

	public IGGAPIInterfacesRegistry getRegistry() {
		return new GGAPIInterfacesRegistry(this.interfaces);
	}
}
