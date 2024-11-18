package com.garganttua.api.security.spring.authentication.loginpassword.provider.entity;

import java.util.HashMap;
import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.garganttua.api.core.caller.GGAPICaller;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.security.spring.authentication.loginpassword.GGAPILoginPasswordEntityAuthenticatorChecker;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.service.GGAPIReadOutputMode;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;
import com.garganttua.reflection.GGObjectAddress;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPISpringSecurityEntityDetailsService implements UserDetailsService {

	private List<IGGAPIDomain> authenticatorDomains;
	private IGGAPIEngine engine;

	public GGAPISpringSecurityEntityDetailsService(List<IGGAPIDomain> authenticatorDomains, IGGAPIEngine engine) {
		this.authenticatorDomains = authenticatorDomains;
		this.engine = engine;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserDetails details = null;
		IGGAPIService service;
		String[] chunk = username.split(":");
		if( chunk.length != 2 ) {
			throw new UsernameNotFoundException("Invalid username ["+username+"] should of format TENANTID:ID");
		}
		
		for( IGGAPIDomain domain: this.authenticatorDomains ) {
			log.atDebug().log("Triing domain "+domain.getDomain());
			GGObjectAddress loginFieldAddress;
			Class<?> entityClass = domain.getEntity().getValue0();
			try {
				loginFieldAddress = GGAPILoginPasswordEntityAuthenticatorChecker.checkEntityAuthenticator(entityClass).loginFieldAddress();
			} catch (GGAPIException e) {
				continue;
			}
			service = this.engine.getServicesRegistry().getService(domain.getDomain());
			details = this.loadUserByUsernameInDomain(chunk[0], chunk[1], service, entityClass, loginFieldAddress, domain.getDomain());
			if( details != null ) {
				break;
			}
		}
		
		if( details == null ) {
			throw new UsernameNotFoundException("Entity with login "+username+" not found");
		}
		
		return details;
	}

	
	@SuppressWarnings("unchecked")
	private UserDetails loadUserByUsernameInDomain(String tenantId, String username, IGGAPIService service, Class<?> entityClass, GGObjectAddress loginFieldAddress, String domainName) {
		GGAPILiteral filter = GGAPILiteral.eq(loginFieldAddress.toString(), username);
		IGGAPIServiceResponse requestResponse;
		try {
			requestResponse = service.getEntities(GGAPICaller.createTenantCaller(tenantId), GGAPIReadOutputMode.full, null, filter, null, new HashMap<String, String>());
			if( requestResponse.getResponseCode() != GGAPIServiceResponseCode.OK ) {
				log.atWarn().log("Entity with login "+username+" not found in domain "+domainName+": "+(String) requestResponse.getResponse());
				return null;
			} 
			
			List<Object> entities = (List<Object>) requestResponse.getResponse();
			if( entities.size() == 0 ) {
				log.atDebug().log("Entity with login "+username+" not found in domain "+domainName);
				return null;
			}
			
			Object entity = entities.get(0);
			
			return new GGAPIEntityDetails(entity);
		} catch (GGAPIEngineException e) {
			return null;
		}
	}
}
