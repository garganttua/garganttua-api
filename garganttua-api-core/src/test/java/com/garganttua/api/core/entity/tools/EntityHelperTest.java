package com.garganttua.api.core.entity.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.ReflectionsAnnotationScanner;
import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.core.entity.GenericEntity;
import com.garganttua.api.core.entity.exceptions.EntityException;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.entity.IEntityDeleteMethod;
import com.garganttua.api.spec.entity.IEntitySaveMethod;
import com.garganttua.api.spec.entity.annotations.Entity;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity(domain = "test", interfaces = { "gg:test" })
class GenericEntityTest extends GenericEntity {

	protected GenericEntityTest(String uuid, String id) {
		super(uuid, id);
	}

}

public class EntityHelperTest {
	@BeforeAll
	public static void setupAnnotationScanner() {
		GGObjectReflectionHelper.annotationScanner = new ReflectionsAnnotationScanner();
	}
	
	@Test
	public void testSetAndGetMethods() throws CoreException {
		GenericEntity entity = new GenericEntityTest(null, null);
		
		assertNull(entity.getDeleteMethod());
		assertNull(entity.getId());
		assertNull(entity.getRepository());
		assertNull(entity.getSaveMethod());
		assertNull(entity.getUuid()); 
				
		EntityHelper.setGotFromRepository(entity, false);
		EntityHelper.setDeleteMethod(entity, new IEntityDeleteMethod() {

			@Override
			public void delete(ICaller caller, Map<String, String> parameters, Object entity)
					throws EntityException, EngineException {
				// TODO Auto-generated method stub
				
			}
		});
		EntityHelper.setSaveMethod(entity, new IEntitySaveMethod() {

			@Override
			public Object save(ICaller caller, Map<String, String> parameters, Object entity) throws EntityException {
				// TODO Auto-generated method stub
				return null;
			}
		});
		EntityHelper.setId(entity, "id");
		EntityHelper.setUuid(entity, "uuid");
		EntityHelper.setRepository(entity, new IRepository() {

			@Override
			public void setEngine(IEngine engine) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean doesExist(ICaller caller, Object entity) throws CoreException {
				// TODO Auto-generated method stub
				return false;
			}


			@Override
			public void save(ICaller caller, Object entity) throws CoreException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public Optional<Object> getOneById(ICaller caller, String id) throws CoreException {
				// TODO Auto-generated method stub
				return Optional.empty();
			}

			@Override
			public void delete(ICaller caller, Object entity) throws CoreException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean doesExist(ICaller caller, String uuid) throws CoreException {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public Optional<Object> getOneByUuid(ICaller caller, String uuid) throws CoreException {
				// TODO Auto-generated method stub
				return Optional.empty();
			}


			@Override
			public void setDaos(List daos) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setDomain(IDomain domain) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public List<Object> getEntities(ICaller caller, IPageable pageable, IFilter filter,
					ISort sort) throws CoreException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public long getCount(ICaller caller, IFilter filter) throws CoreException {
				// TODO Auto-generated method stub
				return 0;
			}


			
		});
		
		assertEquals(false, EntityHelper.isGotFromRepository(entity));
		assertEquals("id", EntityHelper.getId(entity));
		assertEquals("uuid", EntityHelper.getUuid(entity));
		assertNotNull(EntityHelper.getDeleteMethodProvider(entity));
		assertNotNull(EntityHelper.getSaveMethodProvider(entity));
		assertNotNull(EntityHelper.getRepository(entity));
	}

}
