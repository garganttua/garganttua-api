package com.garganttua.api.repository;

import com.garganttua.api.repository.dao.ISpringCrudifyDAORepository;
import com.garganttua.api.repository.dto.ISpringCrudifyDTOObject;
import com.garganttua.api.spec.ISpringCrudifyDomain;
import com.garganttua.api.spec.ISpringCrudifyEntity;

public class SpringCrudifyEngineRepository extends SpringCrudifyRepository<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>> {

	public SpringCrudifyEngineRepository(ISpringCrudifyDomain<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>> domain, ISpringCrudifyDAORepository<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>> daoRepository) {
		super(domain);
		this.daoRepository = (ISpringCrudifyDAORepository<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>>) daoRepository;
	}

}
