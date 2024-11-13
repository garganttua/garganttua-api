package com.garganttua.api.interfaces.spring.rest;

import org.springframework.http.HttpMethod;

import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.GGAPIMethod;

public class GGAPIServiceMethodToHttpMethodBinder {
	
	public static HttpMethod fromServiceMethod(GGAPIEntityOperation operation) {
		switch( operation.getMethod() ) {
		case create:
			return HttpMethod.POST;
		case read:
			return HttpMethod.GET;
		case delete:
			return HttpMethod.DELETE;
		case update:
			return HttpMethod.PATCH;
		};
		return HttpMethod.GET;
	}
	
	public static GGAPIMethod fromHttpMethodAndEndpoint(HttpMethod method) {
		switch( method.name() ) {
		case "POST":
			return GGAPIMethod.create;
		case "GET":
			return GGAPIMethod.read;
		case "DELETE":
			return GGAPIMethod.delete;
		case "PATCH":
		case "PUT":
			return GGAPIMethod.update;
		};
		return GGAPIMethod.read;
	}

}
