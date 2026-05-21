package com.garganttua.api.core.builder;

import java.util.Objects;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IDomainKeyContext;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.IDomainKeyBuilder;
import com.garganttua.api.core.context.DomainKeyContext;
import com.garganttua.api.core.definition.DomainKeyDefinition;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IField;
import com.garganttua.core.reflection.IReflectionProvider;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.fields.FieldResolver;

/**
 * DSL impl that records the key-entity field layout (realmName,
 * algorithm, signatureAlgorithm, publicMaterial, privateMaterial,
 * expiration, revoked) and produces a {@link DomainKeyDefinition}
 * consumed by the runtime auto-create / lookup path.
 *
 * <p>Each setter resolves the supplied String / IField / ObjectAddress
 * against the parent domain's entity class — exactly the pattern used
 * by {@link EntityBuilder}.
 */
public class DomainKeyBuilder<E>
        extends AbstractAutomaticLinkedBuilder<IDomainKeyBuilder<E>, IDomainBuilder<E>, IDomainKeyContext>
        implements IDomainKeyBuilder<E> {

    // Reflection provider is whatever the user installed via IClass.setReflection().
    // Resolved lazily per call so the framework never picks an implementation.
    private static IReflectionProvider provider() {
        return IClass.getReflection();
    }

    private final IClass<?> entityClass;
    private ObjectAddress realmName;
    private ObjectAddress algorithm;
    private ObjectAddress signatureAlgorithm;
    private ObjectAddress publicMaterial;
    private ObjectAddress privateMaterial;
    private ObjectAddress expiration;
    private ObjectAddress revoked;

    public DomainKeyBuilder(IDomainBuilder<E> domainBuilder, IClass<?> entityClass) {
        super(domainBuilder);
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    // ───── realmName ─────

    @Override
    public IDomainKeyBuilder<E> realmName(String fieldName) throws ApiException {
        this.realmName = resolve(fieldName, String.class);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> realmName(IField field) throws ApiException {
        this.realmName = resolve(field, String.class);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> realmName(ObjectAddress fieldAddress) throws ApiException {
        this.realmName = resolve(fieldAddress, String.class);
        return this;
    }

    // ───── algorithm ─────

    @Override
    public IDomainKeyBuilder<E> algorithm(String fieldName) throws ApiException {
        this.algorithm = resolve(fieldName, String.class);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> algorithm(IField field) throws ApiException {
        this.algorithm = resolve(field, String.class);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> algorithm(ObjectAddress fieldAddress) throws ApiException {
        this.algorithm = resolve(fieldAddress, String.class);
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

    // ───── publicMaterial ─────

    @Override
    public IDomainKeyBuilder<E> publicMaterial(String fieldName) throws ApiException {
        this.publicMaterial = resolve(fieldName, byte[].class);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> publicMaterial(IField field) throws ApiException {
        this.publicMaterial = resolve(field, byte[].class);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> publicMaterial(ObjectAddress fieldAddress) throws ApiException {
        this.publicMaterial = resolve(fieldAddress, byte[].class);
        return this;
    }

    // ───── privateMaterial ─────

    @Override
    public IDomainKeyBuilder<E> privateMaterial(String fieldName) throws ApiException {
        this.privateMaterial = resolve(fieldName, byte[].class);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> privateMaterial(IField field) throws ApiException {
        this.privateMaterial = resolve(field, byte[].class);
        return this;
    }

    @Override
    public IDomainKeyBuilder<E> privateMaterial(ObjectAddress fieldAddress) throws ApiException {
        this.privateMaterial = resolve(fieldAddress, byte[].class);
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

    // ───── build / auto-detect ─────

    @Override
    protected synchronized IDomainKeyContext doBuild() throws ApiException {
        return new DomainKeyContext(new DomainKeyDefinition(
                this.realmName,
                this.algorithm,
                this.signatureAlgorithm,
                this.publicMaterial,
                this.privateMaterial,
                this.expiration,
                this.revoked));
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
