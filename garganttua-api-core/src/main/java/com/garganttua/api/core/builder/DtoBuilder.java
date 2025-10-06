package com.garganttua.api.core.builder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.garganttua.api.core.builder.resolver.FieldResolver;
import com.garganttua.api.core.builder.supplier.FixedObjectSupplierBuilder;
import com.garganttua.api.core.context.application.DomainDtoContext;
import com.garganttua.api.core.definition.DomainDtoDefinition;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.dao.IDao;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IDomainDtoContext;
import com.garganttua.api.spec.engine.IDtoBuilder;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DtoBuilder extends AbstractAutomaticLinkedBuilder<IDomainDtoContext, IDtoBuilder, IDomainBuilder>
        implements IDtoBuilder {

    private Class<?> dtoClass;
    private GGObjectAddress id;
    private GGObjectAddress uuid;
    private GGObjectAddress tenantId;
    private List<IObjectSupplierBuilder<?>> daos = new ArrayList<>();
    private IGGObjectQuery objectQuery;

    public DtoBuilder(Class<?> dtoClass, IDomainBuilder domainBuilder) throws BuilderException {
        super(domainBuilder);
        this.dtoClass = Objects.requireNonNull(dtoClass, "Dto class cannot be null");
        try {
            this.objectQuery = GGObjectQueryFactory.objectQuery(this.dtoClass);
        } catch (GGReflectionException e) {
            throw new BuilderException(e.getMessage(), e);
        }
    }

    @Override
    public IDtoBuilder db(IObjectSupplierBuilder<?> daoSupplier) throws BuilderException {
        if (!IDao.class.isAssignableFrom(daoSupplier.getObjectClass())) {
            throw new BuilderException(
                    "Bean " + daoSupplier.getObjectClass().getName() + " does not implement IDao");
        }
        this.daos.add(daoSupplier);
        return this;
    }

    @Override
    public IDtoBuilder db(IDao dao) {
        this.daos
                .add(new FixedObjectSupplierBuilder<IDao>(
                        Objects.requireNonNull(dao, "Dao cannot be null")));
        return this;
    }

    @Override
    public IDtoBuilder id(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.id = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder id(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.id = FieldResolver.fieldByField(field, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder id(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.id = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder uuid(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.uuid = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder uuid(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.uuid = FieldResolver.fieldByField(field, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder uuid(GGObjectAddress fieldAddress) throws BuilderException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.uuid = FieldResolver.fieldByAddress(fieldAddress, this.objectQuery, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder tenantId(String fieldName) throws BuilderException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.tenantId = FieldResolver.fieldByFieldName(fieldName, this.objectQuery, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder tenantId(Field field) throws BuilderException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.tenantId = FieldResolver.fieldByField(field, this.dtoClass, String.class);

        return this;
    }

    @Override
    public IDtoBuilder tenantId(GGObjectAddress fieldAddress) throws BuilderException {
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

        return new DomainDtoContext(new DomainDtoDefinition(this.dtoClass, this.uuid, this.id, this.tenantId), this.daos.get(0).build());
    }

    private void throwExceptionIfNoUuid() throws BuilderException {
        if( this.uuid == null )
            throw new BuilderException("No uuid defined for dto "+this.dtoClass.getSimpleName());
    }

    private void throwExceptionIfNoTenantId() throws BuilderException {
        if( this.tenantId == null )
            throw new BuilderException("No tenant id defined for dto "+this.dtoClass.getSimpleName());
    }

    private void throwExceptionIfNoId() throws BuilderException {
        if( this.id == null )
            throw new BuilderException("No id defined for dto "+this.dtoClass.getSimpleName());
    }

    @Override
    protected void doAutoDetection() {

    }
}
