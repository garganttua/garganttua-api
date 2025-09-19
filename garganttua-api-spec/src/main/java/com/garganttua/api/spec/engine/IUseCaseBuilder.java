package com.garganttua.api.spec.engine;

import com.garganttua.api.spec.Method;
import com.garganttua.api.spec.service.ServiceAccess;

public interface IUseCaseBuilder extends IAutomaticLinkedBuilder<Object, IDomainBuilder, IUseCaseBuilder> {

    IUseCaseBuilder pathSuffix(String string);

    IUseCaseBuilder completePath(String string);

    IUseCaseBuilder action(Action allentities);

    IUseCaseBuilder input(Class<Object> class1);

    IUseCaseBuilder output(Class<Object> class1);

    IUseCaseBuilder authority(boolean b);

    IUseCaseBuilder access(ServiceAccess anonymous);

    IUseCaseBinderBuilder bind(IObjectSupplierBuilder<?> supplier);

    IUseCaseBinderBuilder bind(Object object);

    IUseCaseBuilder method(Method create);
}
