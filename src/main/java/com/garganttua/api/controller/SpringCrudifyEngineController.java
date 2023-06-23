package com.garganttua.api.controller;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.business.ISpringCrudifyBusiness;
import com.garganttua.api.connector.ISpringCrudifyConnector;
import com.garganttua.api.events.ISpringCrudifyEventPublisher;
import com.garganttua.api.repository.ISpringCrudifyRepository;
import com.garganttua.api.repository.dto.ISpringCrudifyDTOObject;
import com.garganttua.api.spec.ISpringCrudifyDomain;
import com.garganttua.api.spec.ISpringCrudifyEntity;

public class SpringCrudifyEngineController extends SpringCrudifyController<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>> {

	public SpringCrudifyEngineController(
			ISpringCrudifyDomain<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>> domain,
			Optional<ISpringCrudifyRepository<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>>> repository,
			Optional<ISpringCrudifyConnector<ISpringCrudifyEntity, List<ISpringCrudifyEntity>, ISpringCrudifyDTOObject<ISpringCrudifyEntity>>> connector,
			Optional<ISpringCrudifyBusiness<ISpringCrudifyEntity>> business,
			Optional<ISpringCrudifyEventPublisher> event ) {
		super(domain);
		this.repository = repository;
		this.connector = connector;
		this.business = business;
		this.eventPublisher = event;
	}

}
