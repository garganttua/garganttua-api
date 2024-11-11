package com.garganttua.api.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public static List<IGGAPIServiceInfos> buildGGAPIServices(IGGAPIDomain domain) {
    	List<IGGAPIServiceInfos> services = new ArrayList<>();
    	
    	String baseUrl = "/"+GGAPIServicesInfosBuilder.CONTEXT_PATH+"/"+domain.getDomain();
    	
		if (domain.isAllowReadAll()) {
			Class<?>[] params = {IGGAPICaller.class, GGAPIReadOutputMode.class, IGGAPIPageable.class, IGGAPIFilter.class, IGGAPISort.class, Map.class};
			services.add(getInfos("getEntities", params, baseUrl, "", GGAPIEntityOperation.read_all));
		}
		if (domain.isAllowDeleteAll()) {
			Class<?>[] params = {IGGAPICaller.class,  IGGAPIFilter.class, Map.class};
			services.add(getInfos("deleteAll", params, baseUrl, "", GGAPIEntityOperation.delete_all));
		}
		if (domain.isAllowCreation()) {
			Class<?>[] params = {IGGAPICaller.class, Object.class, Map.class};
			services.add(getInfos("createEntity", params, baseUrl, "", GGAPIEntityOperation.create_one));
		}
		if (domain.isAllowReadOne()) {
			Class<?>[] params = {IGGAPICaller.class, String.class, Map.class};
			services.add(getInfos("getEntity", params, baseUrl+"/{uuid}", "", GGAPIEntityOperation.read_one));
		}
		if (domain.isAllowUpdateOne()) {
			Class<?>[] params = {IGGAPICaller.class, String.class, Object.class, Map.class};
			services.add(getInfos("updateEntity", params, baseUrl+"/{uuid}", "", GGAPIEntityOperation.update_one));
		}
		if (domain.isAllowDeleteOne()) {
			Class<?>[] params = {IGGAPICaller.class, String.class, Map.class};
			services.add(getInfos("deleteEntity", params, baseUrl+"/{uuid}", "", GGAPIEntityOperation.delete_one));
		}

        return services;
    }

    public static IGGAPIServiceInfos getInfos(String methodName, Class<?>[] parameters, String path, String description, GGAPIEntityOperation operation) {
        return new GGAPIServiceInfos(methodName, operation, parameters, path, description);
    }

}