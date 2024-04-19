package com.garganttua.api.core.engine;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.garganttua.api.core.engine.registries.IGGAPIAccessRulesRegistry;
import com.garganttua.api.core.engine.registries.IGGAPIDaosRegistry;
import com.garganttua.api.core.engine.registries.IGGAPIDomainsRegistry;
import com.garganttua.api.core.engine.registries.IGGAPIFactoriesRegistry;
import com.garganttua.api.core.engine.registries.IGGAPIRepositoriesRegistry;
import com.garganttua.api.core.engine.registries.IGGAPIServicesRegistry;
import com.garganttua.api.core.security.IGGAPISecurity;
import com.garganttua.api.core.security.authentication.ws.GGAPIAuthoritiesRestService;
import com.garganttua.api.services.rest.filters.GGAPIDomainFilter;
import com.garganttua.api.services.rest.filters.GGAPIOwnerFilter;
import com.garganttua.api.services.rest.filters.GGAPITenantFilter;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * This object to gives access to all objects created by the API Framework
 */
@Service(value = "GGAPIEngine")
@Slf4j
public class GGAPIEngine implements IGGAPIEngine {

	
	@Autowired 
	@Getter
	private IGGAPIDomainsRegistry domainsRegistry;
	
	@Autowired
	@Getter
	private IGGAPIDaosRegistry daosRegistry;
	
	@Autowired
	@Getter
	private IGGAPIRepositoriesRegistry repositoriesRegistry;
	
	@Autowired
	@Getter
	private IGGAPIServicesRegistry servicesRegistry;
	
	@Autowired
	@Getter
	private IGGAPIFactoriesRegistry factoriesRegistry;
	
	@Autowired
	private GGAPIDomainFilter ddomainFilter;
	
	@Autowired
	private GGAPITenantFilter tenantFilter;
	
	@Autowired
	private GGAPIOwnerFilter ownerFilter;
	
	@Autowired
	private Optional<GGAPIAuthoritiesRestService> authoritiesWS;
	
	@Autowired
	@Getter
	private IGGAPIAccessRulesRegistry accessRulesRegistry;

	@Autowired
	private Optional<IGGAPISecurity> security;
	
	public GGAPIEngine() {
		log.info("============================================");
		log.info("======                                ======");
		log.info("====== Starting Garganttua API Engine ======");
		log.info("======                                ======");
		log.info("============================================");
		log.info("Version: {}", this.getClass().getPackage().getImplementationVersion());
		log.info("== BOOTING ENGINE ==");
	}
	
	@PostConstruct
	private void init() {
		
		IGGAPIEngine self = this;
		this.daosRegistry.getDaos().forEach(dao -> {
			dao.getValue1().setEngine(self);
		});
		
		this.repositoriesRegistry.getRepositories().forEach(repo -> {
			repo.setEngine(self);
		});

		this.servicesRegistry.getServices().forEach(service -> {
			service.setEngine(self);
//			service.getEventPublisher().ifPresent( ePublisher -> {ePublisher.setEngine(self);});
		});
		
		this.factoriesRegistry.getFactories().forEach( factory -> {
			factory.setEngine(self);
		});
		
		this.ddomainFilter.setEngine(self);
		this.tenantFilter.setEngine(self);
		this.ownerFilter.setEngine(self);
		
		this.authoritiesWS.ifPresent( ws -> {
			ws.setEngine(self);
		});

	}

	@Override
	public GGAPIDomain getOwnerDomain() {
		return this.domainsRegistry.getOwnerDomain();
	}

	@Override
	public GGAPIDomain getTenantDomain() {
		return this.domainsRegistry.getTenantDomain();
	}

	@Override
	public Optional<IGGAPISecurity> getSecurity() {
		return this.security;
	}
}
