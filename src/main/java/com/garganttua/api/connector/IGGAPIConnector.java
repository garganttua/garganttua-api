/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.connector;

import java.util.List;
import java.util.concurrent.Future;

import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.IGGAPIDomainable;
import com.garganttua.api.spec.IGGAPIEntity;

public interface IGGAPIConnector <T extends IGGAPIEntity, S extends List<T>, U extends IGGAPIDTOObject<T>> extends IGGAPIDomainable<T, U>{

	public enum GGAPIConnectorOperation {
		READ, CREATE, UPDATE, DELETE
	}

	/**
	 * 
	 * @param tenantId
	 * @param domain
	 * @param object
	 * @param operation
	 * @return
	 * @throws GGAPIConnectorException
	 */
	public Future<T> requestEntity(String tenantId, T entity, GGAPIConnectorOperation operation) throws GGAPIConnectorException;
	
	/**
	 * 
	 * @param tenantId
	 * @param domain
	 * @param object
	 * @param operation
	 * @return
	 * @throws GGAPIConnectorException
	 */
	public Future<S> requestList(String tenantId, S list, GGAPIConnectorOperation operation) throws GGAPIConnectorException;

}
