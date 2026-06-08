package com.garganttua.api.core.security.key;
import com.garganttua.api.core.entity.EntityBuilder;
import com.garganttua.core.reflection.annotations.Reflected;

import java.util.Objects;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IDomainKeyContext;
import com.garganttua.api.commons.context.dsl.IDomainKeyBuilder;
import com.garganttua.api.commons.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.api.core.security.key.DomainKeyContext;
import com.garganttua.api.core.security.key.DomainKeyDefinition;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IField;
import com.garganttua.core.reflection.IReflectionProvider;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.fields.FieldResolver;

/**
 * DSL impl that records the key-entity field layout — eleven addresses
 * mirroring {@code com.garganttua.core.crypto.IKeyRealm}'s public surface —
 * and produces a {@link DomainKeyDefinition} consumed by the runtime
 * auto-create / lookup path. Each setter resolves the supplied
 * String / IField / ObjectAddress against the parent domain's entity
 * class — exactly the pattern used by {@link EntityBuilder}.
 */
@Reflected
public class DomainKeyBuilder<E>
        extends AbstractAutomaticLinkedBuilder<IDomainKeyBuilder<E>, IDomainSecurityBuilder<E>, IDomainKeyContext>
        implements IDomainKeyBuilder<E> {

    // Reflection provider is whatever the user installed via IClass.setReflection().
    // Resolved lazily per call so the framework never picks an implementation.
    private static IReflectionProvider provider() {
        return IClass.getReflection();
    }

    private final IClass<?> entityClass;
    private ObjectAddress name;
    private ObjectAddress keyAlgorithm;
    private ObjectAddress signatureAlgorithm;
    private ObjectAddress keyForSigning;
    private ObjectAddress keyForSignatureVerification;
    private ObjectAddress keyForEncryption;
    private ObjectAddress keyForDecryption;
    private ObjectAddress expiration;
    private ObjectAddress revoked;
    private ObjectAddress version;
    private ObjectAddress rotate;

    public DomainKeyBuilder(IDomainSecurityBuilder<E> securityBuilder, IClass<?> entityClass) {
        super(securityBuilder);
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    // ───── name ─────

    @Override
    public IDomainKeyBuilder<E> name(String fieldName) throws ApiException {
        this.name = resolve(fieldName, String.class);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> name(IField field) throws ApiException {
        this.name = resolve(field, String.class);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> name(ObjectAddress fieldAddress) throws ApiException {
        this.name = resolve(fieldAddress, String.class);
        return this;
    }

    // ───── keyAlgorithm ─────

    @Override
    public IDomainKeyBuilder<E> keyAlgorithm(String fieldName) throws ApiException {
        this.keyAlgorithm = resolve(fieldName, String.class);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> keyAlgorithm(IField field) throws ApiException {
        this.keyAlgorithm = resolve(field, String.class);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> keyAlgorithm(ObjectAddress fieldAddress) throws ApiException {
        this.keyAlgorithm = resolve(fieldAddress, String.class);
        return this;
    }

    // ───── signatureAlgorithm ─────

    @Override
    public IDomainKeyBuilder<E> signatureAlgorithm(String fieldName) throws ApiException {
        this.signatureAlgorithm = resolve(fieldName, String.class);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> signatureAlgorithm(IField field) throws ApiException {
        this.signatureAlgorithm = resolve(field, String.class);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> signatureAlgorithm(ObjectAddress fieldAddress) throws ApiException {
        this.signatureAlgorithm = resolve(fieldAddress, String.class);
        return this;
    }

    // ───── keyForSigning ─────

    @Override
    public IDomainKeyBuilder<E> keyForSigning(String fieldName) throws ApiException {
        this.keyForSigning = resolve(fieldName, com.garganttua.core.crypto.IKey.class);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> keyForSigning(IField field) throws ApiException {
        this.keyForSigning = resolve(field, com.garganttua.core.crypto.IKey.class);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> keyForSigning(ObjectAddress fieldAddress) throws ApiException {
        this.keyForSigning = resolve(fieldAddress, com.garganttua.core.crypto.IKey.class);
        return this;
    }

    // ───── keyForSignatureVerification ─────

    @Override
    public IDomainKeyBuilder<E> keyForSignatureVerification(String fieldName) throws ApiException {
        this.keyForSignatureVerification = resolve(fieldName, com.garganttua.core.crypto.IKey.class);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> keyForSignatureVerification(IField field) throws ApiException {
        this.keyForSignatureVerification = resolve(field, com.garganttua.core.crypto.IKey.class);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> keyForSignatureVerification(ObjectAddress fieldAddress) throws ApiException {
        this.keyForSignatureVerification = resolve(fieldAddress, com.garganttua.core.crypto.IKey.class);
        return this;
    }

    // ───── keyForEncryption ─────

    @Override
    public IDomainKeyBuilder<E> keyForEncryption(String fieldName) throws ApiException {
        this.keyForEncryption = resolve(fieldName, com.garganttua.core.crypto.IKey.class);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> keyForEncryption(IField field) throws ApiException {
        this.keyForEncryption = resolve(field, com.garganttua.core.crypto.IKey.class);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> keyForEncryption(ObjectAddress fieldAddress) throws ApiException {
        this.keyForEncryption = resolve(fieldAddress, com.garganttua.core.crypto.IKey.class);
        return this;
    }

    // ───── keyForDecryption ─────

    @Override
    public IDomainKeyBuilder<E> keyForDecryption(String fieldName) throws ApiException {
        this.keyForDecryption = resolve(fieldName, com.garganttua.core.crypto.IKey.class);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> keyForDecryption(IField field) throws ApiException {
        this.keyForDecryption = resolve(field, com.garganttua.core.crypto.IKey.class);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> keyForDecryption(ObjectAddress fieldAddress) throws ApiException {
        this.keyForDecryption = resolve(fieldAddress, com.garganttua.core.crypto.IKey.class);
        return this;
    }

    // ───── expiration ─────

    @Override
    public IDomainKeyBuilder<E> expiration(String fieldName) throws ApiException {
        // Type intentionally not constrained: Date / Instant / Long / LocalDateTime are all accepted.
        this.expiration = resolve(fieldName, null);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> expiration(IField field) throws ApiException {
        this.expiration = resolve(field, null);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> expiration(ObjectAddress fieldAddress) throws ApiException {
        this.expiration = resolve(fieldAddress, null);
        return this;
    }

    // ───── revoked ─────

    @Override
    public IDomainKeyBuilder<E> revoked(String fieldName) throws ApiException {
        // boolean | Boolean both accepted.
        this.revoked = resolve(fieldName, null);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> revoked(IField field) throws ApiException {
        this.revoked = resolve(field, null);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> revoked(ObjectAddress fieldAddress) throws ApiException {
        this.revoked = resolve(fieldAddress, null);
        return this;
    }

    // ───── version ─────

    @Override
    public IDomainKeyBuilder<E> version(String fieldName) throws ApiException {
        // int | Integer both accepted.
        this.version = resolve(fieldName, null);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> version(IField field) throws ApiException {
        this.version = resolve(field, null);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> version(ObjectAddress fieldAddress) throws ApiException {
        this.version = resolve(fieldAddress, null);
        return this;
    }

    // ───── rotate ─────

    @Override
    public IDomainKeyBuilder<E> rotate(String fieldName) throws ApiException {
        // Date / Instant / Long all accepted — same shape as expiration.
        this.rotate = resolve(fieldName, null);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> rotate(IField field) throws ApiException {
        this.rotate = resolve(field, null);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> rotate(ObjectAddress fieldAddress) throws ApiException {
        this.rotate = resolve(fieldAddress, null);
        return this;
    }

    // ───── build / auto-detect ─────

    @Override
    protected synchronized IDomainKeyContext doBuild() throws ApiException {
        return new DomainKeyContext(new DomainKeyDefinition(
                this.name,
                this.keyAlgorithm,
                this.signatureAlgorithm,
                this.keyForSigning,
                this.keyForSignatureVerification,
                this.keyForEncryption,
                this.keyForDecryption,
                this.expiration,
                this.revoked,
                this.version,
                this.rotate));
    }

    @Override
    protected void doAutoDetection() throws ApiException {
        // Field resolution is annotation-driven via EntityAnnotationScanner.
    }

    // ───── resolution helpers ─────

    private ObjectAddress resolve(String fieldName, Class<?> expectedType) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        return FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName,
                expectedType == null ? null : IClass.getClass(expectedType)).address();
    }

    private ObjectAddress resolve(IField field, Class<?> expectedType) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");
        return FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(),
                expectedType == null ? null : IClass.getClass(expectedType)).address();
    }

    private ObjectAddress resolve(ObjectAddress fieldAddress, Class<?> expectedType) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");
        return FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress,
                expectedType == null ? null : IClass.getClass(expectedType)).address();
    }
}
