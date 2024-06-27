package com.garganttua.api.core;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.engine.GGApiBuilder;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.reflection.beans.GGBeanLoaderFactory;
import com.garganttua.reflection.beans.IGGBeanLoader;

public class GGAPIEngineTest {
	
	@Test
	public void test() throws GGAPIException {
		IGGBeanLoader l = GGBeanLoaderFactory.getLoader(List.of("com"));
		IGGAPIEngine engine = GGApiBuilder.builder().setSecurity(null).setBeanLoader(l).build().start();
		
	}

}
