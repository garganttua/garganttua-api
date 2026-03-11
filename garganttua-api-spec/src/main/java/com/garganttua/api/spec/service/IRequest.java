package com.garganttua.api.spec.service;

public interface IRequest {

	IOperationRequest operationRequest();

	IOperationResponse execute();

}
