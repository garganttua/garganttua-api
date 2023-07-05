package com.garganttua.api.controller;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.business.IGGAPIBusiness;
import com.garganttua.api.connector.IGGAPIConnector;
import com.garganttua.api.events.IGGAPIEventPublisher;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.IGGAPIDomain;
import com.garganttua.api.spec.IGGAPIEntity;

public class GGAPIEngineController extends GGAPIController<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> {

	public GGAPIEngineController(
			IGGAPIDomain<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> domain,
			Optional<IGGAPIRepository<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>>> repository,
			Optional<IGGAPIConnector<IGGAPIEntity, List<IGGAPIEntity>, IGGAPIDTOObject<IGGAPIEntity>>> connector,
			Optional<IGGAPIBusiness<IGGAPIEntity>> business,
			Optional<IGGAPIEventPublisher> event, boolean tenant) {
		super(domain);
		this.repository = repository;
		this.connector = connector;
		this.business = business;
		this.eventPublisher = event;
		this.tenant = tenant;
	}

}
