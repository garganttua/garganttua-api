package com.garganttua.api.spec.context;

import java.util.Optional;

import com.garganttua.api.spec.domain.IDomainContext;
import com.garganttua.core.injection.IDiContext;

public interface IApiContext extends IDiContext{

    Optional<IDomainContext<?>> getDomainContext(String domainName);

}
