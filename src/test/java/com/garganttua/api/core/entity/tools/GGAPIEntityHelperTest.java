package com.garganttua.api.core.entity.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.domain.GGAPIDomain;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.entity.GenericGGAPIEntity;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.repository.GGAPIRepositoryException;
import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.dao.GGAPILiteral;
import com.garganttua.api.spec.dao.GGAPISort;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;
import com.garganttua.api.spec.entity.annotations.IGGAPIEntityDeleteMethod;
import com.garganttua.api.spec.entity.annotations.IGGAPIEntitySaveMethod;
import com.garganttua.api.spec.repository.IGGAPIRepository;

@GGAPIEntity(domain = "test")
class GenericEntity extends GenericGGAPIEntity {

}

public class GGAPIEntityHelperTest {
	
	@Test
	public void testSetAndGetMethods() throws GGAPIEntityException {
		GenericEntity entity = new GenericEntity();
		
		assertNull(entity.getDeleteMethod());
		assertNull(entity.getId());
		assertNull(entity.getRepository());
		assertNull(entity.getSaveMethod());
		assertNull(entity.getUuid());
				
		GGAPIEntityHelper.setGotFromRepository(entity, false);
		GGAPIEntityHelper.setDeleteMethod(entity, new IGGAPIEntityDeleteMethod<Object>() {

			@Override
			public void delete(IGGAPICaller caller, Map<String, String> parameters, Object entity)
					throws GGAPIEntityException, GGAPIEngineException {
				// TODO Auto-generated method stub
				
			}
		});
		GGAPIEntityHelper.setSaveMethod(entity, new IGGAPIEntitySaveMethod<Object>() {

			@Override
			public void save(IGGAPICaller caller, Map<String, String> parameters, Object entity) throws GGAPIEntityException {
				// TODO Auto-generated method stub
				
			}
		});
		GGAPIEntityHelper.setId(entity, "id");
		GGAPIEntityHelper.setUuid(entity, "uuid");
		GGAPIEntityHelper.setRepository(entity, new IGGAPIRepository<Object>	() {

			@Override
			public void setEngine(IGGAPIEngine engine) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setDomain(GGAPIDomain domain) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean doesExist(IGGAPICaller caller, Object entity) throws GGAPIRepositoryException {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public List<Object> getEntities(IGGAPICaller caller, int pageSize, int pageIndex, GGAPILiteral filter,
					GGAPISort sort) throws GGAPIRepositoryException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void save(IGGAPICaller caller, Object entity) throws GGAPIRepositoryException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public Object update(IGGAPICaller caller, Object entity) throws GGAPIRepositoryException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Object getOneById(IGGAPICaller caller, String id) throws GGAPIRepositoryException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void delete(IGGAPICaller caller, Object entity) throws GGAPIRepositoryException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean doesExist(IGGAPICaller caller, String uuid) throws GGAPIRepositoryException {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public Object getOneByUuid(IGGAPICaller caller, String uuid) throws GGAPIRepositoryException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public long getCount(IGGAPICaller caller, GGAPILiteral filter) throws GGAPIRepositoryException {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public String getTenant(Object entity) throws GGAPIRepositoryException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void setDaos(List daos) {
				// TODO Auto-generated method stub
				
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
