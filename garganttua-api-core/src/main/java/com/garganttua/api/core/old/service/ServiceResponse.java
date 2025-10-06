package com.garganttua.api.core.service;

import com.garganttua.api.spec.service.ServiceResponseCode;
import com.garganttua.api.spec.service.IServiceResponse;

import lombok.Getter;

public class ServiceResponse implements IServiceResponse {

	@Getter
	private ServiceResponseCode responseCode;
	@Getter
	private Object response;

	public ServiceResponse(Object response, ServiceResponseCode responseCode) {
		this.response = response;
		this.responseCode = responseCode;
	}
}
