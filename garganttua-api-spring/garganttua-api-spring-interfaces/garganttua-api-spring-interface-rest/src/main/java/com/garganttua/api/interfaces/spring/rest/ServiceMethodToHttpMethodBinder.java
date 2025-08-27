package com.garganttua.api.interfaces.spring.rest;

import org.springframework.http.HttpMethod;

import com.garganttua.api.spec.EntityOperation;
import com.garganttua.api.spec.Method;

public class ServiceMethodToHttpMethodBinder {
	
	public static HttpMethod fromServiceMethod(EntityOperation operation) {
		switch( operation.getMethod() ) {
		case create:
			return HttpMethod.POST;
		case read:
			return HttpMethod.GET;
		case delete:
			return HttpMethod.DELETE;
		case update:
			return HttpMethod.PATCH;
		case authenticate:
			return HttpMethod.POST;
		};
		return HttpMethod.GET;
	}
	
	public static Method fromHttpMethodAndEndpoint(HttpMethod method) {
		switch( method.name() ) {
		case "POST":
			return Method.create;
		case "GET":
			return Method.read;
		case "DELETE":
			return Method.delete;
		case "PATCH":
		case "PUT":
			return Method.update;
		};
		return Method.read;
	}

}
