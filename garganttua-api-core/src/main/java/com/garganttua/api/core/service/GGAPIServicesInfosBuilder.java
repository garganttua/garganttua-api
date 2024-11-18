package com.garganttua.api.core.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.interfasse.GGAPIInterfaceMethod;
import com.garganttua.api.spec.interfasse.IGGAPIInterface;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;

public class GGAPIServicesInfosBuilder {
	
	public static String CONTEXT_PATH = "api";

    public static List<IGGAPIServiceInfos> buildGGAPIServices(IGGAPIDomain domain, IGGAPIInterface interfasse) throws GGAPIEngineException {
    	List<IGGAPIServiceInfos> services = new ArrayList<>();
    	
    	String baseUrl = "/"+GGAPIServicesInfosBuilder.CONTEXT_PATH+"/"+domain.getDomain();
    	
    	try {
			if (domain.isAllowReadAll()) {
				services.add(getInfos(domain.getDomain(), interfasse.getClass(), interfasse.getMethod(GGAPIInterfaceMethod.readAll), baseUrl, "", GGAPIEntityOperation.readAll(domain.getDomain(), domain.getEntity().getValue0())));
			}
			if (domain.isAllowDeleteAll()) {
				services.add(getInfos(domain.getDomain(), interfasse.getClass(), interfasse.getMethod(GGAPIInterfaceMethod.deleteAll), baseUrl, "", GGAPIEntityOperation.deleteAll(domain.getDomain(), domain.getEntity().getValue0())));
			}
			if (domain.isAllowCreation()) {
				services.add(getInfos(domain.getDomain(), interfasse.getClass(), interfasse.getMethod(GGAPIInterfaceMethod.createOne), baseUrl, "", GGAPIEntityOperation.createOne(domain.getDomain(), domain.getEntity().getValue0())));
			}
			if (domain.isAllowReadOne()) {
				services.add(getInfos(domain.getDomain(), interfasse.getClass(), interfasse.getMethod(GGAPIInterfaceMethod.readOne), baseUrl+"/{uuid}", "", GGAPIEntityOperation.readOne(domain.getDomain(), domain.getEntity().getValue0())));
			}
			if (domain.isAllowUpdateOne()) {
				services.add(getInfos(domain.getDomain(), interfasse.getClass(), interfasse.getMethod(GGAPIInterfaceMethod.updateOne), baseUrl+"/{uuid}", "", GGAPIEntityOperation.updateOne(domain.getDomain(), domain.getEntity().getValue0())));
			}
			if (domain.isAllowDeleteOne()) {
				services.add(getInfos(domain.getDomain(), interfasse.getClass(), interfasse.getMethod(GGAPIInterfaceMethod.deleteOne), baseUrl+"/{uuid}", "", GGAPIEntityOperation.deleteOne(domain.getDomain(), domain.getEntity().getValue0())));
			}
		} catch (GGAPIEngineException | SecurityException e) {
			throw new GGAPIEngineException(e);
		}

        return services;
    }

    public static IGGAPIServiceInfos getInfos(String domainName, Class<?> interfasse, Method method, String path, String description, GGAPIEntityOperation operation) throws GGAPIEngineException {
        return new GGAPIServiceInfos(domainName, operation, interfasse, method, path, description);
    }

}