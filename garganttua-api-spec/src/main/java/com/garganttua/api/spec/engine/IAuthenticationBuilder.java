package com.garganttua.api.spec.engine;

import java.lang.reflect.Method;

import com.garganttua.reflection.GGObjectAddress;

public interface IAuthenticationBuilder extends IAutomaticLinkedBuilder<Object, IContextSecurityBuilder, IAuthenticationBuilder> {

        IAuthenticationBuilder findPrincipal(boolean b);

        IAuthenticationBuilder authenticate(
                        String methodName);

        IAuthenticationBuilder authenticate(
                        Method method);

        IAuthenticationBuilder authenticate(
                        GGObjectAddress methodAddress);

        // ajouter quelque chose pour que l'authentication vérifie que l'entité du
        // domaine contient bien les infos d'authentification

        IAuthenticationBuilder applySecurityOnEntity(
                        String methodName);

        IAuthenticationBuilder applySecurityOnEntity(
                        Method method);

        IAuthenticationBuilder applySecurityOnEntity(
                        GGObjectAddress methodAddress);

}
