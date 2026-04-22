package com.garganttua.api.core.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.protocol.IProtocol;
import com.garganttua.api.commons.repository.IRepository;
import com.garganttua.api.commons.security.authorization.IAuthorizationProtocol;
import com.garganttua.api.commons.serialization.ISerializer;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.api.core.service.OperationRequest;
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

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Api extends AbstractLifecycle implements IApi, com.garganttua.core.bootstrap.banner.IBootstrapSummaryContributor {

    private final IInjectionContext injectionContext;
    private final Map<String, IDomain<?>> domainContexts;
    private final String superTenantId;
    private final boolean superTenantAutoCreate;
    private final boolean multiTenant;
    private final List<IMethodBinder<Void>> startupBinders;
    private final List<ISerializer> serializers;
    private final List<IProtocol<?, ?>> protocols;
    private final List<IAuthorizationProtocol> authorizationProtocols;

    public Api(IInjectionContext injectionContext, Map<String, IDomain<?>> domainContexts,
            String superTenantId, boolean superTenantAutoCreate, boolean multiTenant,
            List<IMethodBinder<Void>> startupBinders, List<ISerializer> serializers,
            List<IProtocol<?, ?>> protocols,
            List<IAuthorizationProtocol> authorizationProtocols) {
        this.injectionContext = Objects.requireNonNull(injectionContext, "Injection context cannot be null");
        this.domainContexts = Collections.unmodifiableMap(new HashMap<>(
                Objects.requireNonNull(domainContexts, "Domain contexts cannot be null")));
        this.superTenantId = superTenantId;
        this.superTenantAutoCreate = superTenantAutoCreate;
        this.multiTenant = multiTenant;
        this.startupBinders = Collections.unmodifiableList(new ArrayList<>(
                Objects.requireNonNull(startupBinders, "Startup binders cannot be null")));
        this.serializers = Collections.unmodifiableList(new ArrayList<>(
                Objects.requireNonNull(serializers, "Serializers cannot be null")));
        this.protocols = Collections.unmodifiableList(new ArrayList<>(
                Objects.requireNonNull(protocols, "Protocols cannot be null")));
        this.authorizationProtocols = Collections.unmodifiableList(new ArrayList<>(
                Objects.requireNonNull(authorizationProtocols, "Authorization protocols cannot be null")));
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
    public Optional<IDomain<?>> getDomain(String domainName) {
        return Optional.ofNullable(this.domainContexts.get(domainName));
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

        return this;
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

    private IOperationRequest buildStartupRequest(IDomain<?> domainContext,
            OperationDefinition operation, ICaller caller) {
        OperationRequest request = new OperationRequest(new HashMap<>());
        request.arg(IOperationRequest.OPERATION, operation);
        request.arg(IOperationRequest.TENANT_ID, caller.tenantId());
        request.arg(IOperationRequest.REQUESTED_TENANT_ID, caller.requestedTenantId());
        request.arg(IOperationRequest.SUPER_TENANT, caller.superTenant());
        request.arg(IOperationRequest.SUPER_OWNER, caller.superOwner());
        return request;
    }

    private void autoCreateMasterTenant(IDomain<?> tenantDomain) {
        log.info("Auto-creating master tenant with id '{}'", this.superTenantId);
        ICaller caller = new Caller(this.superTenantId, this.superTenantId, null, null, true, true, null);

        // Check if master tenant already exists
        IOperationRequest readRequest = buildStartupRequest(tenantDomain,
                OperationDefinition.readOneWithStandardSecurity(
                        tenantDomain.getDomainName(), tenantDomain.getEntityClass()),
                caller);
        readRequest.arg("type", "uuid");
        readRequest.arg("identifier", this.superTenantId);
        IOperationResponse readResponse = tenantDomain.invoke(readRequest);
        if (readResponse.getResponseCode() == OperationResponseCode.OK) {
            log.info("Master tenant '{}' already exists, skipping auto-creation", this.superTenantId);
            return;
        }

        // Create a minimal tenant entity via reflection
        try {
            IClass<?> entityClass = tenantDomain.getEntityClass();
            Object tenantEntity = entityClass.getConstructor().newInstance();

            IReflection reflection = reflection();
            // Set UUID to superTenantId
            ObjectAddress uuidAddress = tenantDomain.getEntityDefinition().uuid();
            reflection.setFieldValue(tenantEntity, uuidAddress, this.superTenantId);

            // Set tenantId to superTenantId
            ObjectAddress tenantIdAddress = tenantDomain.getTenantIdFieldAddress();
            if (tenantIdAddress != null) {
                reflection.setFieldValue(tenantEntity, tenantIdAddress, this.superTenantId);
            }

            IOperationRequest createRequest = buildStartupRequest(tenantDomain,
                    OperationDefinition.createOneWithStandardSecurity(
                            tenantDomain.getDomainName(), tenantDomain.getEntityClass()),
                    caller);
            createRequest.arg("entity", tenantEntity);
            IOperationResponse createResponse = tenantDomain.invoke(createRequest);
            OperationResponseCode code = createResponse.getResponseCode();
            if (code == OperationResponseCode.CREATED || code == OperationResponseCode.OK) {
                log.info("Master tenant '{}' auto-created successfully", this.superTenantId);
            } else {
                log.warn("Could not auto-create master tenant '{}': {} (code={}). "
                        + "Consider registering the master tenant entity via .create() on the tenant domain builder.",
                        this.superTenantId, createResponse.getResponse(), code);
            }
        } catch (Exception e) {
            log.warn("Could not auto-create master tenant '{}': {}. "
                    + "Consider registering the master tenant entity via .create() on the tenant domain builder.",
                    this.superTenantId, e.getMessage());
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
            var secDef = (def instanceof com.garganttua.api.core.definition.DomainDefinition<?> dd)
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
