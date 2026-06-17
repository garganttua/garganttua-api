package com.garganttua.api.core.entity;
import com.garganttua.api.core.api.ApiBuilder;
import com.garganttua.api.core.domain.DomainBuilder;
import com.garganttua.core.reflection.annotations.Reflected;

import java.lang.annotation.Annotation;
import com.garganttua.core.reflection.IField;
import com.garganttua.core.reflection.IMethod;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.javatuples.Pair;

import com.garganttua.api.core.entity.EntityMethodBinderBuilder;
import com.garganttua.api.core.entity.EntityContext;
import com.garganttua.api.core.entity.EntityDefinition;
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

import com.garganttua.core.observability.Logger;

@Reflected
public class EntityBuilder<E> extends AbstractAutomaticLinkedBuilder<IEntityBuilder<E>, IDomainBuilder<E>, IEntityContext<E>>
        implements IEntityBuilder<E> {
	private static final Logger log = Logger.getLogger(EntityBuilder.class);


    // Reflection provider is whatever the user installed via IClass.setReflection().
    // Resolved lazily per call so the framework never picks an implementation.
    private static IReflectionProvider provider() {
        return IClass.getReflection();
    }

    private IClass<?> entityClass;

    public IClass<?> getEntityClass() { return this.entityClass; }
    private IObjectQuery objectQuery;
    private ObjectAddress id;
    private ObjectAddress uuid;
    private boolean overwriteUuid = false;
    private com.garganttua.api.commons.entity.IUuidGenerator uuidGenerator;
    private ObjectAddress tenantId;
    private List<ObjectAddress> mandatories = new ArrayList<>();
    private List<Pair<ObjectAddress, UnicityScope>> unicities = new ArrayList<>();
    private List<Pair<ObjectAddress, String>> creates = new ArrayList<>();
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

    /** Free hooks (bound to the EXACT IMethod, possibly external) keyed by hook name. */
    private final java.util.Map<String, List<EntityMethodBinderBuilder<E>>> freeHookBuilders = new java.util.HashMap<>();

    /**
     * Resolver registry to auto-wire free-hook method parameters (injected framework context like
     * {@code @DomainContext}/{@code @ApiContext}). Set by {@code DomainBuilder} before {@code build()};
     * null when no injection context is available (the entity-typed parameter is still wired).
     */
    private com.garganttua.core.injection.IInjectableElementResolver resolverRegistry;

    /** Provided by {@code DomainBuilder.doBuild} so free-hook parameters can be auto-wired at build. */
    public void setResolverRegistry(com.garganttua.core.injection.IInjectableElementResolver resolverRegistry) {
        this.resolverRegistry = resolverRegistry;
    }

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
    public IEntityBuilder<E> overwriteUuid(boolean overwrite) {
        this.overwriteUuid = overwrite;
        return this;
    }

    @Override
    public IEntityBuilder<E> uuidGenerator(com.garganttua.api.commons.entity.IUuidGenerator generator) {
        Objects.requireNonNull(generator, "UUID generator cannot be null");
        this.uuidGenerator = generator;
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
    public IEntityBuilder<E> create(String fieldName) throws ApiException {
        return create(fieldName, null);
    }

    @Override
    public IEntityBuilder<E> create(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");
        return create(field.getName(), null);
    }

    @Override
    public IEntityBuilder<E> create(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        this.creates.add(new Pair<ObjectAddress, String>(
                FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, null).address(), null));
        return this;
    }

    @Override
    public IEntityBuilder<E> create(String fieldName, String authority) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        this.creates.add(new Pair<ObjectAddress, String>(
                FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, null).address(), authority));
        return this;
    }

    @Override
    public IEntityBuilder<E> create(IField field, String authority) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");
        this.creates.add(new Pair<ObjectAddress, String>(
                FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), null).address(), authority));
        return this;
    }

    @Override
    public IEntityBuilder<E> create(ObjectAddress fieldAddress, String authority) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        this.creates.add(new Pair<ObjectAddress, String>(
                FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, null).address(), authority));
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
        return this.bindFreeHook(method, "afterGet");
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
        return this.bindFreeHook(method, "beforeCreate");
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
        return this.bindFreeHook(method, "beforeUpdate");
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
        return this.bindFreeHook(method, "beforeDelete");
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
        return this.bindFreeHook(method, "afterCreate");
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
        return this.bindFreeHook(method, "afterUpdate");
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
        return this.bindFreeHook(method, "afterDelete");
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

    /**
     * Binds a lifecycle hook to the EXACT {@link IMethod} provided — honouring its declaring class
     * (which may be external), its static/instance nature and its signature — rather than re-resolving
     * a no-arg method by name on the entity. The bound method is fed the current entity (its entity-typed
     * parameter) and injected framework context (other parameters) at {@link #buildFreeBinders()}, and is
     * executed by the lifecycle expressions. Mirrors the use case's {@code bind(...).method(...)}.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private IEntityMethodBinderBuilder<E> bindFreeHook(IMethod method, String hookName) throws ApiException {
        Objects.requireNonNull(method, "Method cannot be null");
        IClass<?> declaringClass = method.getDeclaringClass();
        ISupplierBuilder<?, ? extends ISupplier<?>> target;
        if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
            // Static: the target instance is irrelevant — supply null typed to the declaring class.
            target = FixedSupplierBuilder.ofNullable(null, (IClass<Object>) (IClass<?>) declaringClass);
        } else {
            // Instance: materialise one instance of the (external) hook class via its public no-arg ctor.
            try {
                Object instance = declaringClass.getConstructor().newInstance();
                target = new FixedSupplierBuilder(instance, declaringClass);
            } catch (Exception e) {
                throw new ApiException("Failed to instantiate hook class '" + declaringClass.getName()
                        + "' for " + hookName + "(IMethod) — it needs a public no-arg constructor, "
                        + "or make the hook method static.", e);
            }
        }
        EntityMethodBinderBuilder<E> builder = new EntityMethodBinderBuilder<>(this, target);
        builder.method(method);
        this.freeHookBuilders.computeIfAbsent(hookName, k -> new ArrayList<>()).add(builder);
        return builder;
    }

    /**
     * Builds the free-hook binders (keyed by hook name), auto-wiring each bound method's parameters:
     * the parameter typed as the entity receives the current entity ({@link HookEntitySupplierBuilder}),
     * the others are resolved via the {@code @Resolver} registry ({@code @DomainContext}/{@code @ApiContext}/…).
     */
    private java.util.Map<String, List<IMethodBinder<?>>> buildFreeBinders() throws ApiException {
        java.util.Map<String, List<IMethodBinder<?>>> result = new java.util.HashMap<>();
        for (java.util.Map.Entry<String, List<EntityMethodBinderBuilder<E>>> entry : this.freeHookBuilders.entrySet()) {
            List<IMethodBinder<?>> binders = new ArrayList<>();
            for (EntityMethodBinderBuilder<E> builder : entry.getValue()) {
                autowireHookParameters(builder);
                binders.add(builder.build());
            }
            result.put(entry.getKey(), binders);
        }
        return result;
    }

    private void autowireHookParameters(EntityMethodBinderBuilder<E> binder) throws ApiException {
        IMethod method;
        try {
            method = binder.method();
        } catch (com.garganttua.core.dsl.DslException e) {
            return;
        }
        if (method == null) {
            return;
        }
        com.garganttua.core.reflection.IParameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            com.garganttua.core.reflection.IParameter parameter = parameters[i];
            try {
                if (parameter.getType() != null && this.entityClass.getName().equals(parameter.getType().getName())) {
                    binder.withParam(i, new HookEntitySupplierBuilder(this.entityClass));
                } else if (this.resolverRegistry != null) {
                    com.garganttua.core.injection.Resolved resolved =
                            this.resolverRegistry.resolve(parameter.getType(), parameter);
                    if (resolved != null && resolved.resolved()) {
                        binder.withParam(i, resolved.elementSupplier(), resolved.nullable());
                    }
                }
            } catch (com.garganttua.core.injection.DiException | com.garganttua.core.dsl.DslException e) {
                throw new ApiException("Failed to auto-wire parameter " + i + " of hook method '"
                        + method.getName() + "': " + e.getMessage(), e);
            }
        }
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
                new ArrayList<>(this.creates),
                new ArrayList<>(this.updates),
                new ArrayList<>(this.annotatedFields),
                new ArrayList<>(this.annotatedMethods),
                this.buildMethodBinders(this.afterGetMethodBuilders),
                this.buildMethodBinders(this.beforeCreateMethodBuilders),
                this.buildMethodBinders(this.afterCreateMethodBuilders),
                this.buildMethodBinders(this.beforeUpdateMethodBuilders),
                this.buildMethodBinders(this.afterUpdateMethodBuilders),
                this.buildMethodBinders(this.beforeDeleteMethodBuilders),
                this.buildMethodBinders(this.afterDeleteMethodBuilders),
                this.overwriteUuid,
                this.uuidGenerator,
                this.buildFreeBinders());

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
