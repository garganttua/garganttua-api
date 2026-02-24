package com.garganttua.api.spec.service;

public interface IOperationResponse {

	OperationResponseCode getResponseCode();
	
	Object getResponse();

}
