package com.garganttua.api.core.domain;

import java.lang.reflect.Method;

import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.interfasse.GGAPIInterfaceMethod;
import com.garganttua.api.spec.interfasse.IGGAPIInterface;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;
import com.garganttua.reflection.beans.annotation.GGBean;

@GGBean(name = "test")
public class TestInterface implements IGGAPIInterface {

	@Override
	public void setEngine(IGGAPIEngine engine) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() throws GGAPIException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDomain(IGGAPIDomain domain) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setService(IGGAPIService service) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Method getMethod(GGAPIInterfaceMethod method) throws GGAPIEntityException {
		try {
			return this.getClass().getDeclaredMethod("getName");
		} catch (NoSuchMethodException | SecurityException e) {
			throw new GGAPIEntityException(e);
		}
	}

	@Override
	public void addCustomService(IGGAPIServiceInfos service) {
		// TODO Auto-generated method stub
		
	}

}
