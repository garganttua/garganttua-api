package com.garganttua.api.core.legacy.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.garganttua.api.core.legacy.engine.EngineException;
import com.garganttua.api.spec.EntityOperation;
import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.interfasse.InterfaceMethod;
import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.api.spec.service.IServiceInfos;

public class ServicesInfosBuilder {
	
	public static String CONTEXT_PATH = "api";

    public static List<IServiceInfos> buildServices(IDomain domain, IInterface interfasse) throws EngineException {
    	List<IServiceInfos> services = new ArrayList<>();
    	
    	String baseUrl = "/"+ServicesInfosBuilder.CONTEXT_PATH+"/"+domain.getDomain();
    	
    	try {
			if (domain.isAllowReadAll()) {
				services.add(getInfos(domain.getDomain(), interfasse.getClass(), interfasse.getMethod(InterfaceMethod.readAll), baseUrl, "", EntityOperation.readAll(domain.getDomain(), domain.getEntityClass()), () -> {return interfasse;}));
			}
			if (domain.isAllowDeleteAll()) {
				services.add(getInfos(domain.getDomain(), interfasse.getClass(), interfasse.getMethod(InterfaceMethod.deleteAll), baseUrl, "", EntityOperation.deleteAll(domain.getDomain(), domain.getEntityClass()), () -> {return interfasse;}));
			}
			if (domain.isAllowCreation()) {
				services.add(getInfos(domain.getDomain(), interfasse.getClass(), interfasse.getMethod(InterfaceMethod.createOne), baseUrl, "", EntityOperation.createOne(domain.getDomain(), domain.getEntityClass()), () -> {return interfasse;}));
			}
			if (domain.isAllowReadOne()) {
				services.add(getInfos(domain.getDomain(), interfasse.getClass(), interfasse.getMethod(InterfaceMethod.readOne), baseUrl+"/{uuid}", "", EntityOperation.readOne(domain.getDomain(), domain.getEntityClass()), () -> {return interfasse;}));
			}
			if (domain.isAllowUpdateOne()) {
				services.add(getInfos(domain.getDomain(), interfasse.getClass(), interfasse.getMethod(InterfaceMethod.updateOne), baseUrl+"/{uuid}", "", EntityOperation.updateOne(domain.getDomain(), domain.getEntityClass()), () -> {return interfasse;}));
			}
			if (domain.isAllowDeleteOne()) {
				services.add(getInfos(domain.getDomain(), interfasse.getClass(), interfasse.getMethod(InterfaceMethod.deleteOne), baseUrl+"/{uuid}", "", EntityOperation.deleteOne(domain.getDomain(), domain.getEntityClass()), () -> {return interfasse;}));
			}
		} catch (CoreException | SecurityException e) {
			throw new EngineException(e);
		}

        return services;
    }

    public static IServiceInfos getInfos(String domainName, Class<?> interfasse, Method method, String path, String description, EntityOperation operation, IObjectInstanciator instanciator) throws EngineException {
    	path = path.replace("{domain}", domainName);
    	
    	if( method == null ) {
        	throw new EngineException(CoreExceptionCode.ENTITY_DEFINITION, "Cannot construct service infos as provided method by interface is null");
        }
    	return new ServiceInfos(domainName, operation, interfasse, method, path, description, instanciator);
    }

}