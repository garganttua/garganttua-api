package com.garganttua.api.connector.async;

import com.garganttua.api.repository.dto.AbstractGGAPIDTOObject;
import com.garganttua.api.repository.dto.IGGAPIDTOFactory;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;

public class Dto extends AbstractGGAPIDTOObject<TestEntity>{

	@Override
	public void create(TestEntity entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TestEntity convert() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(IGGAPIDTOObject<TestEntity> object) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IGGAPIDTOFactory<TestEntity, IGGAPIDTOObject<TestEntity>> getFactory() {
		// TODO Auto-generated method stub
		return null;
	}

}
