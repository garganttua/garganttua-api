package com.garganttua.api.commons.context.dsl;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IDomainKeyContext;
import com.garganttua.api.commons.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IField;
import com.garganttua.core.reflection.ObjectAddress;

/**
 * Sub-builder declaring a domain as a key domain. The method surface is
 * derived from {@code com.garganttua.core.crypto.IKeyRealm} so that the
 * user can pick any entity class — {@code KeyRealm} (core's reference
 * implementation, ready to use as-is) or a custom POJO — and bind its
 * fields to the corresponding {@code IKeyRealm} members.
 *
 * <p>Each setter binds the address of the entity field that holds the
 * matching piece of material or metadata. The field-level annotations
 * ({@code @KeyName}, {@code @KeyAlgorithm}, {@code @KeySigningMaterial}…)
 * are read by the entity-role scanner and call these setters automatically
 * when an entity is marked with {@code @Key}.
 *
 * @param <E> the key entity type
 */
public interface IDomainKeyBuilder<E> extends
        IAutomaticLinkedBuilder<IDomainKeyBuilder<E>, IDomainSecurityBuilder<E>, IDomainKeyContext> {

    // ───── IKeyRealm.getName() ─────

    IDomainKeyBuilder<E> name(String fieldName) throws ApiException;

    IDomainKeyBuilder<E> name(IField field) throws ApiException;

    IDomainKeyBuilder<E> name(ObjectAddress fieldAddress) throws ApiException;

    // ───── IKeyRealm.getKeyAlgorithm() ─────

    IDomainKeyBuilder<E> keyAlgorithm(String fieldName) throws ApiException;

    IDomainKeyBuilder<E> keyAlgorithm(IField field) throws ApiException;

    IDomainKeyBuilder<E> keyAlgorithm(ObjectAddress fieldAddress) throws ApiException;

    // ───── IKey.getSignatureAlgorithm() (carried on the signing/verification IKey) ─────

    IDomainKeyBuilder<E> signatureAlgorithm(String fieldName) throws ApiException;

    IDomainKeyBuilder<E> signatureAlgorithm(IField field) throws ApiException;

    IDomainKeyBuilder<E> signatureAlgorithm(ObjectAddress fieldAddress) throws ApiException;

    // ───── IKeyRealm.getKeyForSigning() — PKCS#8 bytes ─────

    IDomainKeyBuilder<E> keyForSigning(String fieldName) throws ApiException;

    IDomainKeyBuilder<E> keyForSigning(IField field) throws ApiException;

    IDomainKeyBuilder<E> keyForSigning(ObjectAddress fieldAddress) throws ApiException;

    // ───── IKeyRealm.getKeyForSignatureVerification() — X.509 bytes ─────

    IDomainKeyBuilder<E> keyForSignatureVerification(String fieldName) throws ApiException;

    IDomainKeyBuilder<E> keyForSignatureVerification(IField field) throws ApiException;

    IDomainKeyBuilder<E> keyForSignatureVerification(ObjectAddress fieldAddress) throws ApiException;

    // ───── IKeyRealm.getKeyForEncryption() — JDK-encoded bytes ─────

    IDomainKeyBuilder<E> keyForEncryption(String fieldName) throws ApiException;

    IDomainKeyBuilder<E> keyForEncryption(IField field) throws ApiException;

    IDomainKeyBuilder<E> keyForEncryption(ObjectAddress fieldAddress) throws ApiException;

    // ───── IKeyRealm.getKeyForDecryption() — JDK-encoded bytes ─────

    IDomainKeyBuilder<E> keyForDecryption(String fieldName) throws ApiException;

    IDomainKeyBuilder<E> keyForDecryption(IField field) throws ApiException;

    IDomainKeyBuilder<E> keyForDecryption(ObjectAddress fieldAddress) throws ApiException;

    // ───── IKeyRealm.getExpiration() / isExpired() ─────

    IDomainKeyBuilder<E> expiration(String fieldName) throws ApiException;

    IDomainKeyBuilder<E> expiration(IField field) throws ApiException;

    IDomainKeyBuilder<E> expiration(ObjectAddress fieldAddress) throws ApiException;

    // ───── IKeyRealm.isRevoked() / revoke() ─────

    IDomainKeyBuilder<E> revoked(String fieldName) throws ApiException;

    IDomainKeyBuilder<E> revoked(IField field) throws ApiException;

    IDomainKeyBuilder<E> revoked(ObjectAddress fieldAddress) throws ApiException;

    // ───── IKeyRealm.getVersion() ─────

    IDomainKeyBuilder<E> version(String fieldName) throws ApiException;

    IDomainKeyBuilder<E> version(IField field) throws ApiException;

    IDomainKeyBuilder<E> version(ObjectAddress fieldAddress) throws ApiException;

    // ───── IKeyRealm.rotate() — last-rotation timestamp ─────

    IDomainKeyBuilder<E> rotate(String fieldName) throws ApiException;

    IDomainKeyBuilder<E> rotate(IField field) throws ApiException;

    IDomainKeyBuilder<E> rotate(ObjectAddress fieldAddress) throws ApiException;
}
