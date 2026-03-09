package com.garganttua.api.core.context.application;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.garganttua.api.core.context.OperationResponse;
import com.garganttua.api.core.context.Repository;
import com.garganttua.api.core.definition.DomainDefinition;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.BusinessOperation;
import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.context.IDtoContext;
import com.garganttua.api.spec.context.IEntityContext;
import com.garganttua.api.spec.definition.IDomainDefinition;
import com.garganttua.api.spec.event.IEventPublisher;
import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.core.injection.BeanDefinition;
import com.garganttua.api.spec.security.IDomainSecurityContext;
import com.garganttua.api.spec.service.IOperationRequest;
import com.garganttua.api.spec.service.IOperationResponse;
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
public class DomainContext<E> extends AbstractLifecycle implements IDomainContext<E> {

    private final DomainDefinition<E> domainDefinition;
    private final List<ISupplier<IInterface>> interfaces;
    private final List<ISupplier<IEventPublisher>> events;
    private final IDomainSecurityContext domainSecurityContext;

    private final IEntityContext<E> entityContext;
    private final List<IDtoContext<?>> dtoContexts;
    @Getter
    private final IRepository repository;

    // Workflows map (replaces ScriptCache + crudScripts)
    private Map<String, IWorkflow> workflows = Collections.emptyMap();

    // Bean definition for runtime DI injection on entities
    @Getter
    private BeanDefinition<?> entityBeanDefinition;
    @Getter
    private boolean doInjection;

    private IApiContext apiContext;

    public void setApiContext(IApiContext apiContext) {
        this.apiContext = apiContext;
    }

    public void setEntityBeanDefinition(BeanDefinition<?> entityBeanDefinition) {
        this.entityBeanDefinition = entityBeanDefinition;
    }

    public void setDoInjection(boolean doInjection) {
        this.doInjection = doInjection;
    }

    public void setWorkflows(Map<String, IWorkflow> workflows) {
        this.workflows = Collections.unmodifiableMap(new HashMap<>(
                Objects.requireNonNull(workflows, "Workflows cannot be null")));
    }

    public DomainContext(DomainDefinition<E> domainDefinition, IEntityContext<E> entityContext,
            IDomainSecurityContext domainSecurityContext,
            List<IDtoContext<?>> dtoContexts,
            List<ISupplier<IInterface>> interfaces,
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
        repo.setDomainContext(this);
        this.repository = repo;
    }

    @Override
    public IReflection reflection() {
        return DefaultMapper.reflection();
    }

    @Override
    protected ILifecycle doInit() {
        log.info("Initializing domain context: {}", this.domainDefinition.domainName());

        // 1. Log loaded workflows
        log.debug("Loaded {} workflows for domain {}", this.workflows.size(), this.domainDefinition.domainName());

        // 3. Build interfaces, pass domain context (with access rules), and init them
        initializeInterfaces();

        return this;
    }

    private void initializeInterfaces() {

        doForAllInterfaces(IInterface::handle, this);
        doForAllInterfaces(IInterface::onInit);

        log.debug("Initialized {} interfaces for domain {}", this.interfaces.size(),
                this.domainDefinition.domainName());
    }

    /**
     * Functional interface for interface actions that can throw exceptions.
     */
    @FunctionalInterface
    private interface InterfaceAction {
        void apply(IInterface intf) throws Exception;
    }

    /**
     * Functional interface for interface actions with an argument that can throw exceptions.
     */
    @FunctionalInterface
    private interface InterfaceActionWithArg<T> {
        void apply(IInterface intf, T arg) throws Exception;
    }

    /**
     * Executes an action on all built interfaces.
     *
     * @param action the action to execute on each interface
     */
    private void doForAllInterfaces(InterfaceAction action) {
        for (ISupplier<IInterface> supplier : this.interfaces) {
            try {
                IInterface intf = supplier.supply()
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
        for (ISupplier<IInterface> supplier : this.interfaces) {
            try {
                IInterface intf = supplier.supply()
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

        // Execute startup binders
        List<IMethodBinder<Void>> startupBinders = this.domainDefinition.startupBinders();
        if (startupBinders != null && !startupBinders.isEmpty()) {
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

        // Start all interfaces
        doForAllInterfaces(IInterface::onStart);

        log.debug("Started {} interfaces for domain {}", this.interfaces.size(),
                this.domainDefinition.domainName());

        return this;
    }

    @Override
    protected ILifecycle doStop() {
        log.info("Stopping domain context: {}", this.domainDefinition.domainName());

        // Stop all interfaces
        doForAllInterfaces(IInterface::onStop);
        log.debug("Stopped {} interfaces for domain {}", this.interfaces.size(),
                this.domainDefinition.domainName());

        return this;
    }

    @Override
    protected ILifecycle doFlush() {
        log.info("Flushing domain context: {}", this.domainDefinition.domainName());
        // Flush all interfaces
        doForAllInterfaces(IInterface::onFlush);
        log.debug("Flushed {} interfaces for domain {}", this.interfaces.size(),
                this.domainDefinition.domainName());
        return this;
    }

    @Override
    public IDomainDefinition<E> getDomainDefinition() {
        return this.domainDefinition;
    }

    @Override
    public IOperationResponse invoke(IOperationRequest request) {
        return invoke(request, WorkflowExecutionOptions.none());
    }

    @Override
    public IOperationResponse invoke(IOperationRequest request, WorkflowExecutionOptions options) {
        ensureStarted();

        BusinessOperation businessOp = request.operation().getBusinessOperation();

        // Map business operation to workflow name
        String workflowName = resolveWorkflowName(businessOp);

        IWorkflow workflow = this.workflows.get(workflowName);
        if (workflow == null) {
            log.warn("No workflow found for operation {} (workflow name: {}) in domain {}",
                    businessOp, workflowName, this.domainDefinition.domainName());
            return OperationResponse.notAvailable("No workflow available for operation: " + businessOp);
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
            workflowParams.put("repository", this.repository);
            workflowParams.put("domainContext", this);
            WorkflowInput input = WorkflowInput.of(request, workflowParams);
            WorkflowResult result = workflow.execute(input, options);

            if (result.isSuccess()) {
                return OperationResponse.ok(result.output());
            } else if (result.hasAborted()) {
                String errorMsg = result.exceptionMessage().orElse("Workflow execution failed");
                log.error("Workflow {} failed for domain {}: {}", workflowName,
                        this.domainDefinition.domainName(), errorMsg);
                return OperationResponse.error(errorMsg);
            } else {
                String errorMsg = result.exceptionMessage().orElse("Workflow execution failed");
                log.warn("Workflow {} returned code {} for domain {}: {}", workflowName,
                        result.code(), this.domainDefinition.domainName(), errorMsg);
                return mapWorkflowCode(result.code(), errorMsg);
            }
        } catch (Exception e) {
            log.error("Error executing workflow {} for domain {}: {}", workflowName,
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
            default -> OperationResponse.error(message);
        };
    }

    private String resolveWorkflowName(BusinessOperation businessOp) {
        return switch (businessOp) {
            case create -> BusinessOperation.create.getLabel();
            case readAll -> BusinessOperation.readAll.getLabel();
            case readOne -> BusinessOperation.readOne.getLabel();
            case update -> BusinessOperation.update.getLabel();
            case deleteOne -> BusinessOperation.deleteOne.getLabel();
            case deleteAll -> BusinessOperation.deleteAll.getLabel();
            case workflow -> businessOp.getLabel();
            default -> businessOp.getLabel();
        };
    }

    @Override
    public Optional<IWorkflow> getWorkflow(String name) {
        return Optional.ofNullable(this.workflows.get(name));
    }

    @Override
    public Map<String, IWorkflow> getWorkflows() {
        return Collections.unmodifiableMap(this.workflows);
    }

}
