package com.garganttua.api.core.entity.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.entity.GenericGGAPIEntity;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.entity.IGGAPIEntityDeleteMethod;
import com.garganttua.api.spec.entity.IGGAPIEntitySaveMethod;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.pageable.IGGAPIPageable;
import com.garganttua.api.spec.repository.IGGAPIRepository;
import com.garganttua.api.spec.sort.IGGAPISort;

@GGAPIEntity(domain = "test", interfaces = { "gg:test" })
class GenericEntity extends GenericGGAPIEntity {

}

public class GGAPIEntityHelperTest {
	
	@Test
	public void testSetAndGetMethods() throws GGAPIException {
		GenericEntity entity = new GenericEntity();
		
		assertNull(entity.getDeleteMethod());
		assertNull(entity.getId());
		assertNull(entity.getRepository());
		assertNull(entity.getSaveMethod());
		assertNull(entity.getUuid()); 
				
		GGAPIEntityHelper.setGotFromRepository(entity, false);
		GGAPIEntityHelper.setDeleteMethod(entity, new IGGAPIEntityDeleteMethod() {

			@Override
			public void delete(IGGAPICaller caller, Map<String, String> parameters, Object entity)
					throws GGAPIEntityException, GGAPIEngineException {
				// TODO Auto-generated method stub
				
			}
		});
		GGAPIEntityHelper.setSaveMethod(entity, new IGGAPIEntitySaveMethod() {

			@Override
			public Object save(IGGAPICaller caller, Map<String, String> parameters, Object entity) throws GGAPIEntityException {
				// TODO Auto-generated method stub
				return null;
			}
		});
		GGAPIEntityHelper.setId(entity, "id");
		GGAPIEntityHelper.setUuid(entity, "uuid");
		GGAPIEntityHelper.setRepository(entity, new IGGAPIRepository() {

			@Override
			public void setEngine(IGGAPIEngine engine) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean doesExist(IGGAPICaller caller, Object entity) throws GGAPIException {
				// TODO Auto-generated method stub
				return false;
			}


			@Override
			public void save(IGGAPICaller caller, Object entity) throws GGAPIException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public Object getOneById(IGGAPICaller caller, String id) throws GGAPIException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void delete(IGGAPICaller caller, Object entity) throws GGAPIException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean doesExist(IGGAPICaller caller, String uuid) throws GGAPIException {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public Object getOneByUuid(IGGAPICaller caller, String uuid) throws GGAPIException {
				// TODO Auto-generated method stub
				return null;
			}


			@Override
			public void setDaos(List daos) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setDomain(IGGAPIDomain domain) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public List<Object> getEntities(IGGAPICaller caller, IGGAPIPageable pageable, IGGAPIFilter filter,
					IGGAPISort sort) throws GGAPIException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public long getCount(IGGAPICaller caller, IGGAPIFilter filter) throws GGAPIException {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public String getTenant(Object entity) throws GGAPIException {
				// TODO Auto-generated method stub
				return null;
			}

			
		});
		
		assertEquals(false, GGAPIEntityHelper.isGotFromRepository(entity));
		assertEquals("id", GGAPIEntityHelper.getId(entity));
		assertEquals("uuid", GGAPIEntityHelper.getUuid(entity));
		assertNotNull(GGAPIEntityHelper.getDeleteMethodProvider(entity));
		assertNotNull(GGAPIEntityHelper.getSaveMethodProvider(entity));
		assertNotNull(GGAPIEntityHelper.getRepository(entity));
	}

}
