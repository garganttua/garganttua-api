package com.garganttua.api.core.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.ReflectionsAnnotationScanner;
import com.garganttua.api.core.domain.GGAPIDomainsFactory;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.repository.IGGAPIRepositoriesRegistry;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

public class GGAPIReposirotyFactoryTest {

	@BeforeAll
	public static void setupAnnotationScanner() {
		GGObjectReflectionHelper.annotationScanner = new ReflectionsAnnotationScanner();
	}
	
	@Test
	public void test() throws GGAPIEngineException {
		GGAPIDomainsFactory dof = new GGAPIDomainsFactory(List.of("com"));
		Collection<IGGAPIDomain> domains = dof.getDomains();
		
		GGAPIRepositoriesFactory rf = new GGAPIRepositoriesFactory(domains);
		IGGAPIRepositoriesRegistry reg = rf.getRegistry();

		assertNotNull(rf);
		assertNotNull(reg);
	}
}
