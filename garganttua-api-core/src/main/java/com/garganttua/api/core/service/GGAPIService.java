package com.garganttua.api.core.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.event.IGGAPIEvent;
import com.garganttua.api.spec.event.IGGAPIEventPublisher;
import com.garganttua.api.spec.factory.GGAPIEntityIdentifier;
import com.garganttua.api.spec.factory.IGGAPIEntityFactory;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.pageable.IGGAPIPageable;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;
import com.garganttua.api.spec.service.GGAPIReadOutputMode;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;
import com.garganttua.api.spec.sort.IGGAPISort;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
	protected Optional<IGGAPIEventPublisher> eventPublisher = Optional.empty();
	@Setter
	protected IGGAPIEntityFactory<Object> factory;
	@Setter
	protected Optional<IGGAPISecurityEngine> security;
	
	private IGGAPIDomain tenantsDomain;

	public GGAPIService(IGGAPIDomain domain) {
		this.domain = domain;
	}

	@Override
	public void setEngine(IGGAPIEngine engine) {
		this.engine = engine;
	}
	
	protected IGGAPIEvent prepareEvent(IGGAPICaller caller, GGAPIEntityOperation operation, Map<String, String> params) {
		GGAPIEvent event = new GGAPIEvent();
		event.setTenantId(caller.getTenantId());
		event.setOwnerId(caller.getOwnerId());
		event.setDomain(this.domain);
		event.setCaller(caller);
		event.setOperation(operation);
		event.setInParams(params);
		return event;
	}
	
	@Override
	public IGGAPIServiceResponse createEntity(IGGAPICaller caller, Object entity, Map<String, String> customParameters) {
		IGGAPIServiceCommand command = (event) -> {
			event.setIn(entity);
			Object preparedEntity = this.factory.prepareNewEntity(customParameters, entity, GGAPIEntityHelper.getUuid(entity), caller.getRequestedTenantId());
			Object createdEntity = GGAPIEntityHelper.save(preparedEntity, caller, customParameters, this.security);
			event.setOut(createdEntity);
			event.setCode(GGAPIServiceResponseCode.CREATED);
			return event;
		};
		
		return this.executeServiceCommand(caller, () -> {return this.domain.isAllowCreation();}, command, customParameters, GGAPIEntityOperation.create_one);
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
			List<Object> finalEntityList = null;
			
			switch(mode) {
			case id:
				finalEntityList = entities.stream().map( entity -> {
					try {
						return GGAPIEntityHelper.getId(entity);
					} catch (GGAPIException e) {
						if( log.isDebugEnabled() ) {
							log.warn("Error : ",e);
						}
					}
					return null;
				}).collect(Collectors.toList());
				break;
			case uuid:
				finalEntityList = entities.stream().map( entity -> {
					try {
						return GGAPIEntityHelper.getUuid(entity);
					} catch (GGAPIException e) {
						if( log.isDebugEnabled() ) {
							log.warn("Error : ",e);
						}
					}
					return null;
				}).collect(Collectors.toList());
				break;
			default:
			case full:
				finalEntityList = entities;
				break;
			}
			
			if( pageable != null && pageable.getPageSize() != 0 ) {
				long totalCount = this.factory.countEntities(caller, filter, customParameters);
				GGAPIPage page = new GGAPIPage(totalCount, ((List<Object>) finalEntityList));
				event.setOut(page);
			} else {
				event.setOut(finalEntityList);
			}
			event.setCode(GGAPIServiceResponseCode.OK);
			return event;
		};
		
		return this.executeServiceCommand(caller, () -> {return this.domain.isAllowReadAll();}, command, customParameters, GGAPIEntityOperation.read_all);
	}

	@Override
	public IGGAPIServiceResponse getEntity(IGGAPICaller caller, String uuid, Map<String, String> customParameters) {
		IGGAPIServiceCommand command = (event) -> {
			event.setIn(GGAPIEntityIdentifier.UUID+":"+uuid);
			Object entity = this.factory.getEntityFromRepository(caller, customParameters, GGAPIEntityIdentifier.UUID, uuid);
			event.setOut(entity);
			event.setCode(GGAPIServiceResponseCode.OK);
			return event;
		};
		
		return this.executeServiceCommand(caller, () -> {return this.domain.isAllowReadOne();}, command, customParameters, GGAPIEntityOperation.read_one);
	}

	@Override
	public IGGAPIServiceResponse updateEntity(IGGAPICaller caller, String uuid, Object entity, Map<String, String> customParameters) {
		IGGAPIServiceCommand command = (event) -> {
			event.setIn(entity);
			customParameters.put("uuid", uuid);
			Object preparedEntity = this.factory.prepareNewEntity(customParameters, entity, uuid, caller.getRequestedTenantId());
			Object udpatedEntity = GGAPIEntityHelper.save(preparedEntity, caller, customParameters, this.security);
			event.setOut(udpatedEntity);
			event.setCode(GGAPIServiceResponseCode.UPDATED);
			return event;
		};
		
		return this.executeServiceCommand(caller, () -> {return this.domain.isAllowUpdateOne();}, command, customParameters, GGAPIEntityOperation.update_one);
	}

	@Override
	public IGGAPIServiceResponse deleteEntity(IGGAPICaller caller, String uuid, Map<String, String> customParameters) {
		IGGAPIServiceCommand command = (event) -> {
			event.setIn(uuid);
			Object entity = this.factory.getEntityFromRepository(caller, customParameters, GGAPIEntityIdentifier.UUID, uuid);
			GGAPIEntityHelper.delete(entity, caller, customParameters);
			event.setOut(entity);
			event.setCode(GGAPIServiceResponseCode.DELETED);
			return event;
		};
		
		return this.executeServiceCommand(caller, () -> {return this.domain.isAllowDeleteOne();}, command, customParameters, GGAPIEntityOperation.delete_one);
	}

	@Override
	public IGGAPIServiceResponse deleteAll(IGGAPICaller caller, IGGAPIFilter filter, Map<String, String> customParameters) {
		IGGAPIServiceCommand command = (event) -> {
			event.setIn(filter);
			if( filter != null )
				customParameters.put("filter", filter.toString());
			List<?> entities = this.factory.getEntitiesFromRepository(caller, null, filter, null, customParameters);
			for( Object entity: entities ) {
					GGAPIEntityHelper.delete(entity, caller, customParameters);
			}
			event.setCode(GGAPIServiceResponseCode.DELETED);
			return event;
		};
		
		return this.executeServiceCommand(caller, () -> {return this.domain.isAllowDeleteAll();}, command, customParameters, GGAPIEntityOperation.delete_all);
	}

	protected IGGAPIServiceResponse executeServiceCommand(IGGAPICaller caller, Allowed allowed, IGGAPIServiceCommand command, Map<String, String> customParameters, GGAPIEntityOperation operation) {
		IGGAPIEvent event = this.prepareEvent(caller, operation, customParameters);
		try {
			if (allowed.isAllowed()) {
				
				if( !this.checkTenantIdIsPresent(caller) ) {
					event.setOut("TenantId not provided");
					event.setCode(GGAPIServiceResponseCode.CLIENT_ERROR);
				} else {
					try {
						event = command.execute(event);
					} catch (GGAPIException e) {
						if( log.isDebugEnabled() ) {
							log.warn("Error ",e);
						}
						event.setExceptionMessage(e.getMessage());
						event.setOut(e.getMessage());
						event.setExceptionCode(e.getCode().getCode());
						event.setCode(GGAPIServiceResponseCode.fromExceptionCode(e));
					} catch (Exception e) {
						if( log.isDebugEnabled() ) {
							log.warn("Error ",e);
						}
						event.setExceptionMessage(e.getMessage());
						event.setOut(e.getMessage());
						event.setCode(GGAPIServiceResponseCode.SERVER_ERROR);
					}
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

	private boolean checkTenantIdIsPresent(IGGAPICaller caller) {
		if( !caller.isAnonymous() && (caller.getTenantId() == null || caller.getTenantId().isEmpty()) )
			return false;
		return true;
	}

}
