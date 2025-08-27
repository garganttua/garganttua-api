package com.garganttua.api.core.domain;

import com.garganttua.api.core.entity.GenericTenantEntity;
import com.garganttua.api.spec.entity.annotations.Entity;
import com.garganttua.api.spec.entity.annotations.EntityTenant;
import com.garganttua.api.spec.entity.annotations.EntityTenantId;

@Entity(domain = "test", interfaces = { "gg:test" })
@EntityTenant
public class TestEntity extends GenericTenantEntity {
	


}
