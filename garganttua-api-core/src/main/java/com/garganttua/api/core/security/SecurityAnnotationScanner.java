package com.garganttua.api.core.security;
import com.garganttua.api.core.entity.EntityAnnotationScanner;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.security.IApiSecurityBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticationBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticatorBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthorizationBuilder;
import com.garganttua.api.commons.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.api.commons.context.dsl.security.IRefreshableAuthorizationBuilder;
import com.garganttua.api.commons.context.dsl.security.ISignableAuthorizationBuilder;
import com.garganttua.api.commons.security.annotations.Authentication;
import com.garganttua.api.commons.security.annotations.AuthenticationAuthenticate;
import com.garganttua.api.commons.security.annotations.Authenticator;
import com.garganttua.api.commons.security.annotations.AuthenticatorAlwaysEnabled;
import com.garganttua.api.commons.security.annotations.AuthenticatorAccountNonExpired;
import com.garganttua.api.commons.security.annotations.AuthenticatorAccountNonLocked;
import com.garganttua.api.commons.security.annotations.AuthenticatorAuthorities;
import com.garganttua.api.commons.security.annotations.AuthenticatorCredentialsNonExpired;
import com.garganttua.api.commons.security.annotations.AuthenticatorEnabled;
import com.garganttua.api.commons.security.annotations.AuthenticatorLogin;
import com.garganttua.api.commons.security.annotations.Authorization;
import com.garganttua.api.commons.security.annotations.AuthorizationAuthorities;
import com.garganttua.api.commons.security.annotations.AuthorizationCreation;
import com.garganttua.api.commons.security.annotations.AuthorizationSignedBy;
import com.garganttua.api.commons.security.annotations.AuthorizationDecode;
import com.garganttua.api.commons.security.annotations.AuthorizationEncode;
import com.garganttua.api.commons.security.annotations.AuthorizationExpiration;
import com.garganttua.api.commons.security.annotations.AuthorizationRefreshTokenExpiration;
import com.garganttua.api.commons.security.annotations.AuthorizationRevoked;
import com.garganttua.api.commons.security.annotations.AuthorizationSign;
import com.garganttua.api.commons.security.annotations.AuthorizationSignature;
import com.garganttua.api.commons.security.annotations.AuthorizationType;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IField;
import com.garganttua.core.reflection.IMethod;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;

import com.garganttua.core.observability.Logger;

/**
 * Scans configured packages for security-related annotations and applies them
 * onto the API builder.
 *
 * <p>Three passes run in order:
 * <ol>
 *   <li>{@link Authentication} classes — registered as authentication strategies
 *       on {@code apiBuilder.security()} with the {@link AuthenticationAuthenticate}
 *       method bound.</li>
 *   <li>{@link Authorization} classes — assumed to also be {@code @Entity},
 *       so {@link EntityAnnotationScanner} has already created their domain;
 *       this scanner re-acquires the same domain builder (via
 *       {@code apiBuilder.domain(class)} which is {@code computeIfAbsent})
 *       and applies {@code .security().authorization()} from the type-level
 *       and field/method-level annotations.</li>
 *   <li>{@link Authenticator} classes — same approach: re-acquire builder,
 *       apply {@code .security().authenticator()}, link to the configured
 *       {@code authentications()} via {@link IApiSecurityBuilder#isAuthenticationAvailable}
 *       and to the {@code authorization()} domain by re-acquiring its builder.</li>
 * </ol>
 *
 * Silently no-ops when no packages are configured or when no
 * {@link IReflection} is available (e.g. native image without metadata).
 */
public final class SecurityAnnotationScanner {
	private static final Logger log = Logger.getLogger(SecurityAnnotationScanner.class);


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
            log.warn("No IReflection available for security auto-detection: {}", e.getMessage());
            return;
        }

        scanAuthentications(reflection);
        scanAuthorizations(reflection);
        scanAuthenticators(reflection);
    }

    // ─────────────────── @Authentication ───────────────────

    private void scanAuthentications(IReflection reflection) throws ApiException {
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
                @SuppressWarnings({ "rawtypes", "unchecked" })
                FixedSupplierBuilder supplier = new FixedSupplierBuilder<>(instance, (IClass) authClass);
                IAuthenticationBuilder authBuilder = security.authentication(supplier);
                bindAuthenticate(reflection, authBuilder, authClass);
                registered++;
            }
        }
        if (registered > 0) {
            log.debug("Auto-detected {} @Authentication class(es)", registered);
        }
    }

    private void bindAuthenticate(IReflection reflection, IAuthenticationBuilder authBuilder, IClass<?> authClass)
            throws ApiException {
        Optional<IMethod> method = reflection.findMethodAnnotatedWith(authClass,
                IClass.getClass(AuthenticationAuthenticate.class));
        if (method.isEmpty()) {
            log.warn("@Authentication on {} has no @AuthenticationAuthenticate method; skipping authenticate binding",
                    authClass.getSimpleName());
            return;
        }
        var binder = authBuilder.authenticate(method.get().getName());

        // Auto-wire standard parameter suppliers based on declared parameter types.
        // The canonical authenticate signature is
        // {@code (Object principal, byte[] credentials, IAuthenticatorDefinition def)}.
        // Authors who want a different signature can still override via DSL after scan.
        IClass<?>[] paramTypes = method.get().getParameterTypes();
        IClass<?> bytesClass = IClass.getClass(byte[].class);
        IClass<?> definitionClass = IClass.getClass(
                com.garganttua.api.commons.definition.IAuthenticatorDefinition.class);
        for (int i = 0; i < paramTypes.length; i++) {
            IClass<?> p = paramTypes[i];
            if (bytesClass.equals(p)) {
                binder.withParam(i, new com.garganttua.api.core.security.authentication.AuthenticateCredentialsSupplierBuilder());
            } else if (definitionClass.equals(p) || p.represents(com.garganttua.api.commons.definition.IAuthenticatorDefinition.class)) {
                binder.withParam(i, new com.garganttua.api.core.security.authentication.AuthenticatorDefinitionSupplierBuilder());
            } else {
                // Default: the principal entity (the matched user). PrincipalSupplier
                // resolves it from the in-flight authentication request via the runtime
                // context populated by AUTHENTICATE.gs.
                binder.withParam(i, new com.garganttua.api.core.security.authentication.PrincipalSupplierBuilder());
            }
        }
    }

    // ─────────────────── @Authorization ───────────────────

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void scanAuthorizations(IReflection reflection) throws ApiException {
        IClass<Authorization> anno = IClass.getClass(Authorization.class);
        int registered = 0;
        for (String pkg : this.packages) {
            for (IClass<?> authzClass : reflection.getClassesWithAnnotation(pkg, anno)) {
                Authorization a = authzClass.getAnnotation(anno);

                IDomainBuilder<Object> domain = (IDomainBuilder<Object>) this.apiBuilder.domain((IClass) authzClass);
                IDomainSecurityBuilder<Object> sec = domain.security();
                IAuthorizationBuilder<Object> authzBuilder = sec.authorization();

                applyAuthorizationFields(reflection, authzBuilder, authzClass);
                if (a.signable()) {
                    applySignable(reflection, authzBuilder, authzClass);
                }
                if (a.renewable()) {
                    applyRefreshable(reflection, authzBuilder, authzClass);
                }
                authzBuilder.up();
                sec.up();
                registered++;
            }
        }
        if (registered > 0) {
            log.debug("Auto-detected {} @Authorization class(es)", registered);
        }
    }

    private void applyAuthorizationFields(IReflection reflection, IAuthorizationBuilder<Object> authzBuilder, IClass<?> authzClass)
            throws ApiException {
        Optional<IField> typeF = reflection.findFieldAnnotatedWith(authzClass, IClass.getClass(AuthorizationType.class));
        if (typeF.isPresent()) authzBuilder.type(typeF.get().getName());

        Optional<IField> authoritiesF = reflection.findFieldAnnotatedWith(authzClass, IClass.getClass(AuthorizationAuthorities.class));
        if (authoritiesF.isPresent()) authzBuilder.authorities(authoritiesF.get().getName());

        Optional<IField> expirationF = reflection.findFieldAnnotatedWith(authzClass, IClass.getClass(AuthorizationExpiration.class));
        if (expirationF.isPresent()) authzBuilder.expirable(expirationF.get().getName());

        Optional<IField> revokedF = reflection.findFieldAnnotatedWith(authzClass, IClass.getClass(AuthorizationRevoked.class));
        if (revokedF.isPresent()) authzBuilder.revokable(revokedF.get().getName());

        Optional<IField> creationF = reflection.findFieldAnnotatedWith(authzClass, IClass.getClass(AuthorizationCreation.class));
        if (creationF.isPresent()) authzBuilder.creation(creationF.get().getName());

        Optional<IField> signedByF = reflection.findFieldAnnotatedWith(authzClass, IClass.getClass(AuthorizationSignedBy.class));
        if (signedByF.isPresent()) authzBuilder.signedBy(signedByF.get().getName());

        // Transport encode/decode are wired on the PLAIN authorization (not gated on
        // refreshable): a signable, non-refreshable token (e.g. a stateless JWT) can
        // declare @AuthorizationEncode to have authenticate return its compact form.
        Optional<IMethod> encM = reflection.findMethodAnnotatedWith(authzClass, IClass.getClass(AuthorizationEncode.class));
        if (encM.isPresent()) authzBuilder.encode(encM.get().getName());
        Optional<IMethod> decM = reflection.findMethodAnnotatedWith(authzClass, IClass.getClass(AuthorizationDecode.class));
        if (decM.isPresent()) authzBuilder.decode(decM.get().getName());
    }

    private void applySignable(IReflection reflection, IAuthorizationBuilder<Object> authzBuilder, IClass<?> authzClass)
            throws ApiException {
        ISignableAuthorizationBuilder<Object> signable = authzBuilder.signable();
        Optional<IField> sigF = reflection.findFieldAnnotatedWith(authzClass, IClass.getClass(AuthorizationSignature.class));
        if (sigF.isPresent()) signable.signature(sigF.get().getName());
        Optional<IMethod> signM = reflection.findMethodAnnotatedWith(authzClass, IClass.getClass(AuthorizationSign.class));
        if (signM.isPresent()) signable.getDataToSign(signM.get().getName());
        signable.up();
    }

    private void applyRefreshable(IReflection reflection, IAuthorizationBuilder<Object> authzBuilder, IClass<?> authzClass)
            throws ApiException {
        IRefreshableAuthorizationBuilder<Object> refreshable = authzBuilder.refreshable();
        Optional<IField> refExpF = reflection.findFieldAnnotatedWith(authzClass, IClass.getClass(AuthorizationRefreshTokenExpiration.class));
        if (refExpF.isPresent()) refreshable.expirable(refExpF.get().getName());
        // @AuthorizationEncode / @AuthorizationDecode are now wired on the plain
        // authorization in applyAuthorizationFields (encode works without refreshable).
        refreshable.up();
    }

    // ─────────────────── @Authenticator ───────────────────

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void scanAuthenticators(IReflection reflection) throws ApiException {
        IClass<Authenticator> anno = IClass.getClass(Authenticator.class);
        IApiSecurityBuilder apiSec = this.apiBuilder.security();
        int registered = 0;
        for (String pkg : this.packages) {
            for (IClass<?> authrClass : reflection.getClassesWithAnnotation(pkg, anno)) {
                Authenticator a = authrClass.getAnnotation(anno);

                IDomainBuilder<Object> domain = (IDomainBuilder<Object>) this.apiBuilder.domain((IClass) authrClass);
                IDomainSecurityBuilder<Object> sec = domain.security();
                IAuthenticatorBuilder<Object> authrBuilder = sec.authenticator();

                applyAuthenticatorFields(reflection, authrBuilder, authrClass);
                authrBuilder.scope(a.scope());
                if (authrClass.getAnnotation(IClass.getClass(AuthenticatorAlwaysEnabled.class)) != null) {
                    authrBuilder.alwaysEnabled(true);
                }

                // Link to the configured authentication strategies
                for (Class<?> auth : a.authentications()) {
                    if (auth == null || auth == void.class) continue;
                    IClass<?> authIClass = IClass.getClass(auth);
                    Optional<IAuthenticationBuilder<IApiSecurityBuilder>> linked = apiSec.isAuthenticationAvailable(authIClass);
                    if (linked.isEmpty()) {
                        log.warn(
                                "@Authenticator on {} references @Authentication {} but it was not registered; ignoring linkage",
                                authrClass.getSimpleName(), authIClass.getSimpleName());
                        continue;
                    }
                    authrBuilder.authentication(linked.get());
                }

                // Link to the authorization domain (if declared) and configure lifetime.
                if (a.authorization() != null && a.authorization() != void.class) {
                    IClass<?> authzClass = IClass.getClass(a.authorization());
                    IDomainBuilder<Object> authzDomain =
                            (IDomainBuilder<Object>) this.apiBuilder.domain((IClass) authzClass);
                    // The authorization DSL lives on IAuthenticatorAuthenticationBuilder, but the
                    // token domain is authenticator-scoped; the scanner wires it through the
                    // internal hook so it works whether or not an authentication was linked.
                    var authzAuth = ((com.garganttua.api.core.security.authenticator.AuthenticatorBuilder<Object>) authrBuilder)
                            .tokenAuthorization(authzDomain);
                    authzAuth.lifeTime(a.authorizationLifeTime(), a.authorizationLifeTimeUnit());
                    authzAuth.refreshLifeTime(a.authorizationRefreshTokenLifeTime(),
                            a.authorizationRefreshTokenLifeTimeUnit());

                    // Apply crypto / key configuration when the annotation declares it.
                    applyAuthorizationKey(a, authrClass, authzAuth);

                    authzAuth.up();
                }

                authrBuilder.up();
                sec.up();
                registered++;
            }
        }
        if (registered > 0) {
            log.debug("Auto-detected {} @Authenticator class(es)", registered);
        }
    }

    /**
     * Wires the {@code authorizationKey*} parameters of {@link Authenticator}
     * onto the authenticator's authorization-key builder. Only the parts that
     * can be resolved cleanly from the annotation alone are applied:
     * <ul>
     *   <li>{@code authorizationKey()} (when not {@code void.class}) — resolves
     *       the key domain via {@code apiBuilder.domain(keyClass)};</li>
     *   <li>{@code authorizationKeyUsage()} — copies the enum directly;</li>
     *   <li>{@code authorizationSignatureAlgorithm()} — copies the enum directly;</li>
     *   <li>{@code authorizationKeyLifeTime()} + {@code authorizationKeyLifeTimeUnit()} —
     *       sets the configured TTL.</li>
     * </ul>
     * {@code authorizationKeyAlgorithm()} (a String name) is intentionally
     * <strong>not</strong> wired here: the DSL takes an {@link com.garganttua.core.crypto.IKeyAlgorithm}
     * instance and the framework ships no name-to-instance registry. Callers
     * who need a specific algorithm must use the fluent DSL to inject one.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void applyAuthorizationKey(Authenticator a, IClass<?> authrClass,
            com.garganttua.api.commons.context.dsl.security.IAuthenticatorAuthorizationBuilder<Object> authzAuth)
            throws ApiException {
        Class<?> keyClass = a.authorizationKey();
        if (keyClass == null || keyClass == void.class) {
            // No key domain declared — skip the whole key sub-builder so users
            // who don't enable signing/crypto see no key context wired.
            if (!a.authorizationKeyAlgorithm().isBlank()) {
                log.warn(
                        "@Authenticator on {} declares authorizationKeyAlgorithm=\"{}\" but no authorizationKey "
                                + "class — algorithm resolution is skipped (use the fluent DSL to wire an IKeyAlgorithm)",
                        authrClass.getSimpleName(), a.authorizationKeyAlgorithm());
            }
            return;
        }

        IDomainBuilder<Object> keyDomain = (IDomainBuilder<Object>) this.apiBuilder.domain((IClass) IClass.getClass(keyClass));
        var keyBuilder = authzAuth.key(keyDomain);
        keyBuilder.usage(a.authorizationKeyUsage());
        keyBuilder.signatureAlgorithm(a.authorizationSignatureAlgorithm());
        keyBuilder.lifeTime(a.authorizationKeyLifeTime(), a.authorizationKeyLifeTimeUnit());
        if (!a.authorizationKeyAlgorithm().isBlank()) {
            log.warn(
                    "@Authenticator on {} declares authorizationKeyAlgorithm=\"{}\" — ignored at scan time "
                            + "(no name-to-IKeyAlgorithm registry); wire it manually via .algorithm(...) if needed",
                    authrClass.getSimpleName(), a.authorizationKeyAlgorithm());
        }
        keyBuilder.up();
    }

    private void applyAuthenticatorFields(IReflection reflection, IAuthenticatorBuilder<Object> authrBuilder, IClass<?> authrClass)
            throws ApiException {
        Optional<IField> loginF = reflection.findFieldAnnotatedWith(authrClass, IClass.getClass(AuthenticatorLogin.class));
        if (loginF.isPresent()) authrBuilder.login(loginF.get().getName());

        Optional<IField> enabledF = reflection.findFieldAnnotatedWith(authrClass, IClass.getClass(AuthenticatorEnabled.class));
        if (enabledF.isPresent()) authrBuilder.enabled(enabledF.get().getName());

        Optional<IField> nonLockedF = reflection.findFieldAnnotatedWith(authrClass, IClass.getClass(AuthenticatorAccountNonLocked.class));
        if (nonLockedF.isPresent()) authrBuilder.accountNonLocked(nonLockedF.get().getName());

        Optional<IField> nonExpiredF = reflection.findFieldAnnotatedWith(authrClass, IClass.getClass(AuthenticatorAccountNonExpired.class));
        if (nonExpiredF.isPresent()) authrBuilder.accountNonExpired(nonExpiredF.get().getName());

        Optional<IField> credsNonExpiredF = reflection.findFieldAnnotatedWith(authrClass, IClass.getClass(AuthenticatorCredentialsNonExpired.class));
        if (credsNonExpiredF.isPresent()) authrBuilder.credentialsNonExpired(credsNonExpiredF.get().getName());

        Optional<IField> authoritiesF = reflection.findFieldAnnotatedWith(authrClass, IClass.getClass(AuthenticatorAuthorities.class));
        if (authoritiesF.isPresent()) authrBuilder.authorities(authoritiesF.get().getName());
    }

    // ─────────────────── helpers ───────────────────

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
}
