package com.garganttua.api.core.caller;

import java.util.List;
import java.util.Objects;

import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.IGGAPIAccessRule;

import lombok.Getter;

public class GGAPICaller implements IGGAPICaller {
	
	protected GGAPICaller(String tenantId, String requestedTenantId, String callerId, String ownerId, boolean superTenant,
			boolean superOwner, IGGAPIAccessRule accessRule, IGGAPIDomain domain, boolean anonymous,
			List<String> authorities) {
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
	}
	
	@Getter
	protected String tenantId;

	@Getter
	protected String requestedTenantId;
	
	@Getter 
	protected String callerId;

	@Getter
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

	public static IGGAPICaller createSuperCaller() {
		return new GGAPICaller(null, null, null, null, true, true, null, null, false, null);
	}

	public static IGGAPICaller createTenantCaller(String uuid) {
		return new GGAPICaller(uuid, uuid, null, null, false, false, null, null, false, null);
	}
}
