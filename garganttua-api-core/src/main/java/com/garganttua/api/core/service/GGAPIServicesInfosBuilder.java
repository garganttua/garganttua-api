package com.garganttua.api.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.pageable.IGGAPIPageable;
import com.garganttua.api.spec.service.GGAPIReadOutputMode;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;
import com.garganttua.api.spec.sort.IGGAPISort;

public class GGAPIServicesInfosBuilder {
	
	public static String CONTEXT_PATH = "api";

    public static List<IGGAPIServiceInfos> buildGGAPIServices(IGGAPIDomain domain) throws GGAPIEngineException {
    	List<IGGAPIServiceInfos> services = new ArrayList<>();
    	
    	String baseUrl = "/"+GGAPIServicesInfosBuilder.CONTEXT_PATH+"/"+domain.getDomain();
    	
		if (domain.isAllowReadAll()) {
			Class<?>[] params = {IGGAPICaller.class, GGAPIReadOutputMode.class, IGGAPIPageable.class, IGGAPIFilter.class, IGGAPISort.class, Map.class};
			services.add(getInfos(domain.getDomain(), "getEntities", null, params, baseUrl, "", GGAPIEntityOperation.readAll(domain.getDomain(), domain.getEntity().getValue0())));
		}
		if (domain.isAllowDeleteAll()) {
			Class<?>[] params = {IGGAPICaller.class,  IGGAPIFilter.class, Map.class};
			services.add(getInfos(domain.getDomain(), "deleteAll", null, params, baseUrl, "", GGAPIEntityOperation.deleteAll(domain.getDomain(), domain.getEntity().getValue0())));
		}
		if (domain.isAllowCreation()) {
			Class<?>[] params = {IGGAPICaller.class, Object.class, Map.class};
			services.add(getInfos(domain.getDomain(), "createEntity", null, params, baseUrl, "", GGAPIEntityOperation.createOne(domain.getDomain(), domain.getEntity().getValue0())));
		}
		if (domain.isAllowReadOne()) {
			Class<?>[] params = {IGGAPICaller.class, String.class, Map.class};
			services.add(getInfos(domain.getDomain(), "getEntity", null, params, baseUrl+"/{uuid}", "", GGAPIEntityOperation.readOne(domain.getDomain(), domain.getEntity().getValue0())));
		}
		if (domain.isAllowUpdateOne()) {
			Class<?>[] params = {IGGAPICaller.class, String.class, Object.class, Map.class};
			services.add(getInfos(domain.getDomain(), "updateEntity", null, params, baseUrl+"/{uuid}", "", GGAPIEntityOperation.updateOne(domain.getDomain(), domain.getEntity().getValue0())));
		}
		if (domain.isAllowDeleteOne()) {
			Class<?>[] params = {IGGAPICaller.class, String.class, Map.class};
			services.add(getInfos(domain.getDomain(), "deleteEntity", null, params, baseUrl+"/{uuid}", "", GGAPIEntityOperation.deleteOne(domain.getDomain(), domain.getEntity().getValue0())));
		}

        return services;
    }

    public static IGGAPIServiceInfos getInfos(String domainName, String methodName, Class<?> interfasse, Class<?>[] parameters, String path, String description, GGAPIEntityOperation operation) throws GGAPIEngineException {
        return new GGAPIServiceInfos(domainName, methodName, operation, interfasse, parameters, path, description);
    }

}