package com.garganttua.api.commons.context.dsl;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IDomainKeyContext;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IField;
import com.garganttua.core.reflection.ObjectAddress;

/**
 * Sub-builder declaring a domain as a key domain — i.e. the entity holds
 * cryptographic key material that the framework can persist and look up
 * via the auto-create path driven by
 * {@code AuthenticatorKeyUsage} (oneForAll / oneForTenant / oneForEach).
 *
 * <p>Each setter binds the address of the entity field that holds the
 * matching piece of material or metadata. The field-level
 * {@code @KeyRealmName}, {@code @KeyAlgorithm}, {@code @KeySignatureAlgorithm},
 * {@code @KeyPublicMaterial}, {@code @KeyPrivateMaterial},
 * {@code @KeyExpiration}, {@code @KeyRevoked} annotations are read by
 * the entity-role scanner and call these setters automatically when an
 * entity is marked with {@code @Key}.
 *
 * @param <E> the key entity type
 */
public interface IDomainKeyBuilder<E> extends
        IAutomaticLinkedBuilder<IDomainKeyBuilder<E>, IDomainBuilder<E>, IDomainKeyContext> {

    IDomainKeyBuilder<E> realmName(String fieldName) throws ApiException;

    IDomainKeyBuilder<E> realmName(IField field) throws ApiException;

    IDomainKeyBuilder<E> realmName(ObjectAddress fieldAddress) throws ApiException;

    IDomainKeyBuilder<E> algorithm(String fieldName) throws ApiException;

    IDomainKeyBuilder<E> algorithm(IField field) throws ApiException;

    IDomainKeyBuilder<E> algorithm(ObjectAddress fieldAddress) throws ApiException;

    IDomainKeyBuilder<E> signatureAlgorithm(String fieldName) throws ApiException;

    IDomainKeyBuilder<E> signatureAlgorithm(IField field) throws ApiException;

    IDomainKeyBuilder<E> signatureAlgorithm(ObjectAddress fieldAddress) throws ApiException;

    IDomainKeyBuilder<E> publicMaterial(String fieldName) throws ApiException;

    IDomainKeyBuilder<E> publicMaterial(IField field) throws ApiException;

    IDomainKeyBuilder<E> publicMaterial(ObjectAddress fieldAddress) throws ApiException;

    IDomainKeyBuilder<E> privateMaterial(String fieldName) throws ApiException;

    IDomainKeyBuilder<E> privateMaterial(IField field) throws ApiException;

    IDomainKeyBuilder<E> privateMaterial(ObjectAddress fieldAddress) throws ApiException;

    IDomainKeyBuilder<E> expiration(String fieldName) throws ApiException;

    IDomainKeyBuilder<E> expiration(IField field) throws ApiException;

    IDomainKeyBuilder<E> expiration(ObjectAddress fieldAddress) throws ApiException;

    IDomainKeyBuilder<E> revoked(String fieldName) throws ApiException;

    IDomainKeyBuilder<E> revoked(IField field) throws ApiException;

    IDomainKeyBuilder<E> revoked(ObjectAddress fieldAddress) throws ApiException;
}
