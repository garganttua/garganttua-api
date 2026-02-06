package com.garganttua.api.core.builder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.garganttua.api.core.context.application.DtoContext;
import com.garganttua.api.core.definition.DtoDefinition;
import com.garganttua.api.spec.context.IDtoContext;
import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.context.dsl.IDtoBuilder;
import com.garganttua.api.spec.dao.IDao;
import com.garganttua.core.CoreException;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.fields.FieldResolver;
import com.garganttua.core.reflection.query.ObjectQueryFactory;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DtoBuilder<E, D> extends AbstractAutomaticLinkedBuilder<IDtoBuilder<E, D>, IDomainBuilder<E>, IDtoContext<D>>
        implements IDtoBuilder<E, D> {

    private Class<?> dtoClass;
    private ObjectAddress id;
    private ObjectAddress uuid;
    private ObjectAddress tenantId;
    private List<ISupplierBuilder<?, ? extends ISupplier<?>>> daos = new ArrayList<>();
    private IObjectQuery objectQuery;

    public DtoBuilder(Class<?> dtoClass, IDomainBuilder<E> domainBuilder) throws DslException {
        super(domainBuilder);
        this.dtoClass = Objects.requireNonNull(dtoClass, "Dto class cannot be null");
        try {
            this.objectQuery = ObjectQueryFactory.objectQuery(this.dtoClass);
        } catch (ReflectionException e) {
            throw new DslException(e.getMessage(), e);
        }
    }

    @Override
    public IDtoBuilder<E, D> db(ISupplierBuilder<? extends IDao, ISupplier<? extends IDao>> daoSupplier) throws DslException {
        if (!IDao.class.isAssignableFrom(daoSupplier.getSuppliedClass())) {
            throw new DslException(
                    "Bean " + daoSupplier.getSuppliedClass().getName() + " does not implement IDao");
        }
        this.daos.add(daoSupplier);
        return this;
    }

    @Override
    public IDtoBuilder<E, D> db(IDao dao) {
        this.daos
                .add(new FixedSupplierBuilder<>(
                        Objects.requireNonNull(dao, "Dao cannot be null")));
        return this;
    }

    @Override
    public IDtoBuilder<E, D> id(String fieldName) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.id = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder<E, D> id(Field field) throws DslException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.id = FieldResolver.fieldByField(field, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder<E, D> id(ObjectAddress fieldAddress) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.id = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder<E, D> uuid(String fieldName) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.uuid = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder<E, D> uuid(Field field) throws DslException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.uuid = FieldResolver.fieldByField(field, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder<E, D> uuid(ObjectAddress fieldAddress) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.uuid = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder<E, D> tenantId(String fieldName) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.tenantId = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder<E, D> tenantId(Field field) throws DslException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.tenantId = FieldResolver.fieldByField(field, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder<E, D> tenantId(ObjectAddress fieldAddress) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.tenantId = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.dtoClass, String.class);

        return this;
    }

    @Override
    protected synchronized IDtoContext<D> doBuild() throws CoreException {
        this.throwExceptionIfNoUuid();
        this.throwExceptionIfNoTenantId();
        this.throwExceptionIfNoId();
        if (this.daos.size() > 1) {
            log.atWarn().log(
                    "Multiple Daos set for dto {}. This feature is not yet supported, the first Dao will be used",
                    this.dtoClass.getSimpleName());
        }

        return new DtoContext(new DtoDefinition<>(this.dtoClass, this.uuid, this.id, this.tenantId), this.daos.get(0));
    }

    private void throwExceptionIfNoUuid() throws DslException {
        if( this.uuid == null )
            throw new DslException("No uuid defined for dto "+this.dtoClass.getSimpleName());
    }

    private void throwExceptionIfNoTenantId() throws DslException {
        if( this.tenantId == null )
            throw new DslException("No tenant id defined for dto "+this.dtoClass.getSimpleName());
    }

    private void throwExceptionIfNoId() throws DslException {
        if( this.id == null )
            throw new DslException("No id defined for dto "+this.dtoClass.getSimpleName());
    }

    @Override
    protected void doAutoDetection() {

    }
}
