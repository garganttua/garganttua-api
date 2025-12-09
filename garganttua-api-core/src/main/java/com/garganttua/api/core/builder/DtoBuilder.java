package com.garganttua.api.core.builder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.garganttua.api.core.context.application.DomainDtoContext;
import com.garganttua.api.core.definition.DtoDefinition;
import com.garganttua.api.spec.context.IDomainDtoContext;
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
import com.garganttua.core.supply.IObjectSupplier;
import com.garganttua.core.supply.dsl.FixedObjectSupplier;
import com.garganttua.core.supply.dsl.IObjectSupplierBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DtoBuilder extends AbstractAutomaticLinkedBuilder<IDomainDtoContext, IDomainBuilder, IDtoBuilder>
        implements IDtoBuilder {

    private Class<?> dtoClass;
    private ObjectAddress id;
    private ObjectAddress uuid;
    private ObjectAddress tenantId;
    private List<IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>>> daos = new ArrayList<>();
    private IObjectQuery objectQuery;

    public DtoBuilder(Class<?> dtoClass, IDomainBuilder domainBuilder) throws DslException {
        super(domainBuilder);
        this.dtoClass = Objects.requireNonNull(dtoClass, "Dto class cannot be null");
        try {
            this.objectQuery = ObjectQueryFactory.objectQuery(this.dtoClass);
        } catch (ReflectionException e) {
            throw new DslException(e.getMessage(), e);
        }
    }

    @Override
    public IDtoBuilder db(IObjectSupplierBuilder<? extends IDao, IObjectSupplier<? extends IDao>> daoSupplier) throws DslException {
        if (!IDao.class.isAssignableFrom(daoSupplier.getSuppliedType())) {
            throw new DslException(
                    "Bean " + daoSupplier.getSuppliedType().getName() + " does not implement IDao");
        }
        this.daos.add(daoSupplier);
        return this;
    }

    @Override
    public IDtoBuilder db(IDao dao) {
        this.daos
                .add(new FixedObjectSupplier<IDao>(
                        Objects.requireNonNull(dao, "Dao cannot be null")));
        return this;
    }

    @Override
    public IDtoBuilder id(String fieldName) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.id = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder id(Field field) throws DslException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.id = FieldResolver.fieldByField(field, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder id(ObjectAddress fieldAddress) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.id = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder uuid(String fieldName) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.uuid = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder uuid(Field field) throws DslException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.uuid = FieldResolver.fieldByField(field, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder uuid(ObjectAddress fieldAddress) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.uuid = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder tenantId(String fieldName) throws DslException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.tenantId = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder tenantId(Field field) throws DslException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.tenantId = FieldResolver.fieldByField(field, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder tenantId(ObjectAddress fieldAddress) throws DslException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.tenantId = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.dtoClass, String.class);

        return this;
    }

    @Override
    protected IDomainDtoContext doBuild() throws CoreException {
        this.throwExceptionIfNoUuid();
        this.throwExceptionIfNoTenantId();
        this.throwExceptionIfNoId();
        if (this.daos.size() > 1) {
            log.atWarn().log(
                    "Multiple Daos set for dto {}. This feature is not yet supported, the first Dao will be used",
                    this.dtoClass.getSimpleName());
        }

        return new DomainDtoContext(new DtoDefinition<>(this.dtoClass, this.uuid, this.id, this.tenantId), this.daos.get(0).build());
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
