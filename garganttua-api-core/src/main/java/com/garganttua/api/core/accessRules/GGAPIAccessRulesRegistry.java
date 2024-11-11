package com.garganttua.api.core.accessRules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIAccessRulesRegistry;
import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;
import com.garganttua.api.spec.service.IGGAPIServicesInfosRegistry;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIAccessRulesRegistry implements IGGAPIAccessRulesRegistry {

	@Getter
	private List<IGGAPIAccessRule> accessRules = new ArrayList<IGGAPIAccessRule>();

	private Set<IGGAPIDomain> domains;

	private IGGAPIServicesInfosRegistry servicesInfosRegistry;

	public GGAPIAccessRulesRegistry(Set<IGGAPIDomain> domains, IGGAPIServicesInfosRegistry servicesInfosRegistry) {
		this.domains = domains;
		this.servicesInfosRegistry = servicesInfosRegistry;
		this.init();
	}

	private void init() {
		log.info("Creating Access Rules ...");
		for (IGGAPIDomain domain : this.domains) {
			this.createAccessRules(domain);
		}
		this.accessRules.forEach(ar -> {
			log.info("	Access Rule added {}", ar);
		});
	}

	@Override
	public IGGAPIAccessRule getAccessRule(GGAPIEntityOperation operation, String endpoint) {
		for (IGGAPIAccessRule auth : this.accessRules) {
			if (auth.getEndpoint().equals(endpoint) && auth.getOperation() == operation) {
				return auth;
			}
		}
		return null;
	}

	private List<IGGAPIAccessRule> createAccessRules(IGGAPIDomain domain) {
		List<IGGAPIServiceInfos> infos = this.servicesInfosRegistry.getServiceInfos(domain.getDomain());

		for (IGGAPIServiceInfos info : infos) {
			String authority = domain.getSecurity().getAuthorityForOperation(info.getOperation());
			this.accessRules.add(new BasicGGAPIAccessRule(info.getPath().replace("{uuid}", "*"), authority, info.getOperation(),
					domain.getSecurity().getAccessForOperation(info.getOperation())));

		}

		return accessRules;
	}

	@Override
	public List<String> getAuthorities() {
		List<String> list = this.domains.parallelStream()
				.flatMap(domain -> domain.getEntity().getValue1().updateAuthorizations().values().stream())
				.collect(Collectors.toList());
		list.addAll(this.accessRules.parallelStream().map(rule -> {
			return rule.getAuthority();
		}).collect(Collectors.toList()));
		return list.parallelStream().filter(authority -> {
			return (authority != null && !authority.isEmpty());
		}).collect(Collectors.toList());
	}

}
