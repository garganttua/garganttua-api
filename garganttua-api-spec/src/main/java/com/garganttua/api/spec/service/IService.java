package com.garganttua.api.spec.service;

import java.util.Map;
import java.util.Optional;

import com.garganttua.api.spec.EntityOperation;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.engine.IEngineObject;
import com.garganttua.api.spec.event.IEventPublisher;
import com.garganttua.api.spec.factory.IEntityFactory;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.sort.ISort;

public interface IService extends IEngineObject {

	@FunctionalInterface
	interface Allowed {
		boolean isAllowed();
	}

	IServiceResponse createEntity(ICaller caller, Object entity,
			Map<String, String> customParameters);
		
	IServiceResponse getEntities (
			ICaller caller,
			ReadOutputMode mode,
			IPageable pageable,
			IFilter filter,
			ISort sort,
			Map<String, String> customParameters);

	IServiceResponse getEntity(ICaller caller, String uuid,
			Map<String, String> customParameters);
	
	IServiceResponse updateEntity(ICaller caller, String uuid, Object entity,
			Map<String, String> customParameters);

	IServiceResponse deleteEntity(ICaller caller, String uuid, Map<String, String> customParameters);
	
	IServiceResponse deleteAll(
			ICaller caller,
			IFilter filter,
			Map<String, String> customParameters);

	void setEventPublisher(Optional<IEventPublisher> eventObj);
	
	void setFactory(IEntityFactory<Object> factory);
	
	IDomain getDomain();

	Class<?> getDomainEntityClass();

	IServiceResponse executeServiceCommand(ICaller caller, Allowed allowed, IServiceCommand command, Map<String, String> customParameters, EntityOperation operation);

}
