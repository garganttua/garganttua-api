package com.garganttua.api.core.interfasse.filter;

import java.util.Optional;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.factory.GGAPIEntityIdentifier;
import com.garganttua.api.spec.factory.IGGAPIEntityFactory;
import com.garganttua.api.spec.interfasse.filter.IGGAPIOwnerFilter;
import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.service.GGAPIServiceAccess;
import com.garganttua.api.spec.service.GGAPIServiceMethod;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GGAPIOwnerFilter implements IGGAPIOwnerFilter {

	private Optional<IGGAPIDomain> ownersDomain;
	
	private IGGAPIEntityFactory<?> ownersFactory;
	
	private String superOwnerId = "0";

	@Override
	public void doOwnerIdFiltering(IGGAPICaller caller, String ownerId, String requestedtenantId) throws GGAPIException {
		if( caller == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.UNKNOWN_ERROR, "Caller is null");
		}
		if( caller.getDomain() == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.UNKNOWN_ERROR, "Domain is null");
		}
		if( caller.getOwnerId() == null ) {
			IGGAPIAccessRule accessRule = caller.getAccessRule();
			GGAPIServiceMethod method = caller.getAccessRule().getMethod();
			
			if( (accessRule != null && accessRule.getAccess() == GGAPIServiceAccess.owner) || (caller.getDomain().getEntity().getValue1().ownedEntity() && (method == GGAPIServiceMethod.CREATE || method == GGAPIServiceMethod.PARTIAL_UPDATE) ) ) {
				if( ownerId == null || ownerId.isEmpty() ) {
					throw new GGAPIEngineException(GGAPIExceptionCode.BAD_REQUEST, "OwnerId null");
				}
			}
			
			if( ownerId != null && !ownerId.isEmpty() ) {
				caller.setOwnerId(ownerId);
					
				if( this.ownersDomain.isPresent() ) {
					Object owner = this.ownersFactory.getEntityFromRepository(caller, null, GGAPIEntityIdentifier.UUID, ownerId);
					IGGObjectQuery q;
					try {
						q = GGObjectQueryFactory.objectQuery(this.ownersDomain.get().getEntity().getValue0(), owner);
						caller.setOwnerId((String) q.getValue(this.ownersDomain.get().getEntity().getValue1().ownerIdFieldAddress()));
						caller.setSuperOwner((boolean) q.getValue(this.ownersDomain.get().getEntity().getValue1().superOnwerIdFieldAddress()));
					} catch (GGReflectionException e) {
						GGAPIException.processException(e);
					}
				} else {
					if( ownerId.equals(this.superOwnerId) ) {
						caller.setSuperOwner(true);
					}
				}
			}
		}
	}
}
