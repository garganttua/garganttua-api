package com.garganttua.api.spec.engine;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.reflection.GGObjectAddress;

public interface IEntityBuilder {

    IEntityBuilder autoDetect(boolean b);

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

    IEntityBuilder annotation(GGObjectAddress fieldAddress, Class<? extends Annotation> annotation) throws CoreException;

    IEntityBuilder afterGet(String string) throws CoreException;

    IEntityBuilder afterGet(Method method) throws CoreException;

    IEntityBuilder afterGet(GGObjectAddress fieldAddress) throws CoreException;

    IEntityBuilder beforeCreate(String string) throws CoreException;

    IEntityBuilder beforeCreate(Method method) throws CoreException;

    IEntityBuilder beforeCreate(GGObjectAddress fieldAddress) throws CoreException;

    IEntityBuilder beforeUpdate(String string) throws CoreException;

    IEntityBuilder beforeUpdate(Method method) throws CoreException;

    IEntityBuilder beforeUpdate(GGObjectAddress fieldAddress) throws CoreException;

    IEntityBuilder beforeDelete(String string) throws CoreException;

    IEntityBuilder beforeDelete(Method method) throws CoreException;

    IEntityBuilder beforeDelete(GGObjectAddress fieldAddress) throws CoreException;

    IEntityBuilder afterCreate(String string) throws CoreException;

    IEntityBuilder afterCreate(Method method) throws CoreException;

    IEntityBuilder afterCreate(GGObjectAddress fieldAddress) throws CoreException;

    IEntityBuilder afterUpdate(String string) throws CoreException;

    IEntityBuilder afterUpdate(Method method) throws CoreException;

    IEntityBuilder afterUpdate(GGObjectAddress fieldAddress) throws CoreException;

    IEntityBuilder afterDelete(String string) throws CoreException;

    IEntityBuilder afterDelete(Method method) throws CoreException;

    IEntityBuilder afterDelete(GGObjectAddress fieldAddress) throws CoreException;

    IDomainBuilder up();

    IEntityBuilder annotation(Method method, Class<? extends Annotation> annotation);

}
