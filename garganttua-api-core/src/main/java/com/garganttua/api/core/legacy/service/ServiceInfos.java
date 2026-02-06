package com.garganttua.api.core.legacy.service;

import java.lang.reflect.Method;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.garganttua.api.core.legacy.engine.EngineException;
import com.garganttua.api.spec.EntityOperation;
import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.service.IServiceInfos;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.utils.GGObjectReflectionHelper;

import lombok.Getter;

public class ServiceInfos implements IServiceInfos {
	@Getter
	private String domainName;
    @Getter
    private EntityOperation operation;

    private Class<?> interfasse;
    @Getter
    private String path;
    @Getter
    private String description;
    @Getter
	@JsonIgnore
	private Method method;
    
	private IObjectInstanciator objectInstanciator;

    public ServiceInfos(String domainName, EntityOperation operation, Class<?> interfasse, Method method, String path, String description, IObjectInstanciator objectInstanciator) throws EngineException {
        this.domainName = domainName;
		this.operation = operation;
		this.interfasse = interfasse;
		this.method = method;
        this.path = path;
        this.description = description;
		this.objectInstanciator = objectInstanciator;
        
        if( operation.isCustom() ) {
	        boolean found = false; 
	        for( Class<?> parameterClass: this.method.getParameterTypes() ) {
	        	if( ICaller.class.isAssignableFrom(parameterClass) ) {
	        		found = true;
	        		break;
	        	}
	        }
	        if( !found ) {
	        	throw new EngineException(CoreExceptionCode.CUSTOM_SERVICE_ERROR, "The custom service "+this.method.getName()+" must have ICaller parameter" );
	        }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ServiceInfos{")
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

	@Override
	public Object invoke(Object[] parameters) throws CoreException {

		Object object = this.objectInstanciator.instanciateNew();
		
		try {
			return GGObjectReflectionHelper.invokeMethod(object, this.getMethodName(), this.method, parameters);
		} catch (ReflectionException e) {
			CoreException.processException(e);
		}
		return object;
	}
}
