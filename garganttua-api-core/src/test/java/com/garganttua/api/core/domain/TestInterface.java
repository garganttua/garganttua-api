package com.garganttua.api.core.domain;

import java.lang.reflect.Method;

import com.garganttua.api.core.entity.exceptions.EntityException;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.interfasse.InterfaceMethod;
import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.api.spec.service.IService;
import com.garganttua.reflection.beans.annotation.GGBean;

@GGBean(name = "test")
public class TestInterface implements IInterface {

	@Override
	public void setEngine(IEngine engine) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() throws CoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDomain(IDomain domain) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setService(IService service) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Method getMethod(InterfaceMethod method) throws EntityException {
		try {
			return this.getClass().getDeclaredMethod("getName");
		} catch (NoSuchMethodException | SecurityException e) {
			throw new EntityException(e);
		}
	}

}
