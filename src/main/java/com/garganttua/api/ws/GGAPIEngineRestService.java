package com.garganttua.api.ws;

import java.util.List;

import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.authorization.IGGAPIAuthorization;
import com.garganttua.api.spec.GGAPICrudAccess;
import com.garganttua.api.spec.IGGAPIDomain;
import com.garganttua.api.spec.IGGAPIEntity;

public class GGAPIEngineRestService extends AbstractGGAPIService<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> {

	public GGAPIEngineRestService(IGGAPIDomain<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> domain, IGGAPIController<IGGAPIEntity, IGGAPIDTOObject<IGGAPIEntity>> controller, boolean allow_creation, boolean allow_read_all, boolean allow_read_one, boolean allow_update_one, boolean allow_delete_one, boolean allow_delete_all, boolean allow_count) {
		super(domain);
		this.controller = controller;
		this.ALLOW_CREATION = allow_creation;
		this.ALLOW_GET_ALL = allow_read_all;
		this.ALLOW_GET_ONE = allow_read_one;
		this.ALLOW_UPDATE = allow_update_one;
		this.ALLOW_DELETE_ONE = allow_delete_one;
		this.ALLOW_DELETE_ALL = allow_delete_all;
		this.ALLOW_COUNT = allow_count;
	}

	@Override
	protected List<IGGAPIAuthorization> createCustomAuthorizations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void allow(boolean allow_creation, boolean allow_read_all, boolean allow_read_one,
			boolean allow_update_one, boolean allow_delete_one, boolean allow_delete_all,
			boolean allow_count) {
		this.ALLOW_CREATION = allow_creation;
		this.ALLOW_GET_ALL = allow_read_all;
		this.ALLOW_COUNT = allow_read_one;
		this.ALLOW_UPDATE = allow_update_one;
		this.ALLOW_DELETE_ONE = allow_delete_one;
		this.ALLOW_DELETE_ALL = allow_delete_all;
		this.ALLOW_COUNT = allow_count;
	}

	@Override
	public void setAccesses(GGAPICrudAccess creation_access, GGAPICrudAccess read_all_access,
			GGAPICrudAccess read_one_access, GGAPICrudAccess update_one_access, GGAPICrudAccess delete_one_access,
			GGAPICrudAccess delete_all_access, GGAPICrudAccess count_access) {
		this.CREATION_ACCESS = creation_access;
		this.GET_ALL_ACCESS = read_all_access;
		this.GET_ONE_ACCESS = read_one_access;
		this.UPDATE_ACCESS = update_one_access;
		this.DELETE_ONE_ACCESS = delete_one_access;
		this.DELETE_ALL_ACCESS = delete_all_access;
		this.COUNT_ACCESS = count_access;
		
	}

	@Override
	public void setAuthorities(boolean creation_authority, boolean read_all_authority, boolean read_one_authority,
			boolean update_one_authority, boolean delete_one_authority, boolean delete_all_authority,
			boolean count_authority) {
		this.CREATION_AUTHORITY = creation_authority;
		this.GET_ALL_AUTHORITY = read_all_authority;
		this.GET_ONE_AUTHORITY = read_one_authority;
		this.UPDATE_AUTHORITY = update_one_authority;
		this.DELETE_ONE_AUTHORITY = delete_one_authority;
		this.DELETE_ALL_AUTHORITY = delete_all_authority;
		this.COUNT_AUTHORITY = count_authority;
	}
}
