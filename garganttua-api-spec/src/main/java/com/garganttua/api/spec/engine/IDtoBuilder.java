package com.garganttua.api.spec.engine;

import java.lang.reflect.Field;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.dao.IDao;
import com.garganttua.reflection.GGObjectAddress;

public interface IDtoBuilder extends IAutomaticLinkedBuilder<IDomainDtoContext, IDomainBuilder, IDtoBuilder> {

    IDtoBuilder db(IObjectSupplierBuilder<?> daoSupplier) throws CoreException;

    IDtoBuilder db(IDao dao);

    IDtoBuilder id(String string) throws CoreException;

    IDtoBuilder id(Field field) throws CoreException;

    IDtoBuilder id(GGObjectAddress fieldAddress) throws CoreException;

    IDtoBuilder uuid(String string) throws CoreException;

    IDtoBuilder uuid(Field field) throws CoreException;

    IDtoBuilder uuid(GGObjectAddress fieldAddress) throws CoreException;

    IDtoBuilder tenantId(String string) throws CoreException;

    IDtoBuilder tenantId(Field field) throws CoreException;

    IDtoBuilder tenantId(GGObjectAddress fieldAddress) throws CoreException;

}