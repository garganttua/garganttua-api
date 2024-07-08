package com.garganttua.api.core.domain;

import java.util.List;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.interfasse.IGGAPIInterface;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;
import com.garganttua.reflection.beans.annotation.GGBean;

@GGBean(name = "test")
public class TestInterface implements IGGAPIInterface{

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
	public void setService(IGGAPIService service, List<IGGAPIServiceInfos> serviceInfos) {
		// TODO Auto-generated method stub
		
	}

}
