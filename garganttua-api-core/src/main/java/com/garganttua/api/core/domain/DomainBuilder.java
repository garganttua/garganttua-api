package com.garganttua.api.core.domain;
import com.garganttua.api.core.api.ApiBuilder;
import com.garganttua.api.core.dto.DtoBuilder;
import com.garganttua.api.core.entity.EntityBuilder;
import com.garganttua.api.core.security.DomainSecurityBuilder;
import com.garganttua.api.core.security.authenticator.AuthenticatorBuilder;
import com.garganttua.api.core.usecase.UseCaseBinderBuilder;
import com.garganttua.api.core.usecase.UseCaseBuilder;
import com.garganttua.core.reflection.annotations.Reflected;

import com.garganttua.core.reflection.IField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.garganttua.api.core.domain.DomainStartupBinderBuilder;
import com.garganttua.api.core.domain.Domain;
import com.garganttua.api.core.dto.DtoContext;
import com.garganttua.api.core.entity.EntityContext;
import com.garganttua.api.core.domain.DomainDefinition;
import com.garganttua.api.core.entity.EntityDefinition;
import com.garganttua.api.core.usecase.UseCaseDefinition;
import com.garganttua.api.core.domain.WorkflowDefinition;
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
import com.garganttua.api.commons.endpoint.IInterface;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.expression.dsl.IExpressionContextBuilder;
import java.util.Set;

import com.garganttua.core.injection.BeanDefinition;
import com.garganttua.core.injection.IBeanFactory;
import com.garganttua.core.injection.IInjectableElementResolver;
import com.garganttua.core.injection.IInjectableElementResolverBuilder;
import com.garganttua.core.injection.Resolved;
import com.garganttua.core.injection.context.dsl.BeanFactoryBuilder;
import com.garganttua.core.injection.context.dsl.InjectableElementResolverBuilder;
import com.garganttua.core.injection.context.dsl.IInjectionContextBuilder;
import com.garganttua.core.injection.context.dsl.InjectionContextBuilder;
import com.garganttua.core.mapper.IMapper;
import com.garganttua.core.mapper.MapperException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IMethod;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.IParameter;
import com.garganttua.core.reflection.IReflectionProvider;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.fields.FieldResolver;
import com.garganttua.core.reflection.query.ObjectQueryFactory;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;
import com.garganttua.core.supply.dsl.ISupplierBuilder;
import com.garganttua.core.workflow.IWorkflow;

@Reflected
public class DomainBuilder<E>
        extends AbstractAutomaticLinkedBuilder<IDomainBuilder<E>, IApiBuilder, IDomain<E>>
        implements IDomainBuilder<E> {

    // Reflection provider is whatever the user installed via IClass.setReflection().
    // Resolved lazily per call so the framework never picks an implementation.
    private static IReflectionProvider provider() {
        return IClass.getReflection();
    }

    private volatile String domainName;
    private volatile IClass<?> entityClass;

    private IMapper mapper = DefaultMapper.mapper();

    private final List<IDomainStartupBinderBuilder> startupBinderBuilders = new CopyOnWriteArrayList<>();
    private final List<ISupplierBuilder<? extends IInterface, ? extends ISupplier<? extends IInterface>>> interfaces = new CopyOnWriteArrayList<>();
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
            this.objectQuery = ObjectQueryFactory.objectQuery(this.entityClass, provider());
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
    public IDomainBuilder<E> interfasse(ISupplierBuilder<? extends IInterface, ? extends ISupplier<? extends IInterface>> bean) throws ApiException {
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
            throw new ApiException("Cannot mark domain '" + this.domainName + "' as the tenant — "
                    + "multi-tenancy is disabled on the parent API. Either drop the .tenant(true) call "
                    + "(single-tenant apps do not need a tenant entity), or remove the .multiTenant(false) "
                    + "call on the apiBuilder to re-enable multi-tenancy.");
        }
        this.tenant = b;
        return this;
    }

    /**
     * Package-private accessor used by {@link EntityBuilder} and
     * {@link DtoBuilder} to skip the {@code tenantId} requirement when the
     * domain entity is itself the tenant (its own uuid plays the role of
     * tenantId — see {@code FilterContext.buildTenantFilter} for the
     * downstream filter rewrite).
     */
    public boolean isTenantDomain() {
        return this.tenant;
    }

    @Override
    public IDomainBuilder<E> owner(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        requireEntityDeclared();

        this.owner = FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> owner(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");
        requireEntityDeclared();

        this.owner = FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> owner(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        requireEntityDeclared();

        this.owner = FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> owned(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        requireEntityDeclared();

        this.owned = FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> owned(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");
        requireEntityDeclared();

        this.owned = FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> owned(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        requireEntityDeclared();

        this.owned = FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, IClass.getClass(String.class)).address();

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
        requireEntityDeclared();

        this.shared = FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> shared(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        requireEntityDeclared();

        this.shared = FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> shared(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        requireEntityDeclared();

        this.shared = FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> hiddenable(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        requireEntityDeclared();

        this.hiddenable = FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> hiddenable(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");
        requireEntityDeclared();

        this.hiddenable = FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> hiddenable(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        requireEntityDeclared();

        this.hiddenable = FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, IClass.getClass(Boolean.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> geolocalized(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        requireEntityDeclared();

        this.geolocalized = FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, IClass.getClass(org.geojson.Point.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> geolocalized(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");
        requireEntityDeclared();

        this.geolocalized = FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), IClass.getClass(org.geojson.Point.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> geolocalized(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");
        requireEntityDeclared();

        this.geolocalized = FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, IClass.getClass(org.geojson.Point.class)).address();

        return this;
    }

    @Override
    public IDomainBuilder<E> superOwner(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        requireEntityDeclared();
        this.superOwner = FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, IClass.getClass(Boolean.class)).address();
        return this;
    }

    @Override
    public IDomainBuilder<E> superOwner(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");
        requireEntityDeclared();
        this.superOwner = FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), IClass.getClass(Boolean.class)).address();
        return this;
    }

    @Override
    public IDomainBuilder<E> superOwner(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");
        requireEntityDeclared();
        this.superOwner = FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, IClass.getClass(Boolean.class)).address();
        return this;
    }

    @Override
    public IDomainBuilder<E> superTenant(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        requireEntityDeclared();
        this.superTenant = FieldResolver.fieldByFieldName(this.entityClass, provider(), fieldName, IClass.getClass(Boolean.class)).address();
        return this;
    }

    @Override
    public IDomainBuilder<E> superTenant(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");
        requireEntityDeclared();
        this.superTenant = FieldResolver.fieldByFieldName(this.entityClass, provider(), field.getName(), IClass.getClass(Boolean.class)).address();
        return this;
    }

    @Override
    public IDomainBuilder<E> superTenant(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address cannot be null");
        requireEntityDeclared();
        this.superTenant = FieldResolver.fieldByAddress(this.entityClass, provider(), fieldAddress, IClass.getClass(Boolean.class)).address();
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
            this.objectQuery = ObjectQueryFactory.objectQuery(this.entityClass, provider());
        } catch (ReflectionException e) {
            throw new ApiException(e.getMessage(), e);
        }

        this.securityBuilder = new DomainSecurityBuilder<>(this, this.interfaces, this.entityClass);

        return this.entityBuilder;
    }

    @Override
    public IDomainSecurityBuilder<E> security() throws ApiException {
        // securityBuilder is wired in the IClass-based constructor; null means
        // this domain was created from a name only.
        requireEntityClassSet("security");
        return this.securityBuilder;
    }

    @Override
    public <D> IDtoBuilder<E, D> dto(IClass<D> dtoClass) throws ApiException {
        requireEntityClassSet("dto");

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
                name -> new UseCaseBuilder<I, O, E>(name, this, inputType, outputType));
    }

    /**
     * Resolves a use case's route path: an explicit {@code completePath} as-is, else the domain base
     * {@code /<domain>} plus the {@code pathSuffix}; a {@code ${uuid}} segment is appended for a
     * {@code oneEntity} scope when not already present.
     */
    private com.garganttua.api.commons.operation.OperationPath buildUseCasePath(
            String completePath, String suffix, com.garganttua.api.commons.operation.Scope scope) {
        String p;
        if (completePath != null && !completePath.isBlank()) {
            p = completePath.startsWith("/") ? completePath : "/" + completePath;
        } else {
            String base = "/" + this.domainName;
            p = (suffix != null && !suffix.isBlank()) ? base + "/" + suffix : base;
        }
        if (scope == com.garganttua.api.commons.operation.Scope.oneEntity && !p.contains("${uuid}")) {
            p = p + "/${uuid}";
        }
        return new com.garganttua.api.commons.operation.OperationPath(p);
    }

    /**
     * Packages scanned for declarative {@code @Resolver} parameter resolvers — the use case's
     * {@code @UseCaseInput} and the security {@code @Caller}/{@code @ApiContext}/{@code @DomainContext}/…
     * resolvers all live under the api tree. Core's built-in resolvers come from
     * {@code setBuiltInResolvers}, not a scan.
     */
    private static final String[] FRAMEWORK_RESOLVER_PACKAGES = { "com.garganttua.api" };

    /**
     * Builds a fresh resolver registry that discovers every declarative {@code @Resolver}
     * ({@code @UseCaseInput}, {@code @Caller}, {@code @DomainContext}, …). A dedicated registry is
     * used rather than the shared {@code injectionContextBuilder.resolvers()} because the latter is
     * memoised — built (without the api packages) when the injection context is, so a late package
     * scan could never reach it. Returns {@code null} when no injection context is available.
     */
    private IInjectableElementResolver buildUseCaseResolverRegistry() throws ApiException {
        if (this.injectionContextBuilder == null) {
            return null;
        }
        try {
            InjectableElementResolverBuilder resolversBuilder =
                    new InjectableElementResolverBuilder(this.injectionContextBuilder);
            resolversBuilder.setReflection(IClass.getReflection());
            InjectionContextBuilder.setBuiltInResolvers(resolversBuilder, Set.of(), false);
            resolversBuilder.withPackages(FRAMEWORK_RESOLVER_PACKAGES);
            resolversBuilder.autoDetect(true);
            return resolversBuilder.build();
        } catch (com.garganttua.core.dsl.DslException e) {
            throw new ApiException("Failed to build the use-case parameter resolver registry: "
                    + e.getMessage(), e);
        }
    }

    /**
     * Auto-wires a use case's bound method parameters from their framework injection annotations —
     * the declarative dual of the explicit {@code .withParam(i, supplier)} the security scanner
     * performs (cf. {@code SecurityAnnotationScanner}). Each parameter carrying a {@code @Resolver}-backed
     * annotation ({@code @UseCaseInput} → the deserialized body, {@code @Caller}, {@code @DomainContext},
     * {@code @ApiContext}, …) is supplied by the {@code SupplierBuilder} its resolver yields, so the
     * method stays completely free: it declares only the parameters it needs, in any order. A parameter
     * no resolver matches is left untouched, so an explicit {@code .withParam(...)} still wins and a
     * truly unwired parameter surfaces as the binder's own "no supplier configured" error at build.
     */
    private void autowireUseCaseParameters(UseCaseBinderBuilder<?, ?, E> binder,
            IInjectableElementResolver resolvers) throws ApiException {
        if (binder == null || resolvers == null) {
            return;
        }
        IMethod method;
        try {
            method = binder.method();
        } catch (com.garganttua.core.dsl.DslException e) {
            // No method bound yet (or address-only) — nothing to auto-wire; the binder build
            // will report a missing method itself.
            return;
        }
        if (method == null) {
            return;
        }
        IParameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            IParameter parameter = parameters[i];
            try {
                Resolved resolved = resolvers.resolve(parameter.getType(), parameter);
                if (resolved != null && resolved.resolved()) {
                    binder.withParam(i, resolved.elementSupplier(), resolved.nullable());
                }
            } catch (com.garganttua.core.injection.DiException | com.garganttua.core.dsl.DslException e) {
                throw new ApiException("Failed to auto-wire parameter " + i + " of use case method '"
                        + method.getName() + "': " + e.getMessage(), e);
            }
        }
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

    /**
     * Throws with a concrete recipe when a domain-level method is called before
     * {@code .entity(...)} has produced an entity builder. The stack trace
     * surfaces the offending method name, the message surfaces the fix.
     */
    private void requireEntityDeclared() throws ApiException {
        if (this.entityBuilder == null) {
            String entityHint = this.entityClass != null
                    ? this.entityClass.getSimpleName()
                    : "MyEntity";
            throw new ApiException(
                    "This call requires .entity() to be declared first on domain '"
                    + this.domainName + "'. Example:\n"
                    + "\n"
                    + "    apiBuilder.domain(" + entityHint + ".class)\n"
                    + "        .entity().id(\"id\").uuid(\"uuid\").tenantId(\"tenantId\").up()\n"
                    + "        // <- now field-resolving calls like .owner/.owned/.shared/.hiddenable/.geolocalized work\n"
                    + "\n"
                    + "(The stack trace shows which specific method was called.)");
        }
    }

    /**
     * Throws with a concrete recipe when a method needs the entity class to be
     * known but the domain was created from a name only (no {@code IClass}).
     */
    private void requireEntityClassSet(String calledMethod) throws ApiException {
        if (this.entityClass == null) {
            throw new ApiException(
                    "." + calledMethod + "() needs an entity class on domain '"
                    + this.domainName + "', but this domain was created from a name only. "
                    + "Use apiBuilder.domain(MyEntity.class) instead of the name-only overload, "
                    + "or call .entity(MyEntity.class) on this domain before ." + calledMethod + "().");
        }
    }

    @Override
    public IClass<E> getEntityClass() throws ApiException {
        requireEntityClassSet("getEntityClass");
        return (IClass<E>) this.entityClass;
    }

    @Override
    protected synchronized IDomain<E> doBuild() throws ApiException {

        this.throwExceptionIfNoDto();
        this.validateSecurityRoles();
        this.validateSuperFields();

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

        // Build the @Resolver registry once (shared by entity free-hooks and the use cases below) and
        // hand it to the entity builder so its free lifecycle-hook parameters auto-wire at build.
        IInjectableElementResolver resolverRegistry = buildUseCaseResolverRegistry();
        if (this.entityBuilder instanceof com.garganttua.api.core.entity.EntityBuilder<E> eb) {
            eb.setResolverRegistry(resolverRegistry);
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

        // Build the full use case definitions: each carries its name, route path, in/out types and
        // the built method binder, plus verb (default read) / scope (default allEntities) / security.
        // The resolver registry that auto-wires each bound method's annotated parameters is the same
        // one built above for the entity free-hooks — reused here across the use cases.
        IInjectableElementResolver useCaseResolvers = resolverRegistry;
        Map<String, IUseCaseDefinition> useCaseDefinitions = new HashMap<>();
        for (Map.Entry<String, IUseCaseBuilder<?, ?, E>> entry : this.useCases.entrySet()) {
            UseCaseBuilder<?, ?, E> ucb = (UseCaseBuilder<?, ?, E>) entry.getValue();
            // Auto-wire the bound method's annotated parameters (@UseCaseInput, @Caller, @DomainContext,
            // …) before the builder materialises the binder — the method stays "completely free".
            autowireUseCaseParameters(ucb.getBinderBuilder(), useCaseResolvers);
            entry.getValue().build();
            com.garganttua.api.commons.operation.Scope scope = ucb.getScope() != null
                    ? ucb.getScope() : com.garganttua.api.commons.operation.Scope.allEntities;
            com.garganttua.api.commons.operation.TechnicalOperation verb = ucb.getOperation() != null
                    ? ucb.getOperation() : com.garganttua.api.commons.operation.TechnicalOperation.read;
            // Default the route suffix to the use case name so each use case on a domain gets a
            // distinct path (/<domain>/<name>) when no explicit pathSuffix/completePath is given.
            String suffix = ucb.getPathSuffix() != null ? ucb.getPathSuffix() : ucb.getName();
            useCaseDefinitions.put(entry.getKey(), new UseCaseDefinition(
                    ucb.getName(),
                    buildUseCasePath(ucb.getCompletePath(), suffix, scope),
                    ucb.getInputType(),
                    ucb.getOutputType(),
                    ucb.getBuiltBinder(),
                    scope,
                    verb,
                    ucb.getAccess(),
                    ucb.hasAuthority(),
                    ucb.getCustomAuthority()));
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
                    wb.getCustomAuthority(),
                    wb.isCustom()));
        }

        // Auto-registration of authenticate/refreshAuthorization workflows
        // moved to autoRegisterSecurityWorkflows() — invoked from
        // populateWorkflowStages() so they're present before stage assembly.

        // Compute configuration flags
        boolean securityEnabled = this.securityBuilder != null
                && ((DomainSecurityBuilder<E>) this.securityBuilder).isSecurityEnabled();
        boolean hasAuthorization = this.securityBuilder != null
                && ((DomainSecurityBuilder<E>) this.securityBuilder).hasAuthenticator()
                && ((AuthenticatorBuilder<E>) ((DomainSecurityBuilder<E>) this.securityBuilder).getAuthenticator()).hasAuthorizationConfig();

        // Compute domain characteristics for workflow assembly
        boolean multiTenancyEnabled = this.up() instanceof ApiBuilder acb && acb.isMultiTenant();
        boolean isOwnerOrOwned = this.owner != null || this.owned != null;

        // Workflow stages were normally populated into the shared
        // IWorkflowsBuilder at CONFIGURATION stage (see
        // ApiBuilder.doConfigureWithDependencyBuilder). If we're called as
        // a fallback (DomainBuilder.build() invoked directly, or auto-
        // detected domains registered AFTER the CONFIG hook ran),
        // populate them now — populateWorkflowStages() is idempotent.
        //
        // For the workflow lookup we go through the per-name child builder
        // (workflowsBuilder.workflow(name).build()) rather than the shared
        // map: WorkflowsBuilder.build() is memoised, so the shared map is
        // frozen at the moment of the first lookup and won't include
        // workflows registered later (e.g. auto-detected domains). The
        // per-child build() side-steps that cache while still benefiting
        // from WorkflowBuilder.build()'s own idempotency.
        IWorkflow builtWorkflow;
        if (this.up() instanceof ApiBuilder ab) {
            com.garganttua.core.workflow.dsl.IWorkflowsBuilder wb = ab.getWorkflowsBuilder();
            if (wb == null) {
                throw new ApiException("IWorkflowsBuilder not provided to ApiBuilder — "
                        + "Bootstrap should auto-discover it via WorkflowsBuilderFactory, or "
                        + "the caller should provide() one explicitly before build().");
            }
            this.populateWorkflowStages(wb, this.injectionContextBuilder,
                    this.expressionContextBuilder, multiTenancyEnabled,
                    ab.getWorkflowTimingConfig());
            try {
                builtWorkflow = wb.workflow(this.domainName).build();
            } catch (com.garganttua.core.dsl.DslException e) {
                throw new ApiException("Failed to build workflow for domain '"
                        + this.domainName + "': " + e.getMessage(), e);
            }
        } else {
            throw new ApiException("DomainBuilder's parent is not an ApiBuilder — "
                    + "cannot retrieve the built workflow for domain '" + this.domainName + "'.");
        }

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
        List<ISupplier<IInterface>> builtInterfaces = new ArrayList<>();
        for (ISupplierBuilder<? extends IInterface, ? extends ISupplier<? extends IInterface>> interfaceBuilder : this.interfaces) {
            ISupplier<IInterface> supplier = (ISupplier<IInterface>) interfaceBuilder.build();
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
                        securityDefinition,
                        this.securityBuilder == null
                                ? null
                                : ((DomainSecurityBuilder<E>) this.securityBuilder).buildKeyDefinition()),
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

    public String getDomainName() {
        return this.domainName;
    }

    /** True when at least one interface was declared on this domain via {@code .interfasse(...)}. */
    public boolean hasInterfaces() {
        return !this.interfaces.isEmpty();
    }

    private volatile boolean workflowStagesPopulated = false;

    /**
     * Populates this domain's workflow stages into the shared
     * {@link com.garganttua.core.workflow.dsl.IWorkflowsBuilder}. Called from
     * {@link ApiBuilder#doConfigureWithDependencyBuilder} at the Bootstrap
     * CONFIGURATION stage so that {@code WorkflowsBuilder.doBuild()} at
     * BUILD stage sees a filled registry (topo orders WorkflowsBuilder before
     * ApiBuilder, so anything we'd push at BUILD time arrives too late). Also
     * called as a fallback from {@link #build()} when a caller drives
     * DomainBuilder directly (typical of unit tests), bypassing both
     * Bootstrap and ApiBuilder.build()'s configuration-stage trigger.
     *
     * <p>Idempotent — subsequent calls are no-ops so the
     * {@code workflowsBuilder.workflow(name)} child doesn't accumulate
     * duplicated stages.
     */
    public void populateWorkflowStages(
            com.garganttua.core.workflow.dsl.IWorkflowsBuilder workflowsBuilder,
            com.garganttua.core.injection.context.dsl.IInjectionContextBuilder injectionContextBuilder,
            com.garganttua.core.expression.dsl.IExpressionContextBuilder expressionContextBuilder,
            boolean multiTenancyEnabled,
            com.garganttua.core.workflow.WorkflowTimingConfig workflowTimingConfig) throws ApiException {
        if (this.workflowStagesPopulated) {
            return;
        }
        // Auto-register security-driven workflows BEFORE assembling stages —
        // these used to live inside build() but stage assembly now happens at
        // CONFIGURATION (well before build()). Without this prelude, the
        // assembler iterates an incomplete workflows map and the authenticate
        // operation's request returns 405.
        autoRegisterSecurityWorkflows();

        boolean securityEnabled = this.securityBuilder != null
                && ((DomainSecurityBuilder<E>) this.securityBuilder).isSecurityEnabled();
        boolean hasAuthorization = this.securityBuilder != null
                && ((DomainSecurityBuilder<E>) this.securityBuilder).hasAuthenticator()
                && ((AuthenticatorBuilder<E>) ((DomainSecurityBuilder<E>) this.securityBuilder).getAuthenticator()).hasAuthorizationConfig();
        boolean isOwnerOrOwned = this.owner != null || this.owned != null;

        new DomainWorkflowAssembler<E>(
                this.domainName, this.workflows, this.useCases.keySet(), securityEnabled, hasAuthorization,
                multiTenancyEnabled, isOwnerOrOwned,
                injectionContextBuilder, expressionContextBuilder,
                workflowsBuilder, workflowTimingConfig).populateStages();
        this.workflowStagesPopulated = true;
    }

    @SuppressWarnings("unchecked")
    private void autoRegisterSecurityWorkflows() {
        // Auto-register authenticate workflow when domain has an authenticator
        if (this.securityBuilder != null
                && ((DomainSecurityBuilder<E>) this.securityBuilder).hasAuthenticator()
                && !this.workflows.containsKey(BusinessOperation.authenticate.getLabel())) {
            registerCrudMetadata(BusinessOperation.authenticate.getLabel(),
                    TechnicalOperation.create, Scope.oneEntity);
        }

        // Auto-register refreshAuthorization workflow when the authenticator has
        // an authorization config and the linked authorization is refreshable.
        // The runtime guard inside REFRESH_AUTHORIZATION.gs additionally checks
        // isAuthorizationRefreshable so a misconfiguration just rejects requests.
        if (this.securityBuilder != null
                && ((DomainSecurityBuilder<E>) this.securityBuilder).hasAuthenticator()
                && ((AuthenticatorBuilder<E>) ((DomainSecurityBuilder<E>) this.securityBuilder).getAuthenticator()).hasAuthorizationConfig()
                && !this.workflows.containsKey(BusinessOperation.refreshAuthorization.getLabel())) {
            registerCrudMetadata(BusinessOperation.refreshAuthorization.getLabel(),
                    TechnicalOperation.create, Scope.oneEntity);
        }
    }


    private void throwExceptionIfNoDto() throws ApiException {
        if (this.dtos.size() == 0) {
            String entityHint = this.entityClass != null
                    ? this.entityClass.getSimpleName()
                    : "MyEntity";
            throw new ApiException("No dto declared for domain '" + this.domainName + "'. "
                    + "Each domain needs at least one DTO (the persisted shape — fields, id/uuid/tenantId mapping, DAO). Example:\n"
                    + "\n"
                    + "    apiBuilder.domain(" + entityHint + ".class)\n"
                    + "        .entity().id(\"id\").uuid(\"uuid\").tenantId(\"tenantId\").up()\n"
                    + "        .dto(" + entityHint + "Dto.class)             // <- missing\n"
                    + "            .id(\"id\").uuid(\"uuid\").tenantId(\"tenantId\")\n"
                    + "            .db(new MyDao())\n"
                    + "        .up()");
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

        // NB: an authorization domain MAY also be an authenticator (declare a
        // @AuthenticationAuthenticate method on the token to verify it with
        // custom logic / external signature), but it is NOT required. When the
        // token domain has no authenticator, verifyAuthorization falls back to
        // the framework's standard verification (signature + expiration +
        // revocation). Custom-or-default, symmetric with the mint-side issuer.

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

    /**
     * Enforces that the structural super-status fields are declared whenever the
     * domain carries the role that needs them. A tenant entity must carry a
     * boolean {@code superTenant} field and an owner entity a boolean
     * {@code superOwner} field, so the framework can identify super-tenants /
     * super-owners server-side (the field feeds the {@code Api} super-registries
     * scanned at startup and maintained on create/update). The {@code Boolean}
     * type is already enforced by {@link FieldResolver} in the
     * {@code superTenant()} / {@code superOwner()} setters; here we only enforce
     * presence.
     */
    private void validateSuperFields() throws ApiException {
        if (this.tenant && this.superTenant == null) {
            throw new ApiException("Domain '" + this.domainName + "' is the tenant (.tenant(true) / @EntityTenant) "
                    + "but declares no superTenant field. A tenant entity must carry a boolean superTenant field so "
                    + "the framework can identify super-tenants server-side — declare it via .superTenant(field) on the "
                    + "domain builder, or annotate the boolean field with @EntitySuperTenant.");
        }
        if (this.owner != null && this.superOwner == null) {
            throw new ApiException("Domain '" + this.domainName + "' is an owner (.owner(field) / @EntityOwner) "
                    + "but declares no superOwner field. An owner entity must carry a boolean superOwner field so "
                    + "the framework can identify super-owners server-side — declare it via .superOwner(field) on the "
                    + "domain builder, or annotate the boolean field with @EntitySuperOwner.");
        }
    }

    @Override
    protected void doAutoDetection() {

    }

    @Override
    public synchronized IEntityBuilder<E> entity() throws ApiException {
        requireEntityClassSet("entity");

        if( this.entityBuilder == null)
            this.entityBuilder = new EntityBuilder<>(entityClass, this);

        return this.entityBuilder;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public IDomainBuilder<E> interfasse(IClass<? extends IInterface> interfasse) throws ApiException {
        Objects.requireNonNull(interfasse, "Interface class cannot be null");
        // Explicit attachment of a (catalogued) @Interface type to this domain:
        // instantiate it via its no-arg constructor and add it to the domain's
        // interface suppliers. A pre-built / configured instance goes through the
        // other overload, interfasse(ISupplierBuilder).
        IInterface instance;
        try {
            instance = (IInterface) interfasse.getConstructor().newInstance();
        } catch (Exception e) {
            throw new ApiException("Failed to instantiate @Interface class '" + interfasse.getName()
                    + "'. A public no-arg constructor is required.", e);
        }
        this.interfaces.add(new FixedSupplierBuilder(instance, interfasse));
        return this;
    }

    @Override
    public IEntityBuilder<E> name(String name) throws ApiException {
        Objects.requireNonNull(name, "Name cannot be null");
        this.domainName = name;
        return this.entity();
    }

}
