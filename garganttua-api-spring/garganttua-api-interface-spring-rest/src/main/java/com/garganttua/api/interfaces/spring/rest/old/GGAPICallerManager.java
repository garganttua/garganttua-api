package com.garganttua.api.interfaces.spring.rest.old;

import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.spec.IGGAPICaller;

import jakarta.servlet.ServletRequest;

public class GGAPICallerManager {

	public static final String CALLER_ATTRIBUTE_NAME = "caller";

	static IGGAPICaller getCallerAttributeAndCreateItIfNotPresent(ServletRequest request) {
		Object attribute = request.getAttribute(CALLER_ATTRIBUTE_NAME);
		
		if( attribute == null ) {
			attribute = new GGAPICaller();
			request.setAttribute(CALLER_ATTRIBUTE_NAME, attribute);
		}
		
		return (IGGAPICaller) attribute;
	}
	
}
