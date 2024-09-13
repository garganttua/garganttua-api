package com.garganttua.api.interfaces.security.key.spring.entity.provider;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.garganttua.api.core.caller.GGAPICaller;
import com.garganttua.api.interfaces.security.key.spring.rest.GGAPIKeyRealmSpringEntity;
import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.security.keys.domain.GGAPIKeyRealmEntity;
import com.garganttua.api.security.spring.core.keys.IGGAPISpringKeyProvider;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.filter.GGAPILiteral;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.security.IGGAPIKeyRealm;
import com.garganttua.api.spec.service.GGAPIReadOutputMode;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;
import com.garganttua.reflection.GGObjectAddress;

import jakarta.annotation.PostConstruct;

@Service
public class GGAPISpringSecurityKeyEntityProvider implements IGGAPISpringKeyProvider {

	@Value("${com.garganttua.api.security.keys.provider.lifetime}")
	private long keyLifetime;

	@Value("${com.garganttua.api.security.keys.provider.lifetime.unit}")
	private TimeUnit keyLifetimeUnit;
	
	@Value("${com.garganttua.api.security.keys.provider.autoCreate}")
	private boolean autoCreate = true;

	@Autowired
	private IGGAPIEngine engine;
	private IGGAPIService keyRealmService;
	private IGGAPIDomain domain;

	@PostConstruct
	private void init() {
		this.keyRealmService = this.engine.getServicesRegistry().getService(GGAPIKeyRealmEntity.domain);
		this.domain = this.engine.getDomainsRegistry().getDomain(GGAPIKeyRealmEntity.domain);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IGGAPIKeyRealm getRealm(String tenantId, String ownerId, String keyRealmName, String algorithm) throws GGAPISecurityException {
		
		IGGAPIFilter filter = this.buildFilter(tenantId, ownerId, keyRealmName, algorithm);
		IGGAPIServiceResponse response = this.keyRealmService.getEntities(GGAPICaller.createTenantCaller(tenantId), GGAPIReadOutputMode.full, null, filter, null, new HashMap<String, String>());
		
		if( response.getResponseCode() == GGAPIServiceResponseCode.OK ) {
			List<IGGAPIKeyRealm> list = (List<IGGAPIKeyRealm>) response.getResponse();
			if( list.size() > 0) {
				return list.get(0);
			} else {
				if( this.autoCreate ) {
					return this.createRealm(tenantId, ownerId, keyRealmName, algorithm);
				} else {
					throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_NOT_FOUND, "Key realm "+keyRealmName+" not found for tenant "+tenantId+" and owner "+ownerId);
				}
			}
		} else {
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Unknown error during Key realm "+keyRealmName+" not for tenant "+tenantId+" and owner "+ownerId+" retrival");
		}
	}

	private IGGAPIFilter buildFilter(String tenantId, String ownerId, String keyRealmName, String algorithm) {
		IGGAPIFilter filter = null;
		GGObjectAddress idFieldAddress = this.domain.getEntity().getValue1().idFieldAddress();
		GGObjectAddress expirationFieldAddress = GGAPIKeyRealmEntity.getExpirationFieldAddress();
		GGObjectAddress revokedFieldAddress = GGAPIKeyRealmEntity.getRevokedFieldAddress();
		GGObjectAddress algorithmFieldAddress = GGAPIKeyRealmEntity.getAlgorithmFieldAddress();
		IGGAPIFilter idFilter = GGAPILiteral.eq(idFieldAddress.toString(), keyRealmName);
		IGGAPIFilter expirationFilter = GGAPILiteral.gt(expirationFieldAddress.toString(), new Date());
		IGGAPIFilter revokedFilter = GGAPILiteral.eq(revokedFieldAddress.toString(), false);
		IGGAPIFilter algorithmFilter = GGAPILiteral.eq(algorithmFieldAddress.toString(), algorithm);
		
		if( this.domain.getEntity().getValue1().ownedEntity() ) {
			GGObjectAddress ownerIdFieldAddress = GGAPIKeyRealmEntity.getOwnerIdFieldAddress();
			GGAPILiteral ownerIdFilter = GGAPILiteral.eq(ownerIdFieldAddress.toString(), ownerId);
			filter = GGAPILiteral.and(idFilter, expirationFilter, revokedFilter, ownerIdFilter, algorithmFilter);
		} else {
			filter = GGAPILiteral.and(idFilter, expirationFilter, revokedFilter, algorithmFilter);
		}
		return filter;
	}

	@Override
	public IGGAPIKeyRealm createRealm(String tenantId, String ownerId, String keyRealmName, String algorithm) throws GGAPISecurityException {
		
		Date keyExpirationDate = new Date(System.currentTimeMillis() + this.keyLifetimeUnit.toMillis(this.keyLifetime));
		GGAPIKeyRealmSpringEntity entity = new GGAPIKeyRealmSpringEntity(keyRealmName, algorithm, keyExpirationDate);
		IGGAPIServiceResponse response = this.keyRealmService.createEntity(GGAPICaller.createTenantCaller(tenantId), entity, new HashMap<String, String>());

		if( response.getResponseCode() != GGAPIServiceResponseCode.CREATED ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.UNKNOWN_ERROR, response.getResponse().toString());
		}

		return entity;
	}

	@Override
	public IGGAPIKeyRealm revokeRealm(String tenantId, String ownerId, String keyRealmName, String algorithm) throws GGAPISecurityException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public IGGAPIKeyRealm revokeAllRealms(String tenantId, String ownerId, String keyRealmName) throws GGAPISecurityException {
		// TODO Auto-generated method stub
		return null;
	}

}
