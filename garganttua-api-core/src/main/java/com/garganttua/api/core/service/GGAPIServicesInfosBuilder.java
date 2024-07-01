package com.garganttua.api.core.service;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.garganttua.api.core.domain.GGAPIDomain;
import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.service.GGAPICustomService;
import com.garganttua.api.spec.service.GGAPIReadOutputMode;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;

public class GGAPIServicesInfosBuilder {

    public static List<IGGAPIServiceInfos> buildGGAPIServices(GGAPIDomain domain, Class<?> clazz) {
    	List<IGGAPIServiceInfos> services = new ArrayList<>();
    	
		if (domain.isAllowReadAll()) {
			Class<?>[] params = {IGGAPICaller.class, GGAPIReadOutputMode.class, Integer.class,
					Integer.class, String.class, String.class, String.class};
			services.add(getInfos("getEntities", params, "/", ""));
		}
		if (domain.isAllowDeleteAll()) {
			Class<?>[] params = {IGGAPICaller.class,  String.class, String.class};
			services.add(getInfos("deleteAll", params, "/", ""));
		}
		if (domain.isAllowCreation()) {
			Class<?>[] params = {IGGAPICaller.class, String.class, String.class};
			services.add(getInfos("createEntity", params, "/", ""));
		}
		if (domain.isAllowCount()) {
			Class<?>[] params = {IGGAPICaller.class, String.class, String.class};
			services.add(getInfos("getCount", params, "/count", ""));
		}
		if (domain.isAllowReadOne()) {
			Class<?>[] params = {IGGAPICaller.class, String.class, String.class};
			services.add(getInfos("getEntity", params, "/{uuid}", ""));
		}
		if (domain.isAllowUpdateOne()) {
			Class<?>[] params = {IGGAPICaller.class, String.class, String.class, String.class};
			services.add(getInfos("updateEntity", params, "/{uuid}", ""));
		}
		if (domain.isAllowDeleteOne()) {
			Class<?>[] params = {IGGAPICaller.class, String.class, String.class};
			services.add(getInfos("deleteEntity", params, "/{uuid}", ""));
		}
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(GGAPICustomService.class)) {
                GGAPICustomService annotation = method.getAnnotation(GGAPICustomService.class);
                IGGAPIServiceInfos service = getInfos(method.getName(), method.getParameterTypes(), annotation.path(), annotation.description());
                services.add(service);
            }
        }
        return services;
    }

    private static IGGAPIServiceInfos getInfos(String methodName, Class<?>[] parameters, String path, String description) {
    	
        return new IGGAPIServiceInfos() {
            @Override
            public String getMethodName() {
                return methodName;
            }

            @Override
            public Class<?>[] getParameters() {
                return parameters;
            }

            @Override
            public String getPath() {
                return path;
            }

//            @Override
//            public String getAuthority() {
//                return annotation.authority();
//            }
            
            @Override
            public String getDescription() {
                return description;
            }

//            public IGGAPIAccessRule getAccessRule() {
//            	return new BasicGGAPIAccessRule(annotation.path().replaceAll("\\{[^}]*}", "*"), annotation.authority(), annotation.method(), annotation.access());
//            }

        };
    }

}