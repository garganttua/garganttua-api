package com.garganttua.api.core.builder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.garganttua.api.core.builder.binder.DomainStartupBinderBuilder;
import com.garganttua.api.core.context.application.DomainContext;
import com.garganttua.api.core.definition.DomainDefinition;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.spec.Pluralizer;
import com.garganttua.api.spec.context.ContextBuildingStage;
import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.context.dsl.IDomainStartupBinderBuilder;
import com.garganttua.api.spec.context.dsl.IDtoBuilder;
import com.garganttua.api.spec.context.dsl.IEntityBuilder;
import com.garganttua.api.spec.context.dsl.IUseCaseBinderBuilder;
import com.garganttua.api.spec.context.dsl.IUseCaseBuilder;
import com.garganttua.api.spec.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.api.spec.domain.IDomainContext;
import com.garganttua.api.spec.context.IDtoContext;
import com.garganttua.api.spec.event.IEventPublisher;
import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.core.CoreException;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.mapper.Mapper;
import com.garganttua.core.mapper.MapperException;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.binders.dsl.IMethodBinderBuilder;
import com.garganttua.core.reflection.fields.FieldResolver;
import com.garganttua.core.reflection.query.ObjectQueryFactory;
import com.garganttua.core.supply.dsl.IObjectSupplierBuilder;
import com.garganttua.core.supply.FixedObjectSupplier;
import com.garganttua.core.supply.IObjectSupplier;
import com.garganttua.core.supply.IObjectSupplier;

public class DomainBuilder<E>
        extends AbstractAutomaticLinkedBuilder<IDomainBuilder<E>, IApiContext, IDomainContext<E>>
        implements IDomainBuilder<E> {

    private String domainName;
    private Class<?> entityClass;

    private Mapper mapper = DefaultMapper.mapper();

    private List<IDomainStartupBinderBuilder> startupBinderBuilders = new ArrayList<IDomainStartupBinderBuilder>();
    private List<IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>>> interfaces = new ArrayList<>();
    private List<IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>>> events = new ArrayList<>();

    private boolean creation = true;
    private boolean readAll = true;
    private boolean readOne = true;
    private boolean update = true;
    private boolean deleteAll = true;
    private boolean deleteOne = true;

    private boolean publik = false;
    private boolean tenant = false;
    private IEntityBuilder<E> entityBuilder;
    private List<Object> createEntities = new ArrayList<>();
    private List<Object> upsertEntities = new ArrayList<>();
    private IObjectQuery objectQuery;
    private ObjectAddress owner;
    private ObjectAddress owned;
    private ObjectAddress shared;
    private ObjectAddress hiddenable;
    private IDomainSecurityBuilder<E> securityBuilder;
    private Map<Class<?>, IDtoBuilder> dtos = new HashMap<>();
    private Map<String, IUseCaseBuilder<?, ?, E>> useCases = new HashMap<>();

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
        this.securityBuilder = new DomainSecurityBuilder(this, this.interfaces, this.objectQuery, this.entityClass);
        this.entityBuilder = this.entity(this.entityClass);
    }

    @Override
    public IDomainStartupBinderBuilder startup(ContextBuildingStage stage, IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> supplier)
            throws DslException {
        DomainStartupBinderBuilder binder = new DomainStartupBinderBuilder(this, supplier);
        this.startupBinderBuilders.add(binder);
        return binder;
    }

    @Override
    public IDomainBuilder<E> interfasse(IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> bean) throws DslException {
        if (!IInterface.class.isAssignableFrom(bean.getSuppliedType())) {
            throw new DslException(
                    "Bean " + bean.getSuppliedType().getName() + " does not implement IInterface");
        }
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
    public IDomainBuilder<E> events(IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> bean) throws DslException {
        if (!IEventPublisher.class.isAssignableFrom(bean.getObjectClass())) {
            throw new DslException(
                    "Bean " + bean.getObjectClass().getName() + " does not implement IEventPublisher");
        }
        this.events.add(bean);
        return this;
    }

    @Override
    public IDomainBuilder<E> events(IEventPublisher eventPublisher) throws DslException {
        this.events.add(new FixedObjectSupplier<IEventPublisher>(
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

    @Override
    public IEntityBuilder<E> entity(Class<?> entityClass) throws DslException {
        Objects.requireNonNull(entityClass, "Entity class cannot be null");

        if (this.entityBuilder != null && Objects.equals(entityClass, this.entityClass)) {
            throw new DslException(
                    "Entity Class is already set with class " + entityClass.getSimpleName());
        }

        this.entityBuilder = new EntityBuilder(entityClass, this);
        this.entityClass = entityClass;

        try {
            this.objectQuery = ObjectQueryFactory.objectQuery(this.entityClass);
        } catch (ReflectionException e) {
            throw new DslException(e.getMessage(), e);
        }

        this.securityBuilder = new DomainSecurityBuilder(this, this.interfaces, this.objectQuery, this.entityClass);

        return this.entityBuilder;
    }

    @Override
    public IDomainSecurityBuilder<E> security() throws DslException {
        if (this.securityBuilder == null)
            throw new DslException("Security builder is null, please set entity class first");
        return this.securityBuilder;
    }

    @Override
    public <D> IDtoBuilder<E, D> dto(Class<D> dtoClass) throws DslException {
        if( this.entityClass == null )
            throw new DslException("Entity class must be set before declaring a dto");

        @SuppressWarnings("unchecked")
        IDtoBuilder<E, D> dtoBuilder = (IDtoBuilder<E, D>) this.dtos.get(dtoClass);

        if (dtoBuilder == null) {
            dtoBuilder = new DtoBuilder(dtoClass, this);
            this.dtos.put(dtoClass, dtoBuilder);
        }

        try {
            this.mapper.recordMappingConfiguration(this.entityClass, dtoClass);
        } catch (MapperException e) {
            throw new DslException(e.getMessage(), e);
        }

        return dtoBuilder;
    }

    @Override
    public <I, O> IUseCaseBuilder<I, O, E> useCase(String useCaseName, Class<I> inputType, Class<O> outputType) {
        Objects.requireNonNull(useCaseName, "Use case name cannot be null");
        @SuppressWarnings("unchecked")
        IUseCaseBuilder<I, O, E> useCaseBuilder = (IUseCaseBuilder<I, O, E>) this.useCases.get(useCaseName);

        if (useCaseBuilder == null) {
            useCaseBuilder = new UseCaseBuilder<I, O, E>(useCaseName, this);
            this.useCases.put(useCaseName, useCaseBuilder);
        }

        return useCaseBuilder;
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

    @Override
    protected IDomainContext doBuild() throws CoreException {

        this.throwExceptionIfNoDto();

        List<IMethodBinderBuilder<?, ?, ?>> binderBuilders = this.startupBinderBuilders.stream().map(builder -> builder)
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

    private void throwExceptionIfNoDto() throws DslException {
        if (this.dtos.size() == 0) {
            throw new DslException("No dto declared for domain " + this.domainName);
        }
    }

    @Override
    protected void doAutoDetection() {

    }

    @Override
    public IEntityBuilder entity() throws DslException {
        if( this.entityClass == null )
            throw new DslException("Entity class is not set");

        if( this.entityBuilder == null)
            this.entityBuilder = new EntityBuilder(entityClass, this);

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
