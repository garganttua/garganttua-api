package com.garganttua.api.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.engine.GGApiBuilder;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.reflection.beans.GGBeanLoaderFactory;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.properties.GGPropertyLoader;
import com.garganttua.reflection.properties.IGGPropertyLoader;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

public class GGAPIEngineTest {
	
	@BeforeAll
	public static void setupAnnotationScanner() {
		GGObjectReflectionHelper.annotationScanner = new ReflectionsAnnotationScanner();
	}
	
	@Test
	public void test() throws GGAPIException {
		IGGPropertyLoader pl = new GGPropertyLoader();
		IGGBeanLoader l = GGBeanLoaderFactory.getLoader(null, List.of("com"));
		IGGAPIEngine engine = GGApiBuilder.builder().propertyLoader(pl).packages(List.of("com")).beanLoader(l).build().init().start();
		
		assertNotNull(engine); 
	}
	
	
	@Test
	public void testNoPackages() throws GGAPIException {
		IGGBeanLoader l = GGBeanLoaderFactory.getLoader(null, List.of("com"));

		GGAPIException exception = assertThrows(GGAPIException.class, () -> {
			GGApiBuilder.builder().beanLoader(l).build().init();
		});
		
		assertNotNull(exception);
		assertEquals(GGAPIExceptionCode.CORE_GENERIC_CODE, exception.getCode());
	}
}
