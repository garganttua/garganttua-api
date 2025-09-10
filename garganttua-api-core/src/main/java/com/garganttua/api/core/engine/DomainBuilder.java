package com.garganttua.api.core.engine;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.garganttua.api.core.security.engine.SecurityBuilder;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.engine.ContextBuildingStage;
import com.garganttua.api.spec.engine.IAuthenticatorBuilder;
import com.garganttua.api.spec.engine.IAuthorizationBuilder;
import com.garganttua.api.spec.engine.IContext;
import com.garganttua.api.spec.engine.IContextBuilder;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IDomainStartupBinderBuilder;
import com.garganttua.api.spec.engine.IDtoBuilder;
import com.garganttua.api.spec.engine.IEntityBuilder;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.api.spec.engine.IUseCaseBuilder;
import com.garganttua.api.spec.event.IEventPublisher;
import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.api.spec.security.ISecurityBuilder;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

public class DomainBuilder implements IDomainBuilder {

    private IContextBuilder builder;
    private String domainName;

    private List<IDomainStartupBinderBuilder> startupBinderBuilders = new ArrayList<IDomainStartupBinderBuilder>();
    private List<IObjectSupplier<?>> interfaces = new ArrayList<>();
    private List<IObjectSupplier<?>> events = new ArrayList<>();

    private boolean creation = true;
    private boolean readAll = true;
    private boolean readOne = true;
    private boolean update = true;
    private boolean deleteAll = true;
    private boolean deleteOne = true;
    private boolean publik = false;
    private Class<?> entityClass;
    private boolean tenant = false;
    private IEntityBuilder entityBuilder;
    private List<Object> createEntities = new ArrayList<>();
    private List<Object> upsertEntities = new ArrayList<>();
    private IGGObjectQuery objectQuery;
    private Field owner;
    private Field owned;
    private Field shared;
    private Field hiddenable;
    private boolean autoDetectDtos = false;
    private boolean autoDetectUseCases = false;
    private ISecurityBuilder securityBuilder;
    private Map<Class<?>, IDtoBuilder> dtos = new HashMap<>();
    private Map<String, IUseCaseBuilder> useCases = new HashMap<>();
    private IAuthorizationBuilder authorization;
    private IAuthenticatorBuilder authenticator;

    public DomainBuilder(IContextBuilder builder, String domainName) throws BuilderException {
        this.builder = Objects.requireNonNull(builder, "Builder cannot be null");
        this.domainName = Objects.requireNonNull(domainName, "Domain name cannot be null");
        this.securityBuilder = new SecurityBuilder();
    }

    @Override
    public IDomainStartupBinderBuilder startup(ContextBuildingStage stage, IObjectSupplier<?> supplier) {
        DomainStartupBinderBuilder binder = new DomainStartupBinderBuilder(this, supplier);
        this.startupBinderBuilders.add(binder);
        return binder;
    }

    @Override
    public IDomainBuilder interfasse(IObjectSupplier<?> bean) throws CoreException {
        if (!IInterface.class.isAssignableFrom(bean.getObjectClass())) {
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                    "Bean " + bean.getObjectClass().getName() + " does not implement IInterface");
        }
        this.interfaces.add(bean);
        return this;
    }

    @Override
    public IDomainBuilder interfasse(IInterface interfasse) throws CoreException {
        this.interfaces
                .add(new ObjectSupplier<IInterface>(Objects.requireNonNull(interfasse, "Interface cannot be null")));
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
    public IDomainBuilder events(IObjectSupplier<?> bean) throws CoreException {
        if (!IEventPublisher.class.isAssignableFrom(bean.getObjectClass())) {
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                    "Bean " + bean.getObjectClass().getName() + " does not implement IEventPublisher");
        }
        this.events.add(bean);
        return this;
    }

    @Override
    public IDomainBuilder events(IEventPublisher eventPublisher) throws CoreException {
        this.events.add(new ObjectSupplier<IEventPublisher>(
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
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE, "Entity class must be defined first");
        }

        this.owner = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder owner(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");
        if (this.entityBuilder == null) {
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE, "Entity class must be defined first");
        }

        this.owner = FieldResolver.fieldByField(field, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder owner(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        if (this.entityBuilder == null) {
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE, "Entity class must be defined first");
        }

        this.owner = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder owned(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        if (this.entityBuilder == null) {
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE, "Entity class must be defined first");
        }

        this.owned = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder owned(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");
        if (this.entityBuilder == null) {
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE, "Entity class must be defined first");
        }

        this.owned = FieldResolver.fieldByField(field, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder owned(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        if (this.entityBuilder == null) {
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE, "Entity class must be defined first");
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
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE, "Entity class must be defined first");
        }

        this.shared = FieldResolver.fieldByField(field, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder shared(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        if (this.entityBuilder == null) {
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE, "Entity class must be defined first");
        }

        this.shared = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder shared(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        if (this.entityBuilder == null) {
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE, "Entity class must be defined first");
        }

        this.shared = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, String.class);

        return this;
    }

    @Override
    public IDomainBuilder hiddenable(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        if (this.entityBuilder == null) {
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE, "Entity class must be defined first");
        }

        this.hiddenable = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IDomainBuilder hiddenable(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");
        if (this.entityBuilder == null) {
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE, "Entity class must be defined first");
        }

        this.hiddenable = FieldResolver.fieldByField(field, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IDomainBuilder hiddenable(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        if (this.entityBuilder == null) {
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE, "Entity class must be defined first");
        }

        this.hiddenable = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.entityClass, Boolean.class);

        return this;
    }

    @Override
    public IAuthorizationBuilder authorization() {
        if( this.authorization != null ){
            this.authorization = new AuthorizationBuilder(this);
        }
        return this.authorization;
    }

    @Override
    public IAuthenticatorBuilder authenticator() {
        if( this.authenticator != null ){
            this.authenticator = new AuthenticatorBuilder(this);
        }
        return this.authenticator;
    }

    @Override
    public IEntityBuilder entity(Class<?> entityClass) throws BuilderException {
        Objects.requireNonNull(entityClass, "Entity class cannot be null");

        if (this.entityBuilder != null && Objects.equals(entityClass, this.entityClass)) {
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                    "Entity Class is already set with class " + entityClass.getSimpleName());
        }

        this.entityBuilder = new EntityBuilder(entityClass, this);
        this.entityClass = entityClass;

        try {
            this.objectQuery = GGObjectQueryFactory.objectQuery(this.entityClass);
        } catch (GGReflectionException e) {
            throw new BuilderException(CoreExceptionCode.CORE_GENERIC_CODE, e.getMessage(), e);
        }

        return this.entityBuilder;
    }

    @Override
    public ISecurityBuilder security() {
        return this.securityBuilder;
    }

    @Override
    public IDomainBuilder autoDetectDtos(boolean b) {
        this.autoDetectDtos = true;
        return this;
    }

    @Override
    public IDtoBuilder dto(Class<?> dtoClass) {
        IDtoBuilder dtoBuilder = this.dtos.get(dtoClass);

        if (dtoBuilder == null) {
            dtoBuilder = new DtoBuilder(dtoClass, this);
            this.dtos.put(dtoClass, dtoBuilder);
        }

        return dtoBuilder;
    }

    @Override
    public IUseCaseBuilder useCase(String useCaseName) {
        IUseCaseBuilder useCaseBuilder = this.useCases.get(useCaseName);

        if (useCaseBuilder == null) {
            useCaseBuilder = new UseCaseBuilder(useCaseName, this);
            this.useCases.put(useCaseName, useCaseBuilder);
        }

        return useCaseBuilder;
    }

    @Override
    public IContext build() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'build'");
    }

    @Override
    public IDomainBuilder autoDetectUseCases(boolean b) {
        this.autoDetectUseCases = b;
        return this;
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

}
