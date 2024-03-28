package com.garganttua.api.core.tools;

import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.objects.utils.GGAPIObjectReflectionHelper;
import com.garganttua.api.core.objects.utils.GGAPIObjectReflectionHelperExcpetion;

class SuperClass {
	Long superField;
}

class Entity extends SuperClass {
	String field;
	public String aMethod(String test, SuperClass sperClass) {
		return null;
	};
}

public class GGAPIObjectReflectionHelperTest {
	
	
	@Test
	public void testGetField() {
		Field fieldField = GGAPIObjectReflectionHelper.getField(Entity.class, "field");
		
		assertNotNull(fieldField);
		
		Field testField = GGAPIObjectReflectionHelper.getField(Entity.class, "test");
		
		assertNull(testField);
		
		Field superFieldField = GGAPIObjectReflectionHelper.getField(Entity.class, "superField");
		
		assertNotNull(superFieldField);
	}
	
	@Test
	public void testMethodAndParamsChecking() {
		Method method = GGAPIObjectReflectionHelper.getMethod(Entity.class, "aMethod");
		
		assertThatNoException().isThrownBy(()-> {
			GGAPIObjectReflectionHelper.checkMethodAndParams(method, "string", new SuperClass());			
		});
		
		assertThatException().isThrownBy(()-> {
			GGAPIObjectReflectionHelper.checkMethodAndParams(method, "string", new SuperClass());			
		});
	}
	

}
