package com.garganttua.api.core.caller;

import java.util.List;
import java.util.Objects;

import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.spec.EntityOperation;
import com.garganttua.core.CoreExceptionCode;
import com.garganttua.api.spec.Method;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.security.IAccessRule;
import com.garganttua.api.spec.service.ServiceAccess;

import lombok.Getter;
import lombok.Setter;

public class Caller implements ICaller {
	
	protected Caller(String tenantId, String requestedTenantId, String callerId, String ownerId, boolean superTenant,
			boolean superOwner, IAccessRule accessRule, IDomain domain, boolean anonymous,
			List<String> authorities) throws EngineException {
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
			throw new EngineException(CoreExceptionCode.BAD_REQUEST, "Invalid ownerId ["+ownerId+"] should be of format DOMAIN:UUID");
	}
	
	@Getter
	protected String tenantId;

	@Getter
	protected String requestedTenantId;
	
	@Getter
	@Setter
	protected String callerId;

	@Getter
	@Setter
	protected String ownerId;

	@Getter
	protected boolean superTenant;

	@Getter
	protected boolean superOwner;

	@Getter
	protected IAccessRule accessRule;

	@Getter
	protected IDomain domain;
	
	@Getter
	protected boolean anonymous;

	@Getter
	protected List<String> authorities;

    @Override
    public String toString() {
        return "Caller{" +
                "tenantId='" + tenantId + '\'' +
                ", requestedTenantId='" + requestedTenantId + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", superTenant=" + superTenant +
                ", superOwner=" + superOwner +
                ", accessRule=" + accessRule +
                ", domain=" + domain +
                ", anonymous=" + anonymous +
                ", authorities=" + authorities +
				", callerId=" + callerId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Caller that = (Caller) o;
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

	public static ICaller createSuperCaller() throws EngineException {
		return new Caller(null, null, null, null, true, true, null, null, false, null);
	}

	public static ICaller createTenantCaller(String uuid) throws EngineException {
		return new Caller(uuid, uuid, null, null, false, false, null, null, false, null);
	}

	public static ICaller createTenantCallerWithOwnerId(String tenantId, String ownerId) throws EngineException {
		return new Caller(tenantId, tenantId, null, ownerId, false, false, null, null, false, null);
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
	public Method getMethod() {
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
	public ServiceAccess getAccess() {
		return this.accessRule.getAccess();
	}

	@Override
	public EntityOperation getOperation() {
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
