package com.garganttua.api.spec.context;

import java.util.List;
import java.util.Optional;

import com.garganttua.core.reflection.binders.dsl.IMethodBinderBuilder;

public interface IExecutionContext {

    boolean isStandard();

    boolean isUseCase();

    String getDomainName();

    boolean isAuthentication();

    IServiceSecurityRequirements getSecurityRequirements();

    String getServiceName();

    void setSecurityRequirements(IServiceSecurityRequirements sReq);

    Optional<?> getResponse();

    BusinessOperation getBusinessOperation();

    Optional<?> getInputParameter(StandardParameter parameter);

    Optional<?> getInputParameter(String parameterName);

    <T> Optional<T> getInputParameter(StandardParameter parameter, Class<T> clazz);

    <T> Optional<T> getInputParameter(String parameterNamer, Class<T> clazz);

    Optional<?> getExecutionVariable(String variableName);

    <T> Optional<T> getExecutionVariable(String variableName, Class<T> clazz);

    void setExecutionVariable(String variableName, Object variable);

    List<IMethodBinderBuilder<?, ?, ?, ?>> getAfterOperationMethods();

    List<IMethodBinderBuilder<?, ?, ?, ?>> getBeforeOperationMethods();

    void setBeforeOperationMethods(List<IMethodBinderBuilder<?, ?, ?, ?>> methods);

    void setAfterOperationMethods(List<IMethodBinderBuilder<?, ?, ?, ?>> methods);

    boolean isReadAllOperation();

}
