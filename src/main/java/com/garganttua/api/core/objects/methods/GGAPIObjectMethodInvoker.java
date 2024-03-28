package com.garganttua.api.core.objects.methods;

import java.lang.reflect.Method;
import java.util.List;

import com.garganttua.api.core.objects.GGAPIObjectAddress;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryException;
import com.garganttua.api.core.objects.utils.GGAPIObjectReflectionHelper;
import com.garganttua.api.core.objects.utils.GGAPIObjectReflectionHelperExcpetion;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIObjectMethodInvoker {

	private Class<?> clazz;
	private List<Object> fields;
	private GGAPIObjectAddress address;

	public GGAPIObjectMethodInvoker(Class<?> clazz, List<Object> fields,
			GGAPIObjectAddress address) throws GGAPIObjectQueryException {
		if (clazz == null) {
			throw new GGAPIObjectQueryException("class is null");
		}
		if (fields == null) {
			throw new GGAPIObjectQueryException("fields is null");
		}
		if (address == null) {
			throw new GGAPIObjectQueryException("address is null");
		}

		this.clazz = clazz;
		this.fields = fields;
		this.address = address;
	}

	public Object invoke(Object object, Object ...args) throws GGAPIObjectQueryException {
		if (log.isDebugEnabled()) {
			log.debug("Invoking method of object : class {}, address {}, parameters {}", this.clazz, this.address, args);
		}
		
		if (object == null) {
			throw new GGAPIObjectQueryException("object is null");
		}
		if( !object.getClass().isAssignableFrom(this.clazz) ) {
			throw new GGAPIObjectQueryException("object is not of type "+this.clazz);
		}
		
		Object returned = null;
		
		if( this.fields.size() == 1 ) {
			Method method = (Method) fields.get(0);
			String methodName = this.address.getElement(0);

			if (!method.getName().equals(methodName)) {
				throw new GGAPIObjectQueryException("method names of address " + methodName + " and fields list "
						+ method.getName() + " do not match");
			}
			
			try {
				return GGAPIObjectReflectionHelper.invokeMethod(object, methodName, method, args);
			} catch (GGAPIObjectReflectionHelperExcpetion e) {
				throw new GGAPIObjectQueryException(e);
			}
		} else {
			
		}
		
		return returned;
	}

}
