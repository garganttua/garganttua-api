package org.sdc.spring.domain.crudify.connector.async;

import com.garganttua.api.repository.dto.AbstractSpringCrudifyDTOObject;
import com.garganttua.api.repository.dto.ISpringCrudifyDTOFactory;
import com.garganttua.api.repository.dto.ISpringCrudifyDTOObject;

public class Dto extends AbstractSpringCrudifyDTOObject<TestEntity>{

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
	public void update(ISpringCrudifyDTOObject<TestEntity> object) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ISpringCrudifyDTOFactory<TestEntity, ISpringCrudifyDTOObject<TestEntity>> getFactory() {
		// TODO Auto-generated method stub
		return null;
	}

}
