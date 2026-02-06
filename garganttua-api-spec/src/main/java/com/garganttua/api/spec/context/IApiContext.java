package com.garganttua.api.spec.context;

import java.util.Optional;

import com.garganttua.core.lifecycle.ILifecycle;
import com.garganttua.core.nativve.INativeReflectionConfiguration;

public interface IApiContext extends ILifecycle, INativeReflectionConfiguration {

    Optional<IDomainContext<?>> getDomainContext(String domainName);

}
