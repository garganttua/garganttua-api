package com.garganttua.api.core.domain;

import com.garganttua.api.core.entity.GenericGGAPITenantEntity;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityTenant;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityTenantId;

@GGAPIEntity(domain = "test", interfaces = { "gg:test" })
@GGAPIEntityTenant
public class TestEntity extends GenericGGAPITenantEntity {
	


}
