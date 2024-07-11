package com.garganttua.api.core.repository;

import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.filter.GGAPILiteral;
import com.garganttua.api.spec.filter.IGGAPIFilter;

public class GGAPIRepositoryFilterTools {

	public static IGGAPIFilter getFilterFromCallerInfosAndDomainInfos(IGGAPICaller caller, IGGAPIDomain domain, IGGAPIFilter filter) {
		String requestedTenantId = caller.getRequestedTenantId(); 
		String ownerId = caller.getOwnerId();
		boolean superTenant = caller.isSuperTenant();
		String shared = domain.getEntity().getValue1().shareFieldAddress()==null?null:domain.getEntity().getValue1().shareFieldAddress().toString();
		
		GGAPILiteral and = GGAPILiteral.and();
		GGAPILiteral tenantIdFilter = requestedTenantId==null?null:GGAPILiteral.eq(domain.getEntity().getValue1().tenantIdFieldAddress().toString(), requestedTenantId);
		GGAPILiteral shareFieldFilter = GGAPILiteral.eq(shared, requestedTenantId);
		GGAPILiteral visibleFilter = GGAPILiteral.eq(domain.getEntity().getValue1().hiddenFieldAddress()==null?null:domain.getEntity().getValue1().hiddenFieldAddress().toString(), true);
		GGAPILiteral ownerIdFilter = ownerId==null?null:GGAPILiteral.eq(domain.getEntity().getValue1().ownerIdFieldAddress().toString(), ownerId);
		
		if( filter != null ) {
			and.andOperator(filter);
		}

		if( superTenant && (requestedTenantId == null || requestedTenantId.isEmpty()) ){
			
		} else {
			if( !domain.getEntity().getValue1().publicEntity() && !domain.getEntity().getValue1().hiddenableEntity() ) {
				
				if( shared != null && !shared.isEmpty() ) {
					if( tenantIdFilter != null ) {
						and.andOperator(shareFieldFilter.orOperator(tenantIdFilter));
					} else {
						and.andOperator(shareFieldFilter);
					}
				} else {
					if( tenantIdFilter != null )
						and.andOperator(tenantIdFilter);
				}
			} else if( !domain.getEntity().getValue1().publicEntity() && domain.getEntity().getValue1().hiddenableEntity() ) {
				if( shared != null && !shared.isEmpty() ) {
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
			} else if( domain.getEntity().getValue1().publicEntity() && domain.getEntity().getValue1().hiddenableEntity() ) {
				and.andOperator(visibleFilter);
				if( tenantIdFilter != null ) {
					and.andOperator(tenantIdFilter);
				}
			} 
			
			if( ownerIdFilter != null && domain.getEntity().getValue1().ownedEntity()) {
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