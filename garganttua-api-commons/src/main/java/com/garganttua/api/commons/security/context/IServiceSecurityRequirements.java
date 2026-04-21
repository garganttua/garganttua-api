package com.garganttua.api.commons.security.context;

public interface IServiceSecurityRequirements {

    boolean noSecurityRequirements();

    boolean mustBeAuthenticated();

    boolean needTenantUuid();

    boolean needOwnerUuid();

    boolean needAuthority();

    String neededAuthority();

}
