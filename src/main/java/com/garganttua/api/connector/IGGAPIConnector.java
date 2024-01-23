/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.connector;

import java.util.List;
import java.util.concurrent.Future;

import com.garganttua.api.core.IGGAPIDomainable;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.engine.IGGAPIEngineObject;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;

public interface IGGAPIConnector <T extends IGGAPIEntity, S extends List<T>, U extends IGGAPIDTOObject<T>> extends IGGAPIDomainable<T, U>, IGGAPIEngineObject {

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
