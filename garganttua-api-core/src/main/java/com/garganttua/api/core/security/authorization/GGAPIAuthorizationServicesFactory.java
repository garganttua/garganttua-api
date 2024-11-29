package com.garganttua.api.core.security.authorization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.javatuples.Pair;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.authorization.IGGAPIAuthorizationInfosRegistry;
import com.garganttua.api.spec.security.authorization.IGGAPIAuthorizationServicesRegistry;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServicesRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIAuthorizationServicesFactory {

	private IGGAPIAuthorizationInfosRegistry authorizationInfosRegistry;
	private IGGAPIServicesRegistry servicesRegistry;
	private Set<IGGAPIDomain> domains;
	private Map<IGGAPIDomain, Pair<Class<?>, IGGAPIService>> services = new HashMap<IGGAPIDomain, Pair<Class<?>,IGGAPIService>>();

	public GGAPIAuthorizationServicesFactory(Set<IGGAPIDomain> domains, IGGAPIAuthorizationInfosRegistry authorizationInfosRegistry,
			IGGAPIServicesRegistry servicesRegistry) {
		this.domains = domains;
		this.authorizationInfosRegistry = authorizationInfosRegistry;
		this.servicesRegistry = servicesRegistry;
		this.collectServices();
	}

	private void collectServices() {
		log.info("*** Collecting Authorization Services ...");

		List<Class<?>> authorizationTypes = this.authorizationInfosRegistry.getAuthorizationsTypes();
		authorizationTypes.forEach(type -> {
			Optional<IGGAPIDomain> domain = this.domains.stream().filter(d -> {
				return d.getEntity().getValue0().equals(type);
			}).findFirst();
			
			domain.ifPresent(d -> {
				IGGAPIService service = this.servicesRegistry.getService(d.getDomain());
				this.services .put(d, new Pair<Class<?>, IGGAPIService>(type, service));
				log.info("		Authorization service added [domain {}, service {}]",
						d.getDomain(), service);	
			});
		});
	}

	public IGGAPIAuthorizationServicesRegistry getRegistry() {
		return new GGAPIAuthorizationServicesRegistry(this.services);
	}

}
