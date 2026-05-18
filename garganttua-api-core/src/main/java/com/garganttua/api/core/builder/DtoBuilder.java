package com.garganttua.api.core.builder;

import com.garganttua.core.reflection.IField;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.garganttua.api.core.context.DtoContext;
import com.garganttua.api.core.definition.DtoDefinition;
import com.garganttua.api.commons.context.IDtoContext;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.IDtoBuilder;
import com.garganttua.api.commons.dao.IDao;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.IReflectionProvider;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.fields.FieldResolver;
import com.garganttua.core.reflection.query.ObjectQueryFactory;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DtoBuilder<E, D> extends AbstractAutomaticLinkedBuilder<IDtoBuilder<E, D>, IDomainBuilder<E>, IDtoContext<D>>
        implements IDtoBuilder<E, D> {

    private static final IReflectionProvider PROVIDER = new RuntimeReflectionProvider();

    private IClass<?> dtoClass;
    private ObjectAddress id;
    private ObjectAddress uuid;
    private ObjectAddress tenantId;
    private List<ISupplierBuilder<?, ? extends ISupplier<?>>> daos = new ArrayList<>();
    private IObjectQuery objectQuery;

    public DtoBuilder(IClass<?> dtoClass, IDomainBuilder<E> domainBuilder) throws ApiException {
        super(domainBuilder);
        this.dtoClass = Objects.requireNonNull(dtoClass, "Dto class cannot be null");
        try {
            this.objectQuery = ObjectQueryFactory.objectQuery(this.dtoClass, PROVIDER);
        } catch (ReflectionException e) {
            throw new ApiException(e.getMessage(), e);
        }
    }

    @Override
    public IDtoBuilder<E, D> db(ISupplierBuilder<? extends IDao, ISupplier<? extends IDao>> daoSupplier) throws ApiException {
        if (!IClass.getClass(IDao.class).isAssignableFrom(daoSupplier.getSuppliedClass())) {
            throw new ApiException(
                    "Bean " + daoSupplier.getSuppliedClass().getName() + " does not implement IDao");
        }
        this.daos.add(daoSupplier);
        return this;
    }

    @Override
    public IDtoBuilder<E, D> db(IDao dao) {
        IDao validDao = Objects.requireNonNull(dao, "Dao cannot be null");
        @SuppressWarnings("unchecked")
        IClass<IDao> daoClass = (IClass<IDao>) IClass.getClass(validDao.getClass());
        this.daos.add(new FixedSupplierBuilder<>(validDao, daoClass));
        return this;
    }

    @Override
    public IDtoBuilder<E, D> id(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.id = FieldResolver.fieldByFieldName(this.dtoClass, PROVIDER, fieldName, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDtoBuilder<E, D> id(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.id = FieldResolver.fieldByFieldName(this.dtoClass, PROVIDER, field.getName(), IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDtoBuilder<E, D> id(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.id = FieldResolver.fieldByAddress(this.dtoClass, PROVIDER, fieldAddress, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDtoBuilder<E, D> uuid(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.uuid = FieldResolver.fieldByFieldName(this.dtoClass, PROVIDER, fieldName, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDtoBuilder<E, D> uuid(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.uuid = FieldResolver.fieldByFieldName(this.dtoClass, PROVIDER, field.getName(), IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDtoBuilder<E, D> uuid(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.uuid = FieldResolver.fieldByAddress(this.dtoClass, PROVIDER, fieldAddress, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDtoBuilder<E, D> tenantId(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.tenantId = FieldResolver.fieldByFieldName(this.dtoClass, PROVIDER, fieldName, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDtoBuilder<E, D> tenantId(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.tenantId = FieldResolver.fieldByFieldName(this.dtoClass, PROVIDER, field.getName(), IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDtoBuilder<E, D> tenantId(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.tenantId = FieldResolver.fieldByAddress(this.dtoClass, PROVIDER, fieldAddress, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    protected synchronized IDtoContext<D> doBuild() throws ApiException {
        this.throwExceptionIfNoUuid();
        this.throwExceptionIfNoTenantId();
        this.throwExceptionIfNoId();
        if (this.daos.isEmpty()) {
            throw new ApiException("No DAO configured for dto " + this.dtoClass.getSimpleName()
                    + ". Use .db(dao) to set a DAO.");
        }
        if (this.daos.size() > 1) {
            log.atWarn().log(
                    "Multiple Daos set for dto {}. This feature is not yet supported, the first Dao will be used",
                    this.dtoClass.getSimpleName());
        }

        return new DtoContext(new DtoDefinition<>(this.dtoClass, this.uuid, this.id, this.tenantId), this.daos.get(0));
    }

    private void throwExceptionIfNoUuid() throws ApiException {
        if( this.uuid == null )
            throw new ApiException("No uuid defined for dto "+this.dtoClass.getSimpleName());
    }

    private void throwExceptionIfNoTenantId() throws ApiException {
        if (this.tenantId == null && isMultiTenantEnabled() && !isTenantDomain()) {
            throw new ApiException("No tenant id defined for dto " + this.dtoClass.getSimpleName());
        }
    }

    private boolean isMultiTenantEnabled() {
        try {
            return up().up() instanceof ApiBuilder acb && acb.isMultiTenant();
        } catch (Exception e) {
            return true; // default to strict
        }
    }

    /**
     * True when the parent domain is marked {@code .tenant(true)} — see
     * {@link EntityBuilder} for the matching skip.
     */
    private boolean isTenantDomain() {
        try {
            return up() instanceof DomainBuilder<?> db && db.isTenantDomain();
        } catch (Exception e) {
            return false;
        }
    }

    private void throwExceptionIfNoId() throws ApiException {
        if( this.id == null )
            throw new ApiException("No id defined for dto "+this.dtoClass.getSimpleName());
    }

    @Override
    protected void doAutoDetection() {

    }
}
