package com.garganttua.api.core.legacy.caller;

import java.util.Map;

import com.garganttua.api.core.legacy.engine.EngineException;
import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;
import com.garganttua.api.spec.EntityOperation;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.caller.ICallerFactory;
import com.garganttua.api.spec.context.IAccessRulesRegistry;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.factory.EntityIdentifier;
import com.garganttua.api.spec.factory.IFactory;
import com.garganttua.api.spec.security.IAccessRule;
import com.garganttua.api.spec.service.ServiceAccess;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.query.ObjectQueryFactory;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CallerFactory implements ICallerFactory {
		
	private IDomain domain;
	
	private IDomain tenantsDomain;
	
	private IFactory tenantsFactory;
	
	private Map<String, IDomain> ownerDomains;
	
	private Map<String, IFactory> ownerFactories;
	
	private IAccessRulesRegistry accessRulesRegistry;

	@Override
	public ICaller getCaller(EntityOperation operation, String endpoint, String tenantId, String ownerId, String requestedTenantId, String callerId) throws CoreException {
		if( operation == null ) {
			throw new EngineException(CoreExceptionCode.UNKNOWN_ERROR, "Method is null"); 
		}
		if( endpoint == null ) {
			throw new EngineException(CoreExceptionCode.UNKNOWN_ERROR, "Endpoint is null");
		}
		if( ownerId != null && !ownerId.isEmpty() && ownerId.split(":").length != 2 ) {
			throw new EngineException(CoreExceptionCode.BAD_REQUEST, "Invalid ownerId ["+ownerId+"] sould be of format DOMAIN:UUID");
		}
		
		IAccessRule accessRule = this.accessRulesRegistry.getAccessRule(operation, endpoint);
		
		if( accessRule == null ) {
			throw new EngineException(CoreExceptionCode.BAD_REQUEST, "No access rule found for Operation ["+operation+"] and EndPoint ["+endpoint+"]");
		}
		
		boolean anonymousTemp = accessRule.getAccess()==ServiceAccess.anonymous;
		String tenantIdTemp = tenantId;
		String requestedTenantIdTemp = requestedTenantId==null?tenantId:requestedTenantId;
		String callerIdTemp = null;
		String ownerIdTemp = ownerId;
		boolean superTenantTemp = false;
		boolean superOwnerTemp = false;
		
		//TenantId rules
		this.throwExceptionIfTenantIdIsMandatoryAndTenantIdNotProvided(accessRule, tenantId);
		if( tenantId != null ){
			this.sendExceptionIfAccessRuleIsTenantOrOwnerAndTenantIdIsNull(accessRule, tenantId);
			superTenantTemp = this.checkIfTenantExistsAndSetSuperTenantIfTenantIsSuperTenant(tenantId) || superTenantTemp;
			this.checkIfRequestedTenantExistsIfRequestedTenantIdHasBeenProvided(requestedTenantIdTemp);
			tenantIdTemp = this.setTenantIdToNullIfThisIsTenantCreationRequest(accessRule, tenantIdTemp);
			requestedTenantIdTemp = this.setRequestedTenantIdToNullIfThisIsTenantCreationRequest(accessRule, requestedTenantIdTemp);
		}

		
		//OwnerId rules
		this.throwExceptionIfOwnerIdIsMandatoryAndOwnerIdNotProvidedOrOwnersDomainIsNull(accessRule, ownerIdTemp);
		if( ownerIdTemp != null ){
			superOwnerTemp = this.checkIfOwnerExistsAndSetSuperOwnerIfOwnerIsSuperOwner(tenantId, ownerIdTemp) || superOwnerTemp;
		} 
				
		return new Caller(tenantIdTemp, requestedTenantIdTemp, callerIdTemp, ownerIdTemp, superTenantTemp, superOwnerTemp, accessRule, this.domain, anonymousTemp, null);
	}

	private boolean checkIfOwnerExistsAndSetSuperOwnerIfOwnerIsSuperOwner(String tenantId, String ownerId) throws CoreException {
		Object owner = this.ownerFactories.get(ownerId.split(":")[0]).getEntityFromRepository(Caller.createTenantCaller(tenantId), null, EntityIdentifier.UUID, ownerId.split(":")[1]);
		if( owner == null ) {
			throw new EngineException(CoreExceptionCode.BAD_REQUEST, "Owner not found ["+ownerId+"]");
		}
		
		try {
			return (boolean) ObjectQueryFactory.objectQuery(owner).getValue(this.ownerDomains.get(ownerId.split(":")[0]).getSuperOnwerIdFieldAddress());
		} catch (ReflectionException e) {
			throw new EngineException(e);
		}
	}

	private boolean throwExceptionIfOwnerIdIsMandatoryAndOwnerIdNotProvidedOrOwnersDomainIsNull(IAccessRule accessRule, String ownerId) throws EngineException {
		boolean ownerIdMandatory = this.domain.isOwnerIdMandatoryForOperation(accessRule.getOperation());
		if( ownerIdMandatory ){
			if( ownerId == null || ownerId.isEmpty() )
				throw new EngineException(CoreExceptionCode.UNKNOWN_ERROR, "OwnerId is null");
			if( ownerId.split(":").length != 2 )
				throw new EngineException(CoreExceptionCode.BAD_REQUEST, "Invalid ownerId ["+ownerId+"] should be of format DOMAIN:UUID");
			if( this.ownerDomains.get(ownerId.split(":")[0]) == null )
				throw new EngineException(CoreExceptionCode.UNKNOWN_ERROR, "Onwers Domain or Owners Factory is null");
		}
		return ownerIdMandatory; 
	}

	private boolean throwExceptionIfTenantIdIsMandatoryAndTenantIdNotProvided(IAccessRule accessRule, String tenantId) throws EngineException {
		boolean tenantIdMandatory = this.domain.isTenantIdMandatoryForOperation(accessRule.getOperation());
		if(  tenantIdMandatory && tenantId == null ){
			throw new EngineException(CoreExceptionCode.BAD_REQUEST, "TenantId is null");
		}
		return tenantIdMandatory;
	}

	private String setRequestedTenantIdToNullIfThisIsTenantCreationRequest(IAccessRule accessRule, String requestedTenantIdTemp) {
		return this.setTenantIdToNullIfThisIsTenantCreationRequest(accessRule, requestedTenantIdTemp);
	}

	private String setTenantIdToNullIfThisIsTenantCreationRequest(IAccessRule accessRule, String tenantIdTemp) {
		if( accessRule.getOperation().equals(EntityOperation.createOne(this.domain.getDomain(), this.domain.getEntityClass())) && this.domain.getDomain().equals(this.tenantsDomain.getDomain()) )
			return null;
		return tenantIdTemp;
	}

	private void checkIfRequestedTenantExistsIfRequestedTenantIdHasBeenProvided(String requestedTenantId) throws CoreException {
		Object tenant = this.tenantsFactory.getEntityFromRepository(Caller.createSuperCaller(), null,
				EntityIdentifier.UUID, requestedTenantId);
		if( tenant == null ) {
			throw new EngineException(CoreExceptionCode.BAD_REQUEST, "Tenant not found ["+requestedTenantId+"]");
		}
	}

	private boolean checkIfTenantExistsAndSetSuperTenantIfTenantIsSuperTenant(String tenantId) throws CoreException {
		Object tenant = this.tenantsFactory.getEntityFromRepository(Caller.createSuperCaller(), null,
				EntityIdentifier.UUID, tenantId);
		if( tenant == null ) {
			throw new EngineException(CoreExceptionCode.BAD_REQUEST, "Tenant not found ["+tenantId+"]");
		}
		
		try {
			return (boolean) ObjectQueryFactory.objectQuery(tenant).getValue(this.tenantsDomain.getSuperTenantFieldAddress());
		} catch (ReflectionException e) {
			throw new EngineException(e);
		}
	}

	private void sendExceptionIfAccessRuleIsTenantOrOwnerAndTenantIdIsNull(IAccessRule accessRule, String tenantId) throws EngineException {
		if( tenantId == null && (accessRule.getAccess() == ServiceAccess.tenant || accessRule.getAccess() == ServiceAccess.owner) ) {
			throw new EngineException(CoreExceptionCode.BAD_REQUEST, "TenantId is null");
		}
	}
}
