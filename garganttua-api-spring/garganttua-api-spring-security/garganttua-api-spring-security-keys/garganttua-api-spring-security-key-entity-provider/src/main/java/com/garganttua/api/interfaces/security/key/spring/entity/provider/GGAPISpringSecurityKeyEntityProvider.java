package com.garganttua.api.interfaces.security.key.spring.entity.provider;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.garganttua.api.interfaces.security.key.spring.rest.GGAPIKeyRealmSpringEntity;
import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.security.keys.domain.GGAPIKeyRealmEntity;
import com.garganttua.api.security.spring.core.keys.GGAPISpringSecurityKeyEntityRequest;
import com.garganttua.api.security.spring.core.keys.IGGAPISpringKeyProvider;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
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
	
	@Override
	public IGGAPIKeyRealm getRealm(IGGAPICaller caller, String realmUuid) throws GGAPIException {
		IGGAPIServiceResponse response = this.keyRealmService.getEntity(caller, realmUuid, new HashMap<String, String>());
		if( response.getResponseCode() == GGAPIServiceResponseCode.NOT_FOUND ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_NOT_FOUND, "Key realm with uuid "+realmUuid+" not found");			 
		} else {
			IGGAPIKeyRealm realm = (IGGAPIKeyRealm) response.getResponse();
			
			//this line should throw exception if realm revoked or expired
			realm.getKeyForCiphering();
			
			return realm;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IGGAPIKeyRealm getRealm(IGGAPICaller caller, GGAPISpringSecurityKeyEntityRequest request) throws GGAPIException {
		request.validate();
		IGGAPIFilter filter = this.buildFilter(request, caller.getOwnerId());
		IGGAPIServiceResponse response = this.keyRealmService.getEntities(caller, GGAPIReadOutputMode.full, null, filter, null, new HashMap<String, String>());
		
		if( response.getResponseCode() == GGAPIServiceResponseCode.OK ) {
			List<IGGAPIKeyRealm> list = (List<IGGAPIKeyRealm>) response.getResponse();
			if( list.size() > 0) {
				return list.get(0);
			} else {
				if( this.autoCreate ) {
					return this.createRealm(caller, request);
				} else {
					throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_NOT_FOUND, "Key realm "+request.keyRealmName()+" not found for tenant "+caller.getRequestedTenantId()+" and owner "+caller.getOwnerId());
				}
			}
		} else {
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Unknown error during Key realm "+request.keyRealmName()+" for tenant "+caller.getRequestedTenantId()+" and owner "+caller.getOwnerId()+" retrival");
		}
	}

	private IGGAPIFilter buildFilter(GGAPISpringSecurityKeyEntityRequest request, String ownerId) {
		IGGAPIFilter filter = null;
		GGObjectAddress idFieldAddress = this.domain.getEntity().getValue1().idFieldAddress();
		GGObjectAddress expirationFieldAddress = GGAPIKeyRealmEntity.getExpirationFieldAddress();
		GGObjectAddress revokedFieldAddress = GGAPIKeyRealmEntity.getRevokedFieldAddress();
		GGObjectAddress algorithmFieldAddress = GGAPIKeyRealmEntity.getAlgorithmFieldAddress();
		IGGAPIFilter idFilter = GGAPILiteral.eq(idFieldAddress.toString(), request.keyRealmName());
		IGGAPIFilter expirationFilter = GGAPILiteral.gt(expirationFieldAddress.toString(), new Date());
		IGGAPIFilter revokedFilter = GGAPILiteral.eq(revokedFieldAddress.toString(), false);
		IGGAPIFilter algorithmFilter = GGAPILiteral.eq(algorithmFieldAddress.toString(), request.algorithm());
		
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
	public IGGAPIKeyRealm createRealm(IGGAPICaller caller, GGAPISpringSecurityKeyEntityRequest request) throws GGAPIException {
		request.validate();
		Date keyExpirationDate = new Date(System.currentTimeMillis() + this.keyLifetimeUnit.toMillis(this.keyLifetime));
		GGAPIKeyRealmSpringEntity entity = new GGAPIKeyRealmSpringEntity(request.keyRealmName(), request.algorithm(), keyExpirationDate);
		IGGAPIServiceResponse response = this.keyRealmService.createEntity(caller, entity, new HashMap<String, String>());

		if( response.getResponseCode() != GGAPIServiceResponseCode.CREATED ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.UNKNOWN_ERROR, response.getResponse().toString());
		}

		return entity;
	}

	@Override
	public IGGAPIKeyRealm revokeRealm(IGGAPICaller caller, GGAPISpringSecurityKeyEntityRequest request) throws GGAPISecurityException {
		request.validate();
		return null;
	}
	
	@Override
	public IGGAPIKeyRealm revokeAllRealms(IGGAPICaller caller, GGAPISpringSecurityKeyEntityRequest request) throws GGAPISecurityException {
		request.validate();
		return null;
	}

}
