package com.garganttua.api.core.repository;

import com.garganttua.api.core.filter.Literal;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.filter.IFilter;

public class RepositoryFilterTools {

	public static IFilter getFilterFromCallerInfosAndDomainInfos(ICaller caller, IDomain domain, Literal filter) {
		String requestedTenantId = caller.getRequestedTenantId(); 
		String ownerId = caller.getOwnerId();
		boolean superOwner = caller.isSuperOwner();
		boolean superTenant = caller.isSuperTenant();
		boolean sharedEntity = domain.isSharedEntity();
		boolean hiddenableEntity = domain.isHiddenableEntity();
		boolean ownedEntity = domain.isOwnedEntity();
		boolean publicEntity = domain.isPublicEntity();
		
		Literal and = Literal.and();
		Literal tenantIdFilter = requestedTenantId==null?null:Literal.eq(domain.getTenantIdFieldAddress().toString(), requestedTenantId);
		Literal shareFieldFilter = null;
		Literal visibleFilter = null;
		Literal ownerIdFilter = ownedEntity==true&&ownerId!=null?Literal.eq(domain.getOwnerIdFieldAddress().toString(), ownerId):null;
		
		if( hiddenableEntity && !superOwner ) {
			visibleFilter = Literal.eq(domain.getHiddenFieldAddress().toString(), false);
		}
		
		if( sharedEntity ) {
			shareFieldFilter = Literal.eq(domain.getShareFieldAddress().toString(), requestedTenantId);
		}
		
		if( filter != null ) {
			and.andOperator(filter);
		}

		if( superTenant && (requestedTenantId == null || requestedTenantId.isEmpty()) ){
			
		} else {
			if( !publicEntity && !hiddenableEntity ) {
				
				if( sharedEntity ) {
					if( tenantIdFilter != null ) {
						and.andOperator(shareFieldFilter.orOperator(tenantIdFilter));
					} else {
						and.andOperator(shareFieldFilter);
					}
				} else {
					if( tenantIdFilter != null )
						and.andOperator(tenantIdFilter);
				}
			} else if( !domain.isPublicEntity() && hiddenableEntity ) {
				if( sharedEntity ) {
					Literal and__ = visibleFilter.andOperator(shareFieldFilter);
					
					if( tenantIdFilter != null ) {
						Literal or = and__.orOperator(tenantIdFilter);
						and.andOperator(or);
					} else {
						and.andOperator(and);
					}
				} else {
					if( tenantIdFilter != null )
						and.andOperator(tenantIdFilter);
				}
			} else if( domain.isPublicEntity() && hiddenableEntity ) {
				
				if( tenantIdFilter != null && visibleFilter != null ) {
					and.andOperator(Literal.or(tenantIdFilter, visibleFilter));
				} else {
				  and.andOperator(visibleFilter);
				}
			} 
			
			if( ownerIdFilter != null && ownedEntity && !superOwner ) {
				and.andOperator(ownerIdFilter);
			}
		}
		if( and.getLiterals().size() == 1 ) {
			return and.getLiterals().get(0);
		} else if( and.getLiterals().size() > 1) {
			return and;
		} else {
			return null;
		}
	}
	
	public static Literal getUuidFilter(String uuidFieldName, String uuid) {
		return Literal.eq(uuidFieldName, uuid);
	}
	
	public static Literal getIdFilter(String idFieldName, String id) {
		return Literal.eq(idFieldName, id);
	}
	
}
