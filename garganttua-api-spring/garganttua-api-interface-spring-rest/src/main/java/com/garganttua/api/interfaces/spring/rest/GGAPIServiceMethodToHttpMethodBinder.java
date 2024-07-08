package com.garganttua.api.interfaces.spring.rest;

import org.springframework.http.HttpMethod;

import com.garganttua.api.spec.service.GGAPIServiceMethod;

public class GGAPIServiceMethodToHttpMethodBinder {
	
	public static HttpMethod fromServiceMethod(GGAPIServiceMethod method) {
		switch( method ) {
		case CREATE:
			return HttpMethod.POST;
		case READ:
			return HttpMethod.GET;
		case DELETE:
			return HttpMethod.DELETE;
		case PARTIAL_UPDATE:
			return HttpMethod.PATCH;
		case FULL_UPDATE:
			return HttpMethod.PUT;
		};
		return HttpMethod.GET;
	}
	
	public static GGAPIServiceMethod fromHttpMethod(HttpMethod method) {
		switch( method.name() ) {
		case "POST":
			return GGAPIServiceMethod.CREATE;
		case "GET":
			return GGAPIServiceMethod.READ;
		case "DELETE":
			return GGAPIServiceMethod.DELETE;
		case "PATCH":
			return GGAPIServiceMethod.PARTIAL_UPDATE;
		case "PUT":
			return GGAPIServiceMethod.FULL_UPDATE;
		};
		return GGAPIServiceMethod.READ;
	}

}
