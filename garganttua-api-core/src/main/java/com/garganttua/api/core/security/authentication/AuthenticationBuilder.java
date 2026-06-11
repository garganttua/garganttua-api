package com.garganttua.api.core.security.authentication;
import com.garganttua.api.core.usecase.UseCaseBuilder;
import com.garganttua.core.reflection.annotations.Reflected;

import java.lang.annotation.Annotation;
import com.garganttua.core.reflection.IMethod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.javatuples.Pair;

import com.garganttua.api.core.security.authentication.AuthenticationMethodBinderBuilder;
import com.garganttua.api.core.security.authentication.AuthenticationContext;
import com.garganttua.api.core.security.authentication.AuthenticationDefinition;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.IUseCaseBuilder;
import com.garganttua.api.commons.context.dsl.security.IApiSecurityBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticationBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticationMethodBinderBuilder;
import com.garganttua.api.commons.security.authentication.IAuthentication;
import com.garganttua.api.commons.security.context.IAuthenticationContext;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

@Reflected
public class AuthenticationBuilder<E> extends AbstractAutomaticLinkedBuilder<IAuthenticationBuilder<E>, E, IAuthenticationContext> implements IAuthenticationBuilder<E> {

    private ISupplierBuilder<?, ? extends ISupplier<?>> supplier;
    private IAuthenticationMethodBinderBuilder<?> authenticate;
    private List<Pair<IClass<? extends Annotation>, IClass<?>>> fieldAnnotations = new ArrayList<>();
    private IAuthenticationMethodBinderBuilder<?> applySecurityOnEntity;
    private Map<String, IUseCaseBuilder<?, ?, ?>> useCases = new HashMap<>();

    protected AuthenticationBuilder(E link) {
        super(link);
    }

    public AuthenticationBuilder(E link, ISupplierBuilder<?, ? extends ISupplier<?>> supplier) {
        super(link);
        this.supplier = Objects.requireNonNull(supplier, "Supplier cannot be null");
    }

    @Override
    public IAuthenticationMethodBinderBuilder<?> authenticate(String methodName) throws ApiException {
        Objects.requireNonNull(methodName, "Method name cannot be null");
        this.authenticate = new AuthenticationMethodBinderBuilder<>(this, this.supplier, methodName);
        return this.authenticate;
    }

    @Override
    public IAuthenticationMethodBinderBuilder<?> authenticate(IMethod method) throws ApiException {
        Objects.requireNonNull(method, "Method cannot be null");
        this.authenticate = new AuthenticationMethodBinderBuilder<>(this, this.supplier, method.getName());
        return this.authenticate;
    }

    @Override
    public IAuthenticationMethodBinderBuilder<?> authenticate(ObjectAddress methodAddress) throws ApiException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");
        String methodName = methodAddress.getElement(methodAddress.length() - 1);
        this.authenticate = new AuthenticationMethodBinderBuilder<>(this, this.supplier, methodName);
        return this.authenticate;
    }

    @Override
    public IAuthenticationBuilder<E> entityMustHaveFieldOfTypeAnnotatedWith(IClass<? extends Annotation> annotation,
            IClass<?> fieldType) throws ApiException {
        Objects.requireNonNull(annotation, "Annotation cannot be null");
        Objects.requireNonNull(fieldType, "Field type cannot be null");
        this.fieldAnnotations.add(new Pair<>(annotation, fieldType));
        return this;
    }

    @Override
    public IAuthenticationMethodBinderBuilder<?> applySecurityOnEntity(String methodName) throws ApiException {
        Objects.requireNonNull(methodName, "Method name cannot be null");
        return bindApplySecurityOnEntity(methodName);
    }

    @Override
    public IAuthenticationMethodBinderBuilder<?> applySecurityOnEntity(IMethod method) throws ApiException {
        Objects.requireNonNull(method, "Method cannot be null");
        return bindApplySecurityOnEntity(method.getName());
    }

    @Override
    public IAuthenticationMethodBinderBuilder<?> applySecurityOnEntity(ObjectAddress methodAddress) throws ApiException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");
        return bindApplySecurityOnEntity(methodAddress.getElement(methodAddress.length() - 1));
    }

    /**
     * Binds the custom {@code applySecurityOnEntity} method and returns the binder builder.
     * The method is completely free: NO parameter is imposed — the caller declares whatever
     * it needs via {@code .withParam(i, supplier)} (e.g. {@link SecuredEntitySupplierBuilder}
     * for the entity being written) and returns with {@code .up()}.
     */
    private IAuthenticationMethodBinderBuilder<?> bindApplySecurityOnEntity(String methodName) throws ApiException {
        this.applySecurityOnEntity = new AuthenticationMethodBinderBuilder<>(this, this.supplier, methodName);
        return this.applySecurityOnEntity;
    }

    @Override
    public IUseCaseBuilder<?, ?, ?> useCase(String methodName) throws ApiException {
        Objects.requireNonNull(methodName, "Method name cannot be null");
        return this.useCases.computeIfAbsent(methodName,
                name -> new UseCaseBuilder<>(name, (IDomainBuilder<?>) null));
    }

    @Override
    public IUseCaseBuilder<?, ?, ?> useCase(IMethod method) throws ApiException {
        Objects.requireNonNull(method, "Method cannot be null");
        return this.useCases.computeIfAbsent(method.getName(),
                name -> new UseCaseBuilder<>(name, (IDomainBuilder<?>) null));
    }

    @Override
    public IUseCaseBuilder<?, ?, ?> useCase(ObjectAddress methodAddress) throws ApiException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");
        String name = methodAddress.getElement(methodAddress.length() - 1);
        return this.useCases.computeIfAbsent(name,
                n -> new UseCaseBuilder<>(n, (IDomainBuilder<?>) null));
    }

    @Override
    protected synchronized IAuthenticationContext doBuild() throws ApiException {

        // Build method binders
        IMethodBinder<?> authenticateBinder = this.authenticate != null ? this.authenticate.build() : null;

        IMethodBinder<?> applySecurityBinder = this.applySecurityOnEntity != null ? this.applySecurityOnEntity.build() : null;

        AuthenticationDefinition definition = new AuthenticationDefinition(
                this.supplier,
                authenticateBinder,
                this.fieldAnnotations,
                applySecurityBinder,
                this.useCases.values());
        return new AuthenticationContext(definition);
    }

    @Override
    protected void doAutoDetection() throws ApiException {
        // Auto-detection will be implemented when Spring integration is ported
    }

}
