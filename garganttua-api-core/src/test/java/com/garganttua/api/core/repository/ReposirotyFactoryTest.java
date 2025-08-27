package com.garganttua.api.core.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.ReflectionsAnnotationScanner;
import com.garganttua.api.core.domain.DomainsFactory;
import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.repository.IRepositoriesRegistry;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

public class ReposirotyFactoryTest {

	@BeforeAll
	public static void setupAnnotationScanner() {
		GGObjectReflectionHelper.annotationScanner = new ReflectionsAnnotationScanner();
	}
	
	@Test
	public void test() throws EngineException {
		DomainsFactory dof = new DomainsFactory(List.of("com"));
		Collection<IDomain> domains = dof.getDomains();
		
		RepositoriesFactory rf = new RepositoriesFactory(domains);
		IRepositoriesRegistry reg = rf.getRegistry();

		assertNotNull(rf);
		assertNotNull(reg);
	}
}
