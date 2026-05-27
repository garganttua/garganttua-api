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
import com.garganttua.api.core.caller.Caller;
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
import com.garganttua.core.observability.IObserver;
import com.garganttua.core.observability.ObservabilityEmitter;
import com.garganttua.core.observability.ObservableEvent;
import com.garganttua.core.observability.ObservableRegistry;
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

    /**
     * Local registry for {@code api:operation:*} ObservableEvents emitted by
     * {@link #invoke(IOperationRequest)}. The bootstrap-wired
     * {@code ObservabilityBuilder} subscribes every {@code @Observer}-scanned
     * observer onto this registry at api build time via
     * {@code ObservabilityBinding.attachSource(this)}.
     */
    private final ObservableRegistry observableRegistry = new ObservableRegistry();

    public void setApi(IApi apiContext) {
        this.apiContext = apiContext;
    }

    @Override
    public void addObserver(IObserver<ObservableEvent> observer) {
        this.observableRegistry.addObserver(observer);
    }

    @Override
    public void removeObserver(IObserver<ObservableEvent> observer) {
        this.observableRegistry.removeObserver(observer);
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
    @SuppressWarnings("unchecked")
    public IOperationResponse invoke(IOperationRequest request, WorkflowExecutionOptions options) {
        ensureStarted();
        long startNanos = System.nanoTime();

        // Fast path: when nothing subscribed to our registry, skip the emit
        // scope entirely and call doInvoke directly. Production traffic that
        // opted out of @Observer pays no overhead beyond the hasObservers()
        // short-circuit on the registry.
        if (!this.observableRegistry.hasObservers()) {
            OperationResponse response = doInvoke(request, options);
            return response.withProcessingTime(java.time.Duration.ofNanos(System.nanoTime() - startNanos));
        }

        // Slow path: emit api:operation:<domain>:<op> Start/End/Error onto
        // the local registry via ObservabilityEmitter, so the bootstrap-wired
        // ObservabilityBinding's observers all see correlated events. The
        // executionId pinned here is also bound on the request so doInvoke
        // and the nested workflow share it.
        java.util.UUID executionUuid = UuidCreator.getTimeOrderedEpoch();
        request.arg(IOperationRequest.EXECUTION_UUID, executionUuid);

        com.garganttua.api.commons.operation.OperationDefinition operation =
                ((java.util.Optional<com.garganttua.api.commons.operation.OperationDefinition>)
                        request.arg(IOperationRequest.OPERATION)).orElse(null);
        String source = "api:operation:" + this.domainDefinition.domainName()
                + ":" + (operation != null ? operation.toString() : "<no-op>");

        try (var scope = ObservabilityEmitter.open(this.observableRegistry, executionUuid)) {
            scope.fireStart(source);
            try {
                OperationResponse response = doInvoke(request, options);
                java.time.Duration duration =
                        java.time.Duration.ofNanos(System.nanoTime() - startNanos);
                Integer code = response.getResponseCode() != null
                        ? response.getResponseCode().ordinal() : null;
                scope.fireEnd(source, code);
                return response.withProcessingTime(duration);
            } catch (RuntimeException e) {
                scope.fireError(source, e);
                throw e;
            }
        }
    }

    private OperationResponse doInvoke(IOperationRequest request, WorkflowExecutionOptions options) {
        if (this.workflow == null) {
            log.warn("No workflow configured for domain {}", this.domainDefinition.domainName());
            return OperationResponse.notAvailable("No workflow configured for domain: " + this.domainDefinition.domainName());
        }

        try {
            // Only generate a UUID when the caller (or the observability path
            // above) didn't already set one — keeps the start/end events
            // correlatable and lets a transport set a request-id of its own.
            @SuppressWarnings("unchecked")
            java.util.Optional<java.util.UUID> existing =
                    (java.util.Optional<java.util.UUID>) request.arg(IOperationRequest.EXECUTION_UUID);
            if (existing.isEmpty()) {
                request.arg(IOperationRequest.EXECUTION_UUID, UuidCreator.getTimeOrderedEpoch());
            }
            request.arg(IOperationRequest.API_CONTEXT, this.apiContext);
            request.arg(IOperationRequest.DOMAIN_CONTEXT, this);
            request.arg(IOperationRequest.REPOSITORY, this.repository);
            ICaller caller = request.caller();
            if (isEmptyCaller(caller)) {
                // No caller information at all on the request — materialize a
                // best-effort one based on the request body, and let
                // VERIFY_AUTHORIZATION decide whether the operation accepts
                // anonymous/auth traffic (anonymous ops pass, non-anonymous
                // ops get a clean 401 from the security script).
                caller = autoCreateCallerFromBody(request);
            } else if (caller.tenantId() == null) {
                // Caller has SOME information (super flags, ownerId, callerId,
                // …) but lacks a tenantId. This is almost always a misuse —
                // most notably the deprecated no-arg createSuperCaller()
                // which sets superTenant=true with tenantId=null. Reject
                // explicitly so the caller gets a parlant error instead of
                // silent under-isolation downstream.
                return OperationResponse.badRequest(new ApiException(
                        "Caller is missing tenantId — super and owner flags require a "
                                + "tenantId binding (use Caller.createSuperCaller(superTenantId) "
                                + "or Caller.createTenantCaller(tenantId))"));
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
                // The workflow surfaced a Throwable directly — propagate it.
                // Fallback synthesizes an ApiException when the engine produced
                // an abort with no exception (defensive — shouldn't normally
                // happen).
                Throwable cause = result.exception()
                        .orElseGet(() -> new ApiException(
                                nonBlank(result.exceptionMessage()).orElseGet(() ->
                                        "Operation '" + opLabel + "' on domain '" + domainName
                                                + "' aborted unexpectedly")));
                log.error("Workflow aborted for domain {} op {}: {}", domainName, opLabel,
                        cause.getMessage(), cause);
                return OperationResponse.error(cause);
            } else {
                // Non-zero code with NO exception attached — the `! -> CODE`
                // pattern in stage scripts catches the functional exception
                // and resets the script's lastException, so it never reaches
                // WorkflowResult. Recovery order:
                //   1. Scripts that use `! => recordCaughtException(@0, @exception) -> CODE`
                //      have stashed the original Throwable on the request under
                //      LAST_EXCEPTION_ARG; surface it verbatim (same class,
                //      same message).
                //   2. Otherwise replay the script-side check Java-side when
                //      the failing stage is identifiable (e.g. owner_rules
                //      running requireOwnerId).
                //   3. Otherwise synthesise a message-only ApiException from
                //      stage + code.
                Throwable recorded = (Throwable) request
                        .arg(com.garganttua.api.core.expression.SecurityExpressions.LAST_EXCEPTION_ARG)
                        .filter(Throwable.class::isInstance)
                        .orElse(null);
                Throwable functional = recorded != null
                        ? recorded
                        : recoverFunctionalException(result, request, opLabel, domainName);
                log.warn("Workflow returned code {} for domain {} op {}: {}",
                        result.code(), domainName, opLabel, functional.getMessage());
                return mapWorkflowCode(result.code(), functional);
            }
        } catch (Exception e) {
            log.error("Error executing workflow for domain {}: {}",
                    this.domainDefinition.domainName(), e.getMessage(), e);
            return OperationResponse.error(new ApiException(
                    "Workflow execution error on domain '"
                            + this.domainDefinition.domainName() + "': " + e.getMessage(), e));
        }
    }

    private static java.util.Optional<String> nonBlank(java.util.Optional<String> opt) {
        return opt.filter(s -> s != null && !s.isBlank());
    }

    /**
     * Builds the caller {@code Domain.invoke} will use when the incoming
     * request carries no caller information. The default is anonymous
     * ({@link Caller#createAnonymousCaller()}), but a few request shapes
     * leak enough information for us to materialize a tenant-scoped
     * pre-authentication caller:
     *
     * <ul>
     *   <li>{@link IAuthenticationRequest} body with a non-blank
     *       {@code tenantId()} → pin that tenantId on the caller so
     *       AUTHENTICATE.gs (and the user-lookup it performs) scope the
     *       login + password match to the right tenant. The caller still
     *       has no callerId / ownerId / authorities — it is not yet
     *       authenticated; it's a tenant context, not an identity.</li>
     * </ul>
     */
    static ICaller autoCreateCallerFromBody(IOperationRequest request) {
        Object body = request == null ? null
                : request.arg(IOperationRequest.BODY).orElse(null);
        if (body instanceof com.garganttua.api.commons.security.authentication.IAuthenticationRequest authReq) {
            String tenantId = authReq.tenantId();
            if (tenantId != null && !tenantId.isBlank()) {
                return new Caller(tenantId, tenantId, null, null, false, false, null);
            }
        }
        return Caller.createAnonymousCaller();
    }

    /**
     * True when the caller carries no meaningful information — every
     * identification field is null and neither super flag is set. This is
     * exactly the synthetic Caller {@code OperationRequest.caller()} produces
     * when no .caller(...) / .tenantId(...) / .ownerId(...) builder call ran.
     * In that case Domain.invoke swaps in {@link #autoCreateCallerFromBody}.
     */
    static boolean isEmptyCaller(ICaller caller) {
        if (caller == null) {
            return true;
        }
        return caller.tenantId() == null
                && caller.requestedTenantId() == null
                && caller.callerId() == null
                && caller.ownerId() == null
                && !caller.superTenant()
                && !caller.superOwner()
                && (caller.authorities() == null || caller.authorities().isEmpty());
    }

    private static String resolveOperationLabel(IOperationRequest request) {
        return request.arg(IOperationRequest.OPERATION)
                .map(op -> op.getBusinessOperation())
                .map(bo -> bo.getLabel())
                .orElse("unknown");
    }

    /**
     * Recovers the functional exception that the script-side guard would have
     * thrown, by replaying the same check Java-side. This addresses the gap
     * mon général flagged on 2026-05-19: a stage script doing
     * {@code ! -> CODE} catches the original exception and ends the script
     * with a non-zero code, but garganttua-core's {@code Workflow.execute}
     * clears {@code lastException} on non-aborted exits — so the original
     * message ("Owner ID is required for this operation") never reaches
     * {@code WorkflowResult.exceptionMessage()}.
     *
     * <p>By identifying the failing stage from
     * {@code _<stage>_<script>_code} variables (populated by
     * {@code Workflow.collectVariables} regardless of script outcome), we
     * can invoke the matching Java-side validator and produce <strong>the
     * exact same {@link ApiException}</strong> the script would have
     * raised — same wording, same type. For stages we cannot replay, a
     * synthesized {@code ApiException} carries the best message we can
     * build from the stage hint and the response code.
     *
     * <p>An evolution proposal will be filed with garganttua-core to
     * propagate the script's last caught exception on non-aborted exits;
     * once that lands, the replay logic can shrink back to reading the
     * exception directly.
     */
    Throwable recoverFunctionalException(WorkflowResult result, IOperationRequest request,
                                         String opLabel, String domainName) {
        Integer code = result.code();
        String stage = findFailingStage(result).orElse(null);

        // 1) Stage-based replay — only works when garganttua-core's
        //    collectVariables surfaces the matching _<stage>_<script>_code
        //    variable. Per-stage code vars for stages whose names contain
        //    dashes ("tenant-rules", "verify-authorization", …) DON'T make
        //    it into result.variables() because collectVariables uses the
        //    raw stage.name() as a script-variable lookup key while
        //    ScriptGenerator sanitizes that same name when writing the
        //    script (replaces "-" with "_"). This is a garganttua-core gap
        //    — see also tryReplayValidator's notes.
        Throwable replayed = tryReplayValidator(stage, code, request);
        if (replayed != null) {
            return replayed;
        }

        // 2) Inference-based replay — independent of which stage variable
        //    the engine managed to surface. Looks at the operation's access
        //    requirements and the caller's state, then invokes the same
        //    SecurityExpressions guard the script would have invoked.
        //    Recovers the original ApiException wording verbatim.
        Throwable inferred = tryReplayFromRequestState(code, request);
        if (inferred != null) {
            return inferred;
        }

        // 3) Fallback: synthesize an ApiException with the most informative
        //    message we can build from stage + code.
        String msg = nonBlank(result.exceptionMessage())
                .orElseGet(() -> functionalMessage(stage, code, opLabel, domainName));
        return new ApiException(msg);
    }

    /**
     * Replays the validator that matches the request's state (caller +
     * operation access requirements). Catches the thrown exception so the
     * caller can hand it straight back to the response — same type, same
     * wording as the script-side guard.
     *
     * <p>This complements {@link #tryReplayValidator(String, Integer,
     * IOperationRequest)}: stage-based replay needs the engine to surface
     * a per-stage code variable, which is broken for dashed-name stages
     * (security and business rules); inference-based replay reads only the
     * caller + operation, so it works regardless of variable visibility.
     */
    static Throwable tryReplayFromRequestState(Integer code, IOperationRequest request) {
        if (request == null || code == null || code != 400) {
            return null;
        }
        ICaller caller = request.caller();
        com.garganttua.api.commons.operation.OperationDefinition op =
                request.arg(IOperationRequest.OPERATION).orElse(null);
        if (op == null) {
            return null;
        }
        com.garganttua.api.commons.operation.Access access = op.access();
        boolean needsOwner = access == com.garganttua.api.commons.operation.Access.owner;
        boolean needsTenant = needsOwner
                || access == com.garganttua.api.commons.operation.Access.tenant;
        try {
            if (needsOwner && (caller == null || caller.ownerId() == null)) {
                com.garganttua.api.core.expression.SecurityExpressions.requireOwnerId(caller);
            }
            if (needsTenant && (caller == null || caller.tenantId() == null)) {
                com.garganttua.api.core.expression.SecurityExpressions.requireTenantId(caller);
            }
        } catch (RuntimeException replayed) {
            return replayed;
        }
        return null;
    }

    /**
     * Attempts to replay the validation that the named stage's script would
     * have performed. Returns the thrown exception (so the caller can pass
     * it on), or {@code null} when no replay is wired up for this stage.
     *
     * <p>This is the bridge that recovers the lost functional message — the
     * Java-side helper throws exactly the same {@link ApiException} the
     * script would have surfaced before {@code ! -> CODE} ate it.
     */
    static Throwable tryReplayValidator(String stage, Integer code, IOperationRequest request) {
        if (stage == null || code == null || request == null) {
            return null;
        }
        ICaller caller = request.caller();
        if (caller == null) {
            return null;
        }
        try {
            if (stage.startsWith("owner_rules") && code == 400) {
                com.garganttua.api.core.expression.SecurityExpressions.requireOwnerId(caller);
                return null;
            }
            if (stage.startsWith("tenant_rules") && code == 400) {
                com.garganttua.api.core.expression.SecurityExpressions.requireTenantId(caller);
                return null;
            }
        } catch (RuntimeException replayed) {
            // This IS the original exception — same class, same message.
            return replayed;
        }
        return null;
    }

    /**
     * Synthesizes a message-only fallback when replay isn't possible.
     * Uses the stage-aware hint when available, then the per-code default.
     */
    static String functionalMessage(String stage, Integer code, String opLabel, String domainName) {
        String hint = stageFunctionalHint(stage, code);
        if (hint != null) {
            return hint + " — '" + opLabel + "' on '" + domainName + "'"
                    + (code != null ? " (code " + code + ")" : "");
        }
        return defaultMessageForCode(code, opLabel, domainName);
    }

    /**
     * Scans the workflow variables for the first non-zero per-stage code.
     * Variables of the form {@code _<stage>_<script>_code} are populated by
     * garganttua-core's {@code Workflow.collectVariables} on every stage
     * execution, regardless of success or failure of the parent workflow.
     */
    static java.util.Optional<String> findFailingStage(WorkflowResult result) {
        if (result == null || result.variables() == null) {
            return java.util.Optional.empty();
        }
        return result.variables().entrySet().stream()
                .filter(e -> e.getKey() != null
                        && e.getKey().startsWith("_")
                        && e.getKey().endsWith("_code"))
                .filter(e -> e.getValue() instanceof Integer i && i != 0)
                .map(java.util.Map.Entry::getKey)
                // Strip leading underscore and trailing "_code", keep the
                // raw "<stage>_<script>" body so the hint can match prefixes
                // even when the script name differs from the stage name.
                .map(k -> k.substring(1, k.length() - "_code".length()))
                .findFirst();
    }

    /**
     * Translates a sanitized stage identifier into a functional sentence.
     * Falls through to {@code null} for unknown stages, letting the caller
     * use the generic per-code fallback instead.
     */
    static String stageFunctionalHint(String stageKey, Integer code) {
        if (stageKey == null) {
            return null;
        }
        // Stage names are slugified by the workflow engine: "-" becomes "_".
        // Match against the prefix because <stage>_<script> is collapsed
        // (e.g. "owner_rules_owner_rules").
        if (stageKey.startsWith("verify_authorization")) {
            return code != null && code == 401
                    ? "Authorization required (token missing, malformed, or rejected)"
                    : "Authorization verification failed";
        }
        if (stageKey.startsWith("verify_tenant")) {
            return "Tenant verification failed — caller's tenantId does not match the request";
        }
        if (stageKey.startsWith("verify_owner")) {
            return "Owner verification failed — caller is not the owner of the resource";
        }
        if (stageKey.startsWith("verify_authority")) {
            return "Authority check failed — caller lacks the required authority";
        }
        if (stageKey.startsWith("tenant_rules")) {
            return "Tenant rules failed — required tenantId missing on the caller";
        }
        if (stageKey.startsWith("owner_rules")) {
            return "Owner rules failed — required ownerId missing on the caller";
        }
        return null;
    }

    /**
     * Generic per-code fallback used when no failing stage can be
     * identified. Still names the operation and domain for context.
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

    private OperationResponse mapWorkflowCode(Integer code, Throwable cause) {
        return switch (code) {
            case 400 -> OperationResponse.badRequest(cause);
            case 401 -> OperationResponse.unauthorized(cause);
            case 403 -> OperationResponse.forbidden(cause);
            case 404 -> OperationResponse.notFound(cause);
            case 409 -> OperationResponse.badRequest(cause);
            default -> OperationResponse.error(cause);
        };
    }

    @Override
    public IWorkflow getWorkflow() {
        return this.workflow;
    }

}
