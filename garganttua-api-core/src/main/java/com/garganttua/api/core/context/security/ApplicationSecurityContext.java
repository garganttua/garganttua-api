package com.garganttua.api.core.context.security;

import java.util.List;

import com.garganttua.api.spec.security.context.IApplicationSecurityContext;
import com.garganttua.api.spec.security.context.IAuthenticationContext;
import com.garganttua.api.spec.security.context.IAuthorizationProtocolContext;

public class ApplicationSecurityContext implements IApplicationSecurityContext {

    public ApplicationSecurityContext(List<String> packages, List<IAuthenticationContext> collect,
            List<IAuthorizationProtocolContext> collect2) {
        //TODO Auto-generated constructor stub
    }

    @Override
    public boolean disabled() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'disabled'");
    }

}
