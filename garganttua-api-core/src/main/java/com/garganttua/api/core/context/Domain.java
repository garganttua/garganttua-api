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
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.IDtoContext;
import com.garganttua.api.commons.context.IEntityContext;
import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.event.IEventPublisher;
import com.garganttua.api.commons.endpoint.IEndpoint;
import com.garganttua.api.commons.repository.IRepository;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.core.injection.BeanDefinition;
import com.garganttua.api.commons.security.IDomainSecurityContext;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.IRequestBuilder;
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

    public IApi getApiContext() {
        return this.apiContext;
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

    /**
     * Best-effort create of declared {@code createEntity(...)} entries at startup.
     * Writes go straight through the repository — <strong>not</strong> through
     * {@link #invoke(IOperationRequest)}.
     *
     * <p>This runs from inside {@link #doStart()}, before the lifecycle has
     * flipped to STARTED, so calling {@code invoke()} from here would trip
     * {@code AbstractLifecycle.ensureStarted()} (regression observed in the
     * example app's tenant domain). The same rationale that drives
     * {@code Api.autoCreateMasterTenant} applies here: framework bootstrap
     * has no caller to authorize and no reason to traverse the public
     * workflow.
     *
     * <p>Consequence: {@code @EntityBeforeCreate} / {@code @EntityAfterCreate}
     * hooks do <strong>not</strong> fire for these entities. If you need
     * lifecycle hooks for startup data, use an API-level startup binder
     * instead — it runs after every domain has started.
     */
    private void createStartupEntities() {
        List<E> createEntities = this.domainDefinition.createEntities();
        if (createEntities == null || createEntities.isEmpty()) {
            return;
        }
        IReflection reflection = reflection();
        String uuidFieldPath = this.domainDefinition.entityDefinition().uuid().toString();
        log.info("Creating {} startup entities for domain {} (direct repository writes)",
                createEntities.size(), this.domainDefinition.domainName());
        for (E entity : createEntities) {
            try {
                Object uuidValue = reflection.getFieldValue(entity, uuidFieldPath);
                String uuid = uuidValue != null ? uuidValue.toString() : null;
                if (uuid != null && this.repository.doesExist(uuid)) {
                    log.warn("Startup entity (uuid={}) already exists for domain {}, skipping",
                            uuid, this.domainDefinition.domainName());
                    continue;
                }
                this.repository.save(entity);
                log.info("Startup entity created (uuid={}) for domain {}", uuid,
                        this.domainDefinition.domainName());
            } catch (ApiException e) {
                log.warn("Startup entity creation failed for domain {} (best-effort, continuing): {}",
                        this.domainDefinition.domainName(), e.getMessage());
            }
        }
    }

    /**
     * Fail-fast upsert of declared {@code upsertEntity(...)} entries at startup.
     * Same rationale and constraints as {@link #createStartupEntities()}:
     * direct repository writes, no workflow, no lifecycle hooks.
     *
     * <p>Upsert semantics here = "delete then save" when the uuid already
     * exists. We do not rely on the DAO implementing native upsert because
     * {@link com.garganttua.api.commons.dao.IDao} makes no such guarantee —
     * the test in-memory DAO appends on every save. The delete-then-save
     * pair is the only IDao-portable way to express "make sure this row is
     * now exactly the declared value".
     */
    private void upsertStartupEntities() {
        List<E> upsertEntities = this.domainDefinition.upsertEntities();
        if (upsertEntities == null || upsertEntities.isEmpty()) {
            return;
        }
        IReflection reflection = reflection();
        String uuidFieldPath = this.domainDefinition.entityDefinition().uuid().toString();
        log.info("Upserting {} startup entities for domain {} (direct repository writes)",
                upsertEntities.size(), this.domainDefinition.domainName());
        for (E entity : upsertEntities) {
            try {
                Object uuidValue = reflection.getFieldValue(entity, uuidFieldPath);
                String uuid = uuidValue != null ? uuidValue.toString() : null;
                if (uuid == null) {
                    throw new ApiException("Upsert startup entity has no UUID for domain "
                            + this.domainDefinition.domainName());
                }
                if (this.repository.doesExist(uuid)) {
                    this.repository.delete(entity);
                    this.repository.save(entity);
                    log.info("Startup entity replaced (uuid={}) for domain {}", uuid,
                            this.domainDefinition.domainName());
                } else {
                    this.repository.save(entity);
                    log.info("Startup entity created (uuid={}) for domain {}", uuid,
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
        return RequestBuilder.builder(this);
    }

    @Override
    public IOperationResponse invoke(IOperationRequest request) {
        return invoke(request, WorkflowExecutionOptions.none());
    }

    @Override
    public IOperationResponse invoke(IOperationRequest request, WorkflowExecutionOptions options) {
        ensureStarted();
        long startNanos = System.nanoTime();
        OperationResponse response = doInvoke(request, options);
        return response.withProcessingTime(java.time.Duration.ofNanos(System.nanoTime() - startNanos));
    }

    private OperationResponse doInvoke(IOperationRequest request, WorkflowExecutionOptions options) {
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

            // Map "body" to "entity" for script compatibility
            request.arg(IOperationRequest.BODY).ifPresent(body -> request.arg("entity", body));

            Map<String, Object> workflowParams = new java.util.LinkedHashMap<>();
            workflowParams.put("$1", this.repository);
            workflowParams.put("$2", this);
            workflowParams.put("$3", this.apiContext);
            WorkflowInput input = WorkflowInput.of(request, workflowParams);
            WorkflowResult result = this.workflow.execute(input, options);

            String opLabel = resolveOperationLabel(request);
            String domainName = this.domainDefinition.domainName();

            if (result.isSuccess()) {
                return OperationResponse.ok(result.output());
            } else if (result.hasAborted()) {
                String errorMsg = nonBlank(result.exceptionMessage())
                        .orElseGet(() -> "Operation '" + opLabel + "' on domain '" + domainName
                                + "' aborted unexpectedly");
                log.error("Workflow aborted for domain {} op {}: {}", domainName, opLabel, errorMsg);
                return OperationResponse.error(errorMsg);
            } else {
                String errorMsg = nonBlank(result.exceptionMessage())
                        .orElseGet(() -> defaultMessageForCode(result.code(), opLabel, domainName));
                log.warn("Workflow returned code {} for domain {} op {}: {}",
                        result.code(), domainName, opLabel, errorMsg);
                return mapWorkflowCode(result.code(), errorMsg);
            }
        } catch (Exception e) {
            log.error("Error executing workflow for domain {}: {}",
                    this.domainDefinition.domainName(), e.getMessage(), e);
            return OperationResponse.error("Workflow execution error on domain '"
                    + this.domainDefinition.domainName() + "': " + e.getMessage());
        }
    }

    private static java.util.Optional<String> nonBlank(java.util.Optional<String> opt) {
        return opt.filter(s -> s != null && !s.isBlank());
    }

    private static String resolveOperationLabel(IOperationRequest request) {
        return request.arg(IOperationRequest.OPERATION)
                .map(op -> op.getBusinessOperation())
                .map(bo -> bo.getLabel())
                .orElse("unknown");
    }

    /**
     * Builds a parlant fallback for workflow failures where no script-level
     * message reached {@code WorkflowResult.exceptionMessage} — e.g. scripts
     * that return {@code ! -> CODE} after a guard that doesn't raise. Without
     * this, the operator-facing error would be the unhelpful "Workflow
     * execution failed" string.
     */
    static String defaultMessageForCode(Integer code, String opLabel, String domainName) {
        if (code == null) {
            return "Operation '" + opLabel + "' on domain '" + domainName + "' failed";
        }
        return switch (code) {
            case 400 -> "Bad request — '" + opLabel + "' on '" + domainName
                    + "' rejected by validation";
            case 401 -> "Authorization required to perform '" + opLabel
                    + "' on '" + domainName + "'";
            case 403 -> "Forbidden — caller lacks the privilege to perform '"
                    + opLabel + "' on '" + domainName + "'";
            case 404 -> "Not found — no matching resource for '" + opLabel
                    + "' on '" + domainName + "'";
            case 409 -> "Conflict — '" + opLabel + "' on '" + domainName
                    + "' could not be applied to the current state";
            default -> "Operation '" + opLabel + "' on '" + domainName
                    + "' failed with code " + code;
        };
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
