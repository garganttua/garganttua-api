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

}