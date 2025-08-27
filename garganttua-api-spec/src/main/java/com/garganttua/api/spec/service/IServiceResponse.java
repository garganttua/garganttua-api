package com.garganttua.api.spec.service;

public interface IServiceResponse {
	
	ServiceResponseCode getResponseCode();
	
	Object getResponse();

}
