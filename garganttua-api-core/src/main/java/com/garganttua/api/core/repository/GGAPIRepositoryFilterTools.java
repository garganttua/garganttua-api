package com.garganttua.api.core.repository;

import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.filter.IGGAPIFilter;

public class GGAPIRepositoryFilterTools {

	public static IGGAPIFilter getFilterFromCallerInfosAndDomainInfos(IGGAPICaller caller, IGGAPIDomain domain, GGAPILiteral filter) {
		String requestedTenantId = caller.getRequestedTenantId(); 
		String ownerId = caller.getOwnerId();
		boolean superOwner = caller.isSuperOwner();
		boolean superTenant = caller.isSuperTenant();
		boolean sharedEntity = domain.isSharedEntity();
		boolean hiddenableEntity = domain.isHiddenableEntity();
		boolean ownedEntity = domain.isOwnedEntity();
		boolean publicEntity = domain.isPublicEntity();
		
		GGAPILiteral and = GGAPILiteral.and();
		GGAPILiteral tenantIdFilter = requestedTenantId==null?null:GGAPILiteral.eq(domain.getTenantIdFieldAddress().toString(), requestedTenantId);
		GGAPILiteral shareFieldFilter = null;
		GGAPILiteral visibleFilter = null;
		GGAPILiteral ownerIdFilter = ownerId==null||ownedEntity==true?null:GGAPILiteral.eq(domain.getOwnerIdFieldAddress().toString(), ownerId);
		
		if( hiddenableEntity ) {
			visibleFilter = GGAPILiteral.eq(domain.getHiddenFieldAddress().toString(), false);
		}
		
		if( sharedEntity ) {
			shareFieldFilter = GGAPILiteral.eq(domain.getShareFieldAddress().toString(), requestedTenantId);
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
					GGAPILiteral and__ = visibleFilter.andOperator(shareFieldFilter);
					
					if( tenantIdFilter != null ) {
						GGAPILiteral or = and__.orOperator(tenantIdFilter);
						and.andOperator(or);
					} else {
						and.andOperator(and);
					}
				} else {
					if( tenantIdFilter != null )
						and.andOperator(tenantIdFilter);
				}
			} else if( domain.isPublicEntity() && hiddenableEntity ) {
				
				if( tenantIdFilter != null ) {
					and.andOperator(GGAPILiteral.or(tenantIdFilter, visibleFilter));
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
	
	public static GGAPILiteral getUuidFilter(String uuidFieldName, String uuid) {
		return GGAPILiteral.eq(uuidFieldName, uuid);
	}
	
	public static GGAPILiteral getIdFilter(String idFieldName, String id) {
		return GGAPILiteral.eq(idFieldName, id);
	}
	
}
