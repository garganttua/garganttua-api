package com.garganttua.api.core.context.application;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.garganttua.api.core.builder.supplier.ApplicationContextObjectSupplierBuilder;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IApplicationContext;
import com.garganttua.api.spec.engine.IApplicationContextStartupBinderBuilder;
import com.garganttua.api.spec.engine.IApplicationSecurityContext;
import com.garganttua.api.spec.engine.IDomainContext;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;
import com.garganttua.api.spec.engine.ISupplyObject;
import com.garganttua.api.spec.factory.IFactory;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.service.IDomainServiceRuntime;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;
import com.garganttua.reflection.properties.IGGPropertyLoader;

public class ApplicationContext implements IApplicationContext {

    public static IApplicationContext context;
    private IGGBeanLoader loader;
    private List<String> packages;
    private IGGPropertyLoader propLoader;
    private IGGInjector injector;
    private String superTenantId;
    private List<IApplicationContextStartupBinderBuilder> startupBinderBuilders;
    private boolean autoCreateSuperTenant;
    private IApplicationSecurityContext securityContext;
    private Collection<IDomainContext> domainContexts;

    public ApplicationContext(IGGBeanLoader loader, List<String> packages, IGGPropertyLoader propLoader,
            IGGInjector injector, String superTenantId,
            List<IApplicationContextStartupBinderBuilder> startupBinderBuilders, boolean autoCreateSuperTenant,
            IApplicationSecurityContext securityContext, Collection<IDomainContext> domainContexts) {
        this.loader = Objects.requireNonNull(loader, "Loader cannot be null");
        this.packages = Objects.requireNonNull(packages, "Packages cannot be null");
        this.propLoader = Objects.requireNonNull(propLoader, "Prop loader cannot be null");
        this.injector = Objects.requireNonNull(injector, "Injector cannot be null");
        this.superTenantId = Objects.requireNonNull(superTenantId, "Super tenant id cannot be null");
        this.startupBinderBuilders = Objects.requireNonNull(startupBinderBuilders,
                "Startup Binder Builder cannot be null");
        this.autoCreateSuperTenant = autoCreateSuperTenant;
        this.securityContext = Objects.requireNonNull(securityContext, "Security context cannot be null");
        this.domainContexts = Objects.requireNonNull(domainContexts, "Domain contexts cannot be null");

    }

    public ApplicationContext() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void start() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'start'");
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'stop'");
    }

    @Override
    public void flush() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'flush'");
    }

    @Override
    public void init() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'init'");
    }

    @Override
    public void reload() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'reload'");
    }

    public class Suppliers {

        public static <Supplied> IObjectSupplierBuilder<Supplied> bean(Class<Supplied> beanClass) {
            ISupplyObject<Supplied, IApplicationContext> supply = (context) -> Optional.empty();

            IObjectSupplierBuilder<Supplied> builder = new ApplicationContextObjectSupplierBuilder<>(supply, beanClass);

            return builder;
        }

        public static <Supplied> IObjectSupplierBuilder<Supplied> bean(String supplier, Class<Supplied> beanClass) {
            ISupplyObject<Supplied, IApplicationContext> supply = (context) -> Optional.empty();

            IObjectSupplierBuilder<Supplied> builder = new ApplicationContextObjectSupplierBuilder<>(supply, beanClass);

            return builder;
        }

        public static IObjectSupplierBuilder<IDomainServiceRuntime> service(String domainName) {
            ISupplyObject<IDomainServiceRuntime, IApplicationContext> supply = (context) -> Optional.empty();

            IObjectSupplierBuilder<IDomainServiceRuntime> builder = new ApplicationContextObjectSupplierBuilder<>(
                    supply,
                    IDomainServiceRuntime.class);

            return builder;
        }

        public static IObjectSupplier<IRepository> repository(String domainName) throws CoreException {
            ISupplyObject<IRepository, IApplicationContext> supply = (context) -> {
                Optional<IDomainContext> dContext = context.getDomainContext(domainName);
                if( dContext.isPresent() ){
                    return Optional.of(dContext.get().getRepository());
                }
                return null;
            };

            IObjectSupplierBuilder<IRepository> builder = new ApplicationContextObjectSupplierBuilder<>(supply,
                    IRepository.class);

            return builder.build();
        }

        public static IObjectSupplierBuilder<IFactory> factory(String domainName) {
            ISupplyObject<IFactory, IApplicationContext> supply = (context) -> Optional.empty();

            IObjectSupplierBuilder<IFactory> builder = new ApplicationContextObjectSupplierBuilder<>(supply,
                    IFactory.class);

            return builder;
        }

    }

    @Override
    public Optional<IDomainContext> getDomainContext(String domainName) {
        return this.domainContexts.stream().filter(domain -> domain.getDomainName().equals(domainName)).findFirst();
    }

}
