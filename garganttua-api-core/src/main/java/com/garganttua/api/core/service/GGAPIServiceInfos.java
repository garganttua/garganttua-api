package com.garganttua.api.core.service;

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
	private String methodName;
    @Getter
    private GGAPIEntityOperation operation;
    @Getter
    private Class<?>[] parameters;
    private Class<?> interfasse;
    @Getter
    private String path;
    @Getter
    private String description;

    public GGAPIServiceInfos(String domainName, String methodName, GGAPIEntityOperation operation, Class<?> interfasse, Class<?>[] parameters, String path, String description) throws GGAPIEngineException {
        this.domainName = domainName;
		this.methodName = methodName;
		this.operation = operation;
		this.interfasse = interfasse;
        this.parameters = parameters;
        this.path = path;
        this.description = description;
        
        if( operation.isCustom() ) {
	        boolean found = false; 
	        for( Class<?> parameterClass: this.parameters ) {
	        	if( IGGAPICaller.class.isAssignableFrom(parameterClass) ) {
	        		found = true;
	        		break;
	        	}
	        }
	        if( !found ) {
	        	throw new GGAPIEngineException(GGAPIExceptionCode.CUSTOM_SERVICE_ERROR, "The custom service "+this.methodName+" must have IGGAPICaller parameter" );
	        }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GGAPIServiceInfos{")
          .append("methodName='").append(methodName).append('\'')
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

	@Override
	public Class<?> getInterface() {
		return this.interfasse;
	}
}
