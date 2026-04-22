package com.garganttua.api.core.builder;

import com.garganttua.core.reflection.IField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.garganttua.api.core.builder.binder.DomainStartupBinderBuilder;
import com.garganttua.api.core.context.Domain;
import com.garganttua.api.core.context.DtoContext;
import com.garganttua.api.core.context.EntityContext;
import com.garganttua.api.core.definition.DomainDefinition;
import com.garganttua.api.core.definition.EntityDefinition;
import com.garganttua.api.core.definition.UseCaseDefinition;
import com.garganttua.api.core.definition.WorkflowDefinition;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.Pluralizer;
import com.garganttua.api.commons.operation.BusinessOperation;
import com.garganttua.api.commons.context.BuildingStage;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.IDtoContext;
import com.garganttua.api.commons.context.IEntityContext;
import com.garganttua.api.commons.operation.Scope;
import com.garganttua.api.commons.operation.TechnicalOperation;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.IDomainStartupBinderBuilder;
import com.garganttua.api.commons.context.dsl.IDomainWorkflowBuilder;
import com.garganttua.api.commons.context.dsl.IDtoBuilder;
import com.garganttua.api.commons.context.dsl.IEntityBuilder;
import com.garganttua.api.commons.context.dsl.IUseCaseBuilder;
import com.garganttua.api.commons.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.api.commons.definition.IDtoDefinition;
import com.garganttua.api.commons.definition.IUseCaseDefinition;
import com.garganttua.api.commons.definition.IWorkflowDefinition;
import com.garganttua.api.commons.event.IEventPublisher;
import com.garganttua.api.commons.endpoint.IEndpoint;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.expression.dsl.IExpressionContextBuilder;
import java.util.Set;

import com.garganttua.core.injection.BeanDefinition;
import com.garganttua.core.injection.IBeanFactory;
import com.garganttua.core.injection.IInjectableElementResolverBuilder;
import com.garganttua.core.injection.context.dsl.BeanFactoryBuilder;
import com.garganttua.core.injection.context.dsl.IInjectionContextBuilder;
import com.garganttua.core.injection.context.dsl.InjectionContextBuilder;
import com.garganttua.core.mapper.IMapper;
import com.garganttua.core.mapper.MapperException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.IReflectionProvider;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.fields.FieldResolver;
import com.garganttua.core.reflection.query.ObjectQueryFactory;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;
import com.garganttua.core.supply.dsl.ISupplierBuilder;
import com.garganttua.core.workflow.IWorkflow;

public class DomainBuilder<E>
        extends AbstractAutomaticLinkedBuilder<IDomainBuilder<E>, IApiBuilder, IDomain<E>>
        implements IDomainBuilder<E> {

    private static final IReflectionProvider PROVIDER = new RuntimeReflectionProvider();

    private volatile String domainName;
    private volatile IClass<?> entityClass;

    private IMapper mapper = DefaultMapper.mapper();

    private final List<IDomainStartupBinderBuilder> startupBinderBuilders = new CopyOnWriteArrayList<>();
    private final List<ISupplierBuilder<? extends IEndpoint, ? extends ISupplier<? extends IEndpoint>>> interfaces = new CopyOnWriteArrayList<>();
    private final List<ISupplierBuilder<?, ? extends ISupplier<?>>> events = new CopyOnWriteArrayList<>();

    private volatile boolean publik = false;
    private volatile boolean tenant = false;
    private volatile boolean doInjection = false;
    private volatile IEntityBuilder<E> entityBuilder;
    private final List<Object> createEntities = new CopyOnWriteArrayList<>();
    private final List<Object> upsertEntities = new CopyOnWriteArrayList<>();
    private volatile IObjectQuery objectQuery;
    private volatile ObjectAddress owner;
    private volatile ObjectAddress owned;
    private volatile ObjectAddress shared;
    private volatile ObjectAddress hiddenable;
    private volatile ObjectAddress geolocalized;
    private volatile ObjectAddress superOwner;
    private volatile ObjectAddress superTenant;
    private volatile IDomainSecurityBuilder<E> securityBuilder;
    private final Map<IClass<?>, IDtoBuilder> dtos = new ConcurrentHashMap<>();
    private final Map<String, IUseCaseBuilder<?, ?, E>> useCases = new ConcurrentHashMap<>();
    private final Map<String, DomainWorkflowBuilder<E>> workflows = new ConcurrentHashMap<>();

    private volatile IInjectionContextBuilder injectionContextBuilder;
    private volatile IExpressionContextBuilder expressionContextBuilder;

    public void setDependencyBuilders(IInjectionContextBuilder injectionContextBuilder,
            IExpressionContextBuilder expressionContextBuilder) {
        this.injectionContextBuilder = injectionContextBuilder;
        this.expressionContextBuilder = expressionContextBuilder;
    }

    public DomainBuilder(IApiBuilder builder, String domainName)
            throws ApiException {
        super(builder);
        this.domainName = Objects.requireNonNull(domainName, "Domain name cannot be null");
        initDefaultCrudWorkflows();
    }

    public DomainBuilder(IApiBuilder builder, IClass<?> entityClass) throws ApiException {
        super(builder);
        this.entityClass = Objects.requireNonNull(entityClass, "Entity Class cannot be null");
        try {
            this.objectQuery = ObjectQueryFactory.objectQuery(this.entityClass, PROVIDER);
        } catch (ReflectionException e) {
            throw new ApiException(e.getMessage(), e);
        }
        this.domainName = Pluralizer.toPlural(this.entityClass.getSimpleName().toLowerCase());
        this.securityBuilder = new DomainSecurityBuilder<>(this, this.interfaces, this.entityClass);
        this.entityBuilder = this.entity(this.entityClass);
        initDefaultCrudWorkflows();
    }

    @Override
    public IDomainStartupBinderBuilder startup(BuildingStage stage, ISupplierBuilder<?, ? extends ISupplier<?>> supplier)
            throws ApiException {
        DomainStartupBinderBuilder binder = new DomainStartupBinderBuilder(this, supplier);
        this.startupBinderBuilders.add(binder);
        return binder;
    }

    @Override
    public IDomainBuilder<E> interfasse(ISupplierBuilder<? extends IEndpoint, ? extends ISupplier<? extends IEndpoint>> bean) throws ApiException {
        this.interfaces.add(bean);
        return this;
    }

    @Override
    public IDomainBuilder<E> events(ISupplierBuilder<?, ? extends ISupplier<?>> bean) throws ApiException {
        IClass<?> suppliedClass = bean.getSuppliedClass();
        if (!IClass.getClass(IEventPublisher.class).isAssignableFrom(suppliedClass)) {
            throw new ApiException(
                    "Bean " + suppliedClass.getName() + " does not implement IEventPublisher");
        }
        this.events.add(bean);
        return this;
    }

    @Override
    public IDomainBuilder<E> events(IEventPublisher eventPublisher) throws ApiException {
        this.events.add(FixedSupplierBuilder.of(
                Objects.requireNonNull(eventPublisher, "EventPublisher cannot be null")));
        return this;
    }

    @Override
    public IDomainBuilder<E> tenant(boolean b) throws ApiException {
        if (b && this.up() instanceof ApiBuilder acb && !acb.isMultiTenant()) {
            throw new ApiException("Cannot mark domain as tenant when multi-tenancy is disabled");
        }
        this.tenant = b;
        return this;
    }

    @Override
    public IDomainBuilder<E> owner(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        if (this.entityBuilder == null) {
            throw new ApiException("Entity class must be defined first");
        }

        this.owner = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, fieldName, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> owner(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");
        if (this.entityBuilder == null) {
            throw new ApiException("Entity class must be defined first");
        }

        this.owner = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, field.getName(), IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> owner(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        if (this.entityBuilder == null) {
            throw new ApiException("Entity class must be defined first");
        }

        this.owner = FieldResolver.fieldByAddress(this.entityClass, PROVIDER, fieldAddress, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> owned(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        if (this.entityBuilder == null) {
            throw new ApiException("Entity class must be defined first");
        }

        this.owned = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, fieldName, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> owned(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");
        if (this.entityBuilder == null) {
            throw new ApiException("Entity class must be defined first");
        }

        this.owned = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, field.getName(), IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> owned(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        if (this.entityBuilder == null) {
            throw new ApiException("Entity class must be defined first");
        }

        this.owned = FieldResolver.fieldByAddress(this.entityClass, PROVIDER, fieldAddress, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> publik() {
        this.publik = true;
        return this;
    }

    @Override
    public IDomainBuilder<E> doInjection(boolean enabled) {
        this.doInjection = enabled;
        return this;
    }

    @Override
    public IDomainBuilder<E> shared(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");
        if (this.entityBuilder == null) {
            throw new ApiException("Entity class must be defined first");
        }

        this.shared = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, field.getName(), IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> shared(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        if (this.entityBuilder == null) {
            throw new ApiException("Entity class must be defined first");
        }

        this.shared = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, fieldName, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> shared(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        if (this.entityBuilder == null) {
            throw new ApiException("Entity class must be defined first");
        }

        this.shared = FieldResolver.fieldByAddress(this.entityClass, PROVIDER, fieldAddress, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> hiddenable(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        if (this.entityBuilder == null) {
            throw new ApiException("Entity class must be defined first");
        }

        this.hiddenable = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, fieldName, IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> hiddenable(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");
        if (this.entityBuilder == null) {
            throw new ApiException("Entity class must be defined first");
        }

        this.hiddenable = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, field.getName(), IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> hiddenable(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        if (this.entityBuilder == null) {
            throw new ApiException("Entity class must be defined first");
        }

        this.hiddenable = FieldResolver.fieldByAddress(this.entityClass, PROVIDER, fieldAddress, IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> geolocalized(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        if (this.entityBuilder == null) {
            throw new ApiException("Entity class must be defined first");
        }

        this.geolocalized = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, fieldName, IClass.getClass(Object.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> geolocalized(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");
        if (this.entityBuilder == null) {
            throw new ApiException("Entity class must be defined first");
        }

        this.geolocalized = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, field.getName(), IClass.getClass(Object.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> geolocalized(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");
        if (this.entityBuilder == null) {
            throw new ApiException("Entity class must be defined first");
        }

        this.geolocalized = FieldResolver.fieldByAddress(this.entityClass, PROVIDER, fieldAddress, IClass.getClass(Object.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> superOwner(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        if (this.entityBuilder == null) {
            throw new ApiException("Entity class must be defined first");
        }
        this.superOwner = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, fieldName, IClass.getClass(Boolean.class)).address();
        return this;
    }

    @Override
    public IDomainBuilder<E> superOwner(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");
        if (this.entityBuilder == null) {
            throw new ApiException("Entity class must be defined first");
        }
        this.superOwner = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, field.getName(), IClass.getClass(Boolean.class)).address();
        return this;
    }

    @Override
    public IDomainBuilder<E> superOwner(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");
        if (this.entityBuilder == null) {
            throw new ApiException("Entity class must be defined first");
        }
        this.superOwner = FieldResolver.fieldByAddress(this.entityClass, PROVIDER, fieldAddress, IClass.getClass(Boolean.class)).address();
        return this;
    }

    @Override
    public IDomainBuilder<E> superTenant(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        if (this.entityBuilder == null) {
            throw new ApiException("Entity class must be defined first");
        }
        this.superTenant = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, fieldName, IClass.getClass(Boolean.class)).address();
        return this;
    }

    @Override
    public IDomainBuilder<E> superTenant(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");
        if (this.entityBuilder == null) {
            throw new ApiException("Entity class must be defined first");
        }
        this.superTenant = FieldResolver.fieldByFieldName(this.entityClass, PROVIDER, field.getName(), IClass.getClass(Boolean.class)).address();
        return this;
    }

    @Override
    public IDomainBuilder<E> superTenant(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");
        if (this.entityBuilder == null) {
            throw new ApiException("Entity class must be defined first");
        }
        this.superTenant = FieldResolver.fieldByAddress(this.entityClass, PROVIDER, fieldAddress, IClass.getClass(Boolean.class)).address();
        return this;
    }

    public IEntityBuilder<E> entity(IClass<?> entityClass) throws ApiException {
        Objects.requireNonNull(entityClass, "Entity class cannot be null");

        if (this.entityBuilder != null && Objects.equals(entityClass, this.entityClass)) {
            throw new ApiException(
                    "Entity Class is already set with class " + entityClass.getSimpleName());
        }

        this.entityBuilder = new EntityBuilder<>(entityClass, this);
        this.entityClass = entityClass;

        try {
            this.objectQuery = ObjectQueryFactory.objectQuery(this.entityClass, PROVIDER);
        } catch (ReflectionException e) {
            throw new ApiException(e.getMessage(), e);
        }

        this.securityBuilder = new DomainSecurityBuilder<>(this, this.interfaces, this.entityClass);

        return this.entityBuilder;
    }

    @Override
    public IDomainSecurityBuilder<E> security() throws ApiException {
        if (this.securityBuilder == null)
            throw new ApiException("Security builder is null, please set entity class first");
        return this.securityBuilder;
    }

    @Override
    public <D> IDtoBuilder<E, D> dto(IClass<D> dtoClass) throws ApiException {
        if( this.entityClass == null )
            throw new ApiException("Entity class must be set before declaring a dto");

        IDtoBuilder<E, D> dtoBuilder = (IDtoBuilder<E, D>) this.dtos.computeIfAbsent(dtoClass, clazz -> {
            return new DtoBuilder<>(clazz, this);
        });

        try {
            this.mapper.recordMappingConfiguration(this.entityClass, dtoClass);
        } catch (MapperException e) {
            // Mapper configuration may fail if no mapping annotations are present
            // This is acceptable - mapping will need to be done manually
        }

        return dtoBuilder;
    }

    @Override
    public <I, O> IUseCaseBuilder<I, O, E> useCase(String useCaseName, IClass<I> inputType, IClass<O> outputType) {
        Objects.requireNonNull(useCaseName, "Use case name cannot be null");

        return (IUseCaseBuilder<I, O, E>) this.useCases.computeIfAbsent(useCaseName,
                name -> new UseCaseBuilder<I, O, E>(name, this));
    }

    @Override
    public IDomainWorkflowBuilder<E> workflow(String workflowName) {
        Objects.requireNonNull(workflowName, "Workflow name cannot be null");
        return this.workflows.computeIfAbsent(workflowName,
                name -> new DomainWorkflowBuilder<>(name, this));
    }

    @Override
    public IDomainBuilder<E> create(Object entity) {
        this.createEntities.add(entity);
        return this;
    }

    @Override
    public IDomainBuilder<E> upsert(Object entity) {
        this.upsertEntities.add(entity);
        return this;
    }

    @Override
    public IDomainBuilder<E> creation(boolean enabled) {
        toggleCrudWorkflow(BusinessOperation.create.getLabel(), enabled);
        return this;
    }

    @Override
    public IDomainBuilder<E> readAll(boolean enabled) {
        toggleCrudWorkflow(BusinessOperation.readAll.getLabel(), enabled);
        return this;
    }

    @Override
    public IDomainBuilder<E> readOne(boolean enabled) {
        toggleCrudWorkflow(BusinessOperation.readOne.getLabel(), enabled);
        return this;
    }

    @Override
    public IDomainBuilder<E> update(boolean enabled) {
        toggleCrudWorkflow(BusinessOperation.update.getLabel(), enabled);
        return this;
    }

    @Override
    public IDomainBuilder<E> deleteOne(boolean enabled) {
        toggleCrudWorkflow(BusinessOperation.deleteOne.getLabel(), enabled);
        return this;
    }

    @Override
    public IDomainBuilder<E> deleteAll(boolean enabled) {
        toggleCrudWorkflow(BusinessOperation.deleteAll.getLabel(), enabled);
        return this;
    }

    private void toggleCrudWorkflow(String label, boolean enabled) {
        if (!enabled) {
            this.workflows.remove(label);
        }
    }

    @Override
    public IClass<E> getEntityClass() throws ApiException {
        if (this.entityClass != null) {
            IClass<E> result = (IClass<E>) this.entityClass;
            return result;
        }

        throw new ApiException("Entity class is not set !");
    }

    @Override
    protected synchronized IDomain<E> doBuild() throws ApiException {

        this.throwExceptionIfNoDto();
        this.validateSecurityRoles();

        // Build DTO contexts and extract definitions
        List<IDtoContext<?>> dtoContexts = new ArrayList<>();
        List<IDtoDefinition<E>> dtoDefinitions = new ArrayList<>();
        for (IDtoBuilder<?, ?> builder : this.dtos.values()) {
            IDtoContext<?> dtoContext = builder.build();
            dtoContexts.add(dtoContext);
            if (dtoContext instanceof DtoContext<?> dtc) {
                dtoDefinitions.add((IDtoDefinition<E>) dtc.getDtoDefinition());
            }
        }

        // Build entity context and extract definition
        IEntityContext<E> entityContext = this.entityBuilder.build();
        EntityDefinition<E> entityDefinition = null;
        if (entityContext instanceof EntityContext<E> ec) {
            entityDefinition = ec.getEntityDefinition();
        }

        // Build entity bean definition for runtime DI injection
        BeanDefinition<?> entityBeanDefinition = null;
        if (this.injectionContextBuilder != null) {
            // Ensure @Property, @Null, @Fixed resolvers are registered
            // (setBuiltInResolvers is normally called during InjectionContext.doBuild(),
            // but we need the resolvers now for BeanFactoryBuilder auto-detection)
            IInjectableElementResolverBuilder resolversBuilder = this.injectionContextBuilder.resolvers();
            InjectionContextBuilder.setBuiltInResolvers(resolversBuilder, Set.of(), false);

            BeanFactoryBuilder<?> bfb = new BeanFactoryBuilder<>(this.entityClass);
            bfb.provide(resolversBuilder);
            IBeanFactory<?> templateFactory = bfb.build();
            entityBeanDefinition = templateFactory.definition();
        }

        // Build startup binders
        List<IMethodBinder<Void>> startupBinders = new ArrayList<>();
        for (IDomainStartupBinderBuilder<?> builder : this.startupBinderBuilders) {
            IMethodBinder<Void> binder = builder.build();
            startupBinders.add(binder);
        }

        // Build use case definitions (deprecated, kept for backwards compatibility)
        Map<String, IUseCaseDefinition> useCaseDefinitions = new HashMap<>();
        for (Map.Entry<String, IUseCaseBuilder<?, ?, E>> entry : this.useCases.entrySet()) {
            entry.getValue().build();
            UseCaseBuilder<?, ?, E> ucb = (UseCaseBuilder<?, ?, E>) entry.getValue();
            useCaseDefinitions.put(entry.getKey(), new UseCaseDefinition(
                    ucb.getScope(),
                    ucb.getOperation(),
                    ucb.getAccess(),
                    ucb.hasAuthority()));
        }

        // Build workflow definitions (metadata)
        Map<String, IWorkflowDefinition> workflowDefinitions = new HashMap<>();
        for (Map.Entry<String, DomainWorkflowBuilder<E>> entry : this.workflows.entrySet()) {
            DomainWorkflowBuilder<E> wb = entry.getValue();
            if (wb.isSecurityDisabled()) {
                continue;
            }
            String label = entry.getKey();
            workflowDefinitions.put(label, new WorkflowDefinition(
                    wb.getWorkflowName(),
                    wb.getPathSuffix(),
                    wb.getCompletePath(),
                    wb.getScope(),
                    wb.getOperation(),
                    wb.getAccess(),
                    wb.hasAuthority(),
                    wb.isCustom()));
        }

        // Auto-register authenticate workflow when domain has an authenticator
        if (this.securityBuilder != null
                && ((DomainSecurityBuilder<E>) this.securityBuilder).hasAuthenticator()
                && !this.workflows.containsKey(BusinessOperation.authenticate.getLabel())) {
            registerCrudMetadata(BusinessOperation.authenticate.getLabel(),
                    TechnicalOperation.create, Scope.oneEntity);
        }

        // Compute configuration flags
        boolean securityEnabled = this.securityBuilder != null
                && ((DomainSecurityBuilder<E>) this.securityBuilder).hasSecurityConfiguration();
        boolean hasAuthorization = this.securityBuilder != null
                && ((DomainSecurityBuilder<E>) this.securityBuilder).hasAuthenticator()
                && ((AuthenticatorBuilder<E>) ((DomainSecurityBuilder<E>) this.securityBuilder).getAuthenticator()).hasAuthorizationConfig();

        // Compute domain characteristics for workflow assembly
        boolean multiTenancyEnabled = this.up() instanceof ApiBuilder acb && acb.isMultiTenant();
        boolean isOwnerOrOwned = this.owner != null || this.owned != null;

        // Assemble workflow stages via dedicated assembler
        IWorkflow builtWorkflow = new DomainWorkflowAssembler<E>(
                this.domainName, this.workflows, securityEnabled, hasAuthorization,
                multiTenancyEnabled, isOwnerOrOwned,
                this.injectionContextBuilder, this.expressionContextBuilder).assemble();

        // Cast entities for create/upsert lists
        List<E> createEntitiesCast = this.createEntities.stream()
                .map(e -> (E) e)
                .toList();
        List<E> upsertEntitiesCast = this.upsertEntities.stream()
                .map(e -> (E) e)
                .toList();

        // Build security definition (null-safe)
        var securityDefinition = this.securityBuilder != null
                ? ((DomainSecurityBuilder<E>) this.securityBuilder).buildSecurityDefinition()
                : null;

        // Build security context (null-safe)
        var securityContext = this.securityBuilder != null
                ? this.securityBuilder.build()
                : new DomainSecurityBuilder<>(this, this.interfaces, this.entityClass).build();

        // Build interface suppliers
        List<ISupplier<IEndpoint>> builtInterfaces = new ArrayList<>();
        for (ISupplierBuilder<? extends IEndpoint, ? extends ISupplier<? extends IEndpoint>> interfaceBuilder : this.interfaces) {
            ISupplier<IEndpoint> supplier = (ISupplier<IEndpoint>) interfaceBuilder.build();
            builtInterfaces.add(supplier);
        }

        // Build event suppliers
        List<ISupplier<IEventPublisher>> builtEvents = new ArrayList<>();
        for (ISupplierBuilder<?, ? extends ISupplier<?>> eventBuilder : this.events) {
            ISupplier<IEventPublisher> supplier = (ISupplier<IEventPublisher>) eventBuilder.build();
            builtEvents.add(supplier);
        }

        Domain<E> domainContext = new Domain<E>(
                new DomainDefinition<E>(
                        this.domainName,
                        entityDefinition,
                        dtoDefinitions,
                        startupBinders,
                        this.publik,
                        this.tenant,
                        createEntitiesCast,
                        upsertEntitiesCast,
                        this.owner,
                        this.owned,
                        this.shared,
                        this.hiddenable,
                        this.geolocalized,
                        this.superOwner,
                        this.superTenant,
                        useCaseDefinitions,
                        workflowDefinitions,
                        securityDefinition),
                entityContext,
                securityContext,
                dtoContexts,
                builtInterfaces,
                builtEvents);
        domainContext.setWorkflow(builtWorkflow);
        domainContext.setEntityBeanDefinition(entityBeanDefinition);
        domainContext.setDoInjection(this.doInjection);

        return domainContext;
    }

    private void initDefaultCrudWorkflows() {
        registerCrudMetadata(BusinessOperation.create.getLabel(), TechnicalOperation.create, Scope.oneEntity);
        registerCrudMetadata(BusinessOperation.readAll.getLabel(), TechnicalOperation.read, Scope.allEntities);
        registerCrudMetadata(BusinessOperation.readOne.getLabel(), TechnicalOperation.read, Scope.oneEntity);
        registerCrudMetadata(BusinessOperation.update.getLabel(), TechnicalOperation.update, Scope.oneEntity);
        registerCrudMetadata(BusinessOperation.deleteOne.getLabel(), TechnicalOperation.delete, Scope.oneEntity);
        registerCrudMetadata(BusinessOperation.deleteAll.getLabel(), TechnicalOperation.delete, Scope.allEntities);
    }

    private void registerCrudMetadata(String name, TechnicalOperation op, Scope scope) {
        DomainWorkflowBuilder<E> wb = new DomainWorkflowBuilder<>(name, this);
        wb.setCustom(false);
        this.workflows.put(name, wb);
    }


    private void throwExceptionIfNoDto() throws ApiException {
        if (this.dtos.size() == 0) {
            throw new ApiException("No dto declared for domain " + this.domainName);
        }
    }

    private void validateSecurityRoles() throws ApiException {
        if (this.securityBuilder == null) return;
        DomainSecurityBuilder<E> secBuilder = (DomainSecurityBuilder<E>) this.securityBuilder;

        // Rule 1: A domain with authorization role MUST be owned
        if (secBuilder.hasAuthorization() && this.owned == null) {
            throw new ApiException("Domain '" + this.domainName
                    + "' has an authorization configuration but is not owned. "
                    + "An authorization entity always belongs to a principal — use .owned(field) on the domain builder.");
        }

        // Rule 2: An authenticator domain that produces an authorization MUST be owner
        if (secBuilder.hasAuthenticator()) {
            var authenticatorBuilder = (AuthenticatorBuilder<E>) secBuilder.getAuthenticator();
            if (authenticatorBuilder.hasAuthorizationConfig() && this.owner == null) {
                throw new ApiException("Domain '" + this.domainName
                        + "' is an authenticator that produces authorizations but is not an owner. "
                        + "The authenticator entity must own the authorization entities — use .owner(field) on the domain builder.");
            }
        }
    }

    @Override
    protected void doAutoDetection() {

    }

    @Override
    public synchronized IEntityBuilder<E> entity() throws ApiException {
        if( this.entityClass == null )
            throw new ApiException("Entity class is not set");

        if( this.entityBuilder == null)
            this.entityBuilder = new EntityBuilder<>(entityClass, this);

        return this.entityBuilder;
    }

    @Override
    public IDomainBuilder<E> interfasse(IClass<? extends IEndpoint> interfasse) throws ApiException {
        Objects.requireNonNull(interfasse, "Interface class cannot be null");
        // TODO: Implement interface instantiation or supplier creation
        throw new UnsupportedOperationException("Unimplemented method 'interfasse(IClass)'");
    }

    @Override
    public IEntityBuilder<E> name(String name) throws ApiException {
        Objects.requireNonNull(name, "Name cannot be null");
        this.domainName = name;
        return this.entity();
    }

}
