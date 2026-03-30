package com.garganttua.api.core.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.garganttua.api.core.service.OperationResponse;
import com.garganttua.api.core.service.RequestBuilder;
import com.garganttua.api.core.repository.Repository;
import com.garganttua.api.core.definition.DomainDefinition;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IApi;
import com.garganttua.api.spec.context.IDomain;
import com.garganttua.api.spec.context.IDtoContext;
import com.garganttua.api.spec.context.IEntityContext;
import com.garganttua.api.spec.definition.IDomainDefinition;
import com.garganttua.api.spec.event.IEventPublisher;
import com.garganttua.api.spec.endpoint.IEndpoint;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.operation.OperationDefinition;
import com.garganttua.api.spec.service.IOperationResponse;
import com.garganttua.api.spec.service.OperationResponseCode;
import com.garganttua.core.injection.BeanDefinition;
import com.garganttua.api.spec.security.IDomainSecurityContext;
import com.garganttua.api.spec.service.IOperationRequest;
import com.garganttua.api.spec.service.IRequestBuilder;
import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.core.lifecycle.AbstractLifecycle;
import com.garganttua.core.lifecycle.ILifecycle;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.workflow.IWorkflow;
import com.garganttua.core.workflow.WorkflowExecutionOptions;
import com.garganttua.core.workflow.WorkflowInput;
import com.garganttua.core.workflow.WorkflowResult;
import com.github.f4b6a3.uuid.UuidCreator;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Domain<E> extends AbstractLifecycle implements IDomain<E> {

    private final DomainDefinition<E> domainDefinition;
    private final List<ISupplier<IEndpoint>> interfaces;
    private final List<ISupplier<IEventPublisher>> events;
    private final IDomainSecurityContext domainSecurityContext;

    private final IEntityContext<E> entityContext;
    private final List<IDtoContext<?>> dtoContexts;
    @Getter
    private final IRepository repository;

    // Single workflow handling the full pipeline (business → security → execution)
    private IWorkflow workflow;

    public List<ISupplier<IEndpoint>> getInterfaces() { return interfaces; }
    public List<ISupplier<IEventPublisher>> getEvents() { return events; }

    // Bean definition for runtime DI injection on entities
    @Getter
    private BeanDefinition<?> entityBeanDefinition;
    @Getter
    private boolean doInjection;

    private IApi apiContext;

    public void setApi(IApi apiContext) {
        this.apiContext = apiContext;
    }

    @Override
    public boolean isMultiTenant() {
        return this.apiContext != null && this.apiContext.isMultiTenant();
    }

    public void setEntityBeanDefinition(BeanDefinition<?> entityBeanDefinition) {
        this.entityBeanDefinition = entityBeanDefinition;
    }

    public void setDoInjection(boolean doInjection) {
        this.doInjection = doInjection;
    }

    public void setWorkflow(IWorkflow workflow) {
        this.workflow = Objects.requireNonNull(workflow, "Workflow cannot be null");
    }

    public Domain(DomainDefinition<E> domainDefinition, IEntityContext<E> entityContext,
            IDomainSecurityContext domainSecurityContext,
            List<IDtoContext<?>> dtoContexts,
            List<ISupplier<IEndpoint>> interfaces,
            List<ISupplier<IEventPublisher>> events
            ) {
        this.domainSecurityContext = Objects.requireNonNull(domainSecurityContext,
                "Domain security context cannot be null");
        this.entityContext = Objects.requireNonNull(entityContext, "Entity context cannot be null");
        this.domainDefinition = Objects.requireNonNull(domainDefinition, "Domain definition cannot be null");
        this.interfaces = Collections.unmodifiableList(new java.util.ArrayList<>(
                Objects.requireNonNull(interfaces, "Interfaces cannot be null")));
        this.events = Collections.unmodifiableList(new java.util.ArrayList<>(
                Objects.requireNonNull(events, "Events cannot be null")));
        this.dtoContexts = Collections.unmodifiableList(new java.util.ArrayList<>(
                Objects.requireNonNull(dtoContexts, "Dto contexts cannot be null")));
        

        Repository repo = new Repository(this.dtoContexts, entityContext.getEntityClass());
        repo.setDomain(this);
        this.repository = repo;
    }

    @Override
    public IReflection reflection() {
        return DefaultMapper.reflection();
    }

    @Override
    protected ILifecycle doInit() {
        log.info("Initializing domain context: {}", this.domainDefinition.domainName());

        // 1. Log workflow
        log.debug("Workflow configured for domain {}: {}", this.domainDefinition.domainName(),
                this.workflow != null ? this.workflow.getName() : "none");

        // 3. Build interfaces, pass domain context (with access rules), and init them
        initializeInterfaces();

        return this;
    }

    private void initializeInterfaces() {

        doForAllInterfaces(IEndpoint::handle, this);
        doForAllInterfaces(IEndpoint::onInit);

        log.debug("Initialized {} interfaces for domain {}", this.interfaces.size(),
                this.domainDefinition.domainName());
    }

    /**
     * Functional interface for interface actions that can throw exceptions.
     */
    @FunctionalInterface
    private interface InterfaceAction {
        void apply(IEndpoint intf) throws Exception;
    }

    /**
     * Functional interface for interface actions with an argument that can throw exceptions.
     */
    @FunctionalInterface
    private interface InterfaceActionWithArg<T> {
        void apply(IEndpoint intf, T arg) throws Exception;
    }

    /**
     * Executes an action on all built interfaces.
     *
     * @param action the action to execute on each interface
     */
    private void doForAllInterfaces(InterfaceAction action) {
        for (ISupplier<IEndpoint> supplier : this.interfaces) {
            try {
                IEndpoint intf = supplier.supply()
                        .orElseThrow(() -> new ApiException("Interface supplier returned empty Optional"));
                action.apply(intf);
            } catch (Exception e) {
                throw new ApiException("Interface action failed for domain " + this.domainDefinition.domainName(),
                        e);
            }
        }
    }

    /**
     * Executes an action with an argument on all built interfaces.
     *
     * @param action the action to execute on each interface
     * @param arg the argument to pass to the action
     */
    private <T> void doForAllInterfaces(InterfaceActionWithArg<T> action, T arg) {
        for (ISupplier<IEndpoint> supplier : this.interfaces) {
            try {
                IEndpoint intf = supplier.supply()
                        .orElseThrow(() -> new ApiException("Interface supplier returned empty Optional"));
                action.apply(intf, arg);
            } catch (Exception e) {
                throw new ApiException("Interface action failed for domain " + this.domainDefinition.domainName(),
                        e);
            }
        }
    }

    @Override
    protected ILifecycle doStart() {
        log.info("Starting domain context: {}", this.domainDefinition.domainName());

        // 1. Execute startup binders
        executeStartupBinders();

        // 2. Create startup entities (ignore duplicates with warning)
        createStartupEntities();

        // 3. Upsert startup entities (fail-fast)
        upsertStartupEntities();

        // 4. Start all interfaces
        doForAllInterfaces(IEndpoint::onStart);

        log.debug("Started {} interfaces for domain {}", this.interfaces.size(),
                this.domainDefinition.domainName());

        return this;
    }

    private void executeStartupBinders() {
        List<IMethodBinder<Void>> startupBinders = this.domainDefinition.startupBinders();
        if (startupBinders == null || startupBinders.isEmpty()) {
            return;
        }
        log.debug("Executing {} startup binders for domain {}", startupBinders.size(),
                this.domainDefinition.domainName());
        for (IMethodBinder<Void> binder : startupBinders) {
            try {
                log.trace("Executing startup binder: {}", binder.getExecutableReference());
                binder.execute();
            } catch (ReflectionException e) {
                log.error("Failed to execute startup binder {} for domain {}: {}",
                        binder.getExecutableReference(), this.domainDefinition.domainName(), e.getMessage(), e);
                throw new ApiException(
                        "Startup binder execution failed for domain " + this.domainDefinition.domainName(), e);
            }
        }
        log.debug("Successfully executed all startup binders for domain {}", this.domainDefinition.domainName());
    }

    private ICaller createStartupCaller() {
        String superTenantId = this.apiContext.getSuperTenantId();
        return new Caller(superTenantId, superTenantId, null, null, true, true, null);
    }

    private IOperationRequest buildStartupRequest(OperationDefinition operation, ICaller caller) {
        OperationRequest request = new OperationRequest(new java.util.HashMap<>());
        request.arg(IOperationRequest.OPERATION, operation);
        request.arg(IOperationRequest.TENANT_ID, caller.tenantId());
        request.arg(IOperationRequest.REQUESTED_TENANT_ID, caller.requestedTenantId());
        request.arg(IOperationRequest.SUPER_TENANT, caller.superTenant());
        request.arg(IOperationRequest.SUPER_OWNER, caller.superOwner());
        return request;
    }

    private void createStartupEntities() {
        List<E> createEntities = this.domainDefinition.createEntities();
        if (createEntities == null || createEntities.isEmpty()) {
            return;
        }
        ICaller caller = createStartupCaller();
        log.info("Creating {} startup entities for domain {}", createEntities.size(),
                this.domainDefinition.domainName());
        for (E entity : createEntities) {
            IOperationRequest request = buildStartupRequest(
                    OperationDefinition.createOneWithStandardSecurity(getDomainName(), getEntityClass()), caller);
            request.arg("entity", entity);
            IOperationResponse response = invoke(request);
            OperationResponseCode code = response.getResponseCode();
            if (code == OperationResponseCode.CREATED || code == OperationResponseCode.OK) {
                log.info("Startup entity created successfully for domain {}", this.domainDefinition.domainName());
            } else {
                log.warn("Startup entity creation returned {} for domain {} (entity may already exist): {}",
                        code, this.domainDefinition.domainName(), response.getResponse());
            }
        }
    }

    private void upsertStartupEntities() {
        List<E> upsertEntities = this.domainDefinition.upsertEntities();
        if (upsertEntities == null || upsertEntities.isEmpty()) {
            return;
        }
        ICaller caller = createStartupCaller();
        IReflection reflection = reflection();
        String uuidFieldPath = this.domainDefinition.entityDefinition().uuid().toString();
        log.info("Upserting {} startup entities for domain {}", upsertEntities.size(),
                this.domainDefinition.domainName());
        for (E entity : upsertEntities) {
            try {
                Object uuidValue = reflection.getFieldValue(entity, uuidFieldPath);
                String uuid = uuidValue != null ? uuidValue.toString() : null;
                if (uuid == null) {
                    throw new ApiException("Upsert startup entity has no UUID for domain "
                            + this.domainDefinition.domainName());
                }
                // Try to read existing entity
                IOperationRequest readRequest = buildStartupRequest(
                        OperationDefinition.readOneWithStandardSecurity(getDomainName(), getEntityClass()), caller);
                readRequest.arg("type", "uuid");
                readRequest.arg("identifier", uuid);
                IOperationResponse readResponse = invoke(readRequest);
                if (readResponse.getResponseCode() == OperationResponseCode.OK) {
                    // Entity exists, update it
                    IOperationRequest updateRequest = buildStartupRequest(
                            OperationDefinition.updateOneWithStandardSecurity(getDomainName(), getEntityClass()), caller);
                    updateRequest.arg("type", "uuid");
                    updateRequest.arg("identifier", uuid);
                    updateRequest.arg("entity", entity);
                    IOperationResponse updateResponse = invoke(updateRequest);
                    OperationResponseCode updateCode = updateResponse.getResponseCode();
                    if (updateCode != OperationResponseCode.OK && updateCode != OperationResponseCode.UPDATED) {
                        throw new ApiException("Failed to update startup entity (uuid=" + uuid
                                + ") for domain " + this.domainDefinition.domainName()
                                + ": " + updateResponse.getResponse());
                    }
                    log.info("Startup entity updated (uuid={}) for domain {}", uuid,
                            this.domainDefinition.domainName());
                } else {
                    // Entity does not exist, create it
                    IOperationRequest createRequest = buildStartupRequest(
                            OperationDefinition.createOneWithStandardSecurity(getDomainName(), getEntityClass()), caller);
                    createRequest.arg("entity", entity);
                    IOperationResponse createResponse = invoke(createRequest);
                    OperationResponseCode createCode = createResponse.getResponseCode();
                    if (createCode != OperationResponseCode.CREATED && createCode != OperationResponseCode.OK) {
                        throw new ApiException("Failed to create startup entity (uuid=" + uuid
                                + ") for domain " + this.domainDefinition.domainName()
                                + ": " + createResponse.getResponse());
                    }
                    log.info("Startup entity created via upsert (uuid={}) for domain {}", uuid,
                            this.domainDefinition.domainName());
                }
            } catch (ApiException e) {
                throw e;
            } catch (Exception e) {
                throw new ApiException("Failed to upsert startup entity for domain "
                        + this.domainDefinition.domainName(), e);
            }
        }
    }

    @Override
    protected ILifecycle doStop() {
        log.info("Stopping domain context: {}", this.domainDefinition.domainName());

        // Stop all interfaces
        doForAllInterfaces(IEndpoint::onStop);
        log.debug("Stopped {} interfaces for domain {}", this.interfaces.size(),
                this.domainDefinition.domainName());

        return this;
    }

    @Override
    protected ILifecycle doFlush() {
        log.info("Flushing domain context: {}", this.domainDefinition.domainName());
        // Flush all interfaces
        doForAllInterfaces(IEndpoint::onFlush);
        log.debug("Flushed {} interfaces for domain {}", this.interfaces.size(),
                this.domainDefinition.domainName());
        return this;
    }

    @Override
    public IDomainDefinition<E> getDomainDefinition() {
        return this.domainDefinition;
    }

    @Override
    public IRequestBuilder request() {
        return new RequestBuilder(this);
    }

    @Override
    public IOperationResponse invoke(IOperationRequest request) {
        return invoke(request, WorkflowExecutionOptions.none());
    }

    @Override
    public IOperationResponse invoke(IOperationRequest request, WorkflowExecutionOptions options) {
        ensureStarted();

        if (this.workflow == null) {
            log.warn("No workflow configured for domain {}", this.domainDefinition.domainName());
            return OperationResponse.notAvailable("No workflow configured for domain: " + this.domainDefinition.domainName());
        }

        try {
            request.arg(IOperationRequest.EXECUTION_UUID, UuidCreator.getTimeOrderedEpoch());
            request.arg(IOperationRequest.API_CONTEXT, this.apiContext);
            request.arg(IOperationRequest.DOMAIN_CONTEXT, this);
            request.arg(IOperationRequest.REPOSITORY, this.repository);
            ICaller caller = request.caller();
            if (caller == null || caller.tenantId() == null) {
                return OperationResponse.badRequest("No caller provided");
            }
            request.arg("caller", caller);

            Map<String, Object> workflowParams = new HashMap<>();
            workflowParams.put("$1", this.repository);
            workflowParams.put("$2", this);
            workflowParams.put("request", request);
            workflowParams.put("domainContext", this);
            WorkflowInput input = WorkflowInput.of(request, workflowParams);
            WorkflowResult result = this.workflow.execute(input, options);

            if (result.isSuccess()) {
                return OperationResponse.ok(result.output());
            } else if (result.hasAborted()) {
                String errorMsg = result.exceptionMessage().orElse("Workflow execution failed");
                log.error("Workflow failed for domain {}: {}", this.domainDefinition.domainName(), errorMsg);
                return OperationResponse.error(errorMsg);
            } else {
                String errorMsg = result.exceptionMessage().orElse("Workflow execution failed");
                log.warn("Workflow returned code {} for domain {}: {}",
                        result.code(), this.domainDefinition.domainName(), errorMsg);
                return mapWorkflowCode(result.code(), errorMsg);
            }
        } catch (Exception e) {
            log.error("Error executing workflow for domain {}: {}",
                    this.domainDefinition.domainName(), e.getMessage(), e);
            return OperationResponse.error("Workflow execution error: " + e.getMessage());
        }
    }

    private OperationResponse mapWorkflowCode(Integer code, String message) {
        return switch (code) {
            case 400 -> OperationResponse.badRequest(message);
            case 401 -> OperationResponse.unauthorized(message);
            case 403 -> OperationResponse.forbidden(message);
            case 404 -> OperationResponse.notFound(message);
            case 409 -> OperationResponse.badRequest(message);
            default -> OperationResponse.error(message);
        };
    }

    @Override
    public IWorkflow getWorkflow() {
        return this.workflow;
    }

}
