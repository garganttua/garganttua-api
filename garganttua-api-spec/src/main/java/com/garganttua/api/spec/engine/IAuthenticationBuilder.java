package com.garganttua.api.spec.engine;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.garganttua.api.spec.CoreException;
import com.garganttua.reflection.GGObjectAddress;

public interface IAuthenticationBuilder
                extends IAutomaticLinkedBuilder<IAuthenticationContext, IContextSecurityBuilder, IAuthenticationBuilder> {

        IAuthenticationBuilder findPrincipal(boolean b);

        IAuthenticationBuilder authenticate(
                        String methodName) throws CoreException;

        IAuthenticationBuilder authenticate(
                        Method method) throws CoreException;

        IAuthenticationBuilder authenticate(
                        GGObjectAddress methodAddress) throws CoreException;

        IAuthenticationBuilder entityMustHaveFieldOfTypeAnnotatedWith(Class<? extends Annotation> annotation,
                        Class<?> fieldType) throws CoreException;

        IAuthenticationBuilder applySecurityOnEntity(
                        String methodName) throws CoreException;

        IAuthenticationBuilder applySecurityOnEntity(
                        Method method) throws CoreException;

        IAuthenticationBuilder applySecurityOnEntity(
                        GGObjectAddress methodAddress) throws CoreException;

        IUseCaseBuilder<IAuthenticationBuilder> useCase(String methodName) throws CoreException;

        IUseCaseBuilder<IAuthenticationBuilder> useCase(Method method) throws CoreException;

        IUseCaseBuilder<IAuthenticationBuilder> useCase(GGObjectAddress methodAddress) throws CoreException;

}
