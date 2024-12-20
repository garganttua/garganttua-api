package com.garganttua.api.core.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
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
				services.add(getInfos(domain.getDomain(), interfasse.getClass(), interfasse.getMethod(GGAPIInterfaceMethod.readAll), baseUrl, "", GGAPIEntityOperation.readAll(domain.getDomain(), domain.getEntityClass()), () -> {return interfasse;}));
			}
			if (domain.isAllowDeleteAll()) {
				services.add(getInfos(domain.getDomain(), interfasse.getClass(), interfasse.getMethod(GGAPIInterfaceMethod.deleteAll), baseUrl, "", GGAPIEntityOperation.deleteAll(domain.getDomain(), domain.getEntityClass()), () -> {return interfasse;}));
			}
			if (domain.isAllowCreation()) {
				services.add(getInfos(domain.getDomain(), interfasse.getClass(), interfasse.getMethod(GGAPIInterfaceMethod.createOne), baseUrl, "", GGAPIEntityOperation.createOne(domain.getDomain(), domain.getEntityClass()), () -> {return interfasse;}));
			}
			if (domain.isAllowReadOne()) {
				services.add(getInfos(domain.getDomain(), interfasse.getClass(), interfasse.getMethod(GGAPIInterfaceMethod.readOne), baseUrl+"/{uuid}", "", GGAPIEntityOperation.readOne(domain.getDomain(), domain.getEntityClass()), () -> {return interfasse;}));
			}
			if (domain.isAllowUpdateOne()) {
				services.add(getInfos(domain.getDomain(), interfasse.getClass(), interfasse.getMethod(GGAPIInterfaceMethod.updateOne), baseUrl+"/{uuid}", "", GGAPIEntityOperation.updateOne(domain.getDomain(), domain.getEntityClass()), () -> {return interfasse;}));
			}
			if (domain.isAllowDeleteOne()) {
				services.add(getInfos(domain.getDomain(), interfasse.getClass(), interfasse.getMethod(GGAPIInterfaceMethod.deleteOne), baseUrl+"/{uuid}", "", GGAPIEntityOperation.deleteOne(domain.getDomain(), domain.getEntityClass()), () -> {return interfasse;}));
			}
		} catch (GGAPIException | SecurityException e) {
			throw new GGAPIEngineException(e);
		}

        return services;
    }

    public static IGGAPIServiceInfos getInfos(String domainName, Class<?> interfasse, Method method, String path, String description, GGAPIEntityOperation operation, IGGAPIObjectInstanciator instanciator) throws GGAPIEngineException {
    	path = path.replace("{domain}", domainName);
    	
    	if( method == null ) {
        	throw new GGAPIEngineException(GGAPIExceptionCode.ENTITY_DEFINITION, "Cannot construct service infos as provided method by interface is null");
        }
    	return new GGAPIServiceInfos(domainName, operation, interfasse, method, path, description, instanciator);
    }

}