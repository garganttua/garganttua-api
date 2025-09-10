package com.garganttua.api.core.engine;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

import lombok.Builder;

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

    public DomainBuilder(IContextBuilder builder, String domainName) throws BuilderException {
        this.builder = Objects.requireNonNull(builder, "Builder cannot be null");
        this.domainName = Objects.requireNonNull(domainName, "Domain name cannot be null");
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
    public IDomainBuilder owned(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'owned'");
    }

    @Override
    public IDomainBuilder owned(Field field) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'owned'");
    }

    @Override
    public IDomainBuilder owned(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'owned'");
    }

    @Override
    public IDomainBuilder publik() {
        this.publik = true;
        return this;
    }

    @Override
    public IDomainBuilder shared(Field field) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'shared'");
    }

    @Override
    public IDomainBuilder shared(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'shared'");
    }

    @Override
    public IDomainBuilder shared(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'shared'");
    }

    @Override
    public IDomainBuilder hiddenable(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hiddenable'");
    }

    @Override
    public IDomainBuilder hiddenable(Field field) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hiddenable'");
    }

    @Override
    public IDomainBuilder hiddenable(GGObjectAddress fieldAddress) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hiddenable'");
    }

    @Override
    public IAuthorizationBuilder authorization() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authorization'");
    }

    @Override
    public IAuthenticatorBuilder authenticator() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'authenticator'");
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'security'");
    }

    @Override
    public IDomainBuilder autoDetectDtos(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'autoDetectDtos'");
    }

    @Override
    public IDtoBuilder dto(Class<?> class1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'dto'");
    }

    @Override
    public IUseCaseBuilder useCase(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'useCase'");
    }

    @Override
    public IContext build() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'build'");
    }

    @Override
    public IDomainBuilder autoDetectUseCases(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'autoDetectUseCases'");
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
