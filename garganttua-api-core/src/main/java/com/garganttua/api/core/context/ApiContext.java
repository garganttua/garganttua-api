package com.garganttua.api.core.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.core.injection.BeanReference;
import com.garganttua.core.injection.DiException;
import com.garganttua.core.injection.IInjectionContext;
import com.garganttua.core.injection.Predefined;
import com.garganttua.core.lifecycle.AbstractLifecycle;
import com.garganttua.core.lifecycle.ILifecycle;
import com.garganttua.core.lifecycle.LifecycleException;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.runtime.RuntimeClass;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiContext extends AbstractLifecycle implements IApiContext {

    private final IInjectionContext injectionContext;
    private final Map<String, IDomainContext<?>> domainContexts;
    private final String superTenantId;
    private final boolean superTenantAutoCreate;
    private final List<IMethodBinder<Void>> startupBinders;

    public ApiContext(IInjectionContext injectionContext, Map<String, IDomainContext<?>> domainContexts,
            String superTenantId, boolean superTenantAutoCreate, List<IMethodBinder<Void>> startupBinders) {
        this.injectionContext = Objects.requireNonNull(injectionContext, "Injection context cannot be null");
        this.domainContexts = Collections.unmodifiableMap(new HashMap<>(
                Objects.requireNonNull(domainContexts, "Domain contexts cannot be null")));
        this.superTenantId = superTenantId;
        this.superTenantAutoCreate = superTenantAutoCreate;
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
                            RuntimeClass.of(IRepository.class),
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
        // Start the injection context
        this.injectionContext.onStart();

        // Start all domain contexts
        for (Map.Entry<String, IDomainContext<?>> entry : this.domainContexts.entrySet()) {
            String domainName = entry.getKey();
            IDomainContext<?> domainContext = entry.getValue();
            try {
                domainContext.onStart();
                log.info("Started domain '{}'", domainName);
            } catch (LifecycleException e) {
                log.error("Failed to start domain '{}': {}", domainName, e.getMessage());
            }
        }

        // Execute startup binders
        for (IMethodBinder<Void> binder : this.startupBinders) {
            try {
                log.trace("Executing startup binder: {}", binder.getExecutableReference());
                binder.execute();
            } catch (Exception e) {
                log.error("Failed to execute startup binder '{}': {}", binder.getExecutableReference(), e.getMessage(), e);
            }
        }

        return this;
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
