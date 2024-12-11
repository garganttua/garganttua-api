package com.garganttua.api.core.security.authentication.pin;

import javax.inject.Inject;

import com.garganttua.api.core.security.authentication.AbstractGGAPIAuthentication;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.IGGAPIPasswordEncoder;
import com.garganttua.api.spec.security.annotations.GGAPIAuthentication;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@GGAPIAuthentication (
		findPrincipal = true
	)
@Slf4j
public class GGAPIPinAuthentication extends AbstractGGAPIAuthentication {
	
	public GGAPIPinAuthentication(IGGAPIDomain domain) {
		super(domain);
	}
	
	public GGAPIPinAuthentication() {
		super(null);
	}

	@Inject 
	private IGGAPIPasswordEncoder encoder;
	
	@Override
	protected Object doFindPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doAuthentication() throws GGAPIException {
		// TODO Auto-generated method stub
		
	}

}
