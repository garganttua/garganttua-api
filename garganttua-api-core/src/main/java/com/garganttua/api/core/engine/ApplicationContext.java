package com.garganttua.api.core.engine;

import com.garganttua.api.spec.engine.IApplicationContext;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;
import com.garganttua.api.spec.engine.ISupplyObject;
import com.garganttua.api.spec.factory.IFactory;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.service.IService;

public class ApplicationContext implements IApplicationContext {

    public static IApplicationContext context;

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
            ISupplyObject<Supplied, IApplicationContext> supply = null;

            IObjectSupplierBuilder<Supplied> builder = new ApplicationContextObjectSupplierBuilder<>(supply, beanClass);

            return builder;
        }

        public static <Supplied> IObjectSupplierBuilder<Supplied> bean(String supplier, Class<Supplied> beanClass) {
            ISupplyObject<Supplied, IApplicationContext> supply = null;

            IObjectSupplierBuilder<Supplied> builder = new ApplicationContextObjectSupplierBuilder<>(supply, beanClass);

            return builder;
        }

        public static IObjectSupplierBuilder<IService> service(String domainName) {
            ISupplyObject<IService, IApplicationContext> supply = null;

            IObjectSupplierBuilder<IService> builder = new ApplicationContextObjectSupplierBuilder<>(supply,
                    IService.class);

            return builder;
        }

        public static IObjectSupplierBuilder<IRepository> repository(String domainName) {
            ISupplyObject<IRepository, IApplicationContext> supply = null;

            IObjectSupplierBuilder<IRepository> builder = new ApplicationContextObjectSupplierBuilder<>(supply,
                    IRepository.class);

            return builder;
        }

        public static IObjectSupplierBuilder<IFactory> factory(String domainName) {
            ISupplyObject<IFactory, IApplicationContext> supply = null;

            IObjectSupplierBuilder<IFactory> builder = new ApplicationContextObjectSupplierBuilder<>(supply,
                    IFactory.class);

            return builder;
        }

    }

}
