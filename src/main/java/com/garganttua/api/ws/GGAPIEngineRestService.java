package com.garganttua.api.ws;

import java.util.List;

import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.authorization.IGGAPIAuthorization;
import com.garganttua.api.spec.IGGAPIDomain;
import com.garganttua.api.spec.IGGAPIEntity;

public class GGAPIEngineRestService extends AbstractGGAPIService<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> {

	public GGAPIEngineRestService(IGGAPIDomain<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> domain, IGGAPIController<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> controller, boolean authorize_creation, boolean authorize_read_all, boolean authorize_read_one, boolean authorize_update_one, boolean authorize_delete_one, boolean authorize_delete_all, boolean authorize_count) {
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
	protected List<IGGAPIAuthorization> createCustomAuthorizations() {
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
