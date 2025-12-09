package com.garganttua.api.spec.caller;

import java.util.List;

import com.garganttua.api.spec.context.Access;
import com.garganttua.api.spec.context.Operation;
import com.garganttua.api.spec.domain.IDomain;

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
	
	boolean isActionOnAllEntities();
	
	boolean isCustom();

	String toString();

	Access getAccess();

	String getEndpointAuthority();

	Operation getOperation();

	boolean isAuthenticatorDomain();

	Class<?> getDomainEntityClass();

	void setCallerId(String callerUuid);

}
