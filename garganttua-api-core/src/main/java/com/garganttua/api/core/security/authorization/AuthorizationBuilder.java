package com.garganttua.api.core.security.authorization;
import com.garganttua.core.reflection.annotations.Reflected;

import com.garganttua.core.reflection.IField;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import com.garganttua.api.core.security.authorization.AuthorizationContext;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.security.context.IAuthorizationContext;
import com.garganttua.api.commons.context.dsl.security.IAuthorizationBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthorizationMethodBinderBuilder;
import com.garganttua.api.commons.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.api.commons.context.dsl.security.IRefreshableAuthorizationBuilder;
import com.garganttua.api.commons.context.dsl.security.ISignableAuthorizationBuilder;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IMethod;
import com.garganttua.core.reflection.IReflectionProvider;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.fields.FieldResolver;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;
import com.garganttua.api.core.security.authentication.CallerSupplierBuilder;

@Reflected
public class AuthorizationBuilder<E>
        extends AbstractAutomaticLinkedBuilder<IAuthorizationBuilder<E>, IDomainSecurityBuilder<E>, IAuthorizationContext>
        implements IAuthorizationBuilder<E> {

    // Reflection provider is whatever the user installed via IClass.setReflection().
    // Resolved lazily per call so the framework never picks an implementation.
    private static IReflectionProvider provider() {
        return IClass.getReflection();
    }

    private IClass<?> entityClass;
    private ObjectAddress type;
    private ObjectAddress revoked;
    private ObjectAddress creation;
    private ObjectAddress expiration;
    private ObjectAddress authorities;
    private ObjectAddress signedBy;
    private ObjectAddress encodeMethod;
    private ObjectAddress decodeMethod;
    private IAuthorizationMethodBinderBuilder<E> toByteArray;
    private IAuthorizationMethodBinderBuilder<E> validate;
    private IAuthorizationMethodBinderBuilder<E> validateAgainst;
    private IAuthorizationMethodBinderBuilder<E> fromByteArray;
    private ISignableAuthorizationBuilder<E> signable;
    private IRefreshableAuthorizationBuilder<E> refreshable;
    private boolean storable = false;
    private IAuthorizationMethodBinderBuilder<E> reconcile;

    public AuthorizationBuilder(IDomainSecurityBuilder<E> domainBuilder, IClass<?> entityClass) {
        super(domainBuilder);
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    @Override
    public IAuthorizationBuilder<E> type(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.type = FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> type(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.type = FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> type(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.type = FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> authorities(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.authorities = FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, IClass.getClass(List.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> authorities(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.authorities = FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), IClass.getClass(List.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> authorities(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.authorities = FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, IClass.getClass(List.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> expirable(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.expiration = FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, IClass.getClass(Instant.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> expirable(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");

        this.expiration = FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), IClass.getClass(Instant.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> expirable(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.expiration = FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, IClass.getClass(Instant.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> revokable(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        this.storable = true;

        this.revoked = FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> revokable(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field name cannot be null");
        this.storable = true;

        this.revoked = FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> revokable(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");
        this.storable = true;

        this.revoked = FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> creation(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.creation = FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, IClass.getClass(Instant.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> creation(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.creation = FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), IClass.getClass(Instant.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> creation(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.creation = FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, IClass.getClass(Instant.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> signedBy(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");

        this.signedBy = FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> signedBy(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.signedBy = FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> signedBy(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.signedBy = FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IAuthorizationBuilder<E> encode(IMethod method) throws ApiException {
        Objects.requireNonNull(method, "Method cannot be null");
        this.encodeMethod = new ObjectAddress(method.getName());
        return this;
    }

    @Override
    public IAuthorizationBuilder<E> encode(String methodName) throws ApiException {
        Objects.requireNonNull(methodName, "Method name cannot be null");
        this.encodeMethod = new ObjectAddress(methodName);
        return this;
    }

    @Override
    public IAuthorizationBuilder<E> encode(ObjectAddress methodAddress) throws ApiException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");
        this.encodeMethod = methodAddress;
        return this;
    }

    @Override
    public IAuthorizationBuilder<E> decode(IMethod method) throws ApiException {
        Objects.requireNonNull(method, "Method cannot be null");
        this.decodeMethod = new ObjectAddress(method.getName());
        return this;
    }

    @Override
    public IAuthorizationBuilder<E> decode(String methodName) throws ApiException {
        Objects.requireNonNull(methodName, "Method name cannot be null");
        this.decodeMethod = new ObjectAddress(methodName);
        return this;
    }

    @Override
    public IAuthorizationBuilder<E> decode(ObjectAddress methodAddress) throws ApiException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");
        this.decodeMethod = methodAddress;
        return this;
    }

    @Override
    public ISignableAuthorizationBuilder<E> signable() {
        if (this.signable == null) {
            this.signable = new SignableAuthorizationBuilder<>(this, this.entityClass);
        }
        return this.signable;
    }

    @Override
    public IRefreshableAuthorizationBuilder<E> refreshable() {
        if (this.refreshable == null) {
            this.refreshable = new RefreshableAuthorizationBuilder<>(this, this.entityClass);
        }
        return this.refreshable;
    }

    @Override
    public IAuthorizationBuilder<E> storable(boolean b) {
        this.storable = b;
        return this;
    }

    @Override
    public Boolean isStorable() {
        return this.storable;
    }

    @Override
    public Boolean isRefreshable() {
        return this.refreshable != null;
    }

    @Override
    public IAuthorizationBuilder<E> reconcile(ISupplierBuilder<?, ? extends ISupplier<?>> supplier, String methodName)
            throws ApiException {
        Objects.requireNonNull(supplier, "reconcile supplier cannot be null");
        Objects.requireNonNull(methodName, "reconcile method name cannot be null");
        // The custom reconcile method is ICaller method(IAuthentication authentication, ICaller caller):
        // its two params are framework-fixed, auto-wired to the suppliers that read the verified
        // authentication and the protocol caller from the runtime context.
        this.reconcile = new AuthorizationMethodBinderBuilder<E>(this, supplier, methodName)
                .withParam(0, new AuthenticationSupplierBuilder())
                .withParam(1, new CallerSupplierBuilder());
        return this;
    }

    @Override
    protected synchronized IAuthorizationContext doBuild() throws ApiException {
        // Collect signable fields if configured
        ObjectAddress signatureField = null;
        ObjectAddress getDataToSignMethod = null;
        if (this.signable != null) {
            SignableAuthorizationBuilder<E> sb = (SignableAuthorizationBuilder<E>) this.signable;
            sb.build();
            signatureField = sb.getSignatureField();
            getDataToSignMethod = sb.getGetDataToSignMethod();
        }

        // Collect refreshable fields if configured. Encode/decode methods are
        // declared inside the refreshable builder but are conceptually for the
        // whole authorization (used to produce a transport-friendly form);
        // we plumb them through so the runtime expressions can invoke them.
        // Encode/decode declared on the plain authorization take precedence; a
        // refreshable token may still declare them on its own builder (legacy path),
        // used only as a fallback. A token need NOT be refreshable to be encoded.
        ObjectAddress refreshExpiration = null;
        ObjectAddress refreshRevoked = null;
        ObjectAddress encodeMethod = this.encodeMethod;
        ObjectAddress decodeMethod = this.decodeMethod;
        if (this.refreshable != null) {
            RefreshableAuthorizationBuilder<E> rb = (RefreshableAuthorizationBuilder<E>) this.refreshable;
            rb.build();
            refreshExpiration = rb.getExpiration();
            refreshRevoked = rb.getRevoked();
            if (encodeMethod == null) encodeMethod = rb.getEncodeMethod();
            if (decodeMethod == null) decodeMethod = rb.getDecodeMethod();
        }

        IMethodBinder<?> reconcileBinder = this.reconcile != null ? this.reconcile.build() : null;

        return new AuthorizationContext(
                this.type, this.authorities, this.expiration, this.creation, this.revoked,
                this.storable, this.signable != null, this.refreshable != null,
                signatureField, getDataToSignMethod,
                refreshExpiration, refreshRevoked,
                encodeMethod, decodeMethod, this.signedBy, reconcileBinder);
    }

    @Override
    protected void doAutoDetection() throws ApiException {

    }

}
