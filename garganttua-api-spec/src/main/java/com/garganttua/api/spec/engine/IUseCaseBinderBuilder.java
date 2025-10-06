package com.garganttua.api.spec.engine;

public interface IUseCaseBinderBuilder<Up> extends IMethodBinderBuilder<IUseCaseBinderBuilder<Up>, Up> {

    String getMethodName();

}
