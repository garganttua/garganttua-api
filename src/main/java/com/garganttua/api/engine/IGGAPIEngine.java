package com.garganttua.api.engine;

import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.repository.dao.IGGAPIDAORepository;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.ws.IGGAPIRestService;

public interface IGGAPIEngine {

	IGGAPIDAORepository<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> getDao(String name);

	IGGAPIRepository<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> getRepository(String name);

	IGGAPIController<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> getController(String name);

	IGGAPIRestService<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> getService(String name);

}
