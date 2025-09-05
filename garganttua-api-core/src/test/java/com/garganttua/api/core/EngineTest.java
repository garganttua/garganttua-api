package com.garganttua.api.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.engine.Builder;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.reflection.beans.GGBeanLoaderFactory;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.properties.GGPropertyLoader;
import com.garganttua.reflection.properties.IGGPropertyLoader;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

public class EngineTest {
	
	@BeforeAll
	public static void setupAnnotationScanner() {
		GGObjectReflectionHelper.annotationScanner = new ReflectionsAnnotationScanner();
	}
	
	@Test
	public void test() throws CoreException {
		IGGPropertyLoader pl = new GGPropertyLoader();
		IGGBeanLoader l = GGBeanLoaderFactory.getLoader(null, List.of("com"));
		IEngine engine = Builder.builder().propertyLoader(pl).packages(List.of("com")).beanLoader(l).build().init().start();
		
		assertNotNull(engine);
	}
	
	
	@Test
	public void testNoPackages() throws CoreException {
		IGGBeanLoader l = GGBeanLoaderFactory.getLoader(null, List.of("com"));

		CoreException exception = assertThrows(CoreException.class, () -> {
			Builder.builder().beanLoader(l).build().init();
		});
		
		assertNotNull(exception);
		assertEquals(CoreExceptionCode.CORE_GENERIC_CODE, exception.getCode());
	}
}
