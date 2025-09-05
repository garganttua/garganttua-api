package com.garganttua.api.core.engine;

import com.garganttua.api.spec.engine.IContext;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.api.spec.factory.IFactory;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.service.IService;

public class Context implements IContext {

    public static IObjectSupplier<IService> service(String className) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'bean'");
    }

    public static IObjectSupplier<IRepository> repository(String className) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'bean'");
    }

    public static IObjectSupplier<IFactory> factory(String className) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'bean'");
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

}
