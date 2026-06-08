package com.garganttua.api.core.security;

import com.garganttua.api.commons.security.context.IServiceSecurityRequirements;

public class ServiceSecurityRequirements implements IServiceSecurityRequirements {

    private final boolean noSecurity;
    private final boolean authenticated;
    private final boolean tenantUuid;
    private final boolean ownerUuid;
    private final boolean authority;
    private final String neededAuthority;

    private ServiceSecurityRequirements(boolean noSecurity, boolean authenticated, boolean tenantUuid,
            boolean ownerUuid, boolean authority, String neededAuthority) {
        this.noSecurity = noSecurity;
        this.authenticated = authenticated;
        this.tenantUuid = tenantUuid;
        this.ownerUuid = ownerUuid;
        this.authority = authority;
        this.neededAuthority = neededAuthority;
    }

    public static IServiceSecurityRequirements disabled() {
        return new ServiceSecurityRequirements(true, false, false, false, false, null);
    }

    public static IServiceSecurityRequirements none() {
        return new ServiceSecurityRequirements(true, false, false, false, false, null);
    }

    public static IServiceSecurityRequirements authenticated() {
        return new ServiceSecurityRequirements(false, true, false, false, false, null);
    }

    public static IServiceSecurityRequirements tenant() {
        return new ServiceSecurityRequirements(false, true, true, false, false, null);
    }

    public static IServiceSecurityRequirements owner() {
        return new ServiceSecurityRequirements(false, true, true, true, false, null);
    }

    public static IServiceSecurityRequirements withAuthority(String authority) {
        return new ServiceSecurityRequirements(false, true, true, false, true, authority);
    }

    @Override
    public boolean noSecurityRequirements() {
        return noSecurity;
    }

    @Override
    public boolean mustBeAuthenticated() {
        return authenticated;
    }

    @Override
    public boolean needTenantUuid() {
        return tenantUuid;
    }

    @Override
    public boolean needOwnerUuid() {
        return ownerUuid;
    }

    @Override
    public boolean needAuthority() {
        return authority;
    }

    @Override
    public String neededAuthority() {
        return neededAuthority;
    }

}
