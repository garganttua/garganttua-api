package com.garganttua.api.spec.security.context;

public interface IServiceSecurityRequirements {

    boolean noSecurityRequirements();

    boolean mustBeAuthenticated();

    boolean needTenantUuid();

    boolean needOwnerUuid();

    boolean needAuthority();

    String neededAuthority();

}
