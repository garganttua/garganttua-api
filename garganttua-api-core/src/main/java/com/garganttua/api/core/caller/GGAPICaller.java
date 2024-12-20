package com.garganttua.api.core.caller;

import java.util.List;
import java.util.Objects;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.GGAPIMethod;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.service.GGAPIServiceAccess;

import lombok.Getter;
import lombok.Setter;

public class GGAPICaller implements IGGAPICaller {
	
	protected GGAPICaller(String tenantId, String requestedTenantId, String callerId, String ownerId, boolean superTenant,
			boolean superOwner, IGGAPIAccessRule accessRule, IGGAPIDomain domain, boolean anonymous,
			List<String> authorities) throws GGAPIEngineException {
		this.tenantId = tenantId;
		this.requestedTenantId = requestedTenantId;
		this.ownerId = ownerId;
		this.superTenant = superTenant;
		this.superOwner = superOwner;
		this.accessRule = accessRule;
		this.domain = domain;
		this.anonymous = anonymous;
		this.authorities = authorities;
		this.callerId = callerId;
		if( this.ownerId!=null && this.ownerId.split(":").length != 2 )
			throw new GGAPIEngineException(GGAPIExceptionCode.BAD_REQUEST, "Invalid ownerId ["+ownerId+"] should be of format DOMAIN:UUID");
	}
	
	@Getter
	protected String tenantId;

	@Getter
	protected String requestedTenantId;
	
	@Getter 
	protected String callerId;

	@Getter
	@Setter
	protected String ownerId;

	@Getter
	protected boolean superTenant;

	@Getter
	protected boolean superOwner;

	@Getter
	protected IGGAPIAccessRule accessRule;

	@Getter
	protected IGGAPIDomain domain;
	
	@Getter
	protected boolean anonymous;

	@Getter
	protected List<String> authorities;

    @Override
    public String toString() {
        return "GGAPICaller{" +
                "tenantId='" + tenantId + '\'' +
                ", requestedTenantId='" + requestedTenantId + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", superTenant=" + superTenant +
                ", superOwner=" + superOwner +
                ", accessRule=" + accessRule +
                ", domain=" + domain +
                ", anonymous=" + anonymous +
                ", authorities=" + authorities +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GGAPICaller that = (GGAPICaller) o;
        return superTenant == that.superTenant &&
                superOwner == that.superOwner &&
                anonymous == that.anonymous &&
                Objects.equals(tenantId, that.tenantId) &&
                Objects.equals(requestedTenantId, that.requestedTenantId) &&
                Objects.equals(ownerId, that.ownerId) &&
                Objects.equals(accessRule, that.accessRule) &&
                Objects.equals(domain, that.domain) &&
                Objects.equals(authorities, that.authorities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, requestedTenantId, ownerId, superTenant, superOwner, accessRule, domain, anonymous, authorities);
    }

	public static IGGAPICaller createSuperCaller() throws GGAPIEngineException {
		return new GGAPICaller(null, null, null, null, true, true, null, null, false, null);
	}

	public static IGGAPICaller createTenantCaller(String uuid) throws GGAPIEngineException {
		return new GGAPICaller(uuid, uuid, null, null, false, false, null, null, false, null);
	}

	public static IGGAPICaller createTenantCallerWithOwnerId(String tenantId, String ownerId) throws GGAPIEngineException {
		return new GGAPICaller(tenantId, tenantId, null, ownerId, false, false, null, null, false, null);
	}

	@Override
	public String getEndpoint() {
		return this.accessRule.getEndpoint();
	}

	@Override
	public String getEndpointAuthority() {
		return this.accessRule.getEndpoint();
	}

	@Override
	public Class<?> getEntity() {
		return this.domain.getEntityClass();
	}

	@Override
	public GGAPIMethod getMethod() {
		return this.accessRule.getOperation().getMethod();
	}

	@Override
	public boolean isActionOnAllEntities() {
		return this.accessRule.getOperation().isActionOnAllEntities();
	}

	@Override
	public boolean isCustom() {
		return this.accessRule.getOperation().isCustom();
	}

	@Override
	public GGAPIServiceAccess getAccess() {
		return this.accessRule.getAccess();
	}

	@Override
	public GGAPIEntityOperation getOperation() {
		return this.accessRule.getOperation();
	}

	@Override
	public boolean isAuthenticatorDomain() {
		return this.domain.isAuthenticatorEntity();
	}

	@Override
	public Class<?> getDomainEntityClass() {
		return this.domain.getEntityClass();
	}
}
