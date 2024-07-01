package com.garganttua.api.core.service;

import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;

import lombok.Getter;

public class GGAPIServiceResponse implements IGGAPIServiceResponse {

	@Getter
	private GGAPIServiceResponseCode responseCode;
	@Getter
	private Object response;

	public GGAPIServiceResponse(Object response, GGAPIServiceResponseCode responseCode) {
		this.response = response;
		this.responseCode = responseCode;
	}
}
