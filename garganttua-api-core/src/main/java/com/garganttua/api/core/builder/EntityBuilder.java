package com.garganttua.api.core.builder;

import java.lang.annotation.Annotation;
import com.garganttua.core.reflection.IField;
import com.garganttua.core.reflection.IMethod;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.javatuples.Pair;

import com.garganttua.api.core.builder.binder.EntityMethodBinderBuilder;
import com.garganttua.api.core.context.EntityContext;
import com.garganttua.api.core.definition.EntityDefinition;
import com.garganttua.api.commons.context.IEntityContext;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.IEntityBuilder;
import com.garganttua.api.commons.context.dsl.IEntityMethodBinderBuilder;
import com.garganttua.api.commons.entity.annotations.UnicityScope;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.IReflectionProvider;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.fields.FieldResolver;
import com.garganttua.core.reflection.query.ObjectQueryFactory;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntityBuilder<E> extends AbstractAutomaticLinkedBuilder<IEntityBuilder<E>, IDomainBuilder<E>, IEntityContext<E>>
        implements IEntityBuilder<E> {

    // Reflection provider is whatever the user installed via IClass.setReflection().
    // Resolved lazily per call so the framework never picks an implementation.
    private static IReflectionProvider provider() {
        return IClass.getReflection();
    }

    @Getter
    private IClass<?> entityClass;
    private IObjectQuery objectQuery;
    private ObjectAddress id;
    private ObjectAddress uuid;
    private ObjectAddress tenantId;
    private List<ObjectAddress> mandatories = new ArrayList<>();
    private List<Pair<ObjectAddress, UnicityScope>> unicities = new ArrayList<>();
    private List<Pair<ObjectAddress, String>> updates = new ArrayList<>();
    private List<Pair<ObjectAddress, IClass<? extends Annotation>>> annotatedFields = new ArrayList<>();
    private List<Pair<ObjectAddress, IClass<? extends Annotation>>> annotatedMethods = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder<E>>> afterGetMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder<E>>> beforeCreateMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder<E>>> afterCreateMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder<E>>> beforeUpdateMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder<E>>> afterUpdateMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder<E>>> beforeDeleteMethodBuilders = new ArrayList<>();
    private List<Pair<String, EntityMethodBinderBuilder<E>>> afterDeleteMethodBuilders = new ArrayList<>();

    public EntityBuilder(IClass<?> entityClass, IDomainBuilder<E> domainBuilder) throws ApiException {
        super(domainBuilder);
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");

        try {
            this.objectQuery = ObjectQueryFactory.objectQuery(this.entityClass, provider());
        } catch (ReflectionException e) {
            throw new ApiException(e.getMessage(), e);
        }
    }

    @Override
    public IEntityBuilder<E> id(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.id = FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IEntityBuilder<E> id(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.id = FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IEntityBuilder<E> id(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.id = FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IEntityBuilder<E> uuid(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.uuid = FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IEntityBuilder<E> uuid(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.uuid = FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IEntityBuilder<E> uuid(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.uuid = FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IEntityBuilder<E> tenantId(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.tenantId = FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IEntityBuilder<E> tenantId(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.tenantId = FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IEntityBuilder<E> tenantId(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.tenantId = FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IEntityBuilder<E> mandatory(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.mandatories.add(FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), null).address());

        return this;
    }

    @Override
    public IEntityBuilder<E> mandatory(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.mandatories.add(FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, null).address());

        return this;
    }

    @Override
    public IEntityBuilder<E> mandatory(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.mandatories.add(FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, null).address());

        return this;
    }

    @Override
    public IEntityBuilder<E> unicity(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.unicities.add(new Pair<ObjectAddress, UnicityScope>(FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), null).address(),
                UnicityScope.system));

        return this;
    }

    @Override
    public IEntityBuilder<E> unicity(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.unicities.add(new Pair<ObjectAddress, UnicityScope>(
                FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, null).address(), UnicityScope.system));

        return this;
    }

    @Override
    public IEntityBuilder<E> unicity(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.unicities.add(new Pair<ObjectAddress, UnicityScope>(
                FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, null).address(), UnicityScope.system));

        return this;
    }

    @Override
    public IEntityBuilder<E> unicity(String fieldName, UnicityScope scope) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.unicities.add(new Pair<ObjectAddress, UnicityScope>(
                FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, null).address(), scope));

        return this;
    }

    @Override
    public IEntityBuilder<E> unicity(IField field, UnicityScope scope) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.unicities.add(
                new Pair<ObjectAddress, UnicityScope>(FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), null).address(), scope));

        return this;
    }

    @Override
    public IEntityBuilder<E> unicity(ObjectAddress fieldAddress, UnicityScope scope) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.unicities.add(new Pair<ObjectAddress, UnicityScope>(
                FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, null).address(), scope));

        return this;
    }

    @Override
    public IEntityBuilder<E> update(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.updates.add(new Pair<ObjectAddress, String>(
                FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, null).address(), null));

        return this;
    }

    @Override
    public IEntityBuilder<E> update(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.updates.add(new Pair<ObjectAddress, String>(FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), null).address(), null));

        return this;
    }

    @Override
    public IEntityBuilder<E> update(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.updates.add(new Pair<ObjectAddress, String>(
                FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, null).address(), null));

        return this;
    }

    @Override
    public IEntityBuilder<E> update(String fieldName, String authority) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.updates.add(new Pair<ObjectAddress, String>(
                FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, null).address(), authority));

        return this;
    }

    @Override
    public IEntityBuilder<E> update(IField field, String authority) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.updates
                .add(new Pair<ObjectAddress, String>(FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), null).address(), authority));

        return this;
    }

    @Override
    public IEntityBuilder<E> update(ObjectAddress fieldAddress, String authority) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.updates.add(new Pair<ObjectAddress, String>(
                FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, null).address(), authority));

        return this;
    }

    @Override
    public IEntityBuilder<E> annotation(String elementName, IClass<? extends Annotation> annotation)
            throws ApiException {
        Objects.requireNonNull(elementName, "Element name cannot be null");
        Objects.requireNonNull(annotation, "Annotation cannot be null");

        try {
            ObjectAddress address = this.objectQuery.address(elementName);
            return this.annotation(address, annotation);
        } catch (ReflectionException e) {
            throw new ApiException(e.getMessage(), e);
        }
    }

    @Override
    public IEntityBuilder<E> annotation(IField field, IClass<? extends Annotation> annotation) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");
        Objects.requireNonNull(annotation, "Annotation cannot be null");

        ObjectAddress address = FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), null).address();

        Pair<ObjectAddress, IClass<? extends Annotation>> candidate = new Pair<>(address, annotation);

        if (this.annotatedFields.contains(candidate)) {
            return this;
        }

        this.annotatedFields.add(candidate);

        return this;
    }

    @Override
    public IEntityBuilder<E> annotation(IMethod method, IClass<? extends Annotation> annotation) throws ApiException {
        Objects.requireNonNull(method, "Method cannot be null");
        Objects.requireNonNull(annotation, "Annotation cannot be null");

        // TODO: Implement when MethodResolver API is clarified
        throw new UnsupportedOperationException("Unimplemented method 'annotation(Method, IClass)'");
    }

    @Override
    public IEntityBuilder<E> annotation(ObjectAddress elementAddress, IClass<? extends Annotation> annotation)
            throws ApiException {
        Objects.requireNonNull(elementAddress, "Element address cannot be null");
        Objects.requireNonNull(annotation, "Annotation cannot be null");

        try {
            Object leaf = this.objectQuery.find(elementAddress).getLast();
            if (leaf instanceof IField f)
                this.annotation(f, annotation);
            else if (leaf instanceof IMethod m)
                this.annotation(m, annotation);
            else
                throw new ApiException("Unsupported element type: " + leaf.getClass().getName());

            return this;
        } catch (ReflectionException e) {
            throw new ApiException(e.getMessage(), e);
        }

    }

    private IEntityMethodBinderBuilder<E> createEntityMethodBuilder(
            String methodName, List<Pair<String, EntityMethodBinderBuilder<E>>> list)
            throws ApiException {
        return this.createEntityMethodBuilder(methodName, list, false);
    }

    private IEntityMethodBinderBuilder<E> createEntityMethodBuilder(
            String methodName, List<Pair<String, EntityMethodBinderBuilder<E>>> list, boolean collection)
            throws ApiException {
        // Create a supplier builder that will supply the entity instance at runtime
        // The entity instance will be set when the binder is executed
        ISupplierBuilder<Object, ISupplier<Object>> supplierBuilder = FixedSupplierBuilder.ofNullable(null, (IClass<Object>) (IClass<?>) this.entityClass);

        EntityMethodBinderBuilder<E> builder = new EntityMethodBinderBuilder<>(this, supplierBuilder, collection);
        // Entity lifecycle methods are void and take no parameters
        builder.method(methodName, IClass.getClass(Void.class));

        list.add(new Pair<>(methodName, builder));
        return builder;
    }

    @Override
    public IEntityMethodBinderBuilder<E> afterGet(String methodName)
            throws ApiException {
        return this.createEntityMethodBuilder(methodName, this.afterGetMethodBuilders, true);
    }

    @Override
    public IEntityMethodBinderBuilder<E> afterGet(IMethod method)
            throws ApiException {
        return this.createEntityMethodBuilder(method.getName(), this.afterGetMethodBuilders, true);
    }

    @Override
    public IEntityMethodBinderBuilder<E> afterGet(
            ObjectAddress methodAddress) throws ApiException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.afterGetMethodBuilders, true);
    }

    @Override
    public IEntityMethodBinderBuilder<E> beforeCreate(String methodName)
            throws ApiException {
        return this.createEntityMethodBuilder(methodName, this.beforeCreateMethodBuilders);
    }

    @Override
    public IEntityMethodBinderBuilder<E> beforeCreate(IMethod method)
            throws ApiException {
        return this.createEntityMethodBuilder(method.getName(), this.beforeCreateMethodBuilders);
    }

    @Override
    public IEntityMethodBinderBuilder<E> beforeCreate(
            ObjectAddress methodAddress) throws ApiException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.beforeCreateMethodBuilders);
    }

    @Override
    public IEntityMethodBinderBuilder<E> beforeUpdate(String methodName)
            throws ApiException {
        return this.createEntityMethodBuilder(methodName, this.beforeUpdateMethodBuilders);
    }

    @Override
    public IEntityMethodBinderBuilder<E> beforeUpdate(IMethod method)
            throws ApiException {
        return this.createEntityMethodBuilder(method.getName(), this.beforeUpdateMethodBuilders);
    }

    @Override
    public IEntityMethodBinderBuilder<E> beforeUpdate(
            ObjectAddress methodAddress) throws ApiException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.beforeUpdateMethodBuilders);
    }

    @Override
    public IEntityMethodBinderBuilder<E> beforeDelete(String methodName)
            throws ApiException {
        return this.createEntityMethodBuilder(methodName, this.beforeDeleteMethodBuilders);
    }

    @Override
    public IEntityMethodBinderBuilder<E> beforeDelete(IMethod method)
            throws ApiException {
        return this.createEntityMethodBuilder(method.getName(), this.beforeDeleteMethodBuilders);
    }

    @Override
    public IEntityMethodBinderBuilder<E> beforeDelete(
            ObjectAddress methodAddress) throws ApiException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.beforeDeleteMethodBuilders);
    }

    @Override
    public IEntityMethodBinderBuilder<E> afterCreate(String methodName)
            throws ApiException {
        return this.createEntityMethodBuilder(methodName, this.afterCreateMethodBuilders);
    }

    @Override
    public IEntityMethodBinderBuilder<E> afterCreate(IMethod method)
            throws ApiException {
        return this.createEntityMethodBuilder(method.getName(), this.afterCreateMethodBuilders);
    }

    @Override
    public IEntityMethodBinderBuilder<E> afterCreate(
            ObjectAddress methodAddress) throws ApiException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.afterCreateMethodBuilders);
    }

    @Override
    public IEntityMethodBinderBuilder<E> afterUpdate(String methodName)
            throws ApiException {
        return this.createEntityMethodBuilder(methodName, this.afterUpdateMethodBuilders);
    }

    @Override
    public IEntityMethodBinderBuilder<E> afterUpdate(IMethod method)
            throws ApiException {
        return this.createEntityMethodBuilder(method.getName(), this.afterUpdateMethodBuilders);
    }

    @Override
    public IEntityMethodBinderBuilder<E> afterUpdate(
            ObjectAddress methodAddress) throws ApiException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.afterUpdateMethodBuilders);
    }

    @Override
    public IEntityMethodBinderBuilder<E> afterDelete(String methodName)
            throws ApiException {
        return this.createEntityMethodBuilder(methodName, this.afterDeleteMethodBuilders);
    }

    @Override
    public IEntityMethodBinderBuilder<E> afterDelete(IMethod method)
            throws ApiException {
        return this.createEntityMethodBuilder(method.getName(), this.afterDeleteMethodBuilders);
    }

    @Override
    public IEntityMethodBinderBuilder<E> afterDelete(
            ObjectAddress methodAddress) throws ApiException {
        return this.createEntityMethodBuilder(methodAddress.getElement(methodAddress.length() - 1),
                this.afterDeleteMethodBuilders);
    }

    private List<IMethodBinder<Void>> buildMethodBinders(
            List<Pair<String, EntityMethodBinderBuilder<E>>> builders) throws ApiException {
        List<IMethodBinder<Void>> binders = new ArrayList<>();
        for (Pair<String, EntityMethodBinderBuilder<E>> pair : builders) {
            binders.add(pair.getValue1().build());
        }
        return binders;
    }

    @Override
    protected synchronized IEntityContext<E> doBuild() throws ApiException {
        this.throwExceptionIfNoUuid();
        this.throwExceptionIfNoTenantId();
        this.throwExceptionIfNoId();

        EntityDefinition<E> definition = new EntityDefinition<>(
                (IClass<E>) this.entityClass,
                this.id,
                this.uuid,
                this.tenantId,
                new ArrayList<>(this.mandatories),
                new ArrayList<>(this.unicities),
                new ArrayList<>(this.updates),
                new ArrayList<>(this.annotatedFields),
                new ArrayList<>(this.annotatedMethods),
                this.buildMethodBinders(this.afterGetMethodBuilders),
                this.buildMethodBinders(this.beforeCreateMethodBuilders),
                this.buildMethodBinders(this.afterCreateMethodBuilders),
                this.buildMethodBinders(this.beforeUpdateMethodBuilders),
                this.buildMethodBinders(this.afterUpdateMethodBuilders),
                this.buildMethodBinders(this.beforeDeleteMethodBuilders),
                this.buildMethodBinders(this.afterDeleteMethodBuilders));

        return new EntityContext<>(definition);
    }

    private void throwExceptionIfNoUuid() throws ApiException {
        if (this.uuid == null)
            throw new ApiException("No uuid field declared on entity " + this.entityClass.getSimpleName()
                    + ". Add .uuid(\"uuid\") (or the matching field name) on the entity builder:\n"
                    + "\n"
                    + "    .domain(" + this.entityClass.getSimpleName() + ".class)\n"
                    + "        .entity()\n"
                    + "            .id(\"id\")\n"
                    + "            .uuid(\"uuid\")                          // <- missing\n"
                    + "            .tenantId(\"tenantId\")\n"
                    + "        .up()");
    }

    private void throwExceptionIfNoTenantId() throws ApiException {
        if (this.tenantId == null && isMultiTenantEnabled() && !isTenantDomain()) {
            throw new ApiException("No tenantId field declared on entity " + this.entityClass.getSimpleName()
                    + ". Multi-tenancy is enabled and this domain is not the tenant domain — every entity needs a tenantId. "
                    + "Either add .tenantId(\"tenantId\") on the entity builder, mark this domain as the tenant via .tenant(true), "
                    + "or disable multi-tenancy globally via apiBuilder.multiTenant(false).");
        }
    }

    private boolean isMultiTenantEnabled() {
        try {
            return up().up() instanceof ApiBuilder acb && acb.isMultiTenant();
        } catch (Exception e) {
            return true; // default to strict
        }
    }

    /**
     * True when the parent domain is marked {@code .tenant(true)} — i.e. the
     * entity IS the tenant. Such entities don't carry a tenantId field; their
     * uuid plays that role downstream (see {@code FilterContext.buildTenantFilter}).
     */
    private boolean isTenantDomain() {
        try {
            return up() instanceof DomainBuilder<?> db && db.isTenantDomain();
        } catch (Exception e) {
            return false;
        }
    }

    private void throwExceptionIfNoId() throws ApiException {
        if (this.id == null)
            throw new ApiException("No id field declared on entity " + this.entityClass.getSimpleName()
                    + ". Add .id(\"id\") (or the matching field name) on the entity builder:\n"
                    + "\n"
                    + "    .domain(" + this.entityClass.getSimpleName() + ".class)\n"
                    + "        .entity()\n"
                    + "            .id(\"id\")                              // <- missing\n"
                    + "            .uuid(\"uuid\").tenantId(\"tenantId\")\n"
                    + "        .up()");
    }

    @Override
    protected void doAutoDetection() {

    }
}
