package com.garganttua.api.spec.context;

public interface IServiceSecurityRequirements {

    boolean noSecurityRequirements();

    boolean mustBeAuthenticated();

    boolean needTenantUuid();

    boolean needOwnerUuid();

    boolean needAuthority();

    String neededAuthority();

}
