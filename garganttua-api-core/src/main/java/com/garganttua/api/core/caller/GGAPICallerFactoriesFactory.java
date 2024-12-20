package com.garganttua.api.core.caller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.garganttua.api.spec.caller.IGGAPICallerFactoriesRegistry;
import com.garganttua.api.spec.caller.IGGAPICallerFactory;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIAccessRulesRegistry;
import com.garganttua.api.spec.factory.IGGAPIEntityFactory;
import com.garganttua.api.spec.factory.IGGAPIFactoriesRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPICallerFactoriesFactory {

	private Collection<IGGAPIDomain> domains;
	private IGGAPIFactoriesRegistry factoriesRegistry;
	
	private Map<String, IGGAPICallerFactory> callerFactories = new HashMap<String, IGGAPICallerFactory>();
	private IGGAPIAccessRulesRegistry accessRulesRegistry;

	public GGAPICallerFactoriesFactory(Collection<IGGAPIDomain> domains, IGGAPIFactoriesRegistry factoriesRegistry, IGGAPIAccessRulesRegistry accessRulesRegistry) {
		this.domains = domains;
		this.factoriesRegistry = factoriesRegistry;
		this.accessRulesRegistry = accessRulesRegistry;
	
		this.createCallerFactories();
	}

	private void createCallerFactories() {
		log.info("*** Creating Caller Factories ...");
		IGGAPIDomain tenantsDomain = this.getTenantDomain();
		IGGAPIEntityFactory<?> tenantsFactory = this.getTenantsFactory(tenantsDomain);
		Map<String, IGGAPIDomain> ownersDomain = this.getOwnerDomains();
		Map<String, IGGAPIEntityFactory<?>> ownersFactory = this.getOwnerFactories(ownersDomain);
		
		this.domains.stream().forEach(domain -> {
			GGAPICallerFactory factory = new GGAPICallerFactory(domain, tenantsDomain, tenantsFactory, ownersDomain, ownersFactory, this.accessRulesRegistry);
			this.callerFactories.put(domain.getDomain(), factory);
			log.info("	Caller factory added [domain {}, caller factory {}]", domain.getDomain(), factory);
		});
	}

	private IGGAPIEntityFactory<?> getTenantsFactory(IGGAPIDomain tenantsDomain) {
		return this.factoriesRegistry.getFactory(tenantsDomain.getDomain());
	}

	private Map<String, IGGAPIEntityFactory<?>> getOwnerFactories(Map<String, IGGAPIDomain> ownersDomain) {
		Map<String, IGGAPIEntityFactory<?>> factories = new HashMap<String, IGGAPIEntityFactory<?>>();
		ownersDomain.forEach((domainName, domain) -> {
			factories.put(domainName, this.factoriesRegistry.getFactory(domainName));
		});
		
		return factories;
	}

	private Map<String, IGGAPIDomain> getOwnerDomains() {
		Map<String, IGGAPIDomain> domains = new HashMap<String, IGGAPIDomain>();
		
		this.domains.forEach(domain -> {
			if( domain.isOwnerEntity() )
				domains.put(domain.getDomain(), domain);
		});
		
		return domains;
	}

	private IGGAPIDomain getTenantDomain() {
		return this.domains.parallelStream().filter(domain -> {
			return domain.isTenantEntity();
		}).collect(Collectors.toList()).get(0);
	}

	public IGGAPICallerFactoriesRegistry getRegistry() {
		return new GGAPICallerFactoriesRegistry(this.callerFactories);
	}
}
