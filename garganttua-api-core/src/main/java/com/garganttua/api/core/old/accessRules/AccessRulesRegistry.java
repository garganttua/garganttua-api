package com.garganttua.api.core.accessRules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.garganttua.api.spec.EntityOperation;
import com.garganttua.api.spec.context.IAccessRulesRegistry;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.security.IAccessRule;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccessRulesRegistry implements IAccessRulesRegistry {

	@Getter
	private List<IAccessRule> accessRules = new ArrayList<IAccessRule>();

	private Set<IDomain> domains;

	public AccessRulesRegistry(Set<IDomain> domains) {
		this.domains = domains;
		this.init();
	}

	private void init() {
		log.info("Creating Access Rules ...");
		for (IDomain domain : this.domains) {
			this.accessRules.addAll(domain.getAccessRules());
		}
		this.accessRules.forEach(ar -> {
			log.info("	Access Rule added {}", ar);
		});
	}

	@Override
	public void addAccessRule(IAccessRule accessRule) {
		this.accessRules.add(accessRule);
		log.info("	Access Rule added {}", accessRule);
	}
	
	
	@Override
	public IAccessRule getAccessRule(EntityOperation operation, String endpoint) {
		for (IAccessRule auth : this.accessRules) {
			if (auth.getEndpoint().equals(endpoint) && auth.getOperation() == operation) {
				return auth;
			}
		}
		return null;
	}
	
	@Override
	public String getAuthority(EntityOperation operation) {
		Optional<IAccessRule> accessRule = this.accessRules.stream().filter(ar -> {
			return ar.getOperation().equals(operation);
		}).findFirst();
		if( accessRule.isPresent() ) {
			return accessRule.get().getAuthority();
		}
		return null;
	}

	@Override
	public List<String> getAuthorities() {
		List<String> list = this.domains.stream()
				.flatMap(domain -> domain.getUpdateAuthorizations().stream())
				.collect(Collectors.toList());
		list.addAll(this.accessRules.stream().map(rule -> {
			return rule.getAuthority();
		}).collect(Collectors.toList()));
		List<String> presque_liste_finale = list.stream().filter(authority -> {
			return (authority != null && !authority.isEmpty());
		}).collect(Collectors.toList());
		
		//remove doobloons
		Set<String> uniqueSet = new HashSet<>(presque_liste_finale);
		return new ArrayList<>(uniqueSet);
	}
}
