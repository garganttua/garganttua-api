package com.garganttua.api.core.builder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.garganttua.api.core.builder.binder.DomainStartupBinderBuilder;
import com.garganttua.api.core.context.application.DomainContext;
import com.garganttua.api.core.context.application.DtoContext;
import com.garganttua.api.core.context.application.EntityContext;
import com.garganttua.api.core.definition.DomainDefinition;
import com.garganttua.api.core.definition.DtoDefinition;
import com.garganttua.api.core.definition.EntityDefinition;
import com.garganttua.api.core.definition.UseCaseDefinition;
import com.garganttua.api.core.definition.WorkflowDefinition;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.spec.security.IAccessRule;
import com.garganttua.api.spec.Pluralizer;
import com.garganttua.api.spec.context.Access;
import com.garganttua.api.spec.context.BusinessOperation;
import com.garganttua.api.spec.context.ContextBuildingStage;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.context.IDtoContext;
import com.garganttua.api.spec.context.IEntityContext;
import com.garganttua.api.spec.context.Scope;
import com.garganttua.api.spec.context.TechnicalOperation;
import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.context.dsl.IDomainStartupBinderBuilder;
import com.garganttua.api.spec.context.dsl.IDomainWorkflowBuilder;
import com.garganttua.api.spec.context.dsl.IDtoBuilder;
import com.garganttua.api.spec.context.dsl.IEntityBuilder;
import com.garganttua.api.spec.context.dsl.IUseCaseBuilder;
import com.garganttua.api.spec.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.api.spec.event.IEventPublisher;
import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.core.CoreException;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.mapper.IMapper;
import com.garganttua.core.mapper.MapperException;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.binders.dsl.IMethodBinderBuilder;
import com.garganttua.core.reflection.fields.FieldResolver;
import com.garganttua.core.reflection.query.ObjectQueryFactory;
import com.garganttua.core.supply.dsl.ISupplierBuilder;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.expression.dsl.IExpressionContextBuilder;
import com.garganttua.core.injection.context.dsl.IInjectionContextBuilder;
import com.garganttua.core.workflow.IWorkflow;

public class DomainBuilder<E>
        extends AbstractAutomaticLinkedBuilder<IDomainBuilder<E>, IApiContextBuilder, IDomainContext<E>>
        implements IDomainBuilder<E> {

    private volatile String domainName;
    private volatile Class<?> entityClass;

    private IMapper mapper = DefaultMapper.mapper();

    private final List<IDomainStartupBinderBuilder> startupBinderBuilders = new CopyOnWriteArrayList<>();
    private final List<ISupplierBuilder<? extends IInterface, ? extends ISupplier<? extends IInterface>>> interfaces = new CopyOnWriteArrayList<>();
    private final List<ISupplierBuilder<?, ? extends ISupplier<?>>> events = new CopyOnWriteArrayList<>();

    private volatile boolean creation = true;
    private volatile boolean readAll = true;
    private volatile boolean readOne = true;
    private volatile boolean update = true;
    private volatile boolean deleteAll = true;
    private volatile boolean deleteOne = true;

    private volatile boolean publik = false;
    private volatile boolean tenant = false;
    private volatile IEntityBuilder<E> entityBuilder;
    private final List<Object> createEntities = new CopyOnWriteArrayList<>();
    private final List<Object> upsertEntities = new CopyOnWriteArrayList<>();
    private volatile IObjectQuery objectQuery;
    private volatile ObjectAddress owner;
    private volatile ObjectAddress owned;
    private volatile ObjectAddress shared;
    private volatile ObjectAddress hiddenable;
    private volatile IDomainSecurityBuilder<E> securityBuilder;
    private final Map<Class<?>, IDtoBuilder> dtos = new ConcurrentHashMap<>();
    private final Map<String, IUseCaseBuilder<?, ?, E>> useCases = new ConcurrentHashMap<>();
    private final Map<String, DomainWorkflowBuilder<E>> workflows = new ConcurrentHashMap<>();

    private volatile IInjectionContextBuilder injectionContextBuilder;
    private volatile IExpressionContextBuilder expressionContextBuilder;

    void setDependencyBuilders(IInjectionContextBuilder injectionContextBuilder,
            IExpressionContextBuilder expressionContextBuilder) {
        this.injectionContextBuilder = injectionContextBuilder;
        this.expressionContextBuilder = expressionContextBuilder;
    }

    public DomainBuilder(IApiContextBuilder builder, String domainName)
            throws DslException {
        super(builder);
        this.domainName = Objects.requireNonNull(domainName, "Domain name cannot be null");
    }

    public DomainBuilder(IApiContextBuilder builder, Class<?> entityClass) throws DslException {
        super(builder);
        this.entityClass = Objects.requireNonNull(entityClass, "Entity Class cannot be null");
        try {
            this.objectQuery = ObjectQueryFactory.objectQuery(this.entityClass);
        } catch (ReflectionException e) {
            throw new DslException(e.getMessage(), e);
        }
        this.domainName = Pluralizer.toPlural(this.entityClass.getSimpleName().toLowerCase());
        this.securityBuilder = new DomainSecurityBuilder<>(this, this.interfaces, this.objectQuery, this.entityClass);
        this.entityBuilder = this.entity(this.entityClass);
    }

    @Override
    public IDomainStartupBinderBuilder startup(ContextBuildingStage stage, ISupplierBuilder<?, ? extends ISupplier<?>> supplier)
            throws DslException {
        DomainStartupBinderBuilder binder = new DomainStartupBinderBuilder(this, supplier);
        this.startupBinderBuilders.add(binder);
        return binder;
    }

    @Override
    public IDomainBuilder<E> interfasse(ISupplierBuilder<? extends IInterface, ? extends ISupplier<? extends IInterface>> bean) throws DslException {
        this.interfaces.add(bean);
        return this;
    }

    @Override
    public IDomainBuilder<E> creation(boolean b) {
        this.creation = b;
        return this;
    }

    @Override
    public IDomainBuilder<E> readAll(boolean b) {
        this.readAll = b;
        return this;
    }

    @Override
    public IDomainBuilder<E> readOne(boolean b) {
        this.readOne = b;
        return this;
    }

    @Override
    public IDomainBuilder<E> update(boolean b) {
        this.update = b;
        return this;
    }

    @Override
    public IDomainBuilder<E> deleteAll(boolean b) {
        this.deleteAll = b;
        return this;
    }

    @Override
    public IDomainBuilder<E> deleteOne(boolean b) {
        this.deleteOne = b;
        return this;
    }

    @Override
    public IDomainBuilder<E> events(ISupplierBuilder<?, ? extends ISupplier<?>> bean) throws DslException {
        Class<?> suppliedClass = bean.getSuppliedClass();
        if (!IEventPublisher.class.isAssignableFrom(suppliedClass)) {
            throw new DslException(
                    "Bean " + suppliedClass.getName() + " does not implement IEventPublisher");
        }
        this.events.add(bean);
        return this;
    }

    @Override
    public IDomainBuilder<E> events(IEventPublisher eventPublisher) throws DslException {
        this.events.add(new FixedSupplierBuilder<>(
                Objects.requireNonNull(eventPublisher, "EventPublisher cannot be null")));
        return this;
    }

    @Override
    public IDomainBuilder<E> tenant(boolean b) throws DslException {
        this.tenant = b;
        return this;
    }

    @Override
    public IDomainBuilder<E> owner(String fieldName) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        if (this.entityBuilder == null) {
            throw new DslException("Entity class must be defined first");
        }

        this.owner = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder<E> owner(Field field) throws DslException {
        Objects.requireNonNull(field, "Field cannot be null");
        if (this.entityBuilder == null) {
            throw new DslException("Entity class must be defined first");
        }

        this.owner = FieldResolver.fieldByField(field, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder<E> owner(ObjectAddress fieldAddress) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        if (this.entityBuilder == null) {
            throw new DslException("Entity class must be defined first");
        }

        this.owner = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder<E> owned(String fieldName) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        if (this.entityBuilder == null) {
            throw new DslException("Entity class must be defined first");
        }

        this.owned = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder<E> owned(Field field) throws DslException {
        Objects.requireNonNull(field, "Field cannot be null");
        if (this.entityBuilder == null) {
            throw new DslException("Entity class must be defined first");
        }

        this.owned = FieldResolver.fieldByField(field, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder<E> owned(ObjectAddress fieldAddress) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        if (this.entityBuilder == null) {
            throw new DslException("Entity class must be defined first");
        }

        this.owned = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder<E> publik() {
        this.publik = true;
        return this;
    }

    @Override
    public IDomainBuilder<E> shared(Field field) throws DslException {
        Objects.requireNonNull(field, "Field cannot be null");
        if (this.entityBuilder == null) {
            throw new DslException("Entity class must be defined first");
        }

        this.shared = FieldResolver.fieldByField(field, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder<E> shared(String fieldName) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        if (this.entityBuilder == null) {
            throw new DslException("Entity class must be defined first");
        }

        this.shared = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder<E> shared(ObjectAddress fieldAddress) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        if (this.entityBuilder == null) {
            throw new DslException("Entity class must be defined first");
        }

        this.shared = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder<E> hiddenable(String fieldName) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        if (this.entityBuilder == null) {
            throw new DslException("Entity class must be defined first");
        }

        this.hiddenable = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IDomainBuilder<E> hiddenable(Field field) throws DslException {
        Objects.requireNonNull(field, "Field cannot be null");
        if (this.entityBuilder == null) {
            throw new DslException("Entity class must be defined first");
        }

        this.hiddenable = FieldResolver.fieldByField(field, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IDomainBuilder<E> hiddenable(ObjectAddress fieldAddress) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        if (this.entityBuilder == null) {
            throw new DslException("Entity class must be defined first");
        }

        this.hiddenable = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    public IEntityBuilder<E> entity(Class<?> entityClass) throws DslException {
        Objects.requireNonNull(entityClass, "Entity class cannot be null");

        if (this.entityBuilder != null && Objects.equals(entityClass, this.entityClass)) {
            throw new DslException(
                    "Entity Class is already set with class " + entityClass.getSimpleName());
        }

        this.entityBuilder = new EntityBuilder<>(entityClass, this);
        this.entityClass = entityClass;

        try {
            this.objectQuery = ObjectQueryFactory.objectQuery(this.entityClass);
        } catch (ReflectionException e) {
            throw new DslException(e.getMessage(), e);
        }

        this.securityBuilder = new DomainSecurityBuilder<>(this, this.interfaces, this.objectQuery, this.entityClass);

        return this.entityBuilder;
    }

    @Override
    public IDomainSecurityBuilder<E> security() throws DslException {
        if (this.securityBuilder == null)
            throw new DslException("Security builder is null, please set entity class first");
        return this.securityBuilder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D> IDtoBuilder<E, D> dto(Class<D> dtoClass) throws DslException {
        if( this.entityClass == null )
            throw new DslException("Entity class must be set before declaring a dto");

        IDtoBuilder<E, D> dtoBuilder = (IDtoBuilder<E, D>) this.dtos.computeIfAbsent(dtoClass, clazz -> {
            try {
                return new DtoBuilder<>(clazz, this);
            } catch (DslException e) {
                throw new RuntimeException(e);
            }
        });

        try {
            this.mapper.recordMappingConfiguration(this.entityClass, dtoClass);
        } catch (MapperException e) {
            // Mapper configuration may fail if no mapping annotations are present
            // This is acceptable - mapping will need to be done manually
        }

        return dtoBuilder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> IUseCaseBuilder<I, O, E> useCase(String useCaseName, Class<I> inputType, Class<O> outputType) {
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
    public Class<E> getEntityClass() throws DslException {
        if (this.entityClass != null) {
            @SuppressWarnings("unchecked")
            Class<E> result = (Class<E>) this.entityClass;
            return result;
        }

        throw new DslException("Entity class is not set !");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected synchronized IDomainContext<E> doBuild() throws CoreException {

        this.throwExceptionIfNoDto();

        // Build DTO contexts and extract definitions
        List<IDtoContext<?>> dtoContexts = new ArrayList<>();
        List<DtoDefinition<?>> dtoDefinitions = new ArrayList<>();
        for (IDtoBuilder<?, ?> builder : this.dtos.values()) {
            IDtoContext<?> dtoContext = builder.build();
            dtoContexts.add(dtoContext);
            if (dtoContext instanceof DtoContext<?> dtc) {
                dtoDefinitions.add(dtc.getDtoDefinition());
            }
        }

        // Build entity context and extract definition
        IEntityContext<E> entityContext = this.entityBuilder.build();
        EntityDefinition<E> entityDefinition = null;
        if (entityContext instanceof EntityContext<E> ec) {
            entityDefinition = ec.getEntityDefinition();
        }

        // Build startup binders
        List<IMethodBinder<Void>> startupBinders = new ArrayList<>();
        for (IDomainStartupBinderBuilder<?> builder : this.startupBinderBuilders) {
            @SuppressWarnings("unchecked")
            IMethodBinder<Void> binder = builder.build();
            startupBinders.add(binder);
        }

        // Build use case definitions (deprecated, kept for backwards compatibility)
        Map<String, UseCaseDefinition> useCaseDefinitions = new HashMap<>();
        for (Map.Entry<String, IUseCaseBuilder<?, ?, E>> entry : this.useCases.entrySet()) {
            entry.getValue().build();
            useCaseDefinitions.put(entry.getKey(), new UseCaseDefinition());
        }

        // Build workflows: auto-generate CRUD workflows for activated operations
        autoGenerateCrudWorkflows();

        // Build all workflows (propagate dependency builders to internal WorkflowBuilder)
        Map<String, WorkflowDefinition> workflowDefinitions = new HashMap<>();
        Map<String, IWorkflow> builtWorkflows = new HashMap<>();
        for (Map.Entry<String, DomainWorkflowBuilder<E>> entry : this.workflows.entrySet()) {
            DomainWorkflowBuilder<E> wb = entry.getValue();
            wb.setDependencyBuilders(this.injectionContextBuilder, this.expressionContextBuilder);
            IWorkflow builtWorkflow = wb.build();
            builtWorkflows.put(entry.getKey(), builtWorkflow);
            workflowDefinitions.put(entry.getKey(), new WorkflowDefinition(
                    wb.getWorkflowName(),
                    wb.getPathSuffix(),
                    wb.getCompletePath(),
                    wb.getScope(),
                    wb.getOperation(),
                    wb.getAccess(),
                    wb.hasAuthority(),
                    wb.isCustom()));
        }

        // Cast entities for create/upsert lists
        List<E> createEntitiesCast = this.createEntities.stream()
                .map(e -> (E) e)
                .collect(Collectors.toList());
        List<E> upsertEntitiesCast = this.upsertEntities.stream()
                .map(e -> (E) e)
                .collect(Collectors.toList());

        // Build security definition (null-safe)
        var securityDefinition = this.securityBuilder != null
                ? ((DomainSecurityBuilder<E>) this.securityBuilder).buildSecurityDefinition()
                : null;

        // Build security context (null-safe)
        var securityContext = this.securityBuilder != null
                ? this.securityBuilder.build()
                : new DomainSecurityBuilder<>(this, this.interfaces, this.objectQuery, this.entityClass).build();

        // Build interface suppliers
        List<ISupplier<IInterface>> builtInterfaces = new ArrayList<>();
        for (ISupplierBuilder<? extends IInterface, ? extends ISupplier<? extends IInterface>> interfaceBuilder : this.interfaces) {
            @SuppressWarnings("unchecked")
            ISupplier<IInterface> supplier = (ISupplier<IInterface>) interfaceBuilder.build();
            builtInterfaces.add(supplier);
        }

        // Build event suppliers
        List<ISupplier<IEventPublisher>> builtEvents = new ArrayList<>();
        for (ISupplierBuilder<?, ? extends ISupplier<?>> eventBuilder : this.events) {
            @SuppressWarnings("unchecked")
            ISupplier<IEventPublisher> supplier = (ISupplier<IEventPublisher>) eventBuilder.build();
            builtEvents.add(supplier);
        }

        return new DomainContext<E>(
                new DomainDefinition<E>(
                        this.domainName,
                        entityDefinition,
                        securityDefinition,
                        dtoDefinitions,
                        startupBinders,
                        this.creation,
                        this.readAll,
                        this.readOne,
                        this.update,
                        this.deleteAll,
                        this.deleteOne,
                        this.publik,
                        this.tenant,
                        createEntitiesCast,
                        upsertEntitiesCast,
                        this.owner,
                        this.owned,
                        this.shared,
                        this.hiddenable,
                        useCaseDefinitions,
                        workflowDefinitions),
                entityContext,
                securityContext,
                dtoContexts,
                builtInterfaces,
                builtEvents,
                builtWorkflows);
    }

    private static final Map<String, String> CRUD_SCRIPT_PATHS = Map.of(
            BusinessOperation.create.getLabel(), "scripts/business/crud/CREATE_ONE.gs",
            BusinessOperation.readAll.getLabel(), "scripts/business/crud/READ_ALL.gs",
            BusinessOperation.readOne.getLabel(), "scripts/business/crud/READ_ONE.gs",
            BusinessOperation.update.getLabel(), "scripts/business/crud/UPDATE_ONE.gs",
            BusinessOperation.deleteOne.getLabel(), "scripts/business/crud/DELETE_ONE.gs",
            BusinessOperation.deleteAll.getLabel(), "scripts/business/crud/DELETE_ALL.gs"
    );

    private void autoGenerateCrudWorkflows() {
        if (this.creation && !this.workflows.containsKey(BusinessOperation.create.getLabel())) {
            createDefaultCrudWorkflow(BusinessOperation.create.getLabel(), TechnicalOperation.create, Scope.oneEntity);
        }
        if (this.readAll && !this.workflows.containsKey(BusinessOperation.readAll.getLabel())) {
            createDefaultCrudWorkflow(BusinessOperation.readAll.getLabel(), TechnicalOperation.read, Scope.allEntities);
        }
        if (this.readOne && !this.workflows.containsKey(BusinessOperation.readOne.getLabel())) {
            createDefaultCrudWorkflow(BusinessOperation.readOne.getLabel(), TechnicalOperation.read, Scope.oneEntity);
        }
        if (this.update && !this.workflows.containsKey(BusinessOperation.update.getLabel())) {
            createDefaultCrudWorkflow(BusinessOperation.update.getLabel(), TechnicalOperation.update, Scope.oneEntity);
        }
        if (this.deleteOne && !this.workflows.containsKey(BusinessOperation.deleteOne.getLabel())) {
            createDefaultCrudWorkflow(BusinessOperation.deleteOne.getLabel(), TechnicalOperation.delete, Scope.oneEntity);
        }
        if (this.deleteAll && !this.workflows.containsKey(BusinessOperation.deleteAll.getLabel())) {
            createDefaultCrudWorkflow(BusinessOperation.deleteAll.getLabel(), TechnicalOperation.delete, Scope.allEntities);
        }
    }

    private void createDefaultCrudWorkflow(String name, TechnicalOperation op, Scope scope) {
        DomainWorkflowBuilder<E> wb = new DomainWorkflowBuilder<>(name, this);
        wb.setCustom(false);
        String scriptPath = CRUD_SCRIPT_PATHS.get(name);
        wb.getInternalBuilder()
                .stage("execute")
                    .script(getClass().getResourceAsStream("/" + scriptPath))
                        .name("crud-" + name)
                        .inline()
                        .up()
                    .up();
        this.workflows.put(name, wb);
    }

    private void throwExceptionIfNoDto() throws DslException {
        if (this.dtos.size() == 0) {
            throw new DslException("No dto declared for domain " + this.domainName);
        }
    }

    @Override
    protected void doAutoDetection() {

    }

    @Override
    public synchronized IEntityBuilder<E> entity() throws DslException {
        if( this.entityClass == null )
            throw new DslException("Entity class is not set");

        if( this.entityBuilder == null)
            this.entityBuilder = new EntityBuilder<>(entityClass, this);

        return this.entityBuilder;
    }

    @Override
    public IDomainBuilder<E> interfasse(Class<? extends IInterface> interfasse) throws DslException {
        Objects.requireNonNull(interfasse, "Interface class cannot be null");
        // TODO: Implement interface instantiation or supplier creation
        throw new UnsupportedOperationException("Unimplemented method 'interfasse(Class)'");
    }

    @Override
    public IEntityBuilder<E> name(String name) throws DslException {
        Objects.requireNonNull(name, "Name cannot be null");
        this.domainName = name;
        return this.entity();
    }

}
