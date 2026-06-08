package com.garganttua.api.core.entity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.IDtoBuilder;
import com.garganttua.api.commons.context.dsl.IEntityBuilder;
import com.garganttua.api.commons.dto.annotations.Dto;
import com.garganttua.api.commons.dto.annotations.DtoId;
import com.garganttua.api.commons.dto.annotations.DtoTenantId;
import com.garganttua.api.commons.dto.annotations.DtoUuid;
import com.garganttua.api.commons.entity.annotations.Entity;
import com.garganttua.api.commons.entity.annotations.EntityAfterCreate;
import com.garganttua.api.commons.entity.annotations.EntityAfterDelete;
import com.garganttua.api.commons.entity.annotations.EntityAfterGet;
import com.garganttua.api.commons.entity.annotations.EntityAfterUpdate;
import com.garganttua.api.commons.entity.annotations.EntityBeforeCreate;
import com.garganttua.api.commons.entity.annotations.EntityBeforeDelete;
import com.garganttua.api.commons.entity.annotations.EntityBeforeUpdate;
import com.garganttua.api.commons.entity.annotations.EntityGeolocalized;
import com.garganttua.api.commons.entity.annotations.EntityHiddenable;
import com.garganttua.api.commons.entity.annotations.EntityId;
import com.garganttua.api.commons.entity.annotations.EntityMandatory;
import com.garganttua.api.commons.entity.annotations.EntityOwned;
import com.garganttua.api.commons.entity.annotations.EntityOwner;
import com.garganttua.api.commons.entity.annotations.EntityPublic;
import com.garganttua.api.commons.entity.annotations.EntityShared;
import com.garganttua.api.commons.entity.annotations.EntitySuperOwner;
import com.garganttua.api.commons.entity.annotations.EntitySuperTenant;
import com.garganttua.api.commons.entity.annotations.EntityTenant;
import com.garganttua.api.commons.entity.annotations.EntityTenantId;
import com.garganttua.api.commons.entity.annotations.EntityUnicity;
import com.garganttua.api.commons.entity.annotations.EntityUuid;
import com.garganttua.api.commons.security.annotations.Key;
import com.garganttua.api.commons.security.annotations.KeyAlgorithm;
import com.garganttua.api.commons.security.annotations.KeyExpiration;
import com.garganttua.api.commons.security.annotations.KeyForDecryption;
import com.garganttua.api.commons.security.annotations.KeyForEncryption;
import com.garganttua.api.commons.security.annotations.KeyForSignatureVerification;
import com.garganttua.api.commons.security.annotations.KeyForSigning;
import com.garganttua.api.commons.security.annotations.KeyName;
import com.garganttua.api.commons.security.annotations.KeyRotate;
import com.garganttua.api.commons.security.annotations.KeyVersion;
import com.garganttua.api.commons.security.annotations.KeyRevoked;
import com.garganttua.api.commons.security.annotations.KeySignatureAlgorithm;
import com.garganttua.api.commons.context.dsl.IDomainKeyBuilder;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IField;
import com.garganttua.core.reflection.IMethod;
import com.garganttua.core.reflection.IReflection;

import com.garganttua.core.observability.Logger;

/**
 * Scans configured packages for {@link Entity}-annotated classes and wires them
 * into an {@link IApiBuilder} via the standard DSL. Designed to be invoked from
 * {@code ApiBuilder.doAutoDetection()}.
 *
 * <p>For every detected entity:
 * <ol>
 *   <li>Pairs it with a matching {@link Dto} (via {@code @Dto.entityClass()}).</li>
 *   <li>Registers the domain with CRUD flags from {@code @Entity}.</li>
 *   <li>Applies type-level markers ({@link EntityPublic}, {@link EntityTenant},
 *       {@link EntityOwner}, {@link EntityOwned}, {@link EntityShared},
 *       {@link EntityHiddenable}, {@link EntityGeolocalized}).</li>
 *   <li>Applies field-level markers ({@link EntityId}, {@link EntityUuid},
 *       {@link EntityTenantId}, {@link EntitySuperOwner}, {@link EntitySuperTenant},
 *       {@link EntityMandatory}, {@link EntityUnicity}).</li>
 *   <li>Applies method-level lifecycle hooks
 *       ({@link EntityBeforeCreate}, {@link EntityAfterCreate}, ...).</li>
 *   <li>Wires the paired DTO with its {@link DtoId}, {@link DtoUuid},
 *       {@link DtoTenantId} fields and {@code db} supplier name.</li>
 * </ol>
 *
 * <p>Silently no-ops when no packages are configured or when no
 * {@link IReflection} is available (e.g. native image without metadata).
 */
public final class EntityAnnotationScanner {
	private static final Logger log = Logger.getLogger(EntityAnnotationScanner.class);


    private final IApiBuilder apiBuilder;
    private final Set<String> packages;

    public EntityAnnotationScanner(IApiBuilder apiBuilder, Set<String> packages) {
        this.apiBuilder = apiBuilder;
        this.packages = packages;
    }

    /**
     * Performs the scan. Returns silently when nothing is to do; throws
     * {@link ApiException} on misconfigured annotations (e.g. an entity with no
     * matching DTO).
     */
    public void scan() throws ApiException {
        if (this.packages.isEmpty()) {
            return;
        }
        IReflection reflection;
        try {
            reflection = IClass.getReflection();
        } catch (Exception e) {
            log.warn("No IReflection available for @Entity auto-detection: {}", e.getMessage());
            return;
        }

        Map<IClass<?>, IClass<?>> entityToDto = pairEntitiesWithDtos(reflection);
        if (entityToDto.isEmpty()) {
            return;
        }

        for (Map.Entry<IClass<?>, IClass<?>> entry : entityToDto.entrySet()) {
            wireDomain(reflection, entry.getKey(), entry.getValue());
        }
        log.debug("Auto-detected {} @Entity class(es)", entityToDto.size());
    }

    private Map<IClass<?>, IClass<?>> pairEntitiesWithDtos(IReflection reflection) {
        IClass<Entity> entityAnno = IClass.getClass(Entity.class);
        IClass<Dto> dtoAnno = IClass.getClass(Dto.class);

        List<IClass<?>> entities = new ArrayList<>();
        List<IClass<?>> dtos = new ArrayList<>();
        for (String pkg : this.packages) {
            entities.addAll(reflection.getClassesWithAnnotation(pkg, entityAnno));
            dtos.addAll(reflection.getClassesWithAnnotation(pkg, dtoAnno));
        }

        Map<IClass<?>, IClass<?>> result = new LinkedHashMap<>();
        for (IClass<?> entity : entities) {
            IClass<?> matchingDto = null;
            for (IClass<?> dto : dtos) {
                Dto a = dto.getAnnotation(dtoAnno);
                if (a == null || a.entityClass() == void.class) continue;
                if (entity.represents(a.entityClass())) {
                    matchingDto = dto;
                    break;
                }
            }
            if (matchingDto == null) {
                log.warn("No @Dto found for @Entity {}; skipping", entity.getSimpleName());
                continue;
            }
            result.put(entity, matchingDto);
        }
        return result;
    }

    private void wireDomain(IReflection reflection, IClass<?> entityClass, IClass<?> dtoClass) throws ApiException {
        IDomainBuilder<Object> domain = (IDomainBuilder<Object>) this.apiBuilder.domain((IClass) entityClass);

        applyEntityAnnotation(domain, entityClass);
        applyTypeLevelMarkers(domain, entityClass);
        applyEntityFields(reflection, domain, entityClass);
        applyLifecycleHooks(reflection, domain, entityClass);
        applyKeyRole(reflection, domain, entityClass);
        applyDto(reflection, domain, entityClass, dtoClass);
    }

    /**
     * If the entity carries {@link Key}, materialize the key sub-builder
     * via {@code domain.security().key()} and wire each field-level marker to the
     * matching DSL setter. The marker set mirrors {@code IKeyRealm}'s
     * public surface — {@link KeyName}/{@link KeyAlgorithm}/{@link KeySignatureAlgorithm}/
     * {@link KeyForSigning}/{@link KeyForSignatureVerification}/{@link KeyForEncryption}/
     * {@link KeyForDecryption}/{@link KeyExpiration}/{@link KeyRevoked}/
     * {@link KeyVersion}/{@link KeyRotate}.
     */
    private void applyKeyRole(IReflection reflection, IDomainBuilder<Object> domain, IClass<?> entityClass)
            throws ApiException {
        if (entityClass.getAnnotation(IClass.getClass(Key.class)) == null) {
            return;
        }
        IDomainKeyBuilder<Object> keyBuilder = domain.security().key();

        reflection.findFieldAnnotatedWith(entityClass, IClass.getClass(KeyName.class))
                .ifPresent(f -> apply(() -> keyBuilder.name(f.getName())));
        reflection.findFieldAnnotatedWith(entityClass, IClass.getClass(KeyAlgorithm.class))
                .ifPresent(f -> apply(() -> keyBuilder.keyAlgorithm(f.getName())));
        reflection.findFieldAnnotatedWith(entityClass, IClass.getClass(KeySignatureAlgorithm.class))
                .ifPresent(f -> apply(() -> keyBuilder.signatureAlgorithm(f.getName())));
        reflection.findFieldAnnotatedWith(entityClass, IClass.getClass(KeyForSigning.class))
                .ifPresent(f -> apply(() -> keyBuilder.keyForSigning(f.getName())));
        reflection.findFieldAnnotatedWith(entityClass, IClass.getClass(KeyForSignatureVerification.class))
                .ifPresent(f -> apply(() -> keyBuilder.keyForSignatureVerification(f.getName())));
        reflection.findFieldAnnotatedWith(entityClass, IClass.getClass(KeyForEncryption.class))
                .ifPresent(f -> apply(() -> keyBuilder.keyForEncryption(f.getName())));
        reflection.findFieldAnnotatedWith(entityClass, IClass.getClass(KeyForDecryption.class))
                .ifPresent(f -> apply(() -> keyBuilder.keyForDecryption(f.getName())));
        reflection.findFieldAnnotatedWith(entityClass, IClass.getClass(KeyExpiration.class))
                .ifPresent(f -> apply(() -> keyBuilder.expiration(f.getName())));
        reflection.findFieldAnnotatedWith(entityClass, IClass.getClass(KeyRevoked.class))
                .ifPresent(f -> apply(() -> keyBuilder.revoked(f.getName())));
        reflection.findFieldAnnotatedWith(entityClass, IClass.getClass(KeyVersion.class))
                .ifPresent(f -> apply(() -> keyBuilder.version(f.getName())));
        reflection.findFieldAnnotatedWith(entityClass, IClass.getClass(KeyRotate.class))
                .ifPresent(f -> apply(() -> keyBuilder.rotate(f.getName())));
    }

    @FunctionalInterface
    private interface ThrowingApiCall {
        void run() throws ApiException;
    }

    private static void apply(ThrowingApiCall call) {
        try {
            call.run();
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void applyEntityAnnotation(IDomainBuilder<Object> domain, IClass<?> entityClass) {
        Entity a = entityClass.getAnnotation(IClass.getClass(Entity.class));
        if (a == null) return;
        domain.creation(a.creation());
        domain.readAll(a.readAll());
        domain.readOne(a.readOne());
        domain.update(a.update());
        domain.deleteOne(a.deleteOne());
        domain.deleteAll(a.deleteAll());
    }

    private void applyTypeLevelMarkers(IDomainBuilder<Object> domain, IClass<?> entityClass) throws ApiException {
        if (entityClass.getAnnotation(IClass.getClass(EntityPublic.class)) != null) {
            domain.publik();
        }
        if (entityClass.getAnnotation(IClass.getClass(EntityTenant.class)) != null) {
            domain.tenant(true);
        }
        EntityOwner ownerAnno = entityClass.getAnnotation(IClass.getClass(EntityOwner.class));
        if (ownerAnno != null) {
            if (!ownerAnno.ownerId().isEmpty()) domain.owner(ownerAnno.ownerId());
            if (!ownerAnno.superOwner().isEmpty()) domain.superOwner(ownerAnno.superOwner());
        }
        EntityOwned ownedAnno = entityClass.getAnnotation(IClass.getClass(EntityOwned.class));
        if (ownedAnno != null && !ownedAnno.ownerId().isEmpty()) {
            domain.owned(ownedAnno.ownerId());
        }
        EntityShared sharedAnno = entityClass.getAnnotation(IClass.getClass(EntityShared.class));
        if (sharedAnno != null && !sharedAnno.share().isEmpty()) {
            domain.shared(sharedAnno.share());
        }
        EntityHiddenable hiddenableAnno = entityClass.getAnnotation(IClass.getClass(EntityHiddenable.class));
        if (hiddenableAnno != null && !hiddenableAnno.hidden().isEmpty()) {
            domain.hiddenable(hiddenableAnno.hidden());
        }
        EntityGeolocalized geoAnno = entityClass.getAnnotation(IClass.getClass(EntityGeolocalized.class));
        if (geoAnno != null && !geoAnno.location().isEmpty()) {
            domain.geolocalized(geoAnno.location());
        }
    }

    private void applyEntityFields(IReflection reflection, IDomainBuilder<Object> domain, IClass<?> entityClass)
            throws ApiException {
        IEntityBuilder<Object> entity = domain.entity();
        Optional<IField> idField = reflection.findFieldAnnotatedWith(entityClass, IClass.getClass(EntityId.class));
        if (idField.isPresent()) entity.id(idField.get().getName());

        Optional<IField> uuidField = reflection.findFieldAnnotatedWith(entityClass, IClass.getClass(EntityUuid.class));
        if (uuidField.isPresent()) entity.uuid(uuidField.get().getName());

        Optional<IField> tenantIdField = reflection.findFieldAnnotatedWith(entityClass, IClass.getClass(EntityTenantId.class));
        if (tenantIdField.isPresent()) entity.tenantId(tenantIdField.get().getName());

        Optional<IField> superOwnerField = reflection.findFieldAnnotatedWith(entityClass, IClass.getClass(EntitySuperOwner.class));
        if (superOwnerField.isPresent()) domain.superOwner(superOwnerField.get().getName());

        Optional<IField> superTenantField = reflection.findFieldAnnotatedWith(entityClass, IClass.getClass(EntitySuperTenant.class));
        if (superTenantField.isPresent()) domain.superTenant(superTenantField.get().getName());

        for (String addr : reflection.findFieldAddressesWithAnnotation(entityClass, IClass.getClass(EntityMandatory.class), true)) {
            entity.mandatory(addr);
        }
        for (String addr : reflection.findFieldAddressesWithAnnotation(entityClass, IClass.getClass(EntityUnicity.class), true)) {
            entity.unicity(addr);
        }
    }

    private void applyLifecycleHooks(IReflection reflection, IDomainBuilder<Object> domain, IClass<?> entityClass)
            throws ApiException {
        IEntityBuilder<Object> entity = domain.entity();
        bindHook(reflection, entity, entityClass, EntityBeforeCreate.class, "beforeCreate");
        bindHook(reflection, entity, entityClass, EntityAfterCreate.class, "afterCreate");
        bindHook(reflection, entity, entityClass, EntityBeforeUpdate.class, "beforeUpdate");
        bindHook(reflection, entity, entityClass, EntityAfterUpdate.class, "afterUpdate");
        bindHook(reflection, entity, entityClass, EntityBeforeDelete.class, "beforeDelete");
        bindHook(reflection, entity, entityClass, EntityAfterDelete.class, "afterDelete");
        bindHook(reflection, entity, entityClass, EntityAfterGet.class, "afterGet");
    }

    private <A extends java.lang.annotation.Annotation> void bindHook(
            IReflection reflection, IEntityBuilder<Object> entity, IClass<?> entityClass,
            Class<A> annotation, String hook) throws ApiException {
        Optional<IMethod> m = reflection.findMethodAnnotatedWith(entityClass, IClass.getClass(annotation));
        if (m.isEmpty()) return;
        switch (hook) {
            case "beforeCreate" -> entity.beforeCreate(m.get().getName());
            case "afterCreate" -> entity.afterCreate(m.get().getName());
            case "beforeUpdate" -> entity.beforeUpdate(m.get().getName());
            case "afterUpdate" -> entity.afterUpdate(m.get().getName());
            case "beforeDelete" -> entity.beforeDelete(m.get().getName());
            case "afterDelete" -> entity.afterDelete(m.get().getName());
            case "afterGet" -> entity.afterGet(m.get().getName());
            default -> throw new IllegalStateException("Unknown hook: " + hook);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void applyDto(IReflection reflection, IDomainBuilder<Object> domain, IClass<?> entityClass, IClass<?> dtoClass)
            throws ApiException {
        IDtoBuilder dtoBuilder = domain.dto((IClass) dtoClass);
        Optional<IField> dtoId = reflection.findFieldAnnotatedWith(dtoClass, IClass.getClass(DtoId.class));
        if (dtoId.isPresent()) dtoBuilder.id(dtoId.get().getName());

        Optional<IField> dtoUuid = reflection.findFieldAnnotatedWith(dtoClass, IClass.getClass(DtoUuid.class));
        if (dtoUuid.isPresent()) dtoBuilder.uuid(dtoUuid.get().getName());

        Optional<IField> dtoTenantId = reflection.findFieldAnnotatedWith(dtoClass, IClass.getClass(DtoTenantId.class));
        if (dtoTenantId.isPresent()) dtoBuilder.tenantId(dtoTenantId.get().getName());
    }
}
