package com.garganttua.api.spec.security.context;

import com.garganttua.api.spec.definition.IAuthenticationDefinition;
import com.garganttua.api.spec.security.authentication.IAuthenticationRequestBuilder;

public interface IAuthenticationContext {

    IAuthenticationDefinition getAuthenticationDefinition();

    IAuthenticationRequestBuilder request();

}
