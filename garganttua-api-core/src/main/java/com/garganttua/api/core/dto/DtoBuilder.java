package com.garganttua.api.core.dto;
import com.garganttua.api.core.api.ApiBuilder;
import com.garganttua.api.core.domain.DomainBuilder;
import com.garganttua.api.core.entity.EntityBuilder;
import com.garganttua.core.reflection.annotations.Reflected;

import com.garganttua.core.reflection.IField;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.garganttua.api.core.dto.DtoContext;
import com.garganttua.api.core.dto.DtoDefinition;
import com.garganttua.api.commons.context.IDtoContext;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.IDtoBuilder;
import com.garganttua.api.commons.dao.IDao;
import com.garganttua.api.commons.dao.IDaoFactory;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.IReflectionProvider;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.fields.FieldResolver;
import com.garganttua.core.reflection.query.ObjectQueryFactory;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import com.garganttua.core.observability.Logger;

@Reflected
public class DtoBuilder<E, D> extends AbstractAutomaticLinkedBuilder<IDtoBuilder<E, D>, IDomainBuilder<E>, IDtoContext<D>>
        implements IDtoBuilder<E, D> {
	private static final Logger log = Logger.getLogger(DtoBuilder.class);


    // Reflection provider is whatever the user installed via IClass.setReflection().
    // Resolved lazily per call so the framework never picks an implementation.
    private static IReflectionProvider provider() {
        return IClass.getReflection();
    }

    private IClass<?> dtoClass;
    private ObjectAddress id;
    private ObjectAddress uuid;
    private ObjectAddress tenantId;
    private final List<com.garganttua.api.commons.definition.DtoComposition> compositions = new ArrayList<>();
    private List<ISupplierBuilder<?, ? extends ISupplier<?>>> daos = new ArrayList<>();
    private IObjectQuery objectQuery;

    public DtoBuilder(IClass<?> dtoClass, IDomainBuilder<E> domainBuilder) throws ApiException {
        super(domainBuilder);
        this.dtoClass = Objects.requireNonNull(dtoClass, "Dto class cannot be null");
        try {
            this.objectQuery = ObjectQueryFactory.objectQuery(this.dtoClass, provider());
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

        this.id = FieldResolver.fieldByFieldName(this.dtoClass, provider(), fieldName, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDtoBuilder<E, D> id(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.id = FieldResolver.fieldByFieldName(this.dtoClass, provider(), field.getName(), IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDtoBuilder<E, D> id(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.id = FieldResolver.fieldByAddress(this.dtoClass, provider(), fieldAddress, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDtoBuilder<E, D> uuid(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.uuid = FieldResolver.fieldByFieldName(this.dtoClass, provider(), fieldName, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDtoBuilder<E, D> uuid(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.uuid = FieldResolver.fieldByFieldName(this.dtoClass, provider(), field.getName(), IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDtoBuilder<E, D> uuid(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.uuid = FieldResolver.fieldByAddress(this.dtoClass, provider(), fieldAddress, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDtoBuilder<E, D> tenantId(String fieldName) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");

        this.tenantId = FieldResolver.fieldByFieldName(this.dtoClass, provider(), fieldName, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDtoBuilder<E, D> tenantId(IField field) throws ApiException {
        Objects.requireNonNull(field, "Field cannot be null");

        this.tenantId = FieldResolver.fieldByFieldName(this.dtoClass, provider(), field.getName(), IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDtoBuilder<E, D> tenantId(ObjectAddress fieldAddress) throws ApiException {
        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");

        this.tenantId = FieldResolver.fieldByAddress(this.dtoClass, provider(), fieldAddress, IClass.getClass(String.class)).address();

        return this;
    }

    @Override
    public IDtoBuilder<E, D> composed(String fieldName, String collection) throws ApiException {
        Objects.requireNonNull(fieldName, "Field name cannot be null");
        Objects.requireNonNull(collection, "Composition collection cannot be null");
        // Resolve to any type: the referenced type is the field's own (its element type for a List).
        ObjectAddress field = FieldResolver.fieldByFieldName(this.dtoClass, provider(), fieldName,
                IClass.getClass(Object.class)).address();
        this.compositions.add(new com.garganttua.api.commons.definition.DtoComposition(field, collection));
        return this;
    }

    @Override
    protected synchronized IDtoContext<D> doBuild() throws ApiException {
        this.throwExceptionIfNoUuid();
        this.throwExceptionIfNoTenantId();
        this.throwExceptionIfNoId();
        if (this.daos.isEmpty()) {
            this.applyDefaultDaoIfAny();
        }
        if (this.daos.isEmpty()) {
            throw new ApiException("No DAO configured for dto " + this.dtoClass.getSimpleName()
                    + ". Call .db(...) on the dto builder before .build(). Example:\n"
                    + "\n"
                    + "    .dto(" + this.dtoClass.getSimpleName() + ".class)\n"
                    + "        .id(\"id\").uuid(\"uuid\").tenantId(\"tenantId\")\n"
                    + "        .db(new MyDao())                          // <- missing\n"
                    + "    .up()");
        }
        if (this.daos.size() > 1) {
            log.warn(
                    "Multiple Daos set for dto {}. This feature is not yet supported, the first Dao will be used",
                    this.dtoClass.getSimpleName());
        }

        detectComposedAnnotations();

        return new DtoContext(new DtoDefinition<>(this.dtoClass, this.uuid, this.id, this.tenantId,
                List.copyOf(this.compositions)), this.daos.get(0));
    }

    /**
     * Adds compositions declared via the {@code @Composed} annotation on the DTO fields,
     * complementing the explicit {@code .composed(...)} DSL (a field declared by both is added once).
     */
    private void detectComposedAnnotations() throws ApiException {
        for (IField field : this.dtoClass.getDeclaredFields()) {
            com.garganttua.api.commons.dto.annotations.Composed anno =
                    field.getAnnotation(IClass.getClass(com.garganttua.api.commons.dto.annotations.Composed.class));
            if (anno == null) {
                continue;
            }
            ObjectAddress addr = FieldResolver.fieldByFieldName(this.dtoClass, provider(), field.getName(),
                    IClass.getClass(Object.class)).address();
            boolean already = this.compositions.stream().anyMatch(c -> c.field().equals(addr));
            if (!already) {
                this.compositions.add(new com.garganttua.api.commons.definition.DtoComposition(addr, anno.collection()));
            }
        }
    }

    private void throwExceptionIfNoUuid() throws ApiException {
        if( this.uuid == null )
            throw new ApiException("No uuid field declared on dto " + this.dtoClass.getSimpleName()
                    + ". Add .uuid(\"uuid\") (or the matching field name) on the dto builder:\n"
                    + "\n"
                    + "    .dto(" + this.dtoClass.getSimpleName() + ".class)\n"
                    + "        .id(\"id\")\n"
                    + "        .uuid(\"uuid\")                              // <- missing\n"
                    + "        .tenantId(\"tenantId\")\n"
                    + "        .db(...)\n"
                    + "    .up()");
    }

    private void throwExceptionIfNoTenantId() throws ApiException {
        if (this.tenantId == null && isMultiTenantEnabled() && !isTenantDomain()) {
            throw new ApiException("No tenantId field declared on dto " + this.dtoClass.getSimpleName()
                    + ". Multi-tenancy is enabled and this domain is not the tenant domain — every dto needs a tenantId. "
                    + "Either add .tenantId(\"tenantId\") on the dto builder, mark the parent domain with .tenant(true), "
                    + "or disable multi-tenancy globally via apiBuilder.multiTenant(false).");
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
     * Last resort before the "no DAO configured" error: if the api builder
     * carries a default DAO factory (registered by a persistence starter), ask
     * it for a DAO for this domain. Explicit {@code .db(...)} already populated
     * {@code daos} so we never reach here when one was set.
     */
    private void applyDefaultDaoIfAny() throws ApiException {
        IDaoFactory factory = defaultDaoFactory();
        if (factory == null) {
            return;
        }
        IDao dao = factory.create(parentDomainName(), this.dtoClass);
        if (dao != null) {
            this.db(dao);
        }
    }

    private IDaoFactory defaultDaoFactory() {
        try {
            return up().up() instanceof ApiBuilder acb ? acb.getDefaultDaoFactory() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String parentDomainName() {
        try {
            return up() instanceof DomainBuilder<?> db ? db.getDomainName() : null;
        } catch (Exception e) {
            return null;
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
            throw new ApiException("No id field declared on dto " + this.dtoClass.getSimpleName()
                    + ". Add .id(\"id\") (or the matching field name) on the dto builder:\n"
                    + "\n"
                    + "    .dto(" + this.dtoClass.getSimpleName() + ".class)\n"
                    + "        .id(\"id\")                                  // <- missing\n"
                    + "        .uuid(\"uuid\").tenantId(\"tenantId\").db(...)\n"
                    + "    .up()");
    }

    @Override
    protected void doAutoDetection() {

    }
}
