package com.garganttua.api.spec.engine;

public interface IServiceSecurityRequirements {

    boolean noSecurityRequirements();

    boolean mustBeAuthenticated();

    boolean needTenantUuid();

    boolean needOwnerUuid();

    boolean needAuthority();

    String neededAuthority();

}
