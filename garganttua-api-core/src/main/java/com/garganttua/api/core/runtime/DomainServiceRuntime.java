package com.garganttua.api.core.runtime;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.garganttua.api.core.old.entity.tools.EntityHelper;
import com.garganttua.api.core.old.service.Page;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.EntityOperation;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.engine.IDomainContext;
import com.garganttua.api.spec.event.IEvent;
import com.garganttua.api.spec.event.IEventPublisher;
import com.garganttua.api.spec.factory.EntityIdentifier;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.service.IDomainServiceRuntime;
import com.garganttua.api.spec.service.IServiceCommand;
import com.garganttua.api.spec.service.IServiceResponse;
import com.garganttua.api.spec.service.ReadOutputMode;
import com.garganttua.api.spec.service.ServiceResponseCode;
import com.garganttua.api.spec.sort.ISort;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DomainServiceRuntime implements IDomainServiceRuntime {


	private @Nonnull IEventRuntime events;
	private @Nonnull IOperationBuilder operation;

	public DomainServiceRuntime(IEventRuntime events, IOperationBuilder operation) {
		this.events = Objects.requireNonNull(events, "Event runtime cannot be null");
		this.operation = Objects.requireNonNull(operation, "Operation builder cannot be null");
	}

	protected IEvent prepareEvent(ICaller caller, EntityOperation operation, Map<String, String> params) {
		Event event = new Event();
		event.setTenantId(caller.getTenantId());
		event.setOwnerId(caller.getOwnerId());
		/* event.setDomain(this.domain); */
		event.setCaller(caller);
		event.setOperation(operation);
		event.setInParams(params);
		return event;
	}

	@Override
	public IServiceResponse createEntity(ICaller caller, Object entity, Map<String, String> customParameters) {
		IServiceCommand command = (event) -> {
			event.setIn(entity);
			Object preparedEntity = entity;// this.factory.prepareNewEntity(customParameters, entity,
											// EntityHelper.getUuid(entity), caller.getRequestedTenantId());
			Object createdEntity = EntityHelper.save(preparedEntity, caller, customParameters);
			event.setOut(createdEntity);
			event.setCode(ServiceResponseCode.CREATED);
			return event;
		};

		return this.executeServiceCommand(caller, () -> {
			return this.domain.isAllowCreation();
		}, command, customParameters, operation.create);
	}

	@Override
	public IServiceResponse getEntities(ICaller caller, ReadOutputMode mode, IPageable pageable, IFilter filter,
			ISort sort, Map<String, String> customParameters) {
		IServiceCommand command = (event) -> {
			customParameters.put("mode", mode.toString());
			if (pageable != null) {
				customParameters.put("pageSize", String.valueOf(pageable.getPageSize()));
				customParameters.put("pageIndex", String.valueOf(pageable.getPageIndex()));
			}
			if (filter != null)
				customParameters.put("filterString", filter.toString());
			if (sort != null)
				customParameters.put("sortString", sort.toString());

			List<?> entities = List.of();//this.factory.getEntitiesFromRepository(caller, pageable, filter, sort, customParameters);
			List<?> finalEntityList = null;

			switch (mode) {
				case id:
					finalEntityList = entities.stream().map(entity -> {
						try {
							return EntityHelper.getId(entity);
						} catch (CoreException e) {
							if (log.isDebugEnabled()) {
								log.warn("Error : ", e);
							}
						}
						return null;
					}).collect(Collectors.toList());
					break;
				case uuid:
					finalEntityList = entities.stream().map(entity -> {
						try {
							return EntityHelper.getUuid(entity);
						} catch (CoreException e) {
							if (log.isDebugEnabled()) {
								log.warn("Error : ", e);
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

			if (pageable != null && pageable.getPageSize() != 0) {
				long totalCount = 0;//this.factory.countEntities(caller, filter, customParameters);
				Page page = new Page(totalCount, ((List<Object>) finalEntityList));
				event.setOut(page);
			} else {
				event.setOut(finalEntityList);
			}
			event.setCode(ServiceResponseCode.OK);
			return event;
		};

		return this.executeServiceCommand(caller, () -> {
			return this.domain.isAllowReadAll();
		}, command, customParameters, EntityOperation.readAll(this.domain.getDomain(), this.domain.getEntityClass()));
	}

	@Override
	public IServiceResponse getEntity(ICaller caller, String uuid, Map<String, String> customParameters) {
		IServiceCommand command = (event) -> {
			event.setIn(EntityIdentifier.UUID + ":" + uuid);
			Object entity = null;//this.factory.getEntityFromRepository(caller, customParameters, EntityIdentifier.UUID, uuid);
			event.setOut(entity);
			event.setCode(ServiceResponseCode.OK);
			return event;
		};

		return this.executeServiceCommand(caller, () -> {
			return this.domain.isAllowReadOne();
		}, command, customParameters, EntityOperation.readOne(this.domain.getDomain(), this.domain.getEntityClass()));
	}

	@Override
	public IServiceResponse updateEntity(ICaller caller, String uuid, Object entity,
			Map<String, String> customParameters) {
		IServiceCommand command = (event) -> {
			event.setIn(entity);
			customParameters.put("uuid", uuid);
			Object preparedEntity = null;//this.factory.prepareNewEntity(customParameters, entity, uuid, caller.getRequestedTenantId());
			Object udpatedEntity = EntityHelper.save(preparedEntity, caller, customParameters);
			event.setOut(udpatedEntity);
			event.setCode(ServiceResponseCode.UPDATED);
			return event;
		};

		return this.executeServiceCommand(caller, () -> {
			return this.domain.isAllowUpdateOne();
		}, command, customParameters, EntityOperation.updateOne(this.domain.getDomain(), this.domain.getEntityClass()));
	}

	@Override
	public IServiceResponse deleteEntity(ICaller caller, String uuid, Map<String, String> customParameters) {
		IServiceCommand command = (event) -> {
			event.setIn(uuid);
			Object entity = null; //this.factory.getEntityFromRepository(caller, customParameters, EntityIdentifier.UUID, uuid);
			EntityHelper.delete(entity, caller, customParameters);
			event.setOut(entity);
			event.setCode(ServiceResponseCode.DELETED);
			return event;
		};

		return this.executeServiceCommand(caller, () -> {
			return this.domain.isAllowDeleteOne();
		}, command, customParameters, EntityOperation.deleteOne(this.domain.getDomain(), this.domain.getEntityClass()));
	}

	@Override
	public IServiceResponse deleteAll(ICaller caller, IFilter filter, Map<String, String> customParameters) {
		IServiceCommand command = (event) -> {
			event.setIn(filter);
			if (filter != null)
				customParameters.put("filter", filter.toString());
			List<?> entities = null; //this.factory.getEntitiesFromRepository(caller, null, filter, null, customParameters);
			for (Object entity : entities) {
				EntityHelper.delete(entity, caller, customParameters);
			}
			event.setCode(ServiceResponseCode.DELETED);
			return event;
		};

		return this.executeServiceCommand(caller, () -> {
			return this.domain.isAllowDeleteAll();
		}, command, customParameters, EntityOperation.deleteAll(this.domain.getDomain(), this.domain.getEntityClass()));
	}

	@Override
	public IServiceResponse executeServiceCommand(ICaller caller, Allowed allowed, IServiceCommand command,
			Map<String, String> customParameters, EntityOperation operation) {
		IEvent event = this.prepareEvent(caller, operation, customParameters);
		try {
			if (allowed.isAllowed()) {

				if (!caller.isSuperTenant() && !this.checkTenantIdIsPresent(caller)
						&& this.domain.isTenantIdMandatoryForOperation(caller.getOperation())) {
					event.setOut("TenantId not provided");
					event.setCode(ServiceResponseCode.CLIENT_ERROR);
				} else {
					try {
						event = command.execute(event);
					} catch (CoreException e) {
						if (log.isDebugEnabled()) {
							log.warn("Error ", e);
						}
						event.setExceptionMessage(e.getMessage());
						event.setOut(e.getMessage());
						event.setExceptionCode(e.getCode().getCode());
						event.setCode(ServiceResponseCode.fromExceptionCode(e));
					} catch (Exception e) {
						if (log.isDebugEnabled()) {
							log.warn("Error ", e);
						}
						event.setExceptionMessage(e.getMessage());
						event.setOut(e.getMessage());
						event.setCode(ServiceResponseCode.SERVER_ERROR);
					}
				}
			} else {
				event.setCode(ServiceResponseCode.NOT_AVAILABLE);
				event.setOut(new String("This method is not available"));
			}

		} finally {
			event.setOutDate(new Date());
			this.events.publishEvent(event);
		}
		return event.toServiceResponse();
	}

	private boolean checkTenantIdIsPresent(ICaller caller) {
		if (!caller.isAnonymous() && (caller.getTenantId() == null || caller.getTenantId().isEmpty()))
			return false;
		return true;
	}

}
