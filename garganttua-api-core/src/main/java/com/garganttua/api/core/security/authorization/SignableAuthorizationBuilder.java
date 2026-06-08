package com.garganttua.api.core.security.authorization;
import com.garganttua.core.reflection.annotations.Reflected;

import com.garganttua.core.reflection.IField;
import com.garganttua.core.reflection.IMethod;
import java.util.Objects;

import com.garganttua.api.commons.context.dsl.security.IAuthorizationBuilder;
import com.garganttua.api.commons.context.dsl.security.ISignableAuthorizationBuilder;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IReflectionProvider;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.fields.FieldResolver;

@Reflected
public class SignableAuthorizationBuilder<E> extends AbstractAutomaticLinkedBuilder<ISignableAuthorizationBuilder<E>, IAuthorizationBuilder<E>, Object> implements ISignableAuthorizationBuilder<E> {

    // Reflection provider is whatever the user installed via IClass.setReflection().
    // Resolved lazily per call so the framework never picks an implementation.
    private static IReflectionProvider provider() {
        return IClass.getReflection();
    }

    private final IClass<?> entityClass;
    private ObjectAddress signatureField;
    private ObjectAddress getDataToSignMethod;

    public SignableAuthorizationBuilder(IAuthorizationBuilder<E> authorizationBuilder,
            IClass<?> entityClass) {
        super(authorizationBuilder);
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    @Override
    protected void doAutoDetection() {
    }

    @Override
    public ISignableAuthorizationBuilder<E> signature(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        this.signatureField = FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, null).address();
        return this;
    }

    @Override
    public ISignableAuthorizationBuilder<E> signature(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");
        this.signatureField = FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), null).address();
        return this;
    }

    @Override
    public ISignableAuthorizationBuilder<E> signature(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");
        this.signatureField = FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, null).address();
        return this;
    }

    @Override
    public ISignableAuthorizationBuilder<E> getDataToSign(String methodName) throws ApiException {
        Objects.requireNonNull(methodName, "Method name cannot be null");
        this.getDataToSignMethod = new ObjectAddress(methodName);
        return this;
    }

    @Override
    public ISignableAuthorizationBuilder<E> getDataToSign(IMethod method) throws ApiException {
        Objects.requireNonNull(method, "Method cannot be null");
        this.getDataToSignMethod = new ObjectAddress(method.getName());
        return this;
    }

    @Override
    public ISignableAuthorizationBuilder<E> getDataToSign(ObjectAddress methodAddress) throws ApiException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");
        this.getDataToSignMethod = methodAddress;
        return this;
    }

    ObjectAddress getSignatureField() {
        return this.signatureField;
    }

    ObjectAddress getGetDataToSignMethod() {
        return this.getDataToSignMethod;
    }

    @Override
    protected synchronized Object doBuild() throws ApiException {
        // The signable data is collected by the parent AuthorizationBuilder
        // via getSignatureField() / getGetDataToSignMethod()
        return null;
    }
}
