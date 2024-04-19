package com.garganttua.api.core.entity.checker.annotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityAfterGet;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;

import lombok.Getter;

public class GGAPIBusinessAnnotationsTest {

	@Test()
	public void testGetAnnotatedMethod() {
		
		class testC  {
			
			@GGAPIEntityAfterGet
			private void test() {
				
			}

		}
		
		 GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			 GGAPIBusinessAnnotations.hasAnnotation(testC.class, GGAPIEntityAfterGet.class);
		 });
		 
		 assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());
	}
	
	@Test()
	public void testAnnotatedMethodParameter1() {
		
		class testC  {
			
			@GGAPIEntityAfterGet
			private void test( int i, long a ) {
				
			}

		}
		
		 GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			 GGAPIBusinessAnnotations.hasAnnotation(testC.class, GGAPIEntityAfterGet.class);
		 });
		 
		 assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode(), exception.getCode());
			}
	
	@Test()
	public void testAnnotatedMethodParameter2() {
		
		class testC {
			
			@GGAPIEntityAfterGet
			private void test( IGGAPICaller caller, long a ) {
				
			}

		}
		
		 GGAPIEntityException exception = assertThrows(GGAPIEntityException.class, () -> {
			 GGAPIBusinessAnnotations.hasAnnotation(testC.class, GGAPIEntityAfterGet.class);
		 });
		 
		 assertEquals(GGAPIEntityException.ENTITY_DEFINITION_ERROR, exception.getCode());

	}
	
	@Test()
	public void testAnnotatedMethod() throws GGAPIEntityException {
		
		class testC{
			
			@GGAPIEntityAfterGet
			private void test( IGGAPICaller caller, Map<String, String> a ) {
				
			}

		}
		assertNotNull(GGAPIBusinessAnnotations.hasAnnotation(testC.class, GGAPIEntityAfterGet.class));		
	}
}
