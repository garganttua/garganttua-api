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
import com.garganttua.api.core.builder.resolver.FieldResolver;
import com.garganttua.api.core.builder.resolver.MethodResolver;
import com.garganttua.api.core.context.application.DomainEntityContext;
import com.garganttua.api.core.definition.DomainEntityDefinition;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IDomainEntityContext;
import com.garganttua.api.spec.engine.IEntityBuilder;
import com.garganttua.api.spec.engine.IEntityMethodBinderBuilder;
import com.garganttua.api.spec.engine.IMethodBinderBuilder;
import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntityBuilder extends AbstractAutomaticLinkedBuilder<IDomainEntityContext, IEntityBuilder, IDomainBuilder>
        implements IEntityBuilder {

    private Class<?> entityClass;
    private Boolean autoDetect = false;
    private IGGObjectQuery objectQuery;
    private GGObjectAddress id;
    private GGObjectAddress uuid;
    private GGObjectAddress tenantId;
    private List<GGObjectAddress> mandatories = new ArrayList<>();
    private List<Pair<GGObjectAddress, UnicityScope>> unicities = new ArrayList<>();
    private List<Pair<GGObjectAddress, String>> updates = new ArrayList<>();
    private List<Pair<GGObjectAddress, Class<? extends Annotation>>> annotatedFields = new ArrayList<>();
    private List<Pair<GGObjectAddress, Class<? extends Annotation>>> annotatedMethods = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder>> afterGetMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder>> beforeCreateMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder>> afterCreateMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder>> beforeUpdateMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder>> afterUpdateMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder>> beforeDeleteMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder>> afterDeleteMethodBuilders = new ArrayList<>();

    public EntityBuilder(Class<?> entityClass, IDomainBuilder domainBuilder) throws BuilderException {
        super(domainBuilder);
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");

        try {
            this.objectQuery = GGObjectQueryFactory.objectQuery(this.entityClass);
        } catch (GGReflectionException e) {
            throw new BuilderException(e.getMessage(), e);
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

        this.unicities.add(new Pair<GGObjectAddress, UnicityScope>(FieldResolver.fieldByField(field, this.entityClass),
                UnicityScope.system));

        return this;
    }

    @Override
    public IEntityBuilder unicity(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.unicities.add(new Pair<GGObjectAddress, UnicityScope>(
                FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass), UnicityScope.system));

        return this;
    }

    @Override
    public IEntityBuilder unicity(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.unicities.add(new Pair<GGObjectAddress, UnicityScope>(
                FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass), UnicityScope.system));

        return this;
    }

    @Override
    public IEntityBuilder unicity(String fieldName, UnicityScope scope) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.unicities.add(new Pair<GGObjectAddress, UnicityScope>(
                FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass), scope));

        return this;
    }

    @Override
    public IEntityBuilder unicity(Field field, UnicityScope scope) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.unicities.add(
                new Pair<GGObjectAddress, UnicityScope>(FieldResolver.fieldByField(field, this.entityClass), scope));

        return this;
    }

    @Override
    public IEntityBuilder unicity(GGObjectAddress fieldAddress, UnicityScope scope) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.unicities.add(new Pair<GGObjectAddress, UnicityScope>(
                FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass), scope));

        return this;
    }

    @Override
    public IEntityBuilder update(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.updates.add(new Pair<GGObjectAddress, String>(
                FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass), null));

        return this;
    }

    @Override
    public IEntityBuilder update(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.updates.add(new Pair<GGObjectAddress, String>(FieldResolver.fieldByField(field, this.entityClass), null));

        return this;
    }

    @Override
    public IEntityBuilder update(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.updates.add(new Pair<GGObjectAddress, String>(
                FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass), null));

        return this;
    }

    @Override
    public IEntityBuilder update(String fieldName, String authority) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.updates.add(new Pair<GGObjectAddress, String>(
                FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass), authority));

        return this;
    }

    @Override
    public IEntityBuilder update(Field field, String authority) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.updates
                .add(new Pair<GGObjectAddress, String>(FieldResolver.fieldByField(field, this.entityClass), authority));

        return this;
    }

    @Override
    public IEntityBuilder update(GGObjectAddress fieldAddress, String authority) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.updates.add(new Pair<GGObjectAddress, String>(
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
            throw new BuilderException(e.getMessage(), e);
        }
    }

    @Override
    public IEntityBuilder annotation(Field field, Class<? extends Annotation> annotation) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");
        Objects.requireNonNull(annotation, "Annotation cannot be null");

        GGObjectAddress address = FieldResolver.fieldByField(field, this.entityClass);

        Pair<GGObjectAddress, Class<? extends Annotation>> candidate = new Pair<>(address, annotation);

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

        GGObjectAddress address = MethodResolver.methodByMethod(method, this.entityClass);

        Pair<GGObjectAddress, Class<? extends Annotation>> candidate = new Pair<>(address, annotation);
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
            throw new BuilderException(e.getMessage(), e);
        }

    }

    private IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> createEntityMethodBuilder(
            String methodName, List<Pair<String, EntityMethodBinderBuilder>> list)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, list, false);
    }

    private IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> createEntityMethodBuilder(
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
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> afterGet(String methodName)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, this.afterGetMethodBuilders, true).method(methodName);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> afterGet(Method method)
            throws CoreException {
        return this.createEntityMethodBuilder(method.getName(), this.afterGetMethodBuilders, true).method(method);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> afterGet(
            GGObjectAddress methodAddress) throws CoreException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.afterGetMethodBuilders, true).method(methodAddress);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> beforeCreate(String methodName)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, this.beforeCreateMethodBuilders).method(methodName);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> beforeCreate(Method method)
            throws CoreException {
        return this.createEntityMethodBuilder(method.getName(), this.beforeCreateMethodBuilders).method(method);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> beforeCreate(
            GGObjectAddress methodAddress) throws CoreException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.beforeCreateMethodBuilders).method(methodAddress);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> beforeUpdate(String methodName)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, this.beforeUpdateMethodBuilders).method(methodName);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> beforeUpdate(Method method)
            throws CoreException {
        return this.createEntityMethodBuilder(method.getName(), this.beforeUpdateMethodBuilders).method(method);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> beforeUpdate(
            GGObjectAddress methodAddress) throws CoreException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.beforeUpdateMethodBuilders).method(methodAddress);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> beforeDelete(String methodName)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, this.beforeDeleteMethodBuilders).method(methodName);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> beforeDelete(Method method)
            throws CoreException {
        return this.createEntityMethodBuilder(method.getName(), this.beforeDeleteMethodBuilders).method(method);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> beforeDelete(
            GGObjectAddress methodAddress) throws CoreException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.beforeDeleteMethodBuilders).method(methodAddress);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> afterCreate(String methodName)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, this.afterCreateMethodBuilders).method(methodName);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> afterCreate(Method method)
            throws CoreException {
        return this.createEntityMethodBuilder(method.getName(), this.afterCreateMethodBuilders).method(method);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> afterCreate(
            GGObjectAddress methodAddress) throws CoreException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.afterCreateMethodBuilders).method(methodAddress);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> afterUpdate(String methodName)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, this.afterUpdateMethodBuilders).method(methodName);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> afterUpdate(Method method)
            throws CoreException {
        return this.createEntityMethodBuilder(method.getName(), this.afterUpdateMethodBuilders).method(method);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> afterUpdate(
            GGObjectAddress methodAddress) throws CoreException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.afterUpdateMethodBuilders).method(methodAddress);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> afterDelete(String methodName)
            throws CoreException {
        return this.createEntityMethodBuilder(methodName, this.afterDeleteMethodBuilders).method(methodName);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> afterDelete(Method method)
            throws CoreException {
        return this.createEntityMethodBuilder(method.getName(), this.afterDeleteMethodBuilders).method(method);
    }

    @Override
    public IMethodBinderBuilder<IEntityMethodBinderBuilder, IEntityBuilder> afterDelete(
            GGObjectAddress methodAddress) throws CoreException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.afterDeleteMethodBuilders).method(methodAddress);
    }

    private List<IMethodBinderBuilder<?, ?>> buildBinderBuilder(
            List<Pair<String, EntityMethodBinderBuilder>> builders) {
        return builders.stream().map(pair -> {
            return pair.getValue1();
        }).collect(Collectors.toList());
    }

    @Override
    protected IDomainEntityContext doBuild() {
        return new DomainEntityContext(new DomainEntityDefinition(
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
                this.buildBinderBuilder(this.afterDeleteMethodBuilders)));
    }

    @Override
    protected void doAutoDetection() {

    }
}
