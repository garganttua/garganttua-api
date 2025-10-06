package com.garganttua.api.core.builder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.garganttua.api.core.builder.binder.DomainStartupBinderBuilder;
import com.garganttua.api.core.builder.resolver.FieldResolver;
import com.garganttua.api.core.builder.supplier.FixedObjectSupplierBuilder;
import com.garganttua.api.core.context.application.DomainContext;
import com.garganttua.api.core.definition.DomainDefinition;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.Pluralizer;
import com.garganttua.api.spec.engine.ContextBuildingStage;
import com.garganttua.api.spec.engine.IApplicationContextBuilder;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IDomainContext;
import com.garganttua.api.spec.engine.IDomainDtoContext;
import com.garganttua.api.spec.engine.IDomainStartupBinderBuilder;
import com.garganttua.api.spec.engine.IDtoBuilder;
import com.garganttua.api.spec.engine.IEntityBuilder;
import com.garganttua.api.spec.engine.IMethodBinderBuilder;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;
import com.garganttua.api.spec.engine.IUseCaseBinderBuilder;
import com.garganttua.api.spec.engine.IUseCaseBuilder;
import com.garganttua.api.spec.event.IEventPublisher;
import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.api.spec.security.IDomainSecurityBuilder;
import com.garganttua.objects.mapper.GGMapperException;
import com.garganttua.objects.mapper.IGGMapper;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

public class DomainBuilder
        extends AbstractAutomaticLinkedBuilder<IDomainContext, IDomainBuilder, IApplicationContextBuilder>
        implements IDomainBuilder {

    private String domainName;
    private Class<?> entityClass;

    private IGGMapper mapper = DefaultMapper.mapper();

    private List<IDomainStartupBinderBuilder> startupBinderBuilders = new ArrayList<IDomainStartupBinderBuilder>();
    private List<IObjectSupplierBuilder<?>> interfaces = new ArrayList<>();
    private List<IObjectSupplierBuilder<?>> events = new ArrayList<>();

    private boolean creation = true;
    private boolean readAll = true;
    private boolean readOne = true;
    private boolean update = true;
    private boolean deleteAll = true;
    private boolean deleteOne = true;

    private boolean publik = false;
    private boolean tenant = false;
    private IEntityBuilder entityBuilder;
    private List<Object> createEntities = new ArrayList<>();
    private List<Object> upsertEntities = new ArrayList<>();
    private IGGObjectQuery objectQuery;
    private GGObjectAddress owner;
    private GGObjectAddress owned;
    private GGObjectAddress shared;
    private GGObjectAddress hiddenable;
    private IDomainSecurityBuilder securityBuilder;
    private Map<Class<?>, IDtoBuilder> dtos = new HashMap<>();
    private Map<String, IUseCaseBuilder<IDomainBuilder>> useCases = new HashMap<>();

    public DomainBuilder(IApplicationContextBuilder builder, String domainName)
            throws BuilderException {
        super(builder);
        this.domainName = Objects.requireNonNull(domainName, "Domain name cannot be null");
    }

    public DomainBuilder(IApplicationContextBuilder builder, Class<?> entityClass) throws BuilderException {
        super(builder);
        this.entityClass = Objects.requireNonNull(entityClass, "Entity Class cannot be null");
        try {
            this.objectQuery = GGObjectQueryFactory.objectQuery(this.entityClass);
        } catch (GGReflectionException e) {
            throw new BuilderException(e.getMessage(), e);
        }
        this.domainName = Pluralizer.toPlural(this.entityClass.getSimpleName().toLowerCase());
        this.securityBuilder = new DomainSecurityBuilder(this, this.interfaces, this.objectQuery, this.entityClass);
        this.entityBuilder = this.entity(this.entityClass);
    }

    @Override
    public IDomainStartupBinderBuilder startup(ContextBuildingStage stage, IObjectSupplierBuilder<?> supplier)
            throws BuilderException {
        DomainStartupBinderBuilder binder = new DomainStartupBinderBuilder(this, supplier);
        this.startupBinderBuilders.add(binder);
        return binder;
    }

    @Override
    public IDomainBuilder interfasse(IObjectSupplierBuilder<?> bean) throws CoreException {
        if (!IInterface.class.isAssignableFrom(bean.getObjectClass())) {
            throw new BuilderException(
                    "Bean " + bean.getObjectClass().getName() + " does not implement IInterface");
        }
        this.interfaces.add(bean);
        return this;
    }

    @Override
    public IDomainBuilder interfasse(IInterface interfasse) throws CoreException {
        this.interfaces
                .add(new FixedObjectSupplierBuilder<IInterface>(
                        Objects.requireNonNull(interfasse, "Interface cannot be null")));
        return this;
    }

    @Override
    public IDomainBuilder creation(boolean b) {
        this.creation = b;
        return this;
    }

    @Override
    public IDomainBuilder readAll(boolean b) {
        this.readAll = b;
        return this;
    }

    @Override
    public IDomainBuilder readOne(boolean b) {
        this.readOne = b;
        return this;
    }

    @Override
    public IDomainBuilder update(boolean b) {
        this.update = b;
        return this;
    }

    @Override
    public IDomainBuilder deleteAll(boolean b) {
        this.deleteAll = b;
        return this;
    }

    @Override
    public IDomainBuilder deleteOne(boolean b) {
        this.deleteOne = b;
        return this;
    }

    @Override
    public IDomainBuilder events(IObjectSupplierBuilder<?> bean) throws CoreException {
        if (!IEventPublisher.class.isAssignableFrom(bean.getObjectClass())) {
            throw new BuilderException(
                    "Bean " + bean.getObjectClass().getName() + " does not implement IEventPublisher");
        }
        this.events.add(bean);
        return this;
    }

    @Override
    public IDomainBuilder events(IEventPublisher eventPublisher) throws CoreException {
        this.events.add(new FixedObjectSupplierBuilder<IEventPublisher>(
                Objects.requireNonNull(eventPublisher, "EventPublisher cannot be null")));
        return this;
    }

    @Override
    public IDomainBuilder tenant(boolean b) {
        this.tenant = b;
        return this;
    }

    @Override
    public IDomainBuilder owner(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        if (this.entityBuilder == null) {
            throw new BuilderException("Entity class must be defined first");
        }

        this.owner = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder owner(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");
        if (this.entityBuilder == null) {
            throw new BuilderException("Entity class must be defined first");
        }

        this.owner = FieldResolver.fieldByField(field, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder owner(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        if (this.entityBuilder == null) {
            throw new BuilderException("Entity class must be defined first");
        }

        this.owner = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder owned(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        if (this.entityBuilder == null) {
            throw new BuilderException("Entity class must be defined first");
        }

        this.owned = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder owned(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");
        if (this.entityBuilder == null) {
            throw new BuilderException("Entity class must be defined first");
        }

        this.owned = FieldResolver.fieldByField(field, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder owned(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        if (this.entityBuilder == null) {
            throw new BuilderException("Entity class must be defined first");
        }

        this.owned = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder publik() {
        this.publik = true;
        return this;
    }

    @Override
    public IDomainBuilder shared(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");
        if (this.entityBuilder == null) {
            throw new BuilderException("Entity class must be defined first");
        }

        this.shared = FieldResolver.fieldByField(field, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder shared(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        if (this.entityBuilder == null) {
            throw new BuilderException("Entity class must be defined first");
        }

        this.shared = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder shared(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        if (this.entityBuilder == null) {
            throw new BuilderException("Entity class must be defined first");
        }

        this.shared = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder hiddenable(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        if (this.entityBuilder == null) {
            throw new BuilderException("Entity class must be defined first");
        }

        this.hiddenable = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IDomainBuilder hiddenable(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");
        if (this.entityBuilder == null) {
            throw new BuilderException("Entity class must be defined first");
        }

        this.hiddenable = FieldResolver.fieldByField(field, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IDomainBuilder hiddenable(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        if (this.entityBuilder == null) {
            throw new BuilderException("Entity class must be defined first");
        }

        this.hiddenable = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IEntityBuilder entity(Class<?> entityClass) throws BuilderException {
        Objects.requireNonNull(entityClass, "Entity class cannot be null");

        if (this.entityBuilder != null && Objects.equals(entityClass, this.entityClass)) {
            throw new BuilderException(
                    "Entity Class is already set with class " + entityClass.getSimpleName());
        }

        this.entityBuilder = new EntityBuilder(entityClass, this);
        this.entityClass = entityClass;

        try {
            this.objectQuery = GGObjectQueryFactory.objectQuery(this.entityClass);
        } catch (GGReflectionException e) {
            throw new BuilderException(e.getMessage(), e);
        }

        this.securityBuilder = new DomainSecurityBuilder(this, this.interfaces, this.objectQuery, this.entityClass);

        return this.entityBuilder;
    }

    @Override
    public IDomainSecurityBuilder security() throws BuilderException {
        if (this.securityBuilder == null)
            throw new BuilderException("Security builder is null, please set entity class first");
        return this.securityBuilder;
    }

    @Override
    public IDtoBuilder dto(Class<?> dtoClass) throws BuilderException {
        if( this.entityClass == null )
            throw new BuilderException("Entity class must be set before declaring a dto");

        IDtoBuilder dtoBuilder = this.dtos.get(dtoClass);

        if (dtoBuilder == null) {
            dtoBuilder = new DtoBuilder(dtoClass, this);
            this.dtos.put(dtoClass, dtoBuilder);
        }

        try {
            this.mapper.recordMappingConfiguration(this.entityClass, dtoClass);
        } catch (GGMapperException e) {
            throw new BuilderException(e.getMessage(), e);
        }

        return dtoBuilder;
    }

    @Override
    public IUseCaseBuilder<IDomainBuilder> useCase(String useCaseName) {
        Objects.requireNonNull(useCaseName, "Use case name cannot be null");
        IUseCaseBuilder<IDomainBuilder> useCaseBuilder = this.useCases.get(useCaseName);

        if (useCaseBuilder == null) {
            useCaseBuilder = new UseCaseBuilder<IDomainBuilder>(useCaseName, this);
            this.useCases.put(useCaseName, useCaseBuilder);
        }

        return useCaseBuilder;
    }

    @Override
    public IUseCaseBuilder<IDomainBuilder> useCase(IUseCaseBinderBuilder<IDomainBuilder> binder) {
        Objects.requireNonNull(binder, "Binder cannot be null");

        String useCaseName = binder.getMethodName();
        IUseCaseBuilder<IDomainBuilder> useCaseBuilder = this.useCases.get(useCaseName);

        if (useCaseBuilder == null) {
            useCaseBuilder = new UseCaseBuilder<IDomainBuilder>(useCaseName, this);
            this.useCases.put(useCaseName, useCaseBuilder);
        }

        return useCaseBuilder;
    }

    @Override
    public IDomainBuilder create(Object entity) {
        this.createEntities.add(entity);
        return this;
    }

    @Override
    public IDomainBuilder upsert(Object entity) {
        this.upsertEntities.add(entity);
        return this;
    }

    @Override
    public Class<?> getEntityClass() throws CoreException {
        if (this.entityClass != null)
            return this.entityClass;

        throw new BuilderException("Entity class is not set !");
    }

    @Override
    protected IDomainContext doBuild() throws CoreException {

        this.throwExceptionIfNoDto();

        List<IMethodBinderBuilder<?, ?>> binderBuilders = this.startupBinderBuilders.stream().map(builder -> builder)
                .collect(Collectors.toList());

        List<IDomainDtoContext> dtoContexts = new ArrayList<>();
        for (IDtoBuilder builder : this.dtos.values()) {
            dtoContexts.add(builder.build());
        }

        return new DomainContext(
                new DomainDefinition(
                        this.domainName,
                        binderBuilders,
                        this.creation,
                        this.readAll,
                        this.readOne,
                        this.update,
                        this.deleteAll,
                        this.deleteOne,
                        this.publik,
                        this.tenant,
                        this.createEntities,
                        this.upsertEntities,
                        this.owner,
                        this.owned,
                        this.shared,
                        this.hiddenable,
                        this.useCases),
                this.entityBuilder.build(),
                this.securityBuilder.build(),
                dtoContexts,
                this.interfaces,
                this.events);
    }

    private void throwExceptionIfNoDto() throws BuilderException {
        if (this.dtos.size() == 0) {
            throw new BuilderException("No dto declared for domain " + this.domainName);
        }
    }

    @Override
    protected void doAutoDetection() {

    }

    @Override
    public IEntityBuilder entity() throws BuilderException {
        if( this.entityClass == null )
            throw new BuilderException("Entity class is not set");

        if( this.entityBuilder == null)
            this.entityBuilder = new EntityBuilder(entityClass, this);

        return this.entityBuilder;
    }

}
