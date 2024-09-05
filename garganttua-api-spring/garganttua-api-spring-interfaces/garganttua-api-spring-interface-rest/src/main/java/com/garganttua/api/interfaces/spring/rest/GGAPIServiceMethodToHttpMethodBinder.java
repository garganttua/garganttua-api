package com.garganttua.api.interfaces.spring.rest;

import org.springframework.http.HttpMethod;

import com.garganttua.api.spec.GGAPIEntityOperation;

public class GGAPIServiceMethodToHttpMethodBinder {
	
	public static HttpMethod fromServiceMethod(GGAPIEntityOperation operation) {
		switch( operation ) {
		case create_one:
			return HttpMethod.POST;
		case read_all:
		case read_one:
			return HttpMethod.GET;
		case delete_one:
		case delete_all:
			return HttpMethod.DELETE;
		case update_one:
			return HttpMethod.PATCH;
		};
		return HttpMethod.GET;
	}
	
	public static GGAPIEntityOperation fromHttpMethod(HttpMethod method) {
		switch( method.name() ) {
		case "POST":
			return GGAPIEntityOperation.create_one;
		case "GET":
			return GGAPIEntityOperation.read_all;
		case "DELETE":
			return GGAPIEntityOperation.delete_one;
		case "PATCH":
			return GGAPIEntityOperation.update_one;
		case "PUT":
			return GGAPIEntityOperation.update_one;
		};
		return GGAPIEntityOperation.read_all;
	}

}
