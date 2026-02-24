package com.garganttua.api.spec.context.dsl.security;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.garganttua.api.spec.context.IAuthenticationContext;
import com.garganttua.api.spec.context.dsl.IUseCaseBuilder;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.ObjectAddress;

public interface IAuthenticationBuilder
                extends IAutomaticLinkedBuilder<IAuthenticationBuilder, IApiContextSecurityBuilder, IAuthenticationContext> {

        IAuthenticationBuilder findPrincipal(boolean b);

        IAuthenticationBuilder authenticate(
                        String methodName) throws ApiException;

        IAuthenticationBuilder authenticate(
                        Method method) throws ApiException;

        IAuthenticationBuilder authenticate(
                        ObjectAddress methodAddress) throws ApiException;

        IAuthenticationBuilder entityMustHaveFieldOfTypeAnnotatedWith(Class<? extends Annotation> annotation,
                        Class<?> fieldType) throws ApiException;

        IAuthenticationBuilder applySecurityOnEntity(
                        String methodName) throws ApiException;

        IAuthenticationBuilder applySecurityOnEntity(
                        Method method) throws ApiException;

        IAuthenticationBuilder applySecurityOnEntity(
                        ObjectAddress methodAddress) throws ApiException;

        IUseCaseBuilder<?, ?, ?> useCase(String methodName) throws ApiException;

        IUseCaseBuilder<?, ?, ?> useCase(Method method) throws ApiException;

        IUseCaseBuilder<?, ?, ?> useCase(ObjectAddress methodAddress) throws ApiException;

}
