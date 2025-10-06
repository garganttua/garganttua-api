package com.garganttua.api.spec.engine;

import java.util.List;

import com.garganttua.api.spec.repository.IRepository;

public interface IDomainContext {

    String getDomainName();

    List<Service> getServices();

    IRepository getRepository();

    List<IMethodBinderBuilder<?, ?>> getAfterGetMethods();
}
