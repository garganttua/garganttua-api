package com.garganttua.api.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.engine.GGApiBuilder;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.reflection.beans.GGBeanLoaderFactory;
import com.garganttua.reflection.beans.IGGBeanLoader;

public class GGAPIEngineTest {
	
	@Test
	public void test() throws GGAPIException {
		IGGBeanLoader l = GGBeanLoaderFactory.getLoader(null, List.of("com"));
		IGGAPIEngine engine = GGApiBuilder.builder().setPropertyLoader(null).setSecurity(null).setPackages(List.of("com")).setBeanLoader(l).build().init().start();
		
		assertNotNull(engine.getDomainsRegistry());
	}
	
	
	@Test
	public void testNoPackages() throws GGAPIException {
		IGGBeanLoader l = GGBeanLoaderFactory.getLoader(null, List.of("com"));

		GGAPIException exception = assertThrows(GGAPIException.class, () -> {
			GGApiBuilder.builder().setBeanLoader(l).build().init();
		});
		
		assertNotNull(exception);
		assertEquals(GGAPIExceptionCode.CORE_GENERIC_CODE, exception.getCode());
	}

}
