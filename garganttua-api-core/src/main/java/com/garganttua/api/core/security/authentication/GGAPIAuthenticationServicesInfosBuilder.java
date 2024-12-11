package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.service.GGAPIServiceInfos;
import com.garganttua.api.core.service.GGAPIServicesInfosBuilder;
import com.garganttua.api.core.service.IGGAPIObjectInstanciator;
import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationInterface;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;

public class GGAPIAuthenticationServicesInfosBuilder {
	
	public static String CONTEXT_PATH = "api";

    public static List<IGGAPIServiceInfos> buildGGAPIServices(IGGAPIDomain domain, IGGAPIAuthenticationInterface interfasse) throws GGAPIEngineException {
    	List<IGGAPIServiceInfos> services = new ArrayList<>();
    	
    	String baseUrl = "/"+GGAPIServicesInfosBuilder.CONTEXT_PATH+"/"+domain.getDomain();
    	
    	try {
			if (domain.getSecurity().isAuthenticatorEntity()) {
				services.add(getInfos(domain.getDomain(), interfasse.getClass(), interfasse.getAuthenticateMethod(), baseUrl+"/authenticate", "", GGAPIEntityOperation.authenticate(domain.getDomain(), domain.getEntity().getValue0()), () -> {return interfasse;}));
			}
		} catch (GGAPIException | SecurityException e) {
			throw new GGAPIEngineException(e);
		}

        return services;
    }

    public static IGGAPIServiceInfos getInfos(String domainName, Class<?> interfasse, Method method, String path, String description, GGAPIEntityOperation operation, IGGAPIObjectInstanciator instanciator) throws GGAPIEngineException {
        if( method == null ) {
        	throw new GGAPIEngineException(GGAPIExceptionCode.ENTITY_DEFINITION, "Cannot construct authentication service infos as provided method by interface is null");
        }
    	return new GGAPIServiceInfos(domainName, operation, interfasse, method, path, description, instanciator);
    }

}