package com.garganttua.api.spec.caller;

import java.util.List;

import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.service.GGAPIServiceAccess;

import lombok.Getter;

import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.GGAPIMethod;
import com.garganttua.api.spec.domain.IGGAPIDomain;

public interface IGGAPICaller {

	String getTenantId();

	String getRequestedTenantId();

	String getOwnerId();

	String getCallerId();

	boolean isSuperTenant();

	boolean isSuperOwner();

	IGGAPIDomain getDomain();

	boolean isAnonymous();

	List<String> getAuthorities();

	void setOwnerId(String ownerId);

	String getEndpoint();

	Class<?> getEntity();

	GGAPIMethod getMethod();
	
	boolean isActionOnAllEntities();
	
	boolean isCustom();

	String toString();

	GGAPIServiceAccess getAccess();

	String getEndpointAuthority();

	GGAPIEntityOperation getOperation();

	boolean isAuthenticatorDomain();

	Class<?> getDomainEntityClass();

}
