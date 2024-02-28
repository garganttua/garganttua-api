package com.garganttua.api.engine.registries.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.garganttua.api.core.GGAPICrudOperation;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.registries.IGGAPIAccessRulesRegistry;
import com.garganttua.api.engine.registries.IGGAPIServicesRegistry;
import com.garganttua.api.security.authentication.ws.GGAPIAuthoritiesRestService;
import com.garganttua.api.security.authorization.BasicGGAPIAccessRule;
import com.garganttua.api.security.authorization.IGGAPIAccessRule;
import com.garganttua.api.service.GGAPICustomServiceBuilder;
import com.garganttua.api.service.GGAPIServiceMethod;
import com.garganttua.api.service.IGGAPICustomService;
import com.garganttua.api.service.IGGAPIService;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GGAPIAccessRulesRegistry implements IGGAPIAccessRulesRegistry {
	
	@Autowired
	private IGGAPIServicesRegistry servicesRegistry;
	
	@Autowired
	private Optional<GGAPIAuthoritiesRestService> authoritiesWS;

	@Getter
	private List<IGGAPIAccessRule> accessRules = new ArrayList<IGGAPIAccessRule>();

	@PostConstruct
	private void init() {
		this.servicesRegistry.getServices().forEach( service -> {
			this.createAccessRules(service);
		});
		this.authoritiesWS.ifPresent(ws -> {
			accessRules.addAll(ws.getCustomAuthorizations());
		});
	}
	
	@Override
	public IGGAPIAccessRule getAccessRule(GGAPIServiceMethod method, String endpoint) {

		for (IGGAPIAccessRule auth : this.accessRules) {
			if (auth.getEndpoint().equals(endpoint) && auth.getMethod() == method) {
				return auth;
			}
		}

		return null;
	}
	
	private List<IGGAPIAccessRule> createAccessRules(IGGAPIService<?,?> service) {
		GGAPIDynamicDomain domain = service.getDynamicDomain();
		if( domain.allow_read_all )
			this.accessRules.add(new BasicGGAPIAccessRule("/" + domain.domain.toLowerCase(),
					domain.read_all_authority == true
							? BasicGGAPIAccessRule.getAuthority(domain.domain.toLowerCase(),
									GGAPICrudOperation.read_all)
							: null,
							GGAPIServiceMethod.READ, domain.read_all_access));
		
		if( domain.allow_creation )
			this.accessRules.add(new BasicGGAPIAccessRule("/" + domain.domain.toLowerCase(),
					domain.creation_authority == true
							? BasicGGAPIAccessRule.getAuthority(domain.domain.toLowerCase(),
									GGAPICrudOperation.create_one)
							: null,
							GGAPIServiceMethod.CREATE, domain.creation_access));
		
		if( domain.allow_delete_all )
			this.accessRules.add(new BasicGGAPIAccessRule("/" + domain.domain.toLowerCase(),
					domain.delete_all_authority == true
							? BasicGGAPIAccessRule.getAuthority(domain.domain.toLowerCase(),
									GGAPICrudOperation.delete_all)
							: null,
							GGAPIServiceMethod.DELETE, domain.delete_all_access));
		
		if( domain.allow_count )
			this.accessRules.add(new BasicGGAPIAccessRule("/" + domain.domain.toLowerCase() + "/count",
					domain.count_authority == true
							? BasicGGAPIAccessRule.getAuthority(domain.domain.toLowerCase(),
									GGAPICrudOperation.count)
							: null,
							GGAPIServiceMethod.READ, domain.count_access));
		
		if( domain.allow_read_one )
			this.accessRules.add(new BasicGGAPIAccessRule("/" + domain.domain.toLowerCase() + "/*",
					domain.read_one_authority == true
							? BasicGGAPIAccessRule.getAuthority(domain.domain.toLowerCase(),
									GGAPICrudOperation.read_one)
							: null,
							GGAPIServiceMethod.READ, domain.read_one_access));
		
		if( domain.allow_update_one )
			this.accessRules.add(new BasicGGAPIAccessRule("/" + domain.domain.toLowerCase() + "/*",
					domain.update_one_authority == true
							? BasicGGAPIAccessRule.getAuthority(domain.domain.toLowerCase(),
									GGAPICrudOperation.update_one)
							: null,
							GGAPIServiceMethod.PARTIAL_UPDATE, domain.update_one_access));
		
		if( domain.allow_delete_one )
			this.accessRules.add(new BasicGGAPIAccessRule("/" + domain.domain.toLowerCase() + "/*",
					domain.delete_one_authority == true
							? BasicGGAPIAccessRule.getAuthority(domain.domain.toLowerCase(),
									GGAPICrudOperation.delete_one)
							: null,
							GGAPIServiceMethod.DELETE, domain.delete_one_access));
		
		List<IGGAPICustomService> customServices = GGAPICustomServiceBuilder.buildGGAPIServices(service.getClass());
		customServices.forEach( cService -> {
			this.accessRules.add(cService.getAccessRule());
		});

		return accessRules;
	}

}
