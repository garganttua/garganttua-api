package com.garganttua.api.spec.context.dsl.security;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.garganttua.api.spec.context.IAuthenticationContext;
import com.garganttua.api.spec.context.dsl.IUseCaseBuilder;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.ObjectAddress;

public interface IAuthenticationBuilder
                extends IAutomaticLinkedBuilder<IAuthenticationBuilder, IApiContextSecurityBuilder, IAuthenticationContext> {

        IAuthenticationBuilder findPrincipal(boolean b);

        IAuthenticationBuilder authenticate(
                        String methodName) throws DslException;

        IAuthenticationBuilder authenticate(
                        Method method) throws DslException;

        IAuthenticationBuilder authenticate(
                        ObjectAddress methodAddress) throws DslException;

        IAuthenticationBuilder entityMustHaveFieldOfTypeAnnotatedWith(Class<? extends Annotation> annotation,
                        Class<?> fieldType) throws DslException;

        IAuthenticationBuilder applySecurityOnEntity(
                        String methodName) throws DslException;

        IAuthenticationBuilder applySecurityOnEntity(
                        Method method) throws DslException;

        IAuthenticationBuilder applySecurityOnEntity(
                        ObjectAddress methodAddress) throws DslException;

        IUseCaseBuilder<?, ?, ?> useCase(String methodName) throws DslException;

        IUseCaseBuilder<?, ?, ?> useCase(Method method) throws DslException;

        IUseCaseBuilder<?, ?, ?> useCase(ObjectAddress methodAddress) throws DslException;

}
