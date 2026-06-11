package com.garganttua.api.commons.context.dsl;

import com.garganttua.core.reflection.IField;

import com.garganttua.api.commons.context.IDtoContext;
import com.garganttua.api.commons.dao.IDao;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public interface IDtoBuilder<E, D> extends IAutomaticLinkedBuilder<IDtoBuilder<E, D>, IDomainBuilder<E>, IDtoContext<D>> {

    IDtoBuilder<E, D> db(ISupplierBuilder<? extends IDao, ISupplier<? extends IDao>> daoSupplier) throws ApiException;

    IDtoBuilder<E, D> db(IDao dao);

    IDtoBuilder<E, D> id(String string) throws ApiException;

    IDtoBuilder<E, D> id(IField field) throws ApiException;

    IDtoBuilder<E, D> id(ObjectAddress fieldAddress) throws ApiException;

    IDtoBuilder<E, D> uuid(String string) throws ApiException;

    IDtoBuilder<E, D> uuid(IField field) throws ApiException;

    IDtoBuilder<E, D> uuid(ObjectAddress fieldAddress) throws ApiException;

    IDtoBuilder<E, D> tenantId(String string) throws ApiException;

    IDtoBuilder<E, D> tenantId(IField field) throws ApiException;

    IDtoBuilder<E, D> tenantId(ObjectAddress fieldAddress) throws ApiException;

    /**
     * Declares a COMPOSITION (à la {@code @DBRef}): {@code fieldName} holds a reference (or a
     * {@code List} of references) to DTOs stored in {@code collection}. The DAO persists only a
     * reference for this field and resolves it back to the full DTO on read. The DSL equivalent
     * of the {@code @Composed} annotation.
     */
    IDtoBuilder<E, D> composed(String fieldName, String collection) throws ApiException;

}