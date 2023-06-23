package com.garganttua.api.ws;

import java.util.List;

import com.garganttua.api.controller.ISpringCrudifyController;
import com.garganttua.api.repository.dto.ISpringCrudifyDTOObject;
import com.garganttua.api.security.authorization.ISpringCrudifyAuthorization;
import com.garganttua.api.spec.ISpringCrudifyDomain;
import com.garganttua.api.spec.ISpringCrudifyEntity;

public class SpringCrudifyEngineRestService extends AbstractSpringCrudifyService<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>> {

	public SpringCrudifyEngineRestService(ISpringCrudifyDomain<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>> domain, ISpringCrudifyController<ISpringCrudifyEntity, ISpringCrudifyDTOObject<ISpringCrudifyEntity>> controller, boolean authorize_creation, boolean authorize_read_all, boolean authorize_read_one, boolean authorize_update_one, boolean authorize_delete_one, boolean authorize_delete_all, boolean authorize_count) {
		super(domain);
		this.controller = controller;
		this.AUTHORIZE_CREATION = authorize_creation;
		this.AUTHORIZE_GET_ALL = authorize_read_all;
		this.AUTHORIZE_GET_ONE = authorize_read_one;
		this.AUTHORIZE_UPDATE = authorize_update_one;
		this.AUTHORIZE_DELETE_ONE = authorize_delete_one;
		this.AUTHORIZE_DELETE_ALL = authorize_delete_all;
		this.AUTHORIZE_COUNT = authorize_count;
	}

	@Override
	protected List<ISpringCrudifyAuthorization> createCustomAuthorizations() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void authorize(boolean authorize_creation, boolean authorize_read_all, boolean authorize_read_one,
			boolean authorize_update_one, boolean authorize_delete_one, boolean authorize_delete_all,
			boolean authorize_count) {
		this.AUTHORIZE_CREATION = authorize_creation;
		this.AUTHORIZE_GET_ALL = authorize_read_all;
		this.AUTHORIZE_COUNT = authorize_read_one;
		this.AUTHORIZE_UPDATE = authorize_update_one;
		this.AUTHORIZE_DELETE_ONE = authorize_delete_one;
		this.AUTHORIZE_DELETE_ALL = authorize_delete_all;
		this.AUTHORIZE_COUNT = authorize_count;
	}
}
