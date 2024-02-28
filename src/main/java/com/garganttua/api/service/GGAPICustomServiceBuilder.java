package com.garganttua.api.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpMethod;

import com.garganttua.api.security.authorization.BasicGGAPIAccessRule;
import com.garganttua.api.security.authorization.IGGAPIAccessRule;

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