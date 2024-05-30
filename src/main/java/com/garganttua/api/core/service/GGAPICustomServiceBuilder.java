package com.garganttua.api.core.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.garganttua.api.core.security.authorization.BasicGGAPIAccessRule;
import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.service.GGAPICustomService;
import com.garganttua.api.spec.service.IGGAPICustomService;

public class GGAPICustomServiceBuilder {

    public static List<IGGAPICustomService> buildGGAPIServices(Class<?> clazz) {
        List<IGGAPICustomService> services = new ArrayList<>();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(GGAPICustomService.class)) {
                GGAPICustomService annotation = method.getAnnotation(GGAPICustomService.class);
                IGGAPICustomService service = buildGGAPIService(method.getName(), method.getParameterTypes(), annotation);
                services.add(service);
            }
        }
        return services;
    }

    private static IGGAPICustomService buildGGAPIService(String methodName, Class<?>[] parameters, GGAPICustomService annotation) {
    	
        return new IGGAPICustomService() {
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
                return annotation.path();
            }

            @Override
            public String getAuthority() {
                return annotation.authority();
            }
            
            @Override
            public String getDescription() {
                return annotation.description();
            }

            public IGGAPIAccessRule getAccessRule() {
            	return new BasicGGAPIAccessRule(annotation.path().replaceAll("\\{[^}]*}", "*"), annotation.authority(), annotation.method(), annotation.access());
            }

        };
    }

}