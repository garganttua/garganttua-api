package com.garganttua.api.spec.engine;

import java.util.List;

public interface IDomainEntityContext {

    String getEntityName();

    Class<?> getEntityClass();

    List<IMethodBinderBuilder<?, ?>> getAfterGetMethods();

}
