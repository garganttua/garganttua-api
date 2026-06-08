package com.garganttua.api.commons.context.dsl;

import com.garganttua.core.reflection.IField;

import com.garganttua.api.commons.context.BuildingStage;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.api.commons.event.IEventPublisher;
import com.garganttua.api.commons.endpoint.IInterface;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public interface IDomainBuilder<E> extends IAutomaticLinkedBuilder<IDomainBuilder<E>, IApiBuilder, IDomain<E>> {

    IDomainStartupBinderBuilder<E> startup(BuildingStage stage, ISupplierBuilder<?, ? extends ISupplier<?>> method) throws ApiException;

    IDomainBuilder<E> interfasse(ISupplierBuilder<? extends IInterface, ? extends ISupplier<? extends IInterface>> bean) throws ApiException;

    IDomainBuilder<E> interfasse(IClass<? extends IInterface> interfasse) throws ApiException;

    IDomainBuilder<E> events(ISupplierBuilder<?, ? extends ISupplier<?>> bean) throws ApiException;

    IDomainBuilder<E> events(IEventPublisher eventPublisher) throws ApiException;

    IDomainBuilder<E> tenant(boolean b) throws ApiException;

    IDomainBuilder<E> owner(String string) throws ApiException;

    IDomainBuilder<E> owner(IField field) throws ApiException;

    IDomainBuilder<E> owner(ObjectAddress fieldAddress) throws ApiException;

    IDomainBuilder<E> owned(String string) throws ApiException;

    IDomainBuilder<E> owned(IField field) throws ApiException;

    IDomainBuilder<E> owned(ObjectAddress fieldAddress) throws ApiException;

    IDomainBuilder<E> publik();

    IDomainBuilder<E> shared(IField field) throws ApiException;

    IDomainBuilder<E> shared(String string) throws ApiException;

    IDomainBuilder<E> shared(ObjectAddress fieldAddress) throws ApiException;

    IDomainBuilder<E> geolocalized(String string) throws ApiException;

    IDomainBuilder<E> geolocalized(IField field) throws ApiException;

    IDomainBuilder<E> geolocalized(ObjectAddress fieldAddress) throws ApiException;

    IDomainBuilder<E> hiddenable(String string) throws ApiException;

    IDomainBuilder<E> hiddenable(IField field) throws ApiException;

    IDomainBuilder<E> hiddenable(ObjectAddress fieldAddress) throws ApiException;

    IDomainBuilder<E> superOwner(String string) throws ApiException;

    IDomainBuilder<E> superOwner(IField field) throws ApiException;

    IDomainBuilder<E> superOwner(ObjectAddress fieldAddress) throws ApiException;

    IDomainBuilder<E> superTenant(String string) throws ApiException;

    IDomainBuilder<E> superTenant(IField field) throws ApiException;

    IDomainBuilder<E> superTenant(ObjectAddress fieldAddress) throws ApiException;

    IEntityBuilder<E> name(String name) throws ApiException;

    IClass<E> getEntityClass() throws ApiException;

    IDomainSecurityBuilder<E> security() throws ApiException;

    // The @Key domain config sub-builder moved under .security(): see
    // IDomainSecurityBuilder.key(). It marks this domain as a key domain whose
    // entity holds cryptographic key material, used as the storage backend when
    // an authenticator's authorization declares .key(IDomainBuilder).

    <D> IDtoBuilder<E, D> dto(IClass<D> dtoClass) throws ApiException;

    <I, O> IUseCaseBuilder<I, O, E> useCase(String useCaseName, IClass<I> inputType, IClass<O> outputType);

    IDomainWorkflowBuilder<E> workflow(String workflowName);

    IDomainBuilder<E> create(Object entity);

    IDomainBuilder<E> upsert(Object entity);

    IDomainBuilder<E> doInjection(boolean enabled);

    IEntityBuilder<E> entity() throws ApiException;

    IDomainBuilder<E> creation(boolean enabled);

    IDomainBuilder<E> readAll(boolean enabled);

    IDomainBuilder<E> readOne(boolean enabled);

    IDomainBuilder<E> update(boolean enabled);

    IDomainBuilder<E> deleteOne(boolean enabled);

    IDomainBuilder<E> deleteAll(boolean enabled);

}
