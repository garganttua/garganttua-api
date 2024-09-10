package com.garganttua.api.security.core.engine;

import java.util.Optional;
import java.util.Set;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.IGGAPIAuthenticationManager;
import com.garganttua.api.spec.security.IGGAPIAuthorizationManager;
import com.garganttua.api.spec.security.IGGAPIOwnerVerifier;
import com.garganttua.api.spec.security.IGGAPISecurityBuilder;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;
import com.garganttua.api.spec.security.IGGAPITenantVerifier;

public class GGAPISecurityBuilder implements IGGAPISecurityBuilder {

	private IGGAPIAuthenticationManager authenticationManager = null;
	private IGGAPIAuthorizationManager authorizationManager = null;
	private IGGAPITenantVerifier tenantVerifier = null;
	private IGGAPIOwnerVerifier ownerVerifier = null;
	private Set<IGGAPIDomain> domains;

	@Override
	public IGGAPISecurityBuilder authenticationManager(IGGAPIAuthenticationManager manager) {
		this.authenticationManager = manager;
		return this;
	}

	@Override
	public IGGAPISecurityBuilder authorizationManager(IGGAPIAuthorizationManager manager) {
		this.authorizationManager = manager;
		return this;
	}

	@Override
	public IGGAPISecurityBuilder tenantVerifier(IGGAPITenantVerifier verifier) {
		this.tenantVerifier = verifier;
		return null;
	}

	@Override
	public IGGAPISecurityBuilder ownerVerifier(IGGAPIOwnerVerifier verifier) {
		this.ownerVerifier = verifier;
		return this;
	}

	@Override
	public IGGAPISecurityBuilder domains(Set<IGGAPIDomain> domains) {
		this.domains = domains;
		return this;
	}

	@Override
	public IGGAPISecurityEngine build() {
		return new GGAPISecurityEngine(
				this.domains,
				Optional.ofNullable(this.authorizationManager), 
				Optional.ofNullable(this.authenticationManager), 
				Optional.ofNullable(this.tenantVerifier), 
				Optional.ofNullable(this.ownerVerifier)
		);
	}

}
