package com.garganttua.api.commons.context.dsl.security;

import java.lang.annotation.Annotation;
import com.garganttua.core.reflection.IMethod;

import com.garganttua.api.commons.security.context.IAuthenticationContext;
import com.garganttua.api.commons.context.dsl.IUseCaseBuilder;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;

public interface IAuthenticationBuilder<E>
                extends IAutomaticLinkedBuilder<IAuthenticationBuilder<E>, E, IAuthenticationContext> {

        IAuthenticationMethodBinderBuilder<?> authenticate(
                        String methodName) throws ApiException;

        IAuthenticationMethodBinderBuilder<?> authenticate(
                        IMethod method) throws ApiException;

        IAuthenticationMethodBinderBuilder<?> authenticate(
                        ObjectAddress methodAddress) throws ApiException;

        IAuthenticationBuilder<E> entityMustHaveFieldOfTypeAnnotatedWith(IClass<? extends Annotation> annotation,
                        IClass<?> fieldType) throws ApiException;

        /**
         * Declares a custom method, run on CREATE and UPDATE of this authenticator entity
         * (after validation, just before persistence), to apply security on it — e.g. hash a
         * password field. Its first parameter is auto-wired to the entity being written; the
         * method may mutate it in place or return a secured entity (both are honored). Declare
         * additional parameters via {@code .withParam(i, supplier)} and return with {@code .up()}.
         */
        IAuthenticationMethodBinderBuilder<?> applySecurityOnEntity(
                        String methodName) throws ApiException;

        IAuthenticationMethodBinderBuilder<?> applySecurityOnEntity(
                        IMethod method) throws ApiException;

        IAuthenticationMethodBinderBuilder<?> applySecurityOnEntity(
                        ObjectAddress methodAddress) throws ApiException;

        IUseCaseBuilder<?, ?, ?> useCase(String methodName) throws ApiException;

        IUseCaseBuilder<?, ?, ?> useCase(IMethod method) throws ApiException;

        IUseCaseBuilder<?, ?, ?> useCase(ObjectAddress methodAddress) throws ApiException;

}
