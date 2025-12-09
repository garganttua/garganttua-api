package com.garganttua.api.spec.context.dsl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.garganttua.api.spec.context.IEntityContext;
import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.ObjectAddress;

public interface IEntityBuilder<E> extends IAutomaticLinkedBuilder<IEntityBuilder<E>, IDomainBuilder<E>, IEntityContext<E>> {

    IEntityBuilder<E> id(String string) throws DslException;

    IEntityBuilder<E> id(Field field) throws DslException;

    IEntityBuilder<E> id(ObjectAddress fieldAddress) throws DslException;

    IEntityBuilder<E> uuid(String string) throws DslException;

    IEntityBuilder<E> uuid(Field field) throws DslException;

    IEntityBuilder<E> uuid(ObjectAddress fieldAddress) throws DslException;

    IEntityBuilder<E> tenantId(String string) throws DslException;

    IEntityBuilder<E> tenantId(Field field) throws DslException;

    IEntityBuilder<E> tenantId(ObjectAddress fieldAddress) throws DslException;

    IEntityBuilder<E> mandatory(Field field) throws DslException;

    IEntityBuilder<E> mandatory(String string) throws DslException;

    IEntityBuilder<E> mandatory(ObjectAddress fieldAddress) throws DslException;

    IEntityBuilder<E> unicity(Field field) throws DslException;

    IEntityBuilder<E> unicity(String string) throws DslException;

    IEntityBuilder<E> unicity(ObjectAddress fieldAddress) throws DslException;

    IEntityBuilder<E> unicity(String string, UnicityScope system) throws DslException;

    IEntityBuilder<E> unicity(Field field, UnicityScope system) throws DslException;

    IEntityBuilder<E> unicity(ObjectAddress fieldAddress, UnicityScope system) throws DslException;

    IEntityBuilder<E> update(String string) throws DslException;

    IEntityBuilder<E> update(Field field) throws DslException;

    IEntityBuilder<E> update(ObjectAddress fieldAddress) throws DslException;

    IEntityBuilder<E> update(String string, String authority) throws DslException;

    IEntityBuilder<E> update(Field field, String authority) throws DslException;

    IEntityBuilder<E> update(ObjectAddress fieldAddress, String authority) throws DslException;

    IEntityBuilder<E> annotation(String elementName, Class<? extends Annotation> annotation) throws DslException;

    IEntityBuilder<E> annotation(Field field, Class<? extends Annotation> annotation) throws DslException;

    IEntityBuilder<E> annotation(ObjectAddress elementAddress, Class<? extends Annotation> annotation) throws DslException;

    IEntityBuilder<E> annotation(Method method, Class<? extends Annotation> annotation) throws DslException;

    IEntityMethodBinderBuilder<E> afterGet(String methodName) throws DslException;

    IEntityMethodBinderBuilder<E> afterGet(Method method) throws DslException;

    IEntityMethodBinderBuilder<E> afterGet(ObjectAddress methodAddress) throws DslException;

    IEntityMethodBinderBuilder<E> beforeCreate(String methodName) throws DslException;

    IEntityMethodBinderBuilder<E> beforeCreate(Method method) throws DslException;

    IEntityMethodBinderBuilder<E> beforeCreate(ObjectAddress methodAddress) throws DslException;

    IEntityMethodBinderBuilder<E> beforeUpdate(String methodName) throws DslException;

    IEntityMethodBinderBuilder<E> beforeUpdate(Method method) throws DslException;

    IEntityMethodBinderBuilder<E> beforeUpdate(ObjectAddress fieldmethodAddressAddress) throws DslException;

    IEntityMethodBinderBuilder<E> beforeDelete(String methodName) throws DslException;

    IEntityMethodBinderBuilder<E> beforeDelete(Method method) throws DslException;

    IEntityMethodBinderBuilder<E> beforeDelete(ObjectAddress methodAddress) throws DslException;

    IEntityMethodBinderBuilder<E> afterCreate(String methodName) throws DslException;

    IEntityMethodBinderBuilder<E> afterCreate(Method method) throws DslException;

    IEntityMethodBinderBuilder<E> afterCreate(ObjectAddress methodAddress) throws DslException;

    IEntityMethodBinderBuilder<E> afterUpdate(String methodName) throws DslException;

    IEntityMethodBinderBuilder<E> afterUpdate(Method method) throws DslException;

    IEntityMethodBinderBuilder<E> afterUpdate(ObjectAddress methodAddress) throws DslException;

    IEntityMethodBinderBuilder<E> afterDelete(String methodName) throws DslException;

    IEntityMethodBinderBuilder<E> afterDelete(Method method) throws DslException;

    IEntityMethodBinderBuilder<E> afterDelete(ObjectAddress methodAddress) throws DslException;

}
