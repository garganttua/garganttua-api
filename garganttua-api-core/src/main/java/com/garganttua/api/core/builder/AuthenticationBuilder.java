package com.garganttua.api.core.builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.garganttua.api.spec.context.IAuthenticationContext;
import com.garganttua.api.spec.context.dsl.IUseCaseBuilder;
import com.garganttua.api.spec.context.dsl.security.IApiContextSecurityBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthenticationBuilder;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;

public class AuthenticationBuilder extends AbstractAutomaticLinkedBuilder<IAuthenticationBuilder, IApiContextSecurityBuilder, IAuthenticationContext>  implements IAuthenticationBuilder {

    protected AuthenticationBuilder(IApiContextSecurityBuilder link) {
        super(link);
        //TODO Auto-generated constructor stub
    }

    @Override
    public IAuthenticationBuilder findPrincipal(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findPrincipal'");
    }

    @Override
    public IAuthenticationBuilder authenticate(String methodName) throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authenticate'");
    }

    @Override
    public IAuthenticationBuilder authenticate(Method method) throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authenticate'");
    }

    @Override
    public IAuthenticationBuilder authenticate(ObjectAddress methodAddress) throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authenticate'");
    }

    @Override
    public IAuthenticationBuilder entityMustHaveFieldOfTypeAnnotatedWith(IClass<? extends Annotation> annotation,
            IClass<?> fieldType) throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'entityMustHaveFieldOfTypeAnnotatedWith'");
    }

    @Override
    public IAuthenticationBuilder applySecurityOnEntity(String methodName) throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'applySecurityOnEntity'");
    }

    @Override
    public IAuthenticationBuilder applySecurityOnEntity(Method method) throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'applySecurityOnEntity'");
    }

    @Override
    public IAuthenticationBuilder applySecurityOnEntity(ObjectAddress methodAddress) throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'applySecurityOnEntity'");
    }

    @Override
    public IUseCaseBuilder<?, ?, ?> useCase(String methodName) throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'useCase'");
    }

    @Override
    public IUseCaseBuilder<?, ?, ?> useCase(Method method) throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'useCase'");
    }

    @Override
    public IUseCaseBuilder<?, ?, ?> useCase(ObjectAddress methodAddress) throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'useCase'");
    }

    @Override
    protected synchronized IAuthenticationContext doBuild() throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doBuild'");
    }

    @Override
    protected void doAutoDetection() throws ApiException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doAutoDetection'");
    }

   /*  private Boolean findPrincipal;
    private @Nonnull ISupplierBuilder<?, ? extends ISupplier<?>> supplier;
    private IAuthenticationMethodBinderBuilder<Boolean> authenticate;
    private List<Pair<Class<? extends Annotation>, Class<?>>> fieldAnnotations = new ArrayList<>();
    private IAuthenticationMethodBinderBuilder<Void> applySecurityOnEntity;
    private Map<String, IUseCaseBuilder<?, IAuthenticationBuilder>> useCases = new HashMap<>();

    public AuthenticationBuilder(IContextSecurityBuilder contextSecurityBuilder, ISupplierBuilder<?, ? extends ISupplier<?>> supplier) {
        super(contextSecurityBuilder);
        this.supplier = Objects.requireNonNull(supplier, "Supplier cannot be null");
    }

    @Override
    protected IAuthenticationContext doBuild() {
        return new AuthenticationContext(
                this.findPrincipal,
                this.supplier,
                this.authenticate,
                this.fieldAnnotations,
                this.applySecurityOnEntity,
                this.useCases.values());
    }

    @Override
    public IAuthenticationBuilder findPrincipal(boolean b) {
        this.findPrincipal = Objects.requireNonNull(b, "Find principal cannot be null");
        return this;
    }

    @Override
    public IAuthenticationBuilder authenticate(String methodName) throws ApiException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        if (this.findPrincipal)
            this.authenticate = new AuthenticationMethodBinderBuilder<>(this,
                    this.supplier)
                    .method(methodName, Boolean.class, Byte[].class, Object.class)
                    .withParam(0, credentials())
                    .withParam(1, principal());
        else
            this.authenticate = new AuthenticationMethodBinderBuilder<>(this,
                    this.supplier)
                    .method(methodName, Boolean.class, Byte[].class)
                    .withParam(0, credentials());

        return this;
    }

    @Override
    public IAuthenticationBuilder authenticate(Method method) throws ApiException {
        Objects.requireNonNull(method, "Method cannot be null");

        if (this.findPrincipal)
            this.authenticate = new AuthenticationMethodBinderBuilder<>(this,
                    this.supplier)
                    .method(method, Boolean.class, Byte[].class, Object.class)
                    .withParam(0, credentials())
                    .withParam(1, principal());
        else
            this.authenticate = new AuthenticationMethodBinderBuilder<>(this,
                    this.supplier)
                    .method(method, Boolean.class, Byte[].class)
                    .withParam(0, credentials());

        return this;
    }

    @Override
    public IAuthenticationBuilder authenticate(ObjectAddress methodAddress) throws ApiException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        if (this.findPrincipal)
            this.authenticate = new AuthenticationMethodBinderBuilder<>(this,
                    this.supplier)
                    .method(methodAddress, Boolean.class, Byte[].class, Object.class)
                    .withParam(0, credentials())
                    .withParam(1, principal());
        else
            this.authenticate = new AuthenticationMethodBinderBuilder<>(this,
                    this.supplier)
                    .method(methodAddress, Boolean.class, Byte[].class)
                    .withParam(0, credentials());

        return this;
    }

    @Override
    public IAuthenticationBuilder applySecurityOnEntity(String methodName) throws ApiException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        this.applySecurityOnEntity = new AuthenticationMethodBinderBuilder<>(this,
                this.supplier)
                .method(methodName, null, Object.class)
                .withParam(0, entity());

        return this;
    }

    @Override
    public IAuthenticationBuilder applySecurityOnEntity(Method method) throws ApiException {
        Objects.requireNonNull(method, "Method cannot be null");

        this.applySecurityOnEntity = new AuthenticationMethodBinderBuilder<>(this,
                this.supplier)
                .method(method, null, Object.class)
                .withParam(0, entity());

        return this;
    }

    @Override
    public IAuthenticationBuilder applySecurityOnEntity(ObjectAddress methodAddress) throws ApiException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        this.applySecurityOnEntity = new AuthenticationMethodBinderBuilder<>(this,
                this.supplier)
                .method(methodAddress, null, Object.class)
                .withParam(0, entity());

        return this;
    }

    @Override
    public IUseCaseBuilder<IAuthenticationBuilder> useCase(String methodName) throws ApiException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        IUseCaseBuilder<IAuthenticationBuilder,IAuthenticationBuilder> useCaseBuilder = this.useCases.get(methodName);

        if (useCaseBuilder == null) {
            useCaseBuilder = new UseCaseBuilder<IAuthenticationBuilder,IAuthenticationBuilder>(methodName, this);
            this.useCases.put(methodName, useCaseBuilder);
        }

        useCaseBuilder.bind(this.supplier).method(methodName);

        return useCaseBuilder;
    }

    @Override
    public IUseCaseBuilder<IAuthenticationBuilder> useCase(Method method) throws ApiException {
        Objects.requireNonNull(method, "Method cannot be null");
        String methodName = method.getName();

        IUseCaseBuilder<IAuthenticationBuilder> useCaseBuilder = this.useCases.get(methodName);

        if (useCaseBuilder == null) {
            useCaseBuilder = new UseCaseBuilder<IAuthenticationBuilder>(methodName, this);
            this.useCases.put(methodName, useCaseBuilder);
        }

        useCaseBuilder.bind(this.supplier).method(method);

        return useCaseBuilder;
    }

    @Override
    public IUseCaseBuilder<IAuthenticationBuilder> useCase(ObjectAddress methodAddress) throws ApiException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");
        String methodName = methodAddress.getElement(methodAddress.length() - 1);

        IUseCaseBuilder<IAuthenticationBuilder> useCaseBuilder = this.useCases.get(methodName);

        if (useCaseBuilder == null) {
            useCaseBuilder = new UseCaseBuilder<IAuthenticationBuilder>(methodName, this);
            this.useCases.put(methodName, useCaseBuilder);
        }

        useCaseBuilder.bind(this.supplier).method(methodAddress);

        return useCaseBuilder;
    }

    @Override
    public IAuthenticationBuilder entityMustHaveFieldOfTypeAnnotatedWith(IClass<? extends Annotation> annotation,
            IClass<?> fieldType) throws ApiException {
        Objects.requireNonNull(annotation, "Annotation cannot be null");
        Objects.requireNonNull(fieldType, "Field type cannot be null");
        this.fieldAnnotations.add(new Pair<Class<? extends Annotation>, Class<?>>(annotation, fieldType));
        return this;
    } */

}
