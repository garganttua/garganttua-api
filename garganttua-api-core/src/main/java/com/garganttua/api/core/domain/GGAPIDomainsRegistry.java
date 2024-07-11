package com.garganttua.api.core.domain;

import java.util.Set;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.domain.IGGAPIDomainsRegistry;
import com.garganttua.api.spec.engine.IGGAPIEngine;

import lombok.Getter;
import lombok.Setter;

public class GGAPIDomainsRegistry implements IGGAPIDomainsRegistry {

	@Getter
	private Set<IGGAPIDomain> domains;
	
	@Setter
	private IGGAPIEngine engine;

	public GGAPIDomainsRegistry(Set<IGGAPIDomain> domains) {
		this.domains = domains;
	}

	@Override
	public IGGAPIDomain getDomain(String domain) {
		for( IGGAPIDomain ddomain: this.domains ) {
			if( ddomain.getEntity().getValue1().domain().toLowerCase().equals(domain)) {
				return ddomain;
			}
		}
		return null;
	}

	@Override
	public IGGAPIDomain getOwnerDomain() {
		for( IGGAPIDomain ddomain: this.domains ) {
			if( ddomain.getEntity().getValue1().ownerEntity() == true) {
				return ddomain;
			}
		}
		return null;
	}

	@Override
	public IGGAPIDomain getTenantDomain() {
		for( IGGAPIDomain ddomain: this.domains ) {
			if( ddomain.getEntity().getValue1().tenantEntity() == true) {
				return ddomain;
			}
		}
		return null;
	}

	@Override
	public void setDomain(IGGAPIDomain domain) {
	}

}
