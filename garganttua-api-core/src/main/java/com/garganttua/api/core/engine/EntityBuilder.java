package com.garganttua.api.core.engine;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.javatuples.Pair;

import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IEntityBuilder;
import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;


public class EntityBuilder implements IEntityBuilder {

    private Class<?> entityClass;
    private IDomainBuilder domainBuilder;
    private boolean autoDetect;
    private IGGObjectQuery objectQuery;
    private Field id;
    private Field uuid;
    private Field tenantId;
    private List<Field> mandatories = new ArrayList<>();
    private List<Pair<Field, UnicityScope>> unicities = new ArrayList<>();
    private List<Pair<Field, String>> updates = new ArrayList<>();

    public EntityBuilder(Class<?> entityClass, IDomainBuilder domainBuilder) throws BuilderException {
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
        this.domainBuilder = Objects.requireNonNull(domainBuilder, "Domain builder cannot be null");
        try {
            this.objectQuery = GGObjectQueryFactory.objectQuery(this.entityClass);
        } catch (GGReflectionException e) {
            throw new BuilderException(CoreExceptionCode.CORE_GENERIC_CODE, e.getMessage(), e);
        }
    }

    @Override
    public IEntityBuilder autoDetect(boolean b) {
        this.autoDetect = b;
        return this;
    }

    @Override
    public IEntityBuilder id(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.id = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IEntityBuilder id(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.id = FieldResolver.fieldByField(field, this.entityClass, String.class);

        return this;
    }

    @Override
    public IEntityBuilder id(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.id = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IEntityBuilder uuid(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.uuid = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IEntityBuilder uuid(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.uuid = FieldResolver.fieldByField(field, this.entityClass, String.class);

        return this;
    }

    @Override
    public IEntityBuilder uuid(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.uuid = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IEntityBuilder tenantId(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.tenantId = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IEntityBuilder tenantId(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.tenantId = FieldResolver.fieldByField(field, this.entityClass, String.class);

        return this;
    }

    @Override
    public IEntityBuilder tenantId(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.tenantId = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IEntityBuilder mandatory(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.mandatories.add(FieldResolver.fieldByField(field, this.entityClass));

        return this;
    }

    @Override
    public IEntityBuilder mandatory(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.mandatories.add(FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass));

        return this;
    }

    @Override
    public IEntityBuilder mandatory(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.mandatories.add(FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass));

        return this;
    }

    @Override
    public IEntityBuilder unicity(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.unicities.add(new Pair<Field, UnicityScope>(FieldResolver.fieldByField(field, this.entityClass), UnicityScope.system));

        return this;
    }

    @Override
    public IEntityBuilder unicity(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.unicities.add(new Pair<Field, UnicityScope>(FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass), UnicityScope.system));

        return this;
    }

    @Override
    public IEntityBuilder unicity(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.unicities.add(new Pair<Field, UnicityScope>(FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass), UnicityScope.system));

        return this;
    }

    @Override
    public IEntityBuilder unicity(String fieldName, UnicityScope scope) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.unicities.add(new Pair<Field, UnicityScope>(FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass), scope));

        return this;
    }

    @Override
    public IEntityBuilder unicity(Field field, UnicityScope scope) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.unicities.add(new Pair<Field, UnicityScope>(FieldResolver.fieldByField(field, this.entityClass), scope));

        return this;
    }

    @Override
    public IEntityBuilder unicity(GGObjectAddress fieldAddress, UnicityScope scope) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.unicities.add(new Pair<Field, UnicityScope>(FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass), scope));

        return this;
    }

    @Override
    public IEntityBuilder update(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.updates.add(new Pair<Field, String>(FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass), null));

        return this;
    }

    @Override
    public IEntityBuilder update(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.updates.add(new Pair<Field, String>(FieldResolver.fieldByField(field, this.entityClass), null));

        return this;
    }

    @Override
    public IEntityBuilder update(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.updates.add(new Pair<Field, String>(FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass), null));

        return this;
    }

    @Override
    public IEntityBuilder update(String fieldName, String authority) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.updates.add(new Pair<Field, String>(FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass), authority));

        return this;
    }

    @Override
    public IEntityBuilder update(Field field, String authority) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.updates.add(new Pair<Field, String>(FieldResolver.fieldByField(field, this.entityClass), authority));

        return this;
    }

    @Override
    public IEntityBuilder update(GGObjectAddress fieldAddress, String authority) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.updates.add(new Pair<Field, String>(FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass), authority));

        return this;
    }

    @Override
    public IEntityBuilder annotation(String fieldName, Class<? extends Annotation> annotation) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'annotation'");
    }

    @Override
    public IEntityBuilder annotation(Field field, Class<? extends Annotation> annotation) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'annotation'");
    }

    @Override
    public IEntityBuilder annotation(Method method, Class<? extends Annotation> annotation) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'annotation'");
    }

    @Override
    public IEntityBuilder annotation(GGObjectAddress fieldAddress, Class<? extends Annotation> annotation) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'annotation'");
    }

    @Override
    public IEntityBuilder afterGet(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'afterGet'");
    }

    @Override
    public IEntityBuilder afterGet(Method method) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'afterGet'");
    }

    @Override
    public IEntityBuilder afterGet(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'afterGet'");
    }

    @Override
    public IEntityBuilder beforeCreate(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'beforeCreate'");
    }

    @Override
    public IEntityBuilder beforeCreate(Method method) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'beforeCreate'");
    }

    @Override
    public IEntityBuilder beforeCreate(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'beforeCreate'");
    }

    @Override
    public IEntityBuilder beforeUpdate(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'beforeUpdate'");
    }

    @Override
    public IEntityBuilder beforeUpdate(Method method) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'beforeUpdate'");
    }

    @Override
    public IEntityBuilder beforeUpdate(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'beforeUpdate'");
    }

    @Override
    public IEntityBuilder beforeDelete(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'beforeDelete'");
    }

    @Override
    public IEntityBuilder beforeDelete(Method method) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'beforeDelete'");
    }

    @Override
    public IEntityBuilder beforeDelete(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'beforeDelete'");
    }

    @Override
    public IEntityBuilder afterCreate(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'afterCreate'");
    }

    @Override
    public IEntityBuilder afterCreate(Method method) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'afterCreate'");
    }

    @Override
    public IEntityBuilder afterCreate(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'afterCreate'");
    }

    @Override
    public IEntityBuilder afterUpdate(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'afterUpdate'");
    }

    @Override
    public IEntityBuilder afterUpdate(Method method) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'afterUpdate'");
    }

    @Override
    public IEntityBuilder afterUpdate(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'afterUpdate'");
    }

    @Override
    public IEntityBuilder afterDelete(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'afterDelete'");
    }

    @Override
    public IEntityBuilder afterDelete(Method method) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'afterDelete'");
    }

    @Override
    public IEntityBuilder afterDelete(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'afterDelete'");
    }

    @Override
    public IDomainBuilder up() {
        return this.domainBuilder;
    }

}
