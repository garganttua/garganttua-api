package com.garganttua.api.security.core.accessRules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIAccessRulesRegistry;
import com.garganttua.api.spec.security.IGGAPIAccessRule;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

//@Service
@Slf4j
public class GGAPIAccessRulesRegistry implements IGGAPIAccessRulesRegistry {
	
	@Getter
	private List<IGGAPIAccessRule> accessRules = new ArrayList<IGGAPIAccessRule>();
	
	private Set<IGGAPIDomain> domains;

	public GGAPIAccessRulesRegistry(Set<IGGAPIDomain> domains) {
		this.domains = domains;
		this.init();
	}

	private void init() {
		log.info("Creating Access Rules ...");
		for( IGGAPIDomain domain: this.domains ) {
			this.createAccessRules(domain/*, this.servicesRegistry.getService(domain.getEntity().getValue1().domain())*/);
		}
//		
//		this.authoritiesWS.ifPresent(ws -> {
//			accessRules.addAll(ws.getCustomAuthorizations());
//		});
		
		this.accessRules.forEach(ar -> {
			log.info("	Access Rule added {}",ar);
		});
	}
//	
	@Override
	public IGGAPIAccessRule getAccessRule(GGAPIEntityOperation operation, String endpoint) {
		for (IGGAPIAccessRule auth : this.accessRules) {
			if (auth.getEndpoint().equals(endpoint) && auth.getOperation() == operation) {
				return auth;
			}
		}
		return null;
	}

	private List<IGGAPIAccessRule> createAccessRules(IGGAPIDomain domain/*, IGGAPIService service*/) {
		if( domain.isAllowReadAll() )
			this.accessRules.add(new BasicGGAPIAccessRule("/" + domain.getEntity().getValue1().domain().toLowerCase(),
					domain.getSecurity().readAllAuthority() == true
							? BasicGGAPIAccessRule.getAuthority(domain.getEntity().getValue1().domain().toLowerCase(),
									GGAPIEntityOperation.read_all)
							: null,
							GGAPIEntityOperation.read_all, domain.getSecurity().readAllAccess()));
		
		if( domain.isAllowCreation() )
			this.accessRules.add(new BasicGGAPIAccessRule("/" + domain.getEntity().getValue1().domain().toLowerCase(),
					domain.getSecurity().creationAuthority() == true
							? BasicGGAPIAccessRule.getAuthority(domain.getEntity().getValue1().domain().toLowerCase(),
									GGAPIEntityOperation.create_one)
							: null,
							GGAPIEntityOperation.create_one, domain.getSecurity().creationAccess()));
		
		if( domain.isAllowDeleteAll() )
			this.accessRules.add(new BasicGGAPIAccessRule("/" + domain.getEntity().getValue1().domain().toLowerCase(),
					domain.getSecurity().deleteAllAuthority() == true
							? BasicGGAPIAccessRule.getAuthority(domain.getEntity().getValue1().domain().toLowerCase(),
									GGAPIEntityOperation.delete_all)
							: null,
							GGAPIEntityOperation.delete_all, domain.getSecurity().deleteAllAccess()));
		
		if( domain.isAllowCount() )
			this.accessRules.add(new BasicGGAPIAccessRule("/" + domain.getEntity().getValue1().domain().toLowerCase() + "/count",
					domain.getSecurity().countAuthority() == true
							? BasicGGAPIAccessRule.getAuthority(domain.getEntity().getValue1().domain().toLowerCase(),
									GGAPIEntityOperation.count)
							: null,
							GGAPIEntityOperation.count, domain.getSecurity().countAccess()));
		
		if( domain.isAllowReadOne() )
			this.accessRules.add(new BasicGGAPIAccessRule("/" + domain.getEntity().getValue1().domain().toLowerCase() + "/*",
					domain.getSecurity().readOneAuthority() == true
							? BasicGGAPIAccessRule.getAuthority(domain.getEntity().getValue1().domain().toLowerCase(),
									GGAPIEntityOperation.read_one)
							: null,
							GGAPIEntityOperation.read_one, domain.getSecurity().readOneAccess()));
		
		if( domain.isAllowUpdateOne() )
			this.accessRules.add(new BasicGGAPIAccessRule("/" + domain.getEntity().getValue1().domain().toLowerCase() + "/*",
					domain.getSecurity().updateOneAuthority() == true
							? BasicGGAPIAccessRule.getAuthority(domain.getEntity().getValue1().domain().toLowerCase(),
									GGAPIEntityOperation.update_one)
							: null,
							GGAPIEntityOperation.update_one, domain.getSecurity().updateOneAccess()));
		
		if( domain.isAllowDeleteOne() )
			this.accessRules.add(new BasicGGAPIAccessRule("/" + domain.getEntity().getValue1().domain().toLowerCase() + "/*",
					domain.getSecurity().deleteOneAuthority() == true
							? BasicGGAPIAccessRule.getAuthority(domain.getEntity().getValue1().domain().toLowerCase(),
									GGAPIEntityOperation.delete_one)
							: null,
							GGAPIEntityOperation.delete_one, domain.getSecurity().deleteOneAccess()));
		
//		List<IGGAPICustomService> customServices = GGAPICustomServiceBuilder.buildGGAPIServices(service.getClass());
//		customServices.forEach( cService -> {
//			this.accessRules.add(cService.getAccessRule());
//		});
		
		return accessRules;
	}

}
