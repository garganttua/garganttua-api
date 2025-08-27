package com.garganttua.api.core.caller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.garganttua.api.spec.caller.ICallerFactoriesRegistry;
import com.garganttua.api.spec.caller.ICallerFactory;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.engine.IAccessRulesRegistry;
import com.garganttua.api.spec.factory.IEntityFactory;
import com.garganttua.api.spec.factory.IFactoriesRegistry;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CallerFactoriesFactory {

	private Collection<IDomain> domains;
	private IFactoriesRegistry factoriesRegistry;
	
	private Map<String, ICallerFactory> callerFactories = new HashMap<String, ICallerFactory>();
	private IAccessRulesRegistry accessRulesRegistry;

	public CallerFactoriesFactory(Collection<IDomain> domains, IFactoriesRegistry factoriesRegistry, IAccessRulesRegistry accessRulesRegistry) {
		this.domains = domains;
		this.factoriesRegistry = factoriesRegistry;
		this.accessRulesRegistry = accessRulesRegistry;
	
		this.createCallerFactories();
	}

	private void createCallerFactories() {
		log.info("*** Creating Caller Factories ...");
		IDomain tenantsDomain = this.getTenantDomain();
		IEntityFactory<?> tenantsFactory = this.getTenantsFactory(tenantsDomain);
		Map<String, IDomain> ownersDomain = this.getOwnerDomains();
		Map<String, IEntityFactory<?>> ownersFactory = this.getOwnerFactories(ownersDomain);
		
		this.domains.stream().forEach(domain -> {
			CallerFactory factory = new CallerFactory(domain, tenantsDomain, tenantsFactory, ownersDomain, ownersFactory, this.accessRulesRegistry);
			this.callerFactories.put(domain.getDomain(), factory);
			log.info("	Caller factory added [domain {}, caller factory {}]", domain.getDomain(), factory);
		});
	}

	private IEntityFactory<?> getTenantsFactory(IDomain tenantsDomain) {
		return this.factoriesRegistry.getFactory(tenantsDomain.getDomain());
	}

	private Map<String, IEntityFactory<?>> getOwnerFactories(Map<String, IDomain> ownersDomain) {
		Map<String, IEntityFactory<?>> factories = new HashMap<String, IEntityFactory<?>>();
		ownersDomain.forEach((domainName, domain) -> {
			factories.put(domainName, this.factoriesRegistry.getFactory(domainName));
		});
		
		return factories;
	}

	private Map<String, IDomain> getOwnerDomains() {
		Map<String, IDomain> domains = new HashMap<String, IDomain>();
		
		this.domains.forEach(domain -> {
			if( domain.isOwnerEntity() )
				domains.put(domain.getDomain(), domain);
		});
		
		return domains;
	}

	private IDomain getTenantDomain() {
		return this.domains.parallelStream().filter(domain -> {
			return domain.isTenantEntity();
		}).collect(Collectors.toList()).get(0);
	}

	public ICallerFactoriesRegistry getRegistry() {
		return new CallerFactoriesRegistry(this.callerFactories);
	}
}
