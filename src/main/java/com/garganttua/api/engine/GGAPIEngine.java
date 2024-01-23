package com.garganttua.api.engine;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.garganttua.api.engine.accessors.IGGAPIAuthenticatorAccessor;
import com.garganttua.api.engine.accessors.IGGAPIOwnersControllerAccessor;
import com.garganttua.api.engine.accessors.IGGAPITenantsControllerAccessor;
import com.garganttua.api.engine.registries.IGGAPIAccessRulesRegistry;
import com.garganttua.api.engine.registries.IGGAPIControllersRegistry;
import com.garganttua.api.engine.registries.IGGAPIDaosRegistry;
import com.garganttua.api.engine.registries.IGGAPIRepositoriesRegistry;
import com.garganttua.api.engine.registries.IGGAPIServicesRegistry;
import com.garganttua.api.security.IGGAPISecurity;
import com.garganttua.api.security.authentication.ws.GGAPIAuthoritiesRestService;
import com.garganttua.api.ws.filters.GGAPIDynamicDomainFilter;
import com.garganttua.api.ws.filters.GGAPIOwnerFilter;
import com.garganttua.api.ws.filters.GGAPITenantFilter;

import jakarta.annotation.PostConstruct;
import lombok.Getter;

/**
 * This object to gives access to all objects created by the API Framework
 */
@Service
public class GGAPIEngine implements IGGAPIEngine {
	
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
	private IGGAPIControllersRegistry controllersRegistry;
	
	@Autowired
	@Getter
	private IGGAPIServicesRegistry servicesRegistry;
	
	@Autowired
	@Getter
	private IGGAPIAuthenticatorAccessor authenticatorAccessor;
	
	@Autowired
	@Getter
	private IGGAPITenantsControllerAccessor tenantsControllerAccessor;
	
	@Autowired
	@Getter
	private IGGAPIOwnersControllerAccessor ownerControllerAccessor;
	
	@Autowired
	@Getter
	private Optional<IGGAPISecurity> security;
	
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
	
	@PostConstruct
	private void injectSelfInEngineObjects() {
		IGGAPIEngine self = this;
		this.daosRegistry.getDaos().forEach(dao -> {
			dao.setEngine(self);
		});
		
		this.repositoriesRegistry.getRepositories().forEach(repo -> {
			repo.setEngine(self);
		});
		
		this.controllersRegistry.getControllers().forEach(controller -> {
			controller.setEngine(self);
			controller.getBusiness().ifPresent(business -> {business.setEngine(self);});
			controller.getConnector().ifPresent(connector -> {connector.setEngine(self);});
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
	}

}
