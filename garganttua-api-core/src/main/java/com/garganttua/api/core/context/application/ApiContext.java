package com.garganttua.api.core.context.application;

import java.util.Optional;

import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.IDomain;
import com.garganttua.core.injection.context.DiContext;

public class ApiContext extends DiContext implements IApiContext {

    @Override
    public Optional<IDomain> getDomainContext(String domainName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDomainContext'");
    }

   /*  public static IApplicationContext context;
    private IGGBeanLoader loader;
    private List<String> packages;
    private IGGPropertyLoader propLoader;
    private IGGInjector injector;
    private String superTenantId;
    private List<IApplicationContextStartupBinderBuilder> startupBinderBuilders;
    private boolean autoCreateSuperTenant;
    private IApplicationSecurityContext securityContext;
    private Collection<IDomainContext> domainContexts;

    public ApiContext(IGGBeanLoader loader, List<String> packages, IGGPropertyLoader propLoader,
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

    public class Suppliers {

        public static <Supplied> INewObjectSupplierBuilder<Supplied> newObject(Class<Supplied> beanClass) {
            ISupplyObject<Supplied, IApplicationContext> supply = (context) -> Optional.empty();

            INewObjectSupplierBuilder<Supplied> builder = new NewObjectSupplierBuilder<>(beanClass);

            return builder;
        }

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

    @Override
    public void doInjection(Object entity) throws ContextException {
        try {
            this.injector.injectBeans(entity);
            this.injector.injectProperties(entity);
        } catch (ReflectionException e) {
            throw new ContextException(e);
        }
    } */

}
