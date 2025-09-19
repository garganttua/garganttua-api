package com.garganttua.api.core.engine;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.javatuples.Pair;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IEntityBuilder;
import com.garganttua.api.spec.engine.IEntityMethodBinderBuilder;
import com.garganttua.api.spec.engine.IMethodBinderBuilder;
import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;
import static com.garganttua.api.core.engine.ExecutionContext.Suppliers.*;

public class EntityBuilder implements IEntityBuilder {

    private Class<?> entityClass;
    private IDomainBuilder domainBuilder;
    private Boolean autoDetect  = false;
    private IGGObjectQuery objectQuery;
    private Field id;
    private Field uuid;
    private Field tenantId;
    private List<Field> mandatories = new ArrayList<>();
    private List<Pair<Field, UnicityScope>> unicities = new ArrayList<>();
    private List<Pair<Field, String>> updates = new ArrayList<>();
    private List<Pair<Field, Class<? extends Annotation>>> annotatedFields = new ArrayList<>();
    private List<Pair<Method, Class<? extends Annotation>>> annotatedMethods = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder>> afterGetMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder>> beforeCreateMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder>> afterCreateMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder>> beforeUpdateMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder>> afterUpdateMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder>> beforeDeleteMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder>> afterDeleteMethodBuilders = new ArrayList<>();

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
        this.autoDetect = Objects.requireNonNull(b, "AutoDetect cannot be null");
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

        this.unicities.add(new Pair<Field, UnicityScope>(FieldResolver.fieldByField(field, this.entityClass),
                UnicityScope.system));

        return this;
    }

    @Override
    public IEntityBuilder unicity(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.unicities.add(new Pair<Field, UnicityScope>(
                FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass), UnicityScope.system));

        return this;
    }

    @Override
    public IEntityBuilder unicity(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.unicities.add(new Pair<Field, UnicityScope>(
                FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass), UnicityScope.system));

        return this;
    }

    @Override
    public IEntityBuilder unicity(String fieldName, UnicityScope scope) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.unicities.add(new Pair<Field, UnicityScope>(
                FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass), scope));

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

        this.unicities.add(new Pair<Field, UnicityScope>(
                FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass), scope));

        return this;
    }

    @Override
    public IEntityBuilder update(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.updates.add(new Pair<Field, String>(
                FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass), null));

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

        this.updates.add(new Pair<Field, String>(
                FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass), null));

        return this;
    }

    @Override
    public IEntityBuilder update(String fieldName, String authority) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.updates.add(new Pair<Field, String>(
                FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass), authority));

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

        this.updates.add(new Pair<Field, String>(
                FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass), authority));

        return this;
    }

    @Override
    public IEntityBuilder annotation(String elementName, Class<? extends Annotation> annotation)
            throws BuilderException {
        Objects.requireNonNull(elementName, "Element name cannot be null");
        Objects.requireNonNull(annotation, "Annotation cannot be null");

        try {
            GGObjectAddress address = this.objectQuery.address(elementName);
            return this.annotation(address, annotation);
        } catch (GGReflectionException e) {
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE, e.getMessage(), e);
        }
    }

    @Override
    public IEntityBuilder annotation(Field field, Class<? extends Annotation> annotation) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");
        Objects.requireNonNull(annotation, "Annotation cannot be null");

        FieldResolver.fieldByField(field, this.entityClass);

        Pair<Field, Class<? extends Annotation>> candidate = new Pair<>(field, annotation);

        if (this.annotatedFields.contains(candidate)) {
            return this;
        }

        this.annotatedFields.add(candidate);

        return this;
    }

    @Override
    public IEntityBuilder annotation(Method method, Class<? extends Annotation> annotation) throws BuilderException {
        Objects.requireNonNull(method, "Method cannot be null");
        Objects.requireNonNull(annotation, "Annotation cannot be null");

        MethodResolver.methodByMethod(method, this.entityClass);

        Pair<Method, Class<? extends Annotation>> candidate = new Pair<>(method, annotation);
        if (this.annotatedMethods.contains(candidate)) {
            return this;
        }

        this.annotatedMethods.add(candidate);

        return this;
    }

    @Override
    public IEntityBuilder annotation(GGObjectAddress elementAddress, Class<? extends Annotation> annotation)
            throws BuilderException {
        Objects.requireNonNull(elementAddress, "Element address cannot be null");
        Objects.requireNonNull(annotation, "Annotation cannot be null");

        try {
            Object leaf = this.objectQuery.find(elementAddress).getLast();
            if (Field.class.isAssignableFrom(leaf.getClass()))
                this.annotation((Field) leaf, annotation);
            else
                this.annotation((Method) leaf, annotation);

            return this;
        } catch (GGReflectionException e) {
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE, e.getMessage(), e);
        }

    }

    @Override
    public IDomainBuilder up() {
        return this.domainBuilder;
    }

    private IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> createEntityMethodBuilder(
            String methodName, List<Pair<String, EntityMethodBinderBuilder>> list)
            throws CoreException {
        Optional<Pair<String, EntityMethodBinderBuilder>> found = list.stream()
                .filter(p -> p.getValue0().equals(methodName)).findFirst();
        if (found.isPresent())
            return found.get().getValue1();
        else {
            EntityMethodBinderBuilder entityMethodBinderBuilder = new EntityMethodBinderBuilder(this,
                    entity());

            list.add(new Pair<String, EntityMethodBinderBuilder>(methodName, entityMethodBinderBuilder));

            return entityMethodBinderBuilder;
        }
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterGet(String methodName)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, this.afterGetMethodBuilders).method(methodName);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterGet(Method method)
            throws CoreException {
        return this.createEntityMethodBuilder(method.getName(), this.afterGetMethodBuilders).method(method);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterGet(
            GGObjectAddress methodAddress) throws CoreException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.afterGetMethodBuilders).method(methodAddress);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> beforeCreate(String methodName)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, this.beforeCreateMethodBuilders).method(methodName);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> beforeCreate(Method method)
            throws CoreException {
        return this.createEntityMethodBuilder(method.getName(), this.beforeCreateMethodBuilders).method(method);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> beforeCreate(
            GGObjectAddress methodAddress) throws CoreException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.beforeCreateMethodBuilders).method(methodAddress);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> beforeUpdate(String methodName)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, this.beforeUpdateMethodBuilders).method(methodName);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> beforeUpdate(Method method)
            throws CoreException {
        return this.createEntityMethodBuilder(method.getName(), this.beforeUpdateMethodBuilders).method(method);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> beforeUpdate(
            GGObjectAddress methodAddress) throws CoreException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.beforeUpdateMethodBuilders).method(methodAddress);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> beforeDelete(String methodName)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, this.beforeDeleteMethodBuilders).method(methodName);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> beforeDelete(Method method)
            throws CoreException {
        return this.createEntityMethodBuilder(method.getName(), this.beforeDeleteMethodBuilders).method(method);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> beforeDelete(
            GGObjectAddress methodAddress) throws CoreException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.beforeDeleteMethodBuilders).method(methodAddress);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterCreate(String methodName)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, this.afterCreateMethodBuilders).method(methodName);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterCreate(Method method)
            throws CoreException {
        return this.createEntityMethodBuilder(method.getName(), this.afterCreateMethodBuilders).method(method);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterCreate(
            GGObjectAddress methodAddress) throws CoreException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.afterCreateMethodBuilders).method(methodAddress);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterUpdate(String methodName)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, this.afterUpdateMethodBuilders).method(methodName);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterUpdate(Method method)
            throws CoreException {
        return this.createEntityMethodBuilder(method.getName(), this.afterUpdateMethodBuilders).method(method);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterUpdate(
            GGObjectAddress methodAddress) throws CoreException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.afterUpdateMethodBuilders).method(methodAddress);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterDelete(String methodName)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, this.afterDeleteMethodBuilders).method(methodName);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterDelete(Method method)
            throws CoreException {
        return this.createEntityMethodBuilder(method.getName(), this.afterDeleteMethodBuilders).method(method);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, Object, IEntityBuilder> afterDelete(
            GGObjectAddress methodAddress) throws CoreException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.afterDeleteMethodBuilders).method(methodAddress);
    }

    @Override
    public Object build() throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'build'");
    }

}
