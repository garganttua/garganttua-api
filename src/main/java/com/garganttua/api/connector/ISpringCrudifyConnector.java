/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.connector;

import java.util.List;
import java.util.concurrent.Future;

import com.garganttua.api.repository.dto.ISpringCrudifyDTOObject;
import com.garganttua.api.spec.ISpringCrudifyDomainable;
import com.garganttua.api.spec.ISpringCrudifyEntity;

public interface ISpringCrudifyConnector <T extends ISpringCrudifyEntity, S extends List<T>, U extends ISpringCrudifyDTOObject<T>> extends ISpringCrudifyDomainable<T, U>{

	public enum SpringCrudifyConnectorOperation {
		READ, CREATE, UPDATE, DELETE
	}

	/**
	 * 
	 * @param tenantId
	 * @param domain
	 * @param object
	 * @param operation
	 * @return
	 * @throws SpringCrudifyConnectorException
	 */
	public Future<T> requestEntity(String tenantId, T entity, SpringCrudifyConnectorOperation operation) throws SpringCrudifyConnectorException;
	
	/**
	 * 
	 * @param tenantId
	 * @param domain
	 * @param object
	 * @param operation
	 * @return
	 * @throws SpringCrudifyConnectorException
	 */
	public Future<S> requestList(String tenantId, S list, SpringCrudifyConnectorOperation operation) throws SpringCrudifyConnectorException;

}
