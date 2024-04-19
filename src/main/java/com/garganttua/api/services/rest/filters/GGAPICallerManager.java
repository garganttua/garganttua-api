package com.garganttua.api.services.rest.filters;

import com.garganttua.api.core.GGAPICaller;

import jakarta.servlet.ServletRequest;

public class GGAPICallerManager {

	public static final String CALLER_ATTRIBUTE_NAME = "caller";

	static GGAPICaller getCallerAttributeAndCreateItIfNotPresent(ServletRequest request) {
		Object attribute = request.getAttribute(CALLER_ATTRIBUTE_NAME);
		
		if( attribute == null ) {
			attribute = new GGAPICaller();
			request.setAttribute(CALLER_ATTRIBUTE_NAME, attribute);
		}
		
		return (GGAPICaller) attribute;
	}
	
}
