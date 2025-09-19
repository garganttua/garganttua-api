package com.garganttua.api.spec.engine;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.reflection.GGObjectAddress;

public interface IEntityBuilder extends IAutomaticLinkedBuilder<Object, IDomainBuilder, IEntityBuilder> {

    IEntityBuilder id(String string) throws CoreException;

    IEntityBuilder id(Field field) throws CoreException;

    IEntityBuilder id(GGObjectAddress fieldAddress) throws CoreException;

    IEntityBuilder uuid(String string) throws CoreException;

    IEntityBuilder uuid(Field field) throws CoreException;

    IEntityBuilder uuid(GGObjectAddress fieldAddress) throws CoreException;

    IEntityBuilder tenantId(String string) throws CoreException;

    IEntityBuilder tenantId(Field field) throws CoreException;

    IEntityBuilder tenantId(GGObjectAddress fieldAddress) throws CoreException;

    IEntityBuilder mandatory(Field field) throws CoreException;

    IEntityBuilder mandatory(String string) throws CoreException;

    IEntityBuilder mandatory(GGObjectAddress fieldAddress) throws CoreException;

    IEntityBuilder unicity(Field field) throws CoreException;

    IEntityBuilder unicity(String string) throws CoreException;

    IEntityBuilder unicity(GGObjectAddress fieldAddress) throws CoreException;

    IEntityBuilder unicity(String string, UnicityScope system) throws CoreException;

    IEntityBuilder unicity(Field field, UnicityScope system) throws CoreException;

    IEntityBuilder unicity(GGObjectAddress fieldAddress, UnicityScope system) throws CoreException;

    IEntityBuilder update(String string) throws CoreException;

    IEntityBuilder update(Field field) throws CoreException;

    IEntityBuilder update(GGObjectAddress fieldAddress) throws CoreException;

    IEntityBuilder update(String string, String string2) throws CoreException;

    IEntityBuilder update(Field field, String string2) throws CoreException;

    IEntityBuilder update(GGObjectAddress fieldAddress, String string2) throws CoreException;

    IEntityBuilder annotation(String elementName, Class<? extends Annotation> annotation) throws CoreException;

    IEntityBuilder annotation(Field field, Class<? extends Annotation> annotation) throws CoreException;

    IEntityBuilder annotation(GGObjectAddress elementAddress, Class<? extends Annotation> annotation) throws CoreException;

    IEntityBuilder annotation(Method method, Class<? extends Annotation> annotation) throws CoreException;

    IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterGet(String methodName) throws CoreException;

    IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterGet(Method method) throws CoreException;

    IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterGet(GGObjectAddress methodAddress) throws CoreException;

    IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> beforeCreate(String methodName) throws CoreException;

    IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> beforeCreate(Method method) throws CoreException;

    IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> beforeCreate(GGObjectAddress methodAddress) throws CoreException;

    IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> beforeUpdate(String methodName) throws CoreException;

    IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> beforeUpdate(Method method) throws CoreException;

    IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> beforeUpdate(GGObjectAddress fieldmethodAddressAddress) throws CoreException;

    IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> beforeDelete(String methodName) throws CoreException;

    IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> beforeDelete(Method method) throws CoreException;

    IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> beforeDelete(GGObjectAddress methodAddress) throws CoreException;

    IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterCreate(String methodName) throws CoreException;

    IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterCreate(Method method) throws CoreException;

    IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterCreate(GGObjectAddress methodAddress) throws CoreException;

    IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterUpdate(String methodName) throws CoreException;

    IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterUpdate(Method method) throws CoreException;

    IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterUpdate(GGObjectAddress methodAddress) throws CoreException;

    IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterDelete(String methodName) throws CoreException;

    IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterDelete(Method method) throws CoreException;

    IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterDelete(GGObjectAddress methodAddress) throws CoreException;

}
