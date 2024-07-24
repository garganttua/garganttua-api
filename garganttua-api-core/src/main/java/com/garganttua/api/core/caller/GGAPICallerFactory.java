package com.garganttua.api.core.caller;

import java.util.Optional;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.caller.IGGAPICallerFactory;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIAccessRulesRegistry;
import com.garganttua.api.spec.factory.GGAPIEntityIdentifier;
import com.garganttua.api.spec.factory.IGGAPIEntityFactory;
import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.service.GGAPIServiceAccess;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GGAPICallerFactory implements IGGAPICallerFactory {
		
	private IGGAPIDomain domain;
	
	private String superTenantId;
	
	private String superOwnerId;
	
	private IGGAPIDomain tenantsDomain;
	
	private IGGAPIEntityFactory<?> tenantsFactory;
	
	private Optional<IGGAPIDomain> ownersDomain;
	
	private Optional<IGGAPIEntityFactory<?>> ownersFactory;
	
	private IGGAPIAccessRulesRegistry accessRulesRegistry;

	@Override
	public IGGAPICaller getCaller(GGAPIEntityOperation operation, String endpoint, String tenantId, String ownerId, String requestedTenantId, String callerId) throws GGAPIException {
		if( operation == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.UNKNOWN_ERROR, "Method is null");
		}
		if( endpoint == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.UNKNOWN_ERROR, "Endpoint is null");
		}

		IGGAPIAccessRule accessRule = this.accessRulesRegistry.getAccessRule(operation, endpoint);
		
		if( accessRule == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.BAD_REQUEST, "No access rule found for Operation ["+operation+"] and EndPoint ["+endpoint+"]");
		}
		
		boolean anonymousTemp = accessRule.getAccess()==GGAPIServiceAccess.anonymous;
		String tenantIdTemp = tenantId;
		String requestedTenantIdTemp = requestedTenantId==null?tenantId:requestedTenantId;
		String callerIdTemp = null;
		String ownerIdTemp = ownerId;
		boolean superTenantTemp = false;
		boolean superOwnerTemp = false;
		
		
		//TenantId rules
		if( this.throwExceptionIfTenantIdIsMandatoryAndTenantIdNotProvided(accessRule, tenantId) ) {
	//		this.sendExceptionIfAccessRuleIsTenantOrOwnerAndTenantIdIsNull(accessRule, tenantId);
			superTenantTemp = this.setSuperTenantIfTenantIdEqualsToSuperTenantId(tenantId);
			superTenantTemp = this.checkIfTenantExistsAndSetSuperTenantIfTenantIsSuperTenant(tenantId) || superTenantTemp;
			this.checkIfRequestedTenantExistsIfRequestedTenantIdHasBeenProvided(requestedTenantIdTemp);
			tenantIdTemp = this.setTenantIdToNullIfThisIsTenantCreationRequest(accessRule, tenantIdTemp);
			requestedTenantIdTemp = this.setRequestedTenantIdToNullIfThisIsTenantCreationRequest(accessRule, requestedTenantIdTemp);
		} else {
			tenantIdTemp = null;
			requestedTenantIdTemp = null;
		}
		
		//OwnerId rules
		if( this.throwExceptionIfOwnerIdIsMandatoryAndOwnerIdNotProvidedOrOwnersDomainIsNull(accessRule, ownerIdTemp) ) {
			superOwnerTemp = this.setSuperOwnerIfOwnerIdEqualsToSuperOwnerId(ownerIdTemp);
			superOwnerTemp = this.checkIfOwnerExistsAndSetSuperOwnerIfOwnerIsSuperOwner(ownerIdTemp) || superOwnerTemp;
		} else {
			ownerIdTemp = null;
		}
				
		return new GGAPICaller(tenantIdTemp, requestedTenantIdTemp, callerIdTemp, ownerIdTemp, superTenantTemp, superOwnerTemp, accessRule, domain, anonymousTemp, null);
	}

	private boolean checkIfOwnerExistsAndSetSuperOwnerIfOwnerIsSuperOwner(String ownerId) throws GGAPIException {
		Object owner = this.ownersFactory.get().getEntityFromRepository(GGAPICaller.createSuperCaller(), null, GGAPIEntityIdentifier.UUID, ownerId);
		if( owner == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.BAD_REQUEST, "Owner not found ["+ownerId+"]");
		}
		
		try {
			return (boolean) GGObjectQueryFactory.objectQuery(owner).getValue(this.ownersDomain.get().getEntity().getValue1().superOnwerIdFieldAddress());
		} catch (GGReflectionException e) {
			throw new GGAPIEngineException(e);
		}
	}

	private boolean setSuperOwnerIfOwnerIdEqualsToSuperOwnerId(String ownerId) {
		return ownerId.equals(this.superOwnerId);
	}

	private boolean throwExceptionIfOwnerIdIsMandatoryAndOwnerIdNotProvidedOrOwnersDomainIsNull(IGGAPIAccessRule accessRule, String ownerId) throws GGAPIEngineException {
		boolean ownerIdMandatory = this.domain.isOwnerIdMandatoryForOperation(accessRule.getOperation());
		if( ownerIdMandatory ){
			if( ownerId == null || ownerId.isEmpty() )
				throw new GGAPIEngineException(GGAPIExceptionCode.UNKNOWN_ERROR, "OwnerId is null");
			if( this.ownersDomain.isEmpty() || this.ownersFactory.isEmpty() )
				throw new GGAPIEngineException(GGAPIExceptionCode.UNKNOWN_ERROR, "Onwers Domain or Owners Factory is null");
		}
		return ownerIdMandatory; 
	}

	private boolean throwExceptionIfTenantIdIsMandatoryAndTenantIdNotProvided(IGGAPIAccessRule accessRule, String tenantId) throws GGAPIEngineException {
		boolean tenantIdMandatory = this.domain.isTenantIdMandatoryForOperation(accessRule.getOperation());
		if(  tenantIdMandatory && tenantId == null ){
			throw new GGAPIEngineException(GGAPIExceptionCode.BAD_REQUEST, "TenantId is null");
		}
		return tenantIdMandatory;
	}

	private String setRequestedTenantIdToNullIfThisIsTenantCreationRequest(IGGAPIAccessRule accessRule, String requestedTenantIdTemp) {
		return this.setTenantIdToNullIfThisIsTenantCreationRequest(accessRule, requestedTenantIdTemp);
	}

	private String setTenantIdToNullIfThisIsTenantCreationRequest(IGGAPIAccessRule accessRule, String tenantIdTemp) {
		if( accessRule.getOperation() == GGAPIEntityOperation.create_one && this.domain.getDomain().equals(this.tenantsDomain.getDomain()) )
			return null;
		return tenantIdTemp;
	}

	private void checkIfRequestedTenantExistsIfRequestedTenantIdHasBeenProvided(String requestedTenantId) throws GGAPIException {
		Object tenant = this.tenantsFactory.getEntityFromRepository(GGAPICaller.createSuperCaller(), null,
				GGAPIEntityIdentifier.UUID, requestedTenantId);
		if( tenant == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.BAD_REQUEST, "Tenant not found ["+requestedTenantId+"]");
		}
	}

	private boolean checkIfTenantExistsAndSetSuperTenantIfTenantIsSuperTenant(String tenantId) throws GGAPIException {
		Object tenant = this.tenantsFactory.getEntityFromRepository(GGAPICaller.createSuperCaller(), null,
				GGAPIEntityIdentifier.UUID, tenantId);
		if( tenant == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.BAD_REQUEST, "Tenant not found ["+tenantId+"]");
		}
		
		try {
			return (boolean) GGObjectQueryFactory.objectQuery(tenant).getValue(this.tenantsDomain.getEntity().getValue1().superTenantFieldAddress());
		} catch (GGReflectionException e) {
			throw new GGAPIEngineException(e);
		}
	}

	private boolean setSuperTenantIfTenantIdEqualsToSuperTenantId(String tenantId) {
		return tenantId.equals(this.superTenantId);
	}

	private void sendExceptionIfAccessRuleIsTenantOrOwnerAndTenantIdIsNull(IGGAPIAccessRule accessRule, String tenantId) throws GGAPIEngineException {
		if( tenantId == null && (accessRule.getAccess() == GGAPIServiceAccess.tenant || accessRule.getAccess() == GGAPIServiceAccess.owner) ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.BAD_REQUEST, "TenantId is null");
		}
	}
}