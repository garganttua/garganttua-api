package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.core.service.ServiceInfos;
import com.garganttua.api.core.service.ServicesInfosBuilder;
import com.garganttua.api.core.service.IObjectInstanciator;
import com.garganttua.api.spec.EntityOperation;
import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.security.authentication.IAuthenticationInterface;
import com.garganttua.api.spec.service.IServiceInfos;

public class AuthenticationServicesInfosBuilder {
	
	public static String CONTEXT_PATH = "api";

    public static List<IServiceInfos> buildServices(IDomain domain, IAuthenticationInterface interfasse) throws EngineException {
    	List<IServiceInfos> services = new ArrayList<>();
    	
    	String baseUrl = "/"+ServicesInfosBuilder.CONTEXT_PATH+"/"+domain.getDomain();
    	
    	try {
			if (domain.isAuthenticatorEntity()) {
				services.add(getInfos(domain.getDomain(), interfasse.getClass(), interfasse.getAuthenticateMethod(), baseUrl+"/authenticate", "", EntityOperation.authenticate(domain.getDomain(), domain.getEntityClass()), () -> {return interfasse;}));
			}
		} catch (CoreException | SecurityException e) {
			throw new EngineException(e);
		}

        return services;
    }

    public static IServiceInfos getInfos(String domainName, Class<?> interfasse, Method method, String path, String description, EntityOperation operation, IObjectInstanciator instanciator) throws EngineException {
        if( method == null ) {
        	throw new EngineException(CoreExceptionCode.ENTITY_DEFINITION, "Cannot construct authentication service infos as provided method by interface is null");
        }
    	return new ServiceInfos(domainName, operation, interfasse, method, path, description, instanciator);
    }

}