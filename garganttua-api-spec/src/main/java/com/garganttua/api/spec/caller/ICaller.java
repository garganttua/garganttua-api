package com.garganttua.api.spec.caller;

import java.util.List;

import com.garganttua.api.spec.EntityOperation;
import com.garganttua.api.spec.Method;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.service.ServiceAccess;

public interface ICaller {

	String getTenantId();

	String getRequestedTenantId();

	String getOwnerId();

	String getCallerId();

	boolean isSuperTenant();

	boolean isSuperOwner();

	IDomain getDomain();

	boolean isAnonymous();

	List<String> getAuthorities();

	void setOwnerId(String ownerId);

	String getEndpoint();

	Class<?> getEntity();

	Method getMethod();
	
	boolean isActionOnAllEntities();
	
	boolean isCustom();

	String toString();

	ServiceAccess getAccess();

	String getEndpointAuthority();

	EntityOperation getOperation();

	boolean isAuthenticatorDomain();

	Class<?> getDomainEntityClass();

	void setCallerId(String callerUuid);

}
