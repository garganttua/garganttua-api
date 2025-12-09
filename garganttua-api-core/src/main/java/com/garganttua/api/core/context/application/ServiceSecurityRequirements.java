package com.garganttua.api.core.context.application;

import com.garganttua.api.spec.context.IServiceSecurityRequirements;

public class ServiceSecurityRequirements implements IServiceSecurityRequirements {

    public static IServiceSecurityRequirements disabled() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'disabled'");
    }

    @Override
    public boolean noSecurityRequirements() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'noSecurityRequirements'");
    }

    @Override
    public boolean mustBeAuthenticated() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mustBeAuthenticated'");
    }

    @Override
    public boolean needTenantUuid() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'needTenantUuid'");
    }

    @Override
    public boolean needOwnerUuid() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'needOwnerUuid'");
    }

    @Override
    public boolean needAuthority() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'needAuthority'");
    }

    @Override
    public String neededAuthority() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'neededAuthority'");
    }

}
