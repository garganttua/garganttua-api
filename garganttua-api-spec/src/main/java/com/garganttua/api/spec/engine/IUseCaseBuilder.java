package com.garganttua.api.spec.engine;

import com.garganttua.api.spec.CoreException;

public interface IUseCaseBuilder<Up> extends IAutomaticLinkedBuilder<IUseCaseContext, Up, IUseCaseBuilder<Up>> {

    IUseCaseBuilder<Up> pathSuffix(String string);

    IUseCaseBuilder<Up> completePath(String string);

    IUseCaseBuilder<Up> action(Action allentities);

    IUseCaseBuilder<Up> input(Class<Object> class1);

    IUseCaseBuilder<Up> output(Class<Object> class1);

    IUseCaseBinderBuilder<IUseCaseBuilder<Up>> bind(IObjectSupplierBuilder<?> supplier) throws CoreException;

    IUseCaseBinderBuilder<IUseCaseBuilder<Up>> bind(Object object) throws CoreException;

    IUseCaseBinderBuilder<IUseCaseBuilder<Up>> bind();

    IUseCaseBuilder<Up> operation(TechnicalOperation operation);
}
