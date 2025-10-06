package com.garganttua.api.core.context.application;

import java.util.List;

import com.garganttua.api.spec.engine.IApplicationSecurityContext;
import com.garganttua.api.spec.engine.IAuthenticationContext;
import com.garganttua.api.spec.engine.IAuthorizationProtocolContext;

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
