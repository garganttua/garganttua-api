package com.garganttua.api.spec.engine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.reflection.GGObjectAddress;

public interface IEntityBuilder {

    IEntityBuilder autoDetect(boolean b);

    IEntityBuilder id(String string);

    IEntityBuilder id(Field field);

    IEntityBuilder id(GGObjectAddress fieldAddress);

    IEntityBuilder uuid(String string);

    IEntityBuilder uuid(Field field);

    IEntityBuilder uuid(GGObjectAddress fieldAddress);

    IEntityBuilder tenantId(String string);

    IEntityBuilder tenantId(Field field);

    IEntityBuilder tenantId(GGObjectAddress fieldAddress);

    IEntityBuilder mandatory(Field field);

    IEntityBuilder mandatory(String string);

    IEntityBuilder mandatory(GGObjectAddress fieldAddress);

    IEntityBuilder unicity(Field field);

    IEntityBuilder unicity(String string);

    IEntityBuilder unicity(GGObjectAddress fieldAddress);

    IEntityBuilder unicity(String string, UnicityScope system);

    IEntityBuilder unicity(Field field, UnicityScope system);

    IEntityBuilder unicity(GGObjectAddress fieldAddress, UnicityScope system);

    IEntityBuilder update(String string);

    IEntityBuilder update(Field field);

    IEntityBuilder update(GGObjectAddress fieldAddress);

    IEntityBuilder update(String string, String string2);

    IEntityBuilder update(Field field, String string2);

    IEntityBuilder update(GGObjectAddress fieldAddress, String string2);

    IEntityBuilder annotation(String string, Class<Object> class1);

    IEntityBuilder annotation(Field field, Class<Object> class1);

    IEntityBuilder annotation(GGObjectAddress fieldAddress, Class<Object> class1);

    IEntityBuilder afterGet(String string);

    IEntityBuilder afterGet(Method method);

    IEntityBuilder afterGet(GGObjectAddress fieldAddress);

    IEntityBuilder beforeCreate(String string);

    IEntityBuilder beforeCreate(Method method);

    IEntityBuilder beforeCreate(GGObjectAddress fieldAddress);

    IEntityBuilder beforeUpdate(String string);

    IEntityBuilder beforeUpdate(Method method);

    IEntityBuilder beforeUpdate(GGObjectAddress fieldAddress);

    IEntityBuilder beforeDelete(String string);

    IEntityBuilder beforeDelete(Method method);

    IEntityBuilder beforeDelete(GGObjectAddress fieldAddress);

    IEntityBuilder afterCreate(String string);

    IEntityBuilder afterCreate(Method method);

    IEntityBuilder afterCreate(GGObjectAddress fieldAddress);

    IEntityBuilder afterUpdate(String string);

    IEntityBuilder afterUpdate(Method method);

    IEntityBuilder afterUpdate(GGObjectAddress fieldAddress);

    IEntityBuilder afterDelete(String string);

    IEntityBuilder afterDelete(Method method);

    IEntityBuilder afterDelete(GGObjectAddress fieldAddress);

    IDomainBuilder up();

}
