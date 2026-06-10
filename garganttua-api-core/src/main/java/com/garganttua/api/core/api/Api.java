package com.garganttua.api.core.api;
import com.garganttua.api.core.domain.Domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.protocol.IProtocol;
import com.garganttua.api.commons.repository.IRepository;
import com.garganttua.api.commons.security.authorization.IAuthorizationProtocol;
import com.garganttua.api.commons.serialization.ISerializer;
import com.garganttua.api.commons.service.IRequestBuilder;
import com.garganttua.core.injection.BeanReference;
import com.garganttua.core.injection.DiException;
import com.garganttua.core.injection.IInjectionContext;
import com.garganttua.core.injection.Predefined;
import com.garganttua.core.lifecycle.AbstractLifecycle;
import com.garganttua.core.lifecycle.ILifecycle;
import com.garganttua.core.lifecycle.LifecycleException;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;

import com.garganttua.core.observability.Logger;

public class Api extends AbstractLifecycle implements IApi, com.garganttua.core.bootstrap.banner.IBootstrapSummaryContributor {
	private static final Logger log = Logger.getLogger(Api.class);


    private final IInjectionContext injectionContext;
    private final Map<String, IDomain<?>> domainContexts;
    private final String superTenantId;
    private final boolean superTenantAutoCreate;
    private final boolean multiTenant;
    private final boolean lockSuperTenantCreation;
    private final boolean lockSuperOwnerCreation;
    // Server-side registries of super-tenant / super-owner ids. Populated at
    // onStart (scan), seeded by the auto-created master tenant, and maintained
    // on create/update of the tenant/owner domains. Concurrent: the scan runs
    // on the start thread; maintenance runs on request threads.
    private final java.util.Set<String> superTenantIds = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final java.util.Set<String> superOwnerIds = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final List<IMethodBinder<Void>> startupBinders;
    private final List<ISerializer> serializers;
    private final List<IProtocol<?, ?>> protocols;
    private final List<IAuthorizationProtocol> authorizationProtocols;
    private final com.garganttua.api.commons.context.IAuthoritiesEndpoint authoritiesEndpoint;

    public Api(IInjectionContext injectionContext, Map<String, IDomain<?>> domainContexts,
            String superTenantId, boolean superTenantAutoCreate, boolean multiTenant,
            boolean lockSuperTenantCreation, boolean lockSuperOwnerCreation,
            List<IMethodBinder<Void>> startupBinders, List<ISerializer> serializers,
            List<IProtocol<?, ?>> protocols,
            List<IAuthorizationProtocol> authorizationProtocols,
            com.garganttua.api.commons.context.IAuthoritiesEndpoint authoritiesEndpoint) {
        this.injectionContext = Objects.requireNonNull(injectionContext, "Injection context cannot be null");
        this.domainContexts = Collections.unmodifiableMap(new HashMap<>(
                Objects.requireNonNull(domainContexts, "Domain contexts cannot be null")));
        this.superTenantId = superTenantId;
        this.superTenantAutoCreate = superTenantAutoCreate;
        this.multiTenant = multiTenant;
        this.lockSuperTenantCreation = lockSuperTenantCreation;
        this.lockSuperOwnerCreation = lockSuperOwnerCreation;
        this.startupBinders = Collections.unmodifiableList(new ArrayList<>(
                Objects.requireNonNull(startupBinders, "Startup binders cannot be null")));
        this.serializers = Collections.unmodifiableList(new ArrayList<>(
                Objects.requireNonNull(serializers, "Serializers cannot be null")));
        this.protocols = Collections.unmodifiableList(new ArrayList<>(
                Objects.requireNonNull(protocols, "Protocols cannot be null")));
        this.authorizationProtocols = Collections.unmodifiableList(new ArrayList<>(
                Objects.requireNonNull(authorizationProtocols, "Authorization protocols cannot be null")));
        // null is a legitimate value: it signals that .exposeAuthorities() was
        // never called, and getAuthoritiesEndpoint() must propagate that null
        // to transport modules so they skip the route.
        this.authoritiesEndpoint = authoritiesEndpoint;
    }

    @Override
    public List<ISerializer> getSerializers() {
        return this.serializers;
    }

    @Override
    public List<IProtocol<?, ?>> getProtocols() {
        return this.protocols;
    }

    @Override
    public List<IAuthorizationProtocol> getAuthorizationProtocols() {
        return this.authorizationProtocols;
    }

    @Override
    public com.garganttua.api.commons.context.IAuthoritiesEndpoint getAuthoritiesEndpoint() {
        return this.authoritiesEndpoint;
    }

    @Override
    public List<String> getAuthorities() {
        java.util.TreeSet<String> sorted = new java.util.TreeSet<>();
        for (IDomain<?> domain : this.domainContexts.values()) {
            if (domain.getDomainDefinition() == null) continue;

            // Source 1: operation-level authorities — every domain operation
            // contributes its effectiveAuthorityName() (null when authority is
            // not enforced).
            List<com.garganttua.api.commons.operation.OperationDefinition> ops =
                    domain.getDomainDefinition().operations();
            if (ops != null) {
                for (com.garganttua.api.commons.operation.OperationDefinition op : ops) {
                    String name = op.effectiveAuthorityName();
                    if (name != null && !name.isBlank()) {
                        sorted.add(name);
                    }
                }
            }

            // Source 2: field-level update authorities — declared via
            // entity().update(field, "auth-name") on the DSL. These guard a
            // specific field of an update operation; they're independent of
            // the operation-level authority and live on the EntityDefinition.
            java.util.Map<com.garganttua.core.reflection.ObjectAddress, String> fieldAuths =
                    domain.getAuthorizedUpdateFieldsAndAuthorizations();
            if (fieldAuths != null) {
                for (String name : fieldAuths.values()) {
                    if (name != null && !name.isBlank()) {
                        sorted.add(name);
                    }
                }
            }
        }
        return new ArrayList<>(sorted);
    }

    @Override
    public List<String> getAuthoritiesForCaller(com.garganttua.api.commons.caller.ICaller caller) {
        if (this.authoritiesEndpoint == null) {
            throw new ApiException("Authorities endpoint is not exposed — call "
                    + ".exposeAuthorities() on ApiBuilder to enable it.");
        }
        com.garganttua.api.commons.operation.Access access = this.authoritiesEndpoint.access();
        // Anonymous bypass — no caller checks needed.
        if (access == com.garganttua.api.commons.operation.Access.anonymous) {
            return getAuthorities();
        }
        // Below this line the caller must be non-null and authenticated.
        if (caller == null) {
            throw new ApiException("Authorities endpoint requires access=" + access
                    + " but no caller was provided.");
        }
        boolean superCaller = caller.superTenant() || caller.superOwner();
        switch (access) {
            case authenticated:
                if (!superCaller && caller.tenantId() == null) {
                    throw new ApiException("Authorities endpoint requires an authenticated caller "
                            + "(no tenantId on the caller).");
                }
                break;
            default:
                break;
        }
        // Authority gate — super-tenant / super-owner bypass it.
        String requiredAuthority = this.authoritiesEndpoint.authority();
        if (requiredAuthority != null && !superCaller) {
            List<String> authorities = caller.authorities();
            if (authorities == null || !authorities.contains(requiredAuthority)) {
                throw new ApiException("Authorities endpoint requires authority '"
                        + requiredAuthority + "', which the caller does not carry.");
            }
        }
        return getAuthorities();
    }

    @Override
    public Optional<IDomain<?>> getDomain(String domainName) {
        return Optional.ofNullable(this.domainContexts.get(domainName));
    }

    @Override
    public IRequestBuilder request(String domainName) {
        IDomain<?> domain = this.domainContexts.get(domainName);
        if (domain == null) {
            throw new ApiException("Domain not found: " + domainName);
        }
        return domain.request();
    }

    public IInjectionContext getInjectionContext() {
        return this.injectionContext;
    }

    public String getSuperTenantId() {
        return this.superTenantId;
    }

    public boolean isSuperTenantAutoCreate() {
        return this.superTenantAutoCreate;
    }

    @Override
    public boolean isMultiTenant() {
        return this.multiTenant;
    }

    // --- Super-tenant / super-owner registries ---

    @Override
    public boolean isSuperTenant(String tenantId) {
        return tenantId != null && this.superTenantIds.contains(tenantId);
    }

    @Override
    public boolean isSuperOwner(String ownerId) {
        return ownerId != null && this.superOwnerIds.contains(ownerId);
    }

    @Override
    public java.util.Set<String> getSuperTenantIds() {
        return Collections.unmodifiableSet(this.superTenantIds);
    }

    @Override
    public java.util.Set<String> getSuperOwnerIds() {
        return Collections.unmodifiableSet(this.superOwnerIds);
    }

    @Override
    public void registerSuperTenant(String id) {
        if (id != null && !id.isBlank()) {
            this.superTenantIds.add(id);
        }
    }

    @Override
    public void unregisterSuperTenant(String id) {
        if (id != null) {
            this.superTenantIds.remove(id);
        }
    }

    @Override
    public void registerSuperOwner(String id) {
        if (id != null && !id.isBlank()) {
            this.superOwnerIds.add(id);
        }
    }

    @Override
    public void unregisterSuperOwner(String id) {
        if (id != null) {
            this.superOwnerIds.remove(id);
        }
    }

    @Override
    public boolean isSuperTenantCreationLocked() {
        return this.lockSuperTenantCreation;
    }

    @Override
    public boolean isSuperOwnerCreationLocked() {
        return this.lockSuperOwnerCreation;
    }

    public Map<String, IDomain<?>> getDomains() {
        return this.domainContexts;
    }

    @Override
    public IReflection reflection() {
        return DefaultMapper.reflection();
    }

    @Override
    protected ILifecycle doInit() {
        // Initialize the injection context first
        this.injectionContext.onInit();

        // Create and register repositories for each domain
        for (Map.Entry<String, IDomain<?>> entry : this.domainContexts.entrySet()) {
            String domainName = entry.getKey();
            IDomain<?> domainContext = entry.getValue();
            // Set parent API context reference
            if (domainContext instanceof Domain<?> dc) {
                dc.setApi(this);
            }
            // Initialize the domain context
            try {
                domainContext.onInit();
                log.info("Initialized domain '{}'", domainName);
            } catch (LifecycleException e) {
                log.error("Failed to initialize domain '{}': {}", domainName, e.getMessage());
            }

            // Get the repository from the domain context
            IRepository repository = domainContext.getRepository();

            if (repository != null) {
                // Register the repository in the injection context
                String repositoryName = domainName + "-repository";
                try {
                    BeanReference<IRepository> beanRef = new BeanReference<>(
                            IClass.getClass(IRepository.class),
                            Optional.empty(),
                            Optional.of(repositoryName),
                            new HashSet<>());
                    this.injectionContext.addBean(Predefined.BeanProviders.garganttua.toString(), beanRef, repository);
                    log.info("Registered repository '{}' for domain '{}'", repositoryName, domainName);
                } catch (DiException e) {
                    log.error("Failed to register repository '{}' for domain '{}': {}", repositoryName, domainName, e.getMessage());
                }
            }

        }
        return this;
    }

    @Override
    protected ILifecycle doStart() {
        // 1. Start the injection context
        this.injectionContext.onStart();

        // 2. Execute API-level startup binders
        executeStartupBinders();

        // 3. Start tenant domain first (other domains may depend on it) — only in multi-tenant mode
        IDomain<?> tenantDomain = this.multiTenant ? startTenantDomainFirst() : null;

        // 4. Auto-create master tenant if configured — only in multi-tenant mode
        if (tenantDomain != null && this.superTenantAutoCreate) {
            autoCreateMasterTenant(tenantDomain);
        }

        // 5. Start all remaining (non-tenant) domain contexts
        for (Map.Entry<String, IDomain<?>> entry : this.domainContexts.entrySet()) {
            String domainName = entry.getKey();
            IDomain<?> domainContext = entry.getValue();
            if (domainContext == tenantDomain) {
                continue; // Already started
            }
            domainContext.onStart();
            log.info("Started domain '{}'", domainName);
        }

        // 6. Populate the super-tenant / super-owner registries by scanning the
        //    tenant/owner domains for entities whose superTenant/superOwner flag
        //    is set. Runs after every domain (and the master auto-create) is
        //    started, so already-persisted supers (incl. the stamped master) are
        //    discovered. Exempt from the creation lock — this is the trusted
        //    bootstrap snapshot of what is super.
        scanSuperRegistries();

        return this;
    }

    /**
     * Scans the tenant and owner domains and registers every entity whose
     * {@code superTenant} / {@code superOwner} field is {@code true}. The id
     * registered is the entity's own uuid (a tenant member carries that uuid as
     * its {@code tenantId}; an owned entity carries its owner's uuid as its
     * {@code ownerId} — so registry membership is checked against the caller's
     * tenantId / ownerId at request time, see {@code SecurityExpressions}).
     */
    private void scanSuperRegistries() {
        IReflection reflection = reflection();
        for (IDomain<?> domain : this.domainContexts.values()) {
            var def = domain.getDomainDefinition();
            if (def == null) {
                continue;
            }
            boolean isTenant = domain.isTenantEntity() && def.superTenant() != null;
            boolean isOwner = def.owner() != null && def.superOwner() != null;
            if (!isTenant && !isOwner) {
                continue;
            }
            ObjectAddress uuidAddress = domain.getEntityDefinition().uuid();
            List<Object> entities;
            try {
                entities = domain.getRepository().getEntities(Optional.empty(), Optional.empty(), Optional.empty());
            } catch (Exception e) {
                log.warn("Could not scan domain '{}' for super-status: {}", domain.getDomainName(), e.getMessage());
                continue;
            }
            for (Object entity : entities) {
                String uuid = readStringField(reflection, entity, uuidAddress);
                if (uuid == null) {
                    continue;
                }
                if (isTenant && readBooleanField(reflection, entity, def.superTenant())) {
                    registerSuperTenant(uuid);
                    log.info("Registered super-tenant '{}' (scanned from domain '{}')", uuid, domain.getDomainName());
                }
                if (isOwner && readBooleanField(reflection, entity, def.superOwner())) {
                    registerSuperOwner(uuid);
                    log.info("Registered super-owner '{}' (scanned from domain '{}')", uuid, domain.getDomainName());
                }
            }
        }
    }

    private static String readStringField(IReflection reflection, Object entity, ObjectAddress addr) {
        if (addr == null) {
            return null;
        }
        Object v = reflection.getFieldValue(entity, addr.toString());
        return v != null ? v.toString() : null;
    }

    private static boolean readBooleanField(IReflection reflection, Object entity, ObjectAddress addr) {
        if (addr == null) {
            return false;
        }
        Object v = reflection.getFieldValue(entity, addr.toString());
        return Boolean.TRUE.equals(v);
    }

    private void executeStartupBinders() {
        if (this.startupBinders.isEmpty()) {
            return;
        }
        log.debug("Executing {} API-level startup binders", this.startupBinders.size());
        for (IMethodBinder<Void> binder : this.startupBinders) {
            try {
                log.trace("Executing startup binder: {}", binder.getExecutableReference());
                binder.execute();
            } catch (Exception e) {
                throw new ApiException("Failed to execute API startup binder '"
                        + binder.getExecutableReference() + "'", e);
            }
        }
    }

    private IDomain<?> startTenantDomainFirst() {
        for (Map.Entry<String, IDomain<?>> entry : this.domainContexts.entrySet()) {
            IDomain<?> domainContext = entry.getValue();
            if (domainContext.isTenantEntity()) {
                String domainName = entry.getKey();
                domainContext.onStart();
                log.info("Started tenant domain '{}' (priority)", domainName);
                return domainContext;
            }
        }
        return null;
    }

    /**
     * Bootstraps the master tenant row by writing it directly to the tenant
     * domain's repository — <strong>without</strong> going through the public
     * workflow pipeline. This is a deliberate framework-internal operation:
     *
     * <ul>
     *   <li>It runs synchronously during {@code onStart()}, before any user
     *       traffic, so there is no caller to authorize anyway.</li>
     *   <li>Going through {@code Domain.invoke()} would force this bootstrap
     *       through {@code VERIFY_AUTHORIZATION}, which has no idea how to
     *       authorize a system-level write — historically this required a
     *       super-tenant short-circuit in the script that turned out to be a
     *       security flaw (any caller could fake {@code superTenant=true}).</li>
     * </ul>
     *
     * <p>Consequence: {@code @EntityBeforeCreate} / {@code @EntityAfterCreate}
     * hooks declared on the tenant entity <strong>do not fire</strong> for the
     * master row. This is intentional — those hooks belong to user-triggered
     * lifecycle, not to framework bootstrap. If you need to react to the
     * master tenant being created, hook on {@link ILifecycle#onStart()} of the
     * tenant domain (which still runs after this method).
     */
    private void autoCreateMasterTenant(IDomain<?> tenantDomain) {
        log.info("Auto-creating master tenant with id '{}'", this.superTenantId);

        IRepository repository = tenantDomain.getRepository();
        if (repository.doesExist(this.superTenantId)) {
            // Already created on a previous run. Re-register it as super: its status
            // must not depend on the persisted superTenant boolean surviving the
            // entity→DTO mapping (a DTO that omits the field drops it, so the startup
            // scan would not re-discover it and the master would silently lose its
            // super status on restart). Existence of the master IS the signal.
            registerSuperTenant(this.superTenantId);
            log.info("Master tenant '{}' already exists — re-registered as super, skipping auto-creation",
                    this.superTenantId);
            return;
        }

        try {
            // Build a minimal entity via reflection. UUID = superTenantId; the
            // tenantId field is set to the same value only if the entity carries
            // one (a tenant entity may legitimately omit it — its uuid plays
            // that role downstream, see RepositoryFilterTools.buildTenantFilter).
            IClass<?> entityClass = tenantDomain.getEntityClass();
            Object tenantEntity = entityClass.getConstructor().newInstance();

            IReflection reflection = reflection();
            ObjectAddress uuidAddress = tenantDomain.getEntityDefinition().uuid();
            reflection.setFieldValue(tenantEntity, uuidAddress, this.superTenantId);

            ObjectAddress tenantIdAddress = tenantDomain.getTenantIdFieldAddress();
            if (tenantIdAddress != null) {
                reflection.setFieldValue(tenantEntity, tenantIdAddress, this.superTenantId);
            }

            // Estampillage: the master tenant IS a super-tenant. Stamp its
            // superTenant field true so the row is self-describing (a later
            // startup scan re-discovers it) and register its id now, exempt
            // from the creation lock — this is framework bootstrap, not a
            // runtime promotion.
            ObjectAddress superTenantAddress = tenantDomain.getDomainDefinition().superTenant();
            if (superTenantAddress != null) {
                reflection.setFieldValue(tenantEntity, superTenantAddress, Boolean.TRUE);
            }
            registerSuperTenant(this.superTenantId);

            // Direct repository write — no workflow, no security pipeline, no
            // lifecycle hooks. The repository handles the entity → DTO mapping
            // internally (see Repository.save).
            repository.save(tenantEntity);

            // Sanity check: confirm the entity actually landed. Catches the
            // "DAO save returned but the row isn't queryable" class of bug.
            if (repository.doesExist(this.superTenantId)) {
                log.info("Master tenant '{}' auto-created successfully", this.superTenantId);
                warnIfSuperTenantStampDropped(repository, reflection, uuidAddress, superTenantAddress, tenantDomain);
            } else {
                log.error("repository.save returned but master tenant '{}' is not present in the "
                        + "repository afterwards. Consider registering the master tenant entity "
                        + "via .upsert(...) on the tenant domain builder.",
                        this.superTenantId);
            }
        } catch (Exception e) {
            log.warn("Could not auto-create master tenant '{}': {}. "
                    + "Consider registering the master tenant entity via .upsert(...) on the tenant domain builder.",
                    this.superTenantId, e.getMessage());
        }
    }

    /**
     * Best-effort diagnostic: re-reads the just-persisted master tenant and warns
     * when its {@code superTenant} boolean did not survive the entity→DTO mapping —
     * the symptom of a DTO that omits the {@code superTenant} field. The configured
     * super-tenant is registered unconditionally at startup, so it still functions;
     * but the persisted row is not self-describing (and reads show {@code false}).
     */
    private void warnIfSuperTenantStampDropped(IRepository repository, IReflection reflection,
            ObjectAddress uuidAddress, ObjectAddress superTenantAddress, IDomain<?> tenantDomain) {
        if (superTenantAddress == null) {
            return;
        }
        try {
            Object reread = repository.getEntities(Optional.empty(), Optional.empty(), Optional.empty()).stream()
                    .filter(e -> this.superTenantId.equals(readStringField(reflection, e, uuidAddress)))
                    .findFirst().orElse(null);
            if (reread != null && !readBooleanField(reflection, reread, superTenantAddress)) {
                log.warn("Master tenant '{}' was stamped superTenant=true but the persisted value did not "
                        + "survive persistence — the DTO for domain '{}' does not carry the superTenant "
                        + "field. The configured super-tenant is still registered at startup, so it "
                        + "functions correctly; declare a matching superTenant field on the DTO to make "
                        + "the persisted row self-describing and visible on reads.",
                        this.superTenantId, tenantDomain.getDomainName());
            }
        } catch (Exception ignore) {
            // Diagnostic only — never let it disturb startup.
        }
    }

    @Override
    protected ILifecycle doStop() {
        // Stop all domain contexts first
        for (Map.Entry<String, IDomain<?>> entry : this.domainContexts.entrySet()) {
            String domainName = entry.getKey();
            IDomain<?> domainContext = entry.getValue();
            try {
                domainContext.onStop();
                log.info("Stopped domain '{}'", domainName);
            } catch (LifecycleException e) {
                log.error("Failed to stop domain '{}': {}", domainName, e.getMessage());
            }
        }

        // Stop the injection context
        this.injectionContext.onStop();

        return this;
    }

    @Override
    public String getSummaryCategory() {
        return "Garganttua API";
    }

    @Override
    public Map<String, String> getSummaryItems() {
        Map<String, String> items = new java.util.LinkedHashMap<>();

        // Global configuration
        String tenancy = multiTenant ? "enabled" : "disabled";
        if (multiTenant && superTenantId != null) {
            tenancy += " (superTenant=" + superTenantId + (superTenantAutoCreate ? ", autoCreate" : "") + ")";
        }
        items.put("Multi-tenancy", tenancy);
        items.put("Domains", String.valueOf(domainContexts.size()));

        // Collect global DAO types and interface counts
        java.util.Set<String> daoTypes = new java.util.LinkedHashSet<>();
        int totalInterfaces = 0;
        int totalEvents = 0;
        int securedDomains = 0;

        // Per-domain details
        for (Map.Entry<String, IDomain<?>> entry : domainContexts.entrySet()) {
            String name = entry.getKey();
            IDomain<?> ctx = entry.getValue();
            var def = ctx.getDomainDefinition();

            // Domain summary line: entity + DTOs + flags
            StringBuilder domainInfo = new StringBuilder();
            domainInfo.append(def.entityDefinition().entityClass().getSimpleName());

            // DTOs
            if (!def.dtoDefinitions().isEmpty()) {
                domainInfo.append(" -> ");
                domainInfo.append(def.dtoDefinitions().stream()
                        .map(dto -> dto.dtoClass().getSimpleName())
                        .collect(Collectors.joining(", ")));
            }

            // Flags
            List<String> flags = new ArrayList<>();
            if (Boolean.TRUE.equals(def.tenant())) flags.add("tenant");
            if (Boolean.TRUE.equals(def.publik())) flags.add("public");
            if (def.owned() != null) flags.add("owned");
            if (def.shared() != null) flags.add("shared");
            if (def.hiddenable() != null) flags.add("hiddenable");
            if (def.geolocalized() != null) flags.add("geolocalized");
            if (def.superOwner() != null) flags.add("superOwner");
            if (def.superTenant() != null) flags.add("superTenant");
            if (!flags.isEmpty()) {
                domainInfo.append(" [").append(String.join(", ", flags)).append("]");
            }
            items.put("Domain '" + name + "'", domainInfo.toString());

            // DAO
            if (ctx.getRepository() != null) {
                daoTypes.add(ctx.getRepository().getClass().getSimpleName());
            }

            // Operations
            var operations = def.operations();
            if (!operations.isEmpty()) {
                items.put("  operations", operations.stream()
                        .map(op -> op.getBusinessOperation().getLabel())
                        .collect(Collectors.joining(", ")));
            }

            // Security
            var secDef = (def instanceof com.garganttua.api.core.domain.DomainDefinition<?> dd)
                    ? dd.domainSecurityDefinition() : null;
            if (secDef != null && !secDef.disabled()) {
                securedDomains++;
                StringBuilder secInfo = new StringBuilder("enabled");
                if (secDef.authenticatorDefinition() != null) {
                    secInfo.append(" (authenticator: ").append(secDef.authenticatorDefinition().scope()).append(")");
                }
                items.put("  security", secInfo.toString());
            }

            // Interfaces / Events
            if (ctx instanceof Domain<?> dc) {
                if (dc.getInterfaces() != null) totalInterfaces += dc.getInterfaces().size();
                if (dc.getEvents() != null) totalEvents += dc.getEvents().size();
            }
        }

        // Global summaries
        if (!daoTypes.isEmpty()) {
            items.put("DAOs", String.join(", ", daoTypes));
        }
        if (totalInterfaces > 0) {
            items.put("Interfaces", String.valueOf(totalInterfaces));
        }
        if (totalEvents > 0) {
            items.put("Event publishers", String.valueOf(totalEvents));
        }
        if (securedDomains > 0) {
            items.put("Secured domains", securedDomains + "/" + domainContexts.size());
        }

        return items;
    }

    @Override
    protected ILifecycle doFlush() {
        // Flush all domain contexts
        for (Map.Entry<String, IDomain<?>> entry : this.domainContexts.entrySet()) {
            String domainName = entry.getKey();
            IDomain<?> domainContext = entry.getValue();
            try {
                domainContext.onFlush();
                log.info("Flushed domain '{}'", domainName);
            } catch (LifecycleException e) {
                log.error("Failed to flush domain '{}': {}", domainName, e.getMessage());
            }
        }
        return this;
    }

}
