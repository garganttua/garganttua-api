package com.garganttua.api.core.service;

import com.garganttua.api.spec.service.GGAPIServiceMethod;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;

import lombok.Getter;

public class GGAPIServiceInfos implements IGGAPIServiceInfos {
    @Getter
	private String methodName;
    @Getter
    private GGAPIServiceMethod method;
    @Getter
    private Class<?>[] parameters;
    @Getter
    private String path;
    @Getter
    private String description;

    public GGAPIServiceInfos(String methodName, GGAPIServiceMethod method, Class<?>[] parameters, String path, String description) {
        this.methodName = methodName;
		this.method = method;
        this.parameters = parameters;
        this.path = path;
        this.description = description;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GGAPIServiceInfos{")
          .append("methodName='").append(methodName).append('\'')
          .append(", method=").append(method)
          .append(", parameters=").append(parametersToString())
          .append(", path='").append(path).append('\'')
          .append(", description='").append(description).append('\'')
          .append('}');
        return sb.toString();
    }

    private String parametersToString() {
        if (parameters == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < parameters.length; i++) {
            sb.append(parameters[i].getName());
            if (i < parameters.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(']');
        return sb.toString();
    }
}