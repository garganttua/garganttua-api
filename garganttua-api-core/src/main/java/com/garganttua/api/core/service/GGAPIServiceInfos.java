package com.garganttua.api.core.service;

import java.lang.reflect.Method;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;

import lombok.Getter;

public class GGAPIServiceInfos implements IGGAPIServiceInfos {
	@Getter
	private String domainName;
    @Getter
    private GGAPIEntityOperation operation;

    private Class<?> interfasse;
    @Getter
    private String path;
    @Getter
    private String description;
    @Getter
	private Method method;

    public GGAPIServiceInfos(String domainName, GGAPIEntityOperation operation, Class<?> interfasse, Method method, String path, String description) throws GGAPIEngineException {
        this.domainName = domainName;
		this.operation = operation;
		this.interfasse = interfasse;
		this.method = method;
        this.path = path;
        this.description = description;
        
        if( operation.isCustom() ) {
	        boolean found = false; 
	        for( Class<?> parameterClass: this.method.getParameterTypes() ) {
	        	if( IGGAPICaller.class.isAssignableFrom(parameterClass) ) {
	        		found = true;
	        		break;
	        	}
	        }
	        if( !found ) {
	        	throw new GGAPIEngineException(GGAPIExceptionCode.CUSTOM_SERVICE_ERROR, "The custom service "+this.method.getName()+" must have IGGAPICaller parameter" );
	        }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GGAPIServiceInfos{")
          .append("methodName='").append(this.method.getName()).append('\'')
          .append(", domain=").append(domainName)
          .append(", method=").append(operation)
          .append(", interface=").append(interfasse==null?"all":interfasse.getSimpleName())
          .append(", parameters=").append(parametersToString())
          .append(", path='").append(path).append('\'')
          .append(", description='").append(description).append('\'')
          .append('}');
        return sb.toString();
    }

    private String parametersToString() {
        Class<?>[] parameterTypes = this.method.getParameterTypes();
		if (parameterTypes == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < parameterTypes.length; i++) {
            sb.append(parameterTypes[i].getName());
            if (i < parameterTypes.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(']');
        return sb.toString();
    }

	@Override
	public Class<?> getInterface() {
		return this.interfasse;
	}

	@Override
	public Class<?>[] getParameters() {
		return this.method.getParameterTypes();
	}

	@Override
	public String getMethodName() {
		return this.method.getName();
	}
}
