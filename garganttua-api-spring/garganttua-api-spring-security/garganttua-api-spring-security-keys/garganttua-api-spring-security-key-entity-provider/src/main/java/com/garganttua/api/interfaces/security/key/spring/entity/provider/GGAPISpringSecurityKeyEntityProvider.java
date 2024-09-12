package com.garganttua.api.interfaces.security.key.spring.entity.provider;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.garganttua.api.core.caller.GGAPICaller;
import com.garganttua.api.interfaces.security.key.spring.rest.GGAPIKeyRealmSpringEntity;
import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.security.keys.domain.GGAPIKeyRealmEntity;
import com.garganttua.api.security.spring.core.keys.IGGAPISpringKeyProvider;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.filter.GGAPILiteral;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.security.IGGAPIAuthenticator;
import com.garganttua.api.spec.security.IGGAPIKeyRealm;
import com.garganttua.api.spec.service.GGAPIReadOutputMode;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;

import jakarta.annotation.PostConstruct;

@Service
public class GGAPISpringSecurityKeyEntityProvider implements IGGAPISpringKeyProvider {

	@Autowired
	private IGGAPIEngine engine;
	private IGGAPIService keyRealmService;
	
	@PostConstruct
	private void init() {
		this.keyRealmService = this.engine.getServicesRegistry().getService(GGAPIKeyRealmEntity.domain);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IGGAPIKeyRealm getRealm(IGGAPIAuthenticator authenticator, String keyRealmName) throws GGAPISecurityException {
	
		IGGAPIFilter filter = GGAPILiteral.eq("id", keyRealmName);
		IGGAPIServiceResponse response = this.keyRealmService.getEntities(GGAPICaller.createTenantCaller(authenticator.getTenantId()), GGAPIReadOutputMode.full, null, filter, null, new HashMap<String, String>());
		
		if( response.getResponseCode() == GGAPIServiceResponseCode.OK ) {
			List<IGGAPIKeyRealm> list = (List<IGGAPIKeyRealm>) response.getResponse();
			if( list.size() > 0)
				return list.get(0);
			else 
				throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_NOT_FOUND, "Key realm "+keyRealmName+" not found for user "+authenticator.getUuid());
		} else {
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Unknown error during Key realm "+keyRealmName+" not for user "+authenticator.getUuid()+" retrival");
		}
	}

	@Override
	public IGGAPIKeyRealm createRealm(IGGAPIAuthenticator authenticator, String keyRealmName, String algorithm, Date expiration) throws GGAPISecurityException {
		GGAPIKeyRealmSpringEntity entity = new GGAPIKeyRealmSpringEntity(keyRealmName, algorithm, expiration);
		IGGAPIServiceResponse response = this.keyRealmService.createEntity(GGAPICaller.createTenantCaller(authenticator.getTenantId()), entity, new HashMap<String, String>());

		return entity;
	
	}

}
