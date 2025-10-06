package com.garganttua.api.core;

import java.lang.reflect.Method;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.engine.Service;
import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.api.spec.interfasse.InterfaceMethod;

public class DummyInterface implements IInterface {

    @Override
    public void setEngine(IEngine engine) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setEngine'");
    }

    @Override
    public void start() throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'start'");
    }

    @Override
    public void setDomain(IDomain domain) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setDomain'");
    }

    @Override
    public void setService(Service service) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setService'");
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

    @Override
    public Method getMethod(InterfaceMethod method) throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMethod'");
    }

}
