package com.garganttua.api.core.caller;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.ReflectionsAnnotationScanner;
import com.garganttua.api.core.domain.GGAPIDomainsFactory;
import com.garganttua.api.core.factory.GGAPIEntityFactoriesFactory;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICallerFactoriesRegistry;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.factory.IGGAPIFactoriesRegistry;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

public class GGAPICallerFactoriesFactoryTest {

	@BeforeAll
	public static void setupAnnotationScanner() {
		GGObjectReflectionHelper.annotationScanner = new ReflectionsAnnotationScanner();
	}
	
	@Test
	public void test() throws GGAPIException {
		GGAPIDomainsFactory dof = new GGAPIDomainsFactory(List.of("com"));
		Collection<IGGAPIDomain> domains = dof.getDomains();
		GGAPIEntityFactoriesFactory daf = new GGAPIEntityFactoriesFactory(domains, null);
		IGGAPIFactoriesRegistry reg = daf.getRegistry();
		
		GGAPICallerFactoriesFactory callerFactoriesFactory = new GGAPICallerFactoriesFactory(domains, reg, null);
		IGGAPICallerFactoriesRegistry registry = callerFactoriesFactory.getRegistry();

		assertNotNull(callerFactoriesFactory);
		assertNotNull(registry);
	}
	
}
