package com.garganttua.api.core.caller;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
	private String superTenantId;
	private String superOwnerId;
	private IGGAPIAccessRulesRegistry accessRulesRegistry;

	public GGAPICallerFactoriesFactory(Collection<IGGAPIDomain> domains, IGGAPIFactoriesRegistry factoriesRegistry, IGGAPIAccessRulesRegistry accessRulesRegistry) {
		this.domains = domains;
		this.factoriesRegistry = factoriesRegistry;
	
		this.createCallerFactories();
	}

	private void createCallerFactories() {
		log.info("*** Creating Caller Factories ...");
		IGGAPIDomain tenantsDomain = this.getTenantDomain();
		IGGAPIEntityFactory<?> tenantsFactory = this.getTenantsFactory(tenantsDomain);
		Optional<IGGAPIDomain> ownersDomain = this.getOwnersDomain();
		Optional<IGGAPIEntityFactory<?>> ownersFactory = this.getOwnersFactory(ownersDomain);
		
		this.domains.stream().forEach(domain -> {
			GGAPICallerFactory factory = new GGAPICallerFactory(domain, this.superTenantId, this.superOwnerId, tenantsDomain, tenantsFactory, ownersDomain, ownersFactory, this.accessRulesRegistry);
			this.callerFactories.put(domain.getDomain(), factory);
			log.info("	Caller factory added [domain {}, caller factory {}]", domain.getEntity().getValue1().domain(), factory);
		});
	}

	private IGGAPIEntityFactory<?> getTenantsFactory(IGGAPIDomain tenantsDomain) {
		return this.factoriesRegistry.getFactory(tenantsDomain.getDomain());
	}

	private Optional<IGGAPIEntityFactory<?>> getOwnersFactory(Optional<IGGAPIDomain> ownersDomain) {
		if( ownersDomain.isPresent() ) {
			return Optional.of(this.factoriesRegistry.getFactory(ownersDomain.get().getDomain()));
		}
		
		return Optional.empty(); 
	}

	private Optional<IGGAPIDomain> getOwnersDomain() {
		List<IGGAPIDomain> owners = this.domains.parallelStream().filter(domain -> {
			return domain.getEntity().getValue1().ownerEntity();
		}).collect(Collectors.toList());
				
				
		return owners.size()>0?Optional.ofNullable(owners.get(0)):Optional.empty();
	}

	private IGGAPIDomain getTenantDomain() {
		return this.domains.parallelStream().filter(domain -> {
			return domain.getEntity().getValue1().tenantEntity();
		}).collect(Collectors.toList()).get(0);
	}

	public IGGAPICallerFactoriesRegistry getRegistry() {
		return new GGAPICallerFactoriesRegistry(this.callerFactories);
	}
}
