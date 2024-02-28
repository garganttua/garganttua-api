package com.garganttua.api.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityAfterGet;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntity;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntityDeleteMethod;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntitySaveMethod;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.security.IGGAPISecurity;

import lombok.Getter;

public class GGAPIBusinessAnnotationsTest {

	@Test()
	public void testGetAnnotatedMethod() {
		
		class testC implements IGGAPIEntity{
			
			@GGAPIEntityAfterGet
			private void test() {
				
			}

			
			@Override
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security)
					throws GGAPIEntityException, GGAPIEngineException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void delete(IGGAPICaller caller, Map<String, String> parameters)
					throws GGAPIEntityException, GGAPIEngineException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setRepository(IGGAPIRepository repository) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setSaveMethod(IGGAPIEntitySaveMethod saveMethod) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setDeleteMethod(IGGAPIEntityDeleteMethod deleteMethod) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setGotFromRepository(boolean gotFromRepository) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean isGotFromRepository() {
				// TODO Auto-generated method stub
				return false;
			}
		}
		
		 GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			 GGAPIBusinessAnnotations.hasAnnotation(testC.class, GGAPIEntityAfterGet.class);
		 });
		 
		 assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test()
	public void testAnnotatedMethodParameter1() {
		
		class testC  implements IGGAPIEntity {
			
			@GGAPIEntityAfterGet
			private void test( int i, long a ) {
				
			}

			@Override
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security)
					throws GGAPIEntityException, GGAPIEngineException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void delete(IGGAPICaller caller, Map<String, String> parameters)
					throws GGAPIEntityException, GGAPIEngineException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setRepository(IGGAPIRepository repository) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setSaveMethod(IGGAPIEntitySaveMethod saveMethod) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setDeleteMethod(IGGAPIEntityDeleteMethod deleteMethod) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setGotFromRepository(boolean gotFromRepository) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean isGotFromRepository() {
				// TODO Auto-generated method stub
				return false;
			}
		}
		
		 GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			 GGAPIBusinessAnnotations.hasAnnotation(testC.class, GGAPIEntityAfterGet.class);
		 });
		 
		 assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode(), exception.getCode());
			}
	
	@Test()
	public void testAnnotatedMethodParameter2() {
		
		class testC  implements IGGAPIEntity{
			
			@GGAPIEntityAfterGet
			private void test( IGGAPICaller caller, long a ) {
				
			}


			@Override
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security)
					throws GGAPIEntityException, GGAPIEngineException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void delete(IGGAPICaller caller, Map<String, String> parameters)
					throws GGAPIEntityException, GGAPIEngineException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setRepository(IGGAPIRepository repository) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setSaveMethod(IGGAPIEntitySaveMethod saveMethod) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setDeleteMethod(IGGAPIEntityDeleteMethod deleteMethod) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setGotFromRepository(boolean gotFromRepository) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean isGotFromRepository() {
				// TODO Auto-generated method stub
				return false;
			}
		}
		
		 GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			 GGAPIBusinessAnnotations.hasAnnotation(testC.class, GGAPIEntityAfterGet.class);
		 });
		 
		 assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());

	}
	
	@Test()
	public void testAnnotatedMethod() throws GGAPIEntityException {
		
		class testC implements IGGAPIEntity{
			
			@GGAPIEntityAfterGet
			private void test( IGGAPICaller caller, Map<String, String> a ) {
				
			}


			@Override
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security)
					throws GGAPIEntityException, GGAPIEngineException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void delete(IGGAPICaller caller, Map<String, String> parameters)
					throws GGAPIEntityException, GGAPIEngineException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setRepository(IGGAPIRepository repository) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setSaveMethod(IGGAPIEntitySaveMethod saveMethod) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setDeleteMethod(IGGAPIEntityDeleteMethod deleteMethod) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setGotFromRepository(boolean gotFromRepository) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean isGotFromRepository() {
				// TODO Auto-generated method stub
				return false;
			}
		}
		assertNotNull(GGAPIBusinessAnnotations.hasAnnotation(testC.class, GGAPIEntityAfterGet.class));		
	}
	
	@Test
	public void testInvokeMethod() throws GGAPIEntityException {
		
		class TestC implements IGGAPIEntity {
			
			@Getter
			private int i = 0;
			
			@GGAPIEntityAfterGet
			private void test( IGGAPICaller caller, Map<String, String> map ) {
				i++;
			}

			@Override
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security)
					throws GGAPIEntityException, GGAPIEngineException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void delete(IGGAPICaller caller, Map<String, String> parameters)
					throws GGAPIEntityException, GGAPIEngineException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setRepository(IGGAPIRepository repository) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setSaveMethod(IGGAPIEntitySaveMethod saveMethod) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setDeleteMethod(IGGAPIEntityDeleteMethod deleteMethod) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setGotFromRepository(boolean gotFromRepository) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean isGotFromRepository() {
				// TODO Auto-generated method stub
				return false;
			}
		}
		
		TestC c = new TestC();
		
		assertEquals(0, c.i);
		
		GGAPIBusinessAnnotations.hasAnnotationAndInvoke(c.getClass(), GGAPIEntityAfterGet.class, c, null, null);
		
		assertEquals(1, c.i);
		
	}
	
	
	@Test
	public void testInvokeNoMethod() throws GGAPIEntityException {
		
		class TestC implements IGGAPIEntity {
			
			@Getter
			private int i = 0;
			
			private void test( IGGAPICaller caller, Map<String, String> map ) {
				i++;
			}


			@Override
			public void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security)
					throws GGAPIEntityException, GGAPIEngineException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void delete(IGGAPICaller caller, Map<String, String> parameters)
					throws GGAPIEntityException, GGAPIEngineException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setRepository(IGGAPIRepository repository) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setSaveMethod(IGGAPIEntitySaveMethod saveMethod) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setDeleteMethod(IGGAPIEntityDeleteMethod deleteMethod) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setGotFromRepository(boolean gotFromRepository) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean isGotFromRepository() {
				// TODO Auto-generated method stub
				return false;
			}
		}
		
		TestC c = new TestC();
		
		assertEquals(0, c.i);
		
		GGAPIBusinessAnnotations.hasAnnotationAndInvoke(c.getClass(), GGAPIEntityAfterGet.class, c, null, null);
		
		assertEquals(0, c.i);
		
	}
}
