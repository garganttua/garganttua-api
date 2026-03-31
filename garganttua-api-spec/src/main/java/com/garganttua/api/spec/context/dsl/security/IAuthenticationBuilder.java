package com.garganttua.api.spec.context.dsl.security;

import java.lang.annotation.Annotation;
import com.garganttua.core.reflection.IMethod;

import com.garganttua.api.spec.security.context.IAuthenticationContext;
import com.garganttua.api.spec.context.dsl.IUseCaseBuilder;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;

public interface IAuthenticationBuilder
                extends IAutomaticLinkedBuilder<IAuthenticationBuilder, IApiSecurityBuilder, IAuthenticationContext> {

        IAuthenticationMethodBinderBuilder<?> authenticate(
                        String methodName) throws ApiException;

        IAuthenticationMethodBinderBuilder<?> authenticate(
                        IMethod method) throws ApiException;

        IAuthenticationMethodBinderBuilder<?> authenticate(
                        ObjectAddress methodAddress) throws ApiException;

        IAuthenticationBuilder entityMustHaveFieldOfTypeAnnotatedWith(IClass<? extends Annotation> annotation,
                        IClass<?> fieldType) throws ApiException;

        IAuthenticationBuilder applySecurityOnEntity(
                        String methodName) throws ApiException;

        IAuthenticationBuilder applySecurityOnEntity(
                        IMethod method) throws ApiException;

        IAuthenticationBuilder applySecurityOnEntity(
                        ObjectAddress methodAddress) throws ApiException;

        IUseCaseBuilder<?, ?, ?> useCase(String methodName) throws ApiException;

        IUseCaseBuilder<?, ?, ?> useCase(IMethod method) throws ApiException;

        IUseCaseBuilder<?, ?, ?> useCase(ObjectAddress methodAddress) throws ApiException;

}
