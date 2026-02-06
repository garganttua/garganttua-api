package com.garganttua.api.core.legacy.security.authorization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.javatuples.Pair;

import com.garganttua.api.spec.context.IEngine;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.security.authorization.IAuthorizationInfosRegistry;
import com.garganttua.api.spec.security.authorization.IAuthorizationServicesRegistry;
import com.garganttua.api.spec.service.IService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthorizationServicesFactory {

	private IAuthorizationInfosRegistry authorizationInfosRegistry;
	private IEngine engine;
	private Set<IDomain> domains;
	private Map<IDomain, Pair<Class<?>, IService>> services = new HashMap<IDomain, Pair<Class<?>,IService>>();

	public AuthorizationServicesFactory(Set<IDomain> domains, IAuthorizationInfosRegistry authorizationInfosRegistry,
			IEngine engine) {
		this.domains = domains;
		this.authorizationInfosRegistry = authorizationInfosRegistry;
		this.engine = engine;
		this.collectServices();
	}

	private void collectServices() {
		log.info("*** Collecting Authorization Services ...");

		List<Class<?>> authorizationTypes = this.authorizationInfosRegistry.getAuthorizationsTypes();
		authorizationTypes.forEach(type -> {
			Optional<IDomain> domain = this.domains.stream().filter(d -> {
				return d.getEntityClass().equals(type);
			}).findFirst();
			
			domain.ifPresent(d -> {
				IService service = this.engine.getService(d.getDomain());
				this.services .put(d, new Pair<Class<?>, IService>(type, service));
				log.info("		Authorization service added [domain {}, service {}]",
						d.getDomain(), service);	
			});
		});
	}

	public IAuthorizationServicesRegistry getRegistry() {
		return new AuthorizationServicesRegistry(this.services);
	}

}
