package com.garganttua.api.core.builder;

import static com.garganttua.api.core.context.execution.ExecutionContext.Suppliers.entity;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.javatuples.Pair;

import com.garganttua.api.core.builder.binder.EntityMethodBinderBuilder;
import com.garganttua.api.core.context.application.DomainEntityContext;
import com.garganttua.api.core.definition.DomainEntityDefinition;
import com.garganttua.api.spec.context.IDomainEntityContext;
import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.context.dsl.IEntityBuilder;
import com.garganttua.api.spec.context.dsl.IEntityMethodBinderBuilder;
import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.core.CoreException;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.IMethodBinderBuilder;
import com.garganttua.core.reflection.fields.FieldResolver;
import com.garganttua.core.reflection.methods.MethodResolver;
import com.garganttua.core.reflection.query.ObjectQueryFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntityBuilder extends AbstractAutomaticLinkedBuilder<IEntityBuilder, IDomainBuilder, IDomainEntityContext>
        implements IEntityBuilder {

    private Class<?> entityClass;
    private IObjectQuery objectQuery;
    private ObjectAddress id;
    private ObjectAddress uuid;
    private ObjectAddress tenantId;
    private List<ObjectAddress> mandatories = new ArrayList<>();
    private List<Pair<ObjectAddress, UnicityScope>> unicities = new ArrayList<>();
    private List<Pair<ObjectAddress, String>> updates = new ArrayList<>();
    private List<Pair<ObjectAddress, Class<? extends Annotation>>> annotatedFields = new ArrayList<>();
    private List<Pair<ObjectAddress, Class<? extends Annotation>>> annotatedMethods = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder>> afterGetMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder>> beforeCreateMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder>> afterCreateMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder>> beforeUpdateMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder>> afterUpdateMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder>> beforeDeleteMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder>> afterDeleteMethodBuilders = new ArrayList<>();

    public EntityBuilder(Class<?> entityClass, IDomainBuilder domainBuilder) throws DslException {
        super(domainBuilder);
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");

        try {
            this.objectQuery = ObjectQueryFactory.objectQuery(this.entityClass);
        } catch (ReflectionException e) {
            throw new DslException(e.getMessage(), e);
        }
    }

    @Override
    public IEntityBuilder id(String fieldName) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.id = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IEntityBuilder id(Field field) throws DslException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.id = FieldResolver.fieldByField(field, this.entityClass, String.class);

        return this;
    }

    @Override
    public IEntityBuilder id(ObjectAddress fieldAddress) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.id = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IEntityBuilder uuid(String fieldName) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.uuid = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IEntityBuilder uuid(Field field) throws DslException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.uuid = FieldResolver.fieldByField(field, this.entityClass, String.class);

        return this;
    }

    @Override
    public IEntityBuilder uuid(ObjectAddress fieldAddress) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.uuid = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IEntityBuilder tenantId(String fieldName) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.tenantId = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IEntityBuilder tenantId(Field field) throws DslException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.tenantId = FieldResolver.fieldByField(field, this.entityClass, String.class);

        return this;
    }

    @Override
    public IEntityBuilder tenantId(ObjectAddress fieldAddress) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.tenantId = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IEntityBuilder mandatory(Field field) throws DslException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.mandatories.add(FieldResolver.fieldByField(field, this.entityClass));

        return this;
    }

    @Override
    public IEntityBuilder mandatory(String fieldName) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.mandatories.add(FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass));

        return this;
    }

    @Override
    public IEntityBuilder mandatory(ObjectAddress fieldAddress) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.mandatories.add(FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass));

        return this;
    }

    @Override
    public IEntityBuilder unicity(Field field) throws DslException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.unicities.add(new Pair<ObjectAddress, UnicityScope>(FieldResolver.fieldByField(field, this.entityClass),
                UnicityScope.system));

        return this;
    }

    @Override
    public IEntityBuilder unicity(String fieldName) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.unicities.add(new Pair<ObjectAddress, UnicityScope>(
                FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass), UnicityScope.system));

        return this;
    }

    @Override
    public IEntityBuilder unicity(ObjectAddress fieldAddress) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.unicities.add(new Pair<ObjectAddress, UnicityScope>(
                FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass), UnicityScope.system));

        return this;
    }

    @Override
    public IEntityBuilder unicity(String fieldName, UnicityScope scope) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.unicities.add(new Pair<ObjectAddress, UnicityScope>(
                FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass), scope));

        return this;
    }

    @Override
    public IEntityBuilder unicity(Field field, UnicityScope scope) throws DslException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.unicities.add(
                new Pair<ObjectAddress, UnicityScope>(FieldResolver.fieldByField(field, this.entityClass), scope));

        return this;
    }

    @Override
    public IEntityBuilder unicity(ObjectAddress fieldAddress, UnicityScope scope) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.unicities.add(new Pair<ObjectAddress, UnicityScope>(
                FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass), scope));

        return this;
    }

    @Override
    public IEntityBuilder update(String fieldName) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.updates.add(new Pair<ObjectAddress, String>(
                FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass), null));

        return this;
    }

    @Override
    public IEntityBuilder update(Field field) throws DslException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.updates.add(new Pair<ObjectAddress, String>(FieldResolver.fieldByField(field, this.entityClass), null));

        return this;
    }

    @Override
    public IEntityBuilder update(ObjectAddress fieldAddress) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.updates.add(new Pair<ObjectAddress, String>(
                FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass), null));

        return this;
    }

    @Override
    public IEntityBuilder update(String fieldName, String authority) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.updates.add(new Pair<ObjectAddress, String>(
                FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass), authority));

        return this;
    }

    @Override
    public IEntityBuilder update(Field field, String authority) throws DslException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.updates
                .add(new Pair<ObjectAddress, String>(FieldResolver.fieldByField(field, this.entityClass), authority));

        return this;
    }

    @Override
    public IEntityBuilder update(ObjectAddress fieldAddress, String authority) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.updates.add(new Pair<ObjectAddress, String>(
                FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass), authority));

        return this;
    }

    @Override
    public IEntityBuilder annotation(String elementName, Class<? extends Annotation> annotation)
            throws DslException {
        Objects.requireNonNull(elementName, "Element name cannot be null");
        Objects.requireNonNull(annotation, "Annotation cannot be null");

        try {
            ObjectAddress address = this.objectQuery.address(elementName);
            return this.annotation(address, annotation);
        } catch (ReflectionException e) {
            throw new DslException(e.getMessage(), e);
        }
    }

    @Override
    public IEntityBuilder annotation(Field field, Class<? extends Annotation> annotation) throws DslException {
        Objects.requireNonNull(field, "Field cannot be null");
        Objects.requireNonNull(annotation, "Annotation cannot be null");

        ObjectAddress address = FieldResolver.fieldByField(field, this.entityClass);

        Pair<ObjectAddress, Class<? extends Annotation>> candidate = new Pair<>(address, annotation);

        if (this.annotatedFields.contains(candidate)) {
            return this;
        }

        this.annotatedFields.add(candidate);

        return this;
    }

    @Override
    public IEntityBuilder annotation(Method method, Class<? extends Annotation> annotation) throws DslException {
        Objects.requireNonNull(method, "Method cannot be null");
        Objects.requireNonNull(annotation, "Annotation cannot be null");

        ObjectAddress address = MethodResolver.methodByMethod(method, this.entityClass);

        Pair<ObjectAddress, Class<? extends Annotation>> candidate = new Pair<>(address, annotation);
        if (this.annotatedMethods.contains(candidate)) {
            return this;
        }

        this.annotatedMethods.add(candidate);

        return this;
    }

    @Override
    public IEntityBuilder annotation(ObjectAddress elementAddress, Class<? extends Annotation> annotation)
            throws DslException {
        Objects.requireNonNull(elementAddress, "Element address cannot be null");
        Objects.requireNonNull(annotation, "Annotation cannot be null");

        try {
            Object leaf = this.objectQuery.find(elementAddress).getLast();
            if (Field.class.isAssignableFrom(leaf.getClass()))
                this.annotation((Field) leaf, annotation);
            else
                this.annotation((Method) leaf, annotation);

            return this;
        } catch (ReflectionException e) {
            throw new DslException(e.getMessage(), e);
        }

    }

    private IEntityMethodBinderBuilder createEntityMethodBuilder(
            String methodName, List<Pair<String, EntityMethodBinderBuilder>> list)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, list, false);
    }

    private IEntityMethodBinderBuilder createEntityMethodBuilder(
            String methodName, List<Pair<String, EntityMethodBinderBuilder>> list, boolean collection)
            throws CoreException {
        Optional<Pair<String, EntityMethodBinderBuilder>> found = list.stream()
                .filter(p -> p.getValue0().equals(methodName)).findFirst();
        if (found.isPresent())
            return found.get().getValue1();
        else {
            EntityMethodBinderBuilder entityMethodBinderBuilder = new EntityMethodBinderBuilder(this,
                    entity(this.entityClass), collection);

            list.add(new Pair<String, EntityMethodBinderBuilder>(methodName, entityMethodBinderBuilder));

            return entityMethodBinderBuilder;
        }
    }

    @Override
    public IEntityMethodBinderBuilder afterGet(String methodName)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, this.afterGetMethodBuilders, true).method(methodName);
    }

    @Override
    public IEntityMethodBinderBuilder afterGet(Method method)
            throws CoreException {
        return this.createEntityMethodBuilder(method.getName(), this.afterGetMethodBuilders, true).method(method);
    }

    @Override
    public IEntityMethodBinderBuilder afterGet(
            ObjectAddress methodAddress) throws CoreException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.afterGetMethodBuilders, true).method(methodAddress);
    }

    @Override
    public IEntityMethodBinderBuilder beforeCreate(String methodName)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, this.beforeCreateMethodBuilders).method(methodName);
    }

    @Override
    public IEntityMethodBinderBuilder beforeCreate(Method method)
            throws CoreException {
        return this.createEntityMethodBuilder(method.getName(), this.beforeCreateMethodBuilders).method(method);
    }

    @Override
    public IEntityMethodBinderBuilder beforeCreate(
            ObjectAddress methodAddress) throws CoreException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.beforeCreateMethodBuilders).method(methodAddress);
    }

    @Override
    public IEntityMethodBinderBuilder beforeUpdate(String methodName)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, this.beforeUpdateMethodBuilders).method(methodName);
    }

    @Override
    public IEntityMethodBinderBuilder beforeUpdate(Method method)
            throws CoreException {
        return this.createEntityMethodBuilder(method.getName(), this.beforeUpdateMethodBuilders).method(method);
    }

    @Override
    public IEntityMethodBinderBuilder beforeUpdate(
            ObjectAddress methodAddress) throws CoreException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.beforeUpdateMethodBuilders).method(methodAddress);
    }

    @Override
    public IEntityMethodBinderBuilder beforeDelete(String methodName)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, this.beforeDeleteMethodBuilders).method(methodName);
    }

    @Override
    public IEntityMethodBinderBuilder beforeDelete(Method method)
            throws CoreException {
        return this.createEntityMethodBuilder(method.getName(), this.beforeDeleteMethodBuilders).method(method);
    }

    @Override
    public IEntityMethodBinderBuilder beforeDelete(
            ObjectAddress methodAddress) throws CoreException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.beforeDeleteMethodBuilders).method(methodAddress);
    }

    @Override
    public IEntityMethodBinderBuilder afterCreate(String methodName)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, this.afterCreateMethodBuilders).method(methodName);
    }

    @Override
    public IEntityMethodBinderBuilder afterCreate(Method method)
            throws CoreException {
        return this.createEntityMethodBuilder(method.getName(), this.afterCreateMethodBuilders).method(method);
    }

    @Override
    public IEntityMethodBinderBuilder afterCreate(
            ObjectAddress methodAddress) throws CoreException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.afterCreateMethodBuilders).method(methodAddress);
    }

    @Override
    public IEntityMethodBinderBuilder afterUpdate(String methodName)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, this.afterUpdateMethodBuilders).method(methodName);
    }

    @Override
    public IEntityMethodBinderBuilder afterUpdate(Method method)
            throws CoreException {
        return this.createEntityMethodBuilder(method.getName(), this.afterUpdateMethodBuilders).method(method);
    }

    @Override
    public IEntityMethodBinderBuilder afterUpdate(
            ObjectAddress methodAddress) throws CoreException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.afterUpdateMethodBuilders).method(methodAddress);
    }

    @Override
    public IEntityMethodBinderBuilder afterDelete(String methodName)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, this.afterDeleteMethodBuilders).method(methodName);
    }

    @Override
    public IEntityMethodBinderBuilder afterDelete(Method method)
            throws CoreException {
        return this.createEntityMethodBuilder(method.getName(), this.afterDeleteMethodBuilders).method(method);
    }

    @Override
    public IEntityMethodBinderBuilder afterDelete(
            ObjectAddress methodAddress) throws CoreException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.afterDeleteMethodBuilders).method(methodAddress);
    }

 /*    private List<IEntityMethodBinder> buildBinderBuilder(
            List<Pair<String, EntityMethodBinderBuilder>> builders) {
        return builders.stream().map(pair -> {
            return pair.getValue1();
        }).collect(Collectors.toList());
    }
 */
    @Override
    protected IDomainEntityContext doBuild() {
        return null;
       /*  return new DomainEntityContext(new DomainEntityDefinition(
                this.entityClass,
                this.id,
                this.uuid,
                this.tenantId,
                this.mandatories,
                this.unicities,
                this.updates,
                this.annotatedFields,
                this.annotatedMethods,
                this.buildBinderBuilder(this.afterGetMethodBuilders),
                this.buildBinderBuilder(this.beforeCreateMethodBuilders),
                this.buildBinderBuilder(this.afterCreateMethodBuilders),
                this.buildBinderBuilder(this.beforeUpdateMethodBuilders),
                this.buildBinderBuilder(this.afterCreateMethodBuilders),
                this.buildBinderBuilder(this.beforeDeleteMethodBuilders),
                this.buildBinderBuilder(this.afterDeleteMethodBuilders))); */
    }

    @Override
    protected void doAutoDetection() {

    }
}
