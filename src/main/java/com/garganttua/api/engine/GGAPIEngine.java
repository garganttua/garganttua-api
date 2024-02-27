package com.garganttua.api.engine;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.garganttua.api.core.GGAPIEntityFactory;
import com.garganttua.api.core.IGGAPIEntityFactory;
import com.garganttua.api.engine.registries.IGGAPIAccessRulesRegistry;
import com.garganttua.api.engine.registries.IGGAPIDaosRegistry;
import com.garganttua.api.engine.registries.IGGAPIDynamicDomainsRegistry;
import com.garganttua.api.engine.registries.IGGAPIRepositoriesRegistry;
import com.garganttua.api.engine.registries.IGGAPIServicesRegistry;
import com.garganttua.api.security.authentication.ws.GGAPIAuthoritiesRestService;
import com.garganttua.api.ws.filters.GGAPIDynamicDomainFilter;
import com.garganttua.api.ws.filters.GGAPIOwnerFilter;
import com.garganttua.api.ws.filters.GGAPITenantFilter;

import jakarta.annotation.PostConstruct;
import lombok.Getter;

/**
 * This object to gives access to all objects created by the API Framework
 */
@Service(value = "GGAPIEngine")
public class GGAPIEngine implements IGGAPIEngine {
	
	@Autowired
	protected ApplicationContext context;
	
    @Autowired
    private Environment environment;
	
	@Autowired 
	@Getter
	private IGGAPIDynamicDomainsRegistry dynamicDomainsRegistry;
	
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
	private GGAPIDynamicDomainFilter ddomainFilter;
	
	@Autowired
	private GGAPITenantFilter tenantFilter;
	
	@Autowired
	private GGAPIOwnerFilter ownerFilter;
	
	@Autowired
	private Optional<GGAPIAuthoritiesRestService> authoritiesWS;
	
	@Autowired
	@Getter
	private IGGAPIAccessRulesRegistry accessRulesRegistry;
	
	@Getter
	private IGGAPIEntityFactory entityFactory = new GGAPIEntityFactory();
	
	@PostConstruct
	private void injectSelfInEngineObjects() {
		this.entityFactory.setRepositoriesRegistry(this.repositoriesRegistry);
		
		IGGAPIEngine self = this;
		this.daosRegistry.getDaos().forEach(dao -> {
			dao.setEngine(self);
		});
		
		this.repositoriesRegistry.getRepositories().forEach(repo -> {
			repo.setEngine(self);
		});

		this.servicesRegistry.getServices().forEach(service -> {
			service.setEngine(self);
			service.getEventPublisher().ifPresent( ePublisher -> {ePublisher.setEngine(self);});
		});
		
		this.ddomainFilter.setEngine(self);
		this.tenantFilter.setEngine(self);
		this.ownerFilter.setEngine(self);
		
		this.authoritiesWS.ifPresent( ws -> {
			ws.setEngine(self);
		});
		
		this.entityFactory.setSpringContext(this.context);
		this.entityFactory.setEnvironment(this.environment);
	}

	@Bean
	private IGGAPIEntityFactory factory() {
		return this.entityFactory;
	}
	
	@Override
	public GGAPIDynamicDomain getAuthenticatorDomain() {
		return this.dynamicDomainsRegistry.getAuthenticatorDomain();
	}

	@Override
	public GGAPIDynamicDomain getOwnerDomain() {
		return this.dynamicDomainsRegistry.getOwnerDomain();
	}

	@Override
	public GGAPIDynamicDomain getTenantDomain() {
		return this.dynamicDomainsRegistry.getTenantDomain();
	}

}
