package com.garganttua.api.engine;

import com.garganttua.api.controller.ISpringCrudifyController;
import com.garganttua.api.repository.ISpringCrudifyRepository;
import com.garganttua.api.repository.dao.ISpringCrudifyDAORepository;
import com.garganttua.api.repository.dto.ISpringCrudifyDTOObject;
import com.garganttua.api.spec.ISpringCrudifyEntity;
import com.garganttua.api.ws.ISpringCrudifyRestService;

public interface ISpringCrudifyDynamicDomainEngine {

	ISpringCrudifyDAORepository<? extends ISpringCrudifyEntity, ? extends ISpringCrudifyDTOObject<? extends ISpringCrudifyEntity>> getDao(String name);

	ISpringCrudifyRepository<? extends ISpringCrudifyEntity, ? extends ISpringCrudifyDTOObject<? extends ISpringCrudifyEntity>> getRepository(String name);

	ISpringCrudifyController<? extends ISpringCrudifyEntity, ? extends ISpringCrudifyDTOObject<? extends ISpringCrudifyEntity>> getController(String name);

	ISpringCrudifyRestService<? extends ISpringCrudifyEntity, ? extends ISpringCrudifyDTOObject<? extends ISpringCrudifyEntity>> getService(String name);

}
