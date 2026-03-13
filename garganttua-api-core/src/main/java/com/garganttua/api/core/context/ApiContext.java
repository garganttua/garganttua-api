package com.garganttua.api.core.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.operation.OperationDefinition;
import com.garganttua.api.spec.service.IOperationRequest;
import com.garganttua.api.spec.service.IOperationResponse;
import com.garganttua.api.spec.service.OperationResponseCode;
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
public class ApiContext extends AbstractLifecycle implements IApiContext {

    private final IInjectionContext injectionContext;
    private final Map<String, IDomainContext<?>> domainContexts;
    private final String superTenantId;
    private final boolean superTenantAutoCreate;
    private final boolean multiTenant;
    private final List<IMethodBinder<Void>> startupBinders;

    public ApiContext(IInjectionContext injectionContext, Map<String, IDomainContext<?>> domainContexts,
            String superTenantId, boolean superTenantAutoCreate, boolean multiTenant, List<IMethodBinder<Void>> startupBinders) {
        this.injectionContext = Objects.requireNonNull(injectionContext, "Injection context cannot be null");
        this.domainContexts = Collections.unmodifiableMap(new HashMap<>(
                Objects.requireNonNull(domainContexts, "Domain contexts cannot be null")));
        this.superTenantId = superTenantId;
        this.superTenantAutoCreate = superTenantAutoCreate;
        this.multiTenant = multiTenant;
        this.startupBinders = Collections.unmodifiableList(new ArrayList<>(
                Objects.requireNonNull(startupBinders, "Startup binders cannot be null")));
    }

    @Override
    public Optional<IDomainContext<?>> getDomainContext(String domainName) {
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

    public Map<String, IDomainContext<?>> getDomainContexts() {
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
        for (Map.Entry<String, IDomainContext<?>> entry : this.domainContexts.entrySet()) {
            String domainName = entry.getKey();
            IDomainContext<?> domainContext = entry.getValue();
            // Set parent API context reference
            if (domainContext instanceof DomainContext<?> dc) {
                dc.setApiContext(this);
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
        IDomainContext<?> tenantDomainContext = this.multiTenant ? startTenantDomainFirst() : null;

        // 4. Auto-create master tenant if configured — only in multi-tenant mode
        if (tenantDomainContext != null && this.superTenantAutoCreate) {
            autoCreateMasterTenant(tenantDomainContext);
        }

        // 5. Start all remaining (non-tenant) domain contexts
        for (Map.Entry<String, IDomainContext<?>> entry : this.domainContexts.entrySet()) {
            String domainName = entry.getKey();
            IDomainContext<?> domainContext = entry.getValue();
            if (domainContext == tenantDomainContext) {
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

    private IDomainContext<?> startTenantDomainFirst() {
        for (Map.Entry<String, IDomainContext<?>> entry : this.domainContexts.entrySet()) {
            IDomainContext<?> domainContext = entry.getValue();
            if (domainContext.isTenantEntity()) {
                String domainName = entry.getKey();
                domainContext.onStart();
                log.info("Started tenant domain '{}' (priority)", domainName);
                return domainContext;
            }
        }
        return null;
    }

    private IOperationRequest buildStartupRequest(IDomainContext<?> domainContext,
            OperationDefinition operation, ICaller caller) {
        OperationRequest request = new OperationRequest(new HashMap<>());
        request.arg(IOperationRequest.OPERATION, operation);
        request.arg(IOperationRequest.TENANT_ID, caller.tenantId());
        request.arg(IOperationRequest.REQUESTED_TENANT_ID, caller.requestedTenantId());
        request.arg(IOperationRequest.SUPER_TENANT, caller.superTenant());
        request.arg(IOperationRequest.SUPER_OWNER, caller.superOwner());
        return request;
    }

    private void autoCreateMasterTenant(IDomainContext<?> tenantDomainContext) {
        log.info("Auto-creating master tenant with id '{}'", this.superTenantId);
        ICaller caller = new Caller(this.superTenantId, this.superTenantId, null, null, true, true, null);

        // Check if master tenant already exists
        IOperationRequest readRequest = buildStartupRequest(tenantDomainContext,
                OperationDefinition.readOneWithStandardSecurity(
                        tenantDomainContext.getDomainName(), tenantDomainContext.getEntityClass()),
                caller);
        readRequest.arg("type", "uuid");
        readRequest.arg("identifier", this.superTenantId);
        IOperationResponse readResponse = tenantDomainContext.invoke(readRequest);
        if (readResponse.getResponseCode() == OperationResponseCode.OK) {
            log.info("Master tenant '{}' already exists, skipping auto-creation", this.superTenantId);
            return;
        }

        // Create a minimal tenant entity via reflection
        try {
            IClass<?> entityClass = tenantDomainContext.getEntityClass();
            Object tenantEntity = entityClass.getConstructor().newInstance();

            IReflection reflection = reflection();
            // Set UUID to superTenantId
            ObjectAddress uuidAddress = tenantDomainContext.getEntityDefinition().uuid();
            reflection.setFieldValue(tenantEntity, uuidAddress, this.superTenantId);

            // Set tenantId to superTenantId
            ObjectAddress tenantIdAddress = tenantDomainContext.getTenantIdFieldAddress();
            if (tenantIdAddress != null) {
                reflection.setFieldValue(tenantEntity, tenantIdAddress, this.superTenantId);
            }

            IOperationRequest createRequest = buildStartupRequest(tenantDomainContext,
                    OperationDefinition.createOneWithStandardSecurity(
                            tenantDomainContext.getDomainName(), tenantDomainContext.getEntityClass()),
                    caller);
            createRequest.arg("entity", tenantEntity);
            IOperationResponse createResponse = tenantDomainContext.invoke(createRequest);
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
        for (Map.Entry<String, IDomainContext<?>> entry : this.domainContexts.entrySet()) {
            String domainName = entry.getKey();
            IDomainContext<?> domainContext = entry.getValue();
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
    protected ILifecycle doFlush() {
        // Flush all domain contexts
        for (Map.Entry<String, IDomainContext<?>> entry : this.domainContexts.entrySet()) {
            String domainName = entry.getKey();
            IDomainContext<?> domainContext = entry.getValue();
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
