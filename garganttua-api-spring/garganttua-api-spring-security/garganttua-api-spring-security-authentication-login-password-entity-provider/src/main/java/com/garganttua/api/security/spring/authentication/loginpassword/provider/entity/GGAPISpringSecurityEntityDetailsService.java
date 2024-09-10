package com.garganttua.api.security.spring.authentication.loginpassword.provider.entity;

import java.util.HashMap;
import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.garganttua.api.core.caller.GGAPICaller;
import com.garganttua.api.spec.filter.GGAPILiteral;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.security.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.service.GGAPIReadOutputMode;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;
import com.garganttua.reflection.GGObjectAddress;

public class GGAPISpringSecurityEntityDetailsService implements UserDetailsService {

	private IGGAPIService service;
	private GGAPIAuthenticatorInfos infos;

	public GGAPISpringSecurityEntityDetailsService(GGAPIAuthenticatorInfos infos, IGGAPIService service) {
		this.infos = infos;
		this.service = service;
	}

	@SuppressWarnings("unchecked")
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		GGObjectAddress loginFieldAddress = this.infos.loginFieldAddress();
		
		GGAPILiteral filter = GGAPILiteral.eq(loginFieldAddress.toString(), username);
		IGGAPIServiceResponse requestResponse = this.service.getEntities(GGAPICaller.createSuperCaller(), GGAPIReadOutputMode.full, null, filter, null, new HashMap<String, String>());
		
		if( requestResponse.getResponseCode() != GGAPIServiceResponseCode.OK ) {
			throw new RuntimeException((String) requestResponse.getResponse());
		} 
		
		List<Object> entities = (List<Object>) requestResponse.getResponse();
		if( entities.size() == 0 ) {
			throw new UsernameNotFoundException("Entity with login "+username+" not found");
		}
		
		Object entity = entities.get(0);

		return new GGAPIEntityDetails(entity);
	}

}
