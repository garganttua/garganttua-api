package com.garganttua.api.core.engine.registries.impl;

import lombok.extern.slf4j.Slf4j;

//@Service
@Slf4j
public class GGAPIAccessRulesRegistry {
//	
//	@Autowired
//	private IGGAPIDomainsRegistry domainsRegistry;
//	
//	@Autowired
//	private IGGAPIServicesRegistry servicesRegistry;
//	
//	@Autowired
//	private Optional<GGAPIAuthoritiesRestService> authoritiesWS;
//
//	@Getter
//	private List<IGGAPIAccessRule> accessRules = new ArrayList<IGGAPIAccessRule>();
//
//	@PostConstruct
//	private void init() {
//		log.info("Creating Access Rules ...");
//		for( GGAPIDomain domain: this.domainsRegistry.getDomains() ) {
//			this.createAccessRules(domain, this.servicesRegistry.getService(domain.entity.getValue1().domain()));
//		}
//		
//		this.authoritiesWS.ifPresent(ws -> {
//			accessRules.addAll(ws.getCustomAuthorizations());
//		});
//		
//		this.accessRules.forEach(ar -> {
//			log.info("	Access Rule added {}",ar);
//		});
//	}
//	
//	@Override
//	public IGGAPIAccessRule getAccessRule(GGAPIServiceMethod method, String endpoint) {
//		for (IGGAPIAccessRule auth : this.accessRules) {
//			if (auth.getEndpoint().equals(endpoint) && auth.getMethod() == method) {
//				return auth;
//			}
//		}
//		return null;
//	}
//	
//	private List<IGGAPIAccessRule> createAccessRules(GGAPIDomain domain, IGGAPIService service) {
//		if( domain.allow_read_all )
//			this.accessRules.add(new BasicGGAPIAccessRule("/" + domain.entity.getValue1().domain().toLowerCase(),
//					domain.read_all_authority == true
//							? BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(),
//									GGAPICrudOperation.read_all)
//							: null,
//							GGAPIServiceMethod.READ, domain.read_all_access));
//		
//		if( domain.allow_creation )
//			this.accessRules.add(new BasicGGAPIAccessRule("/" + domain.entity.getValue1().domain().toLowerCase(),
//					domain.creation_authority == true
//							? BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(),
//									GGAPICrudOperation.create_one)
//							: null,
//							GGAPIServiceMethod.CREATE, domain.creation_access));
//		
//		if( domain.allow_delete_all )
//			this.accessRules.add(new BasicGGAPIAccessRule("/" + domain.entity.getValue1().domain().toLowerCase(),
//					domain.delete_all_authority == true
//							? BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(),
//									GGAPICrudOperation.delete_all)
//							: null,
//							GGAPIServiceMethod.DELETE, domain.delete_all_access));
//		
//		if( domain.allow_count )
//			this.accessRules.add(new BasicGGAPIAccessRule("/" + domain.entity.getValue1().domain().toLowerCase() + "/count",
//					domain.count_authority == true
//							? BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(),
//									GGAPICrudOperation.count)
//							: null,
//							GGAPIServiceMethod.READ, domain.count_access));
//		
//		if( domain.allow_read_one )
//			this.accessRules.add(new BasicGGAPIAccessRule("/" + domain.entity.getValue1().domain().toLowerCase() + "/*",
//					domain.read_one_authority == true
//							? BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(),
//									GGAPICrudOperation.read_one)
//							: null,
//							GGAPIServiceMethod.READ, domain.read_one_access));
//		
//		if( domain.allow_update_one )
//			this.accessRules.add(new BasicGGAPIAccessRule("/" + domain.entity.getValue1().domain().toLowerCase() + "/*",
//					domain.update_one_authority == true
//							? BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(),
//									GGAPICrudOperation.update_one)
//							: null,
//							GGAPIServiceMethod.PARTIAL_UPDATE, domain.update_one_access));
//		
//		if( domain.allow_delete_one )
//			this.accessRules.add(new BasicGGAPIAccessRule("/" + domain.entity.getValue1().domain().toLowerCase() + "/*",
//					domain.delete_one_authority == true
//							? BasicGGAPIAccessRule.getAuthority(domain.entity.getValue1().domain().toLowerCase(),
//									GGAPICrudOperation.delete_one)
//							: null,
//							GGAPIServiceMethod.DELETE, domain.delete_one_access));
//		
//		List<IGGAPICustomService> customServices = GGAPICustomServiceBuilder.buildGGAPIServices(service.getClass());
//		customServices.forEach( cService -> {
//			this.accessRules.add(cService.getAccessRule());
//		});
//		
//		return accessRules;
//	}

}
