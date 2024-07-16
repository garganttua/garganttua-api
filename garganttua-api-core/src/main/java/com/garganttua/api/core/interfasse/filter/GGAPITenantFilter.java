package com.garganttua.api.core.interfasse.filter;

import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.factory.GGAPIEntityIdentifier;
import com.garganttua.api.spec.factory.IGGAPIEntityFactory;
import com.garganttua.api.spec.interfasse.filter.IGGAPITenantFilter;
import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.service.GGAPIServiceAccess;
import com.garganttua.api.spec.service.GGAPIServiceMethod;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GGAPITenantFilter implements IGGAPITenantFilter {

	private IGGAPIDomain tenantsDomain;
	
	private IGGAPIEntityFactory<?> tenantsFactory;
	
	private String superTenantId = "0";

	@Override
	public void doTenantIdFiltering(IGGAPICaller caller, String tenantId, String requestedtenantId) throws GGAPIException {
		if( caller == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.UNKNOWN_ERROR, "Caller is null");
		}
		IGGAPIAccessRule accessRule = caller.getAccessRule();

		if (accessRule != null && caller.getTenantId() == null) {

			if (accessRule != null && accessRule.getAccess() == GGAPIServiceAccess.anonymous) {
				caller.setAnonymous(true);
			}

			if ((tenantId == null || tenantId.isEmpty())
					&& (accessRule != null && accessRule.getAccess() != GGAPIServiceAccess.anonymous)) {
				throw new GGAPIEngineException(GGAPIExceptionCode.BAD_REQUEST, "TenantId null");
			}

			caller.setTenantId(tenantId);
			if (requestedtenantId != null && !requestedtenantId.isEmpty()) {
				caller.setRequestedTenantId(requestedtenantId);
			} else {
				caller.setRequestedTenantId(tenantId);
			}

			if (tenantId != null && tenantId.equals(this.superTenantId)) {
				caller.setSuperTenant(true);
			}

			if (this.tenantsDomain != null && tenantId != null) {
				GGAPICaller superCaller = new GGAPICaller();
				superCaller.setTenantId(tenantId);

				Object tenant = this.tenantsFactory.getEntityFromRepository(caller, null,
						GGAPIEntityIdentifier.UUID, tenantId);

				try {
					caller.setSuperTenant((boolean) GGObjectQueryFactory.objectQuery(tenant)
							.getValue(tenantsDomain.getEntity().getValue1().superTenantFieldAddress()));
				} catch (GGReflectionException e) {
					GGAPIException.processException(e);
				}

				if (!caller.getTenantId().equals(caller.getRequestedTenantId())) {
					superCaller.setTenantId(requestedtenantId);
					this.tenantsFactory.getEntityFromRepository(caller, null, GGAPIEntityIdentifier.UUID,
							caller.getRequestedTenantId());
				}
			}
			if (caller.isSuperTenant()) {
				if (caller.getRequestedTenantId() == null) {
					if (accessRule.getMethod() == GGAPIServiceMethod.READ) {
						caller.setRequestedTenantId(caller.getTenantId());
					}
				}
			} else {
				if (caller.getRequestedTenantId() == null) {
					caller.setRequestedTenantId(caller.getTenantId());
				}
			}
		}
	}
}
