package com.garganttua.api.commons.service;

public interface IRequest {

	IOperationRequest operationRequest();

	IOperationResponse execute();

}
