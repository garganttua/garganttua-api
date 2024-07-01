package com.garganttua.api.core.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.event.IGGAPIEvent;
import com.garganttua.api.spec.event.IGGAPIEventPublisher;
import com.garganttua.api.spec.factory.GGAPIEntityIdentifier;
import com.garganttua.api.spec.factory.IGGAPIEntityFactory;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.pageable.IGGAPIPageable;
import com.garganttua.api.spec.security.IGGAPISecurity;
import com.garganttua.api.spec.service.GGAPIReadOutputMode;
import com.garganttua.api.spec.service.GGAPIServiceMethod;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;
import com.garganttua.api.spec.sort.IGGAPISort;

import lombok.Setter;

public class GGAPIService implements IGGAPIService {
	
	@FunctionalInterface
	protected interface Allowed {
		boolean isAllowed();
	}

	@Setter
	protected IGGAPIEngine engine;
	@Setter
	protected IGGAPIDomain domain;
	@Setter
	protected Optional<IGGAPIEventPublisher> eventPublisher;
	@Setter
	protected IGGAPIEntityFactory<Object> factory;
	@Setter
	protected Optional<IGGAPISecurity> security;

	public GGAPIService(IGGAPIDomain domain) {
		this.domain = domain;
	}

	@Override
	public void setEngine(IGGAPIEngine engine) {
		this.engine = engine;
	}
	
	protected IGGAPIEvent prepareEvent(IGGAPICaller caller, GGAPIServiceMethod method, Map<String, String> params) {
		GGAPIEvent event = new GGAPIEvent();
		event.setTenantId(caller.getTenantId());
		event.setOwnerId(caller.getOwnerId());
		event.setCaller(caller);
		event.setMethod(method);
		event.setEndPoint(caller.getAccessRule().getEndpoint());
		event.setEntityClass(this.domain.getEntity().getValue0().getName());
		event.setInParams(params);
		return event;
	}
	
	@Override
	public IGGAPIServiceResponse createEntity(IGGAPICaller caller, Object entity, Map<String, String> customParameters) {
		IGGAPIServiceCommand command = (event) -> {
			event.setIn(entity);
			Object preparedEntity = this.factory.prepareNewEntity(customParameters, entity);
			GGAPIEntityHelper.save(preparedEntity, caller, customParameters, this.security);
			event.setOut(preparedEntity);
			return event;
		};
		
		return this.executeServiceCommand(caller, () -> {return this.domain.isAllowCreation();}, command, customParameters);
	}

	@Override
	public IGGAPIServiceResponse getEntities(IGGAPICaller caller, GGAPIReadOutputMode mode, IGGAPIPageable pageable, IGGAPIFilter filter, IGGAPISort sort, Map<String, String> customParameters) {
		IGGAPIServiceCommand command = (event) -> {
			customParameters.put("mode", mode.toString());
			if( pageable != null ) {
				customParameters.put("pageSize", String.valueOf(pageable.getPageSize()));
				customParameters.put("pageIndex", String.valueOf(pageable.getPageIndex()));
			}
			if( filter != null )
				customParameters.put("filterString", filter.toString());
			if( sort != null )
				customParameters.put("sortString", sort.toString());
			
			List<Object> entities = this.factory.getEntitiesFromRepository(caller, pageable, filter, sort, customParameters);
			
			if( pageable != null && pageable.getPageSize() != 0 ) {
				long totalCount = this.factory.countEntities(caller, filter, customParameters);
				GGAPIPage page = new GGAPIPage(totalCount, ((List<Object>) entities));
				event.setOut(page);
			} else {
				event.setOut(entities);
			}
			
			return event;
		};
		
		return this.executeServiceCommand(caller, () -> {return this.domain.isAllowReadAll();}, command, customParameters);
	}

	@Override
	public IGGAPIServiceResponse getEntity(IGGAPICaller caller, String uuid, Map<String, String> customParameters) {
		IGGAPIServiceCommand command = (event) -> {
			event.setIn(GGAPIEntityIdentifier.UUID+":"+uuid);
			Object entity = this.factory.getEntityFromRepository(caller, customParameters, GGAPIEntityIdentifier.UUID, uuid);
			event.setOut(entity);
			return event;
		};
		
		return this.executeServiceCommand(caller, () -> {return this.domain.isAllowReadOne();}, command, customParameters);
	}

	@Override
	public IGGAPIServiceResponse updateEntity(IGGAPICaller caller, String uuid, Object entity, Map<String, String> customParameters) {
		IGGAPIServiceCommand command = (event) -> {
			event.setIn(entity);
			customParameters.put("uuid", uuid);
			GGAPIEntityHelper.setUuid(entity, uuid);
			GGAPIEntityHelper.save(entity, caller, customParameters, this.security);
			event.setOut(entity);
			return event;
		};
		
		return this.executeServiceCommand(caller, () -> {return this.domain.isAllowUpdateOne();}, command, customParameters);
	}

	@Override
	public IGGAPIServiceResponse deleteEntity(IGGAPICaller caller, String uuid, Map<String, String> customParameters) {
		IGGAPIServiceCommand command = (event) -> {
			event.setIn(uuid);
			Object entity = this.factory.getEntityFromRepository(caller, customParameters, GGAPIEntityIdentifier.UUID, uuid);
			GGAPIEntityHelper.delete(entity, caller, customParameters);
			event.setOut(entity);
			return event;
		};
		
		return this.executeServiceCommand(caller, () -> {return this.domain.isAllowDeleteOne();}, command, customParameters);
	}

	@Override
	public IGGAPIServiceResponse deleteAll(IGGAPICaller caller, IGGAPIFilter filter, Map<String, String> customParameters) {
		IGGAPIServiceCommand command = (event) -> {
			event.setIn(filter);
			customParameters.put("filter", filter.toString());
			List<?> entities = this.factory.getEntitiesFromRepository(caller, null, filter, null, customParameters);
			for( Object entity: entities ) {
					GGAPIEntityHelper.delete(entity, caller, customParameters);
			}

			return event;
		};
		
		return this.executeServiceCommand(caller, () -> {return this.domain.isAllowDeleteAll();}, command, customParameters);
	}

	@Override
	public IGGAPIServiceResponse getCount(IGGAPICaller caller, IGGAPIFilter filter, Map<String, String> customParameters) {
		IGGAPIServiceCommand command = (event) -> {
			long totalCount = this.factory.countEntities(caller, filter, customParameters);
			event.setOut(totalCount);
			return event;
		};
		
		return this.executeServiceCommand(caller, () -> {return this.domain.isAllowCount();}, command, customParameters);
	}

	protected IGGAPIServiceResponse executeServiceCommand(IGGAPICaller caller, Allowed allowed, IGGAPIServiceCommand command, Map<String, String> customParameters) {
		IGGAPIEvent event = this.prepareEvent(caller, GGAPIServiceMethod.READ, customParameters);
		try {
			if (allowed.isAllowed()) {
				try {
					event = command.execute(event);
					event.setCode(GGAPIServiceResponseCode.OK);
				} catch (GGAPIException e) {
					event.setExceptionMessage(e.getMessage());
					event.setExceptionCode(e.getCode().getCode());
					event.setCode(GGAPIServiceResponseCode.SERVER_ERROR);
				} catch (Exception e) {
					event.setExceptionMessage(e.getMessage());
					event.setOut(new String("Unexpected internal server error"));
					event.setCode(GGAPIServiceResponseCode.SERVER_ERROR);
				}
			} else {
				event.setCode(GGAPIServiceResponseCode.NOT_AVAILABLE);
				event.setOut(new String("This method is not available"));
			}

		} finally {
			if (this.eventPublisher.isPresent()) {
				event.setOutDate(new Date());
				this.eventPublisher.get().publishEvent(event);
			}
		}
		return event.toServiceResponse();
	}

}
