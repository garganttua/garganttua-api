package com.garganttua.api.core.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.pageable.IGGAPIPageable;
import com.garganttua.api.spec.service.GGAPICustomService;
import com.garganttua.api.spec.service.GGAPIReadOutputMode;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;
import com.garganttua.api.spec.sort.IGGAPISort;

public class GGAPIServicesInfosBuilder {

    public static List<IGGAPIServiceInfos> buildGGAPIServices(IGGAPIDomain domain, Class<? extends IGGAPIService> clazz) {
    	List<IGGAPIServiceInfos> services = new ArrayList<>();
    	
    	String baseUrl = "/"+domain.getDomain();
    	
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
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(GGAPICustomService.class)) {
                GGAPICustomService annotation = method.getAnnotation(GGAPICustomService.class);
                IGGAPIServiceInfos service = getInfos(method.getName(), method.getParameterTypes(), annotation.path(), annotation.description(), annotation.operation());
                services.add(service);
            }
        }
        return services;
    }

    private static IGGAPIServiceInfos getInfos(String methodName, Class<?>[] parameters, String path, String description, GGAPIEntityOperation operation) {
        return new GGAPIServiceInfos(methodName, operation, parameters, path, description);
    }

}