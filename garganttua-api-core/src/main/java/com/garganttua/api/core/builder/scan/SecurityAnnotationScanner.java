package com.garganttua.api.core.builder.scan;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.context.dsl.security.IApiSecurityBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticationBuilder;
import com.garganttua.api.commons.security.annotations.Authentication;
import com.garganttua.api.commons.security.annotations.AuthenticationAuthenticate;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IMethod;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;

import lombok.extern.slf4j.Slf4j;

/**
 * Scans configured packages for {@link Authentication}-annotated classes and
 * registers them on the API security builder. For each authentication class,
 * the {@link AuthenticationAuthenticate}-annotated method is bound as the
 * authenticate operation.
 *
 * <p>Silently no-ops when no packages are configured or when no
 * {@link IReflection} is available.
 */
@Slf4j
public final class SecurityAnnotationScanner {

    private final IApiBuilder apiBuilder;
    private final Set<String> packages;

    public SecurityAnnotationScanner(IApiBuilder apiBuilder, Set<String> packages) {
        this.apiBuilder = apiBuilder;
        this.packages = packages;
    }

    public void scan() throws ApiException {
        if (this.packages.isEmpty()) {
            return;
        }
        IReflection reflection;
        try {
            reflection = IClass.getReflection();
        } catch (Exception e) {
            log.atWarn().log("No IReflection available for @Authentication auto-detection: {}", e.getMessage());
            return;
        }

        IClass<Authentication> annotation = IClass.getClass(Authentication.class);
        IApiSecurityBuilder security = this.apiBuilder.security();

        int registered = 0;
        for (String pkg : this.packages) {
            List<IClass<?>> found = reflection.getClassesWithAnnotation(pkg, annotation);
            for (IClass<?> authClass : found) {
                if (security.isAuthenticationAvailable(authClass).isPresent()) {
                    continue;
                }
                Object instance = instantiate(authClass);
                @SuppressWarnings({"rawtypes", "unchecked"})
                FixedSupplierBuilder supplier = new FixedSupplierBuilder<>(instance, (IClass) authClass);
                IAuthenticationBuilder authBuilder = security.authentication(supplier);
                bindAuthenticate(reflection, authBuilder, authClass);
                registered++;
            }
        }
        if (registered > 0) {
            log.atDebug().log("Auto-detected {} @Authentication class(es)", registered);
        }
    }

    private static Object instantiate(IClass<?> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (Throwable t) {
            throw new IllegalStateException(
                    "Failed to instantiate @Authentication class " + clazz.getName()
                            + ": needs a public no-arg constructor",
                    t);
        }
    }

    private void bindAuthenticate(IReflection reflection, IAuthenticationBuilder authBuilder, IClass<?> authClass)
            throws ApiException {
        Optional<IMethod> method = reflection.findMethodAnnotatedWith(authClass,
                IClass.getClass(AuthenticationAuthenticate.class));
        if (method.isEmpty()) {
            log.atWarn().log("@Authentication on {} has no @AuthenticationAuthenticate method; skipping authenticate binding",
                    authClass.getSimpleName());
            return;
        }
        authBuilder.authenticate(method.get().getName());
    }
}
