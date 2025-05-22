package com.garganttua.api.core.service;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

public class GGAPIMethodConciliator {

	private final Method method;
    private IGGAPICaller caller;
    private Map<String, String> customParameters = new HashMap<>();
    private String referencePath;
    private String valuedPath;
    private byte[] body;

    public GGAPIMethodConciliator(Method method) {
        this.method = method;
    }

    public GGAPIMethodConciliator setCaller(IGGAPICaller caller) {
        this.caller = caller;
        return this;
    }

    public GGAPIMethodConciliator setCustomParameters(Map<String, String> customParameters) {
        this.customParameters = customParameters;
        return this;
    }

    public GGAPIMethodConciliator setReferencePath(String path) {
        this.referencePath = path;
        return this;
    }

    public GGAPIMethodConciliator setValuedPath(String servletPath) {
        this.valuedPath = servletPath;
        return this;
    }

    public GGAPIMethodConciliator setBody(byte[] body) {
        this.body = body;
        return this;
    }

    public Object[] getParameters() {
        Map<String, String> pathParameters = extractPathParameters(valuedPath, referencePath);

        Map<String, Class<?>> methodParameters = extractMethodParameters(method);

        Object[] parameters = new Object[methodParameters.size()];
        int index = 0;

        for (String paramName : methodParameters.keySet()) {
            Class<?> type = methodParameters.get(paramName);

            Object value = null;
            if( type.isAssignableFrom(IGGAPICaller.class) ) {
            	value = caller;
            }
            if( GGObjectReflectionHelper.isMapOfString(type) ) {
            	value = customParameters;
            }
            if( paramName.equals("body") ) {
            	value = this.body;
            }
            if( pathParameters.get(paramName) != null ) {
            	value = convertValue(pathParameters.get(paramName), type);
            }

            parameters[index++] = value;
        }

        return parameters;
    }

    private Map<String, String> extractPathParameters(String servletPath, String referencePath) {
        Map<String, String> pathParameters = new HashMap<>();

        String[] actualParts = servletPath.split("/");
        String[] referenceParts = referencePath.split("/");

        for (int i = 0; i < referenceParts.length; i++) {
            if (referenceParts[i].startsWith("{")) {
                String paramName = referenceParts[i].replaceAll("[{}]", "");
                pathParameters.put(paramName, actualParts[i]);
            }
        }

        return pathParameters;
    }

    private Map<String, Class<?>> extractMethodParameters(Method method) {
        Map<String, Class<?>> methodParameters = new LinkedHashMap<>();
        
        var params = method.getParameters();
        for (var param : params) {
            methodParameters.put(param.getName(), param.getType());
        }

        return methodParameters;
    }

    private Object convertValue(String value, Class<?> type) {
        switch (type.getName()) {
            case "java.lang.String":
                return value;
            case "int":
            case "java.lang.Integer":
                return Integer.valueOf(value);
            case "long":
            case "java.lang.Long":
                return Long.valueOf(value);
            case "boolean":
            case "java.lang.Boolean":
                return Boolean.valueOf(value);
            case "double":
            case "java.lang.Double":
                return Double.valueOf(value);
            default:
                throw new IllegalArgumentException("Unsupported parameter type: " + type);
        }
    }

}
