package com.garganttua.api.core.caller;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.ReflectionsAnnotationScanner;
import com.garganttua.api.core.old.caller.CallerFactoriesFactory;
import com.garganttua.api.core.old.domain.DomainsFactory;
import com.garganttua.api.core.old.factory.EntityFactoriesFactory;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.caller.ICallerFactoriesRegistry;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.factory.IFactoriesRegistry;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

public class CallerFactoriesFactoryTest {

	@BeforeAll
	public static void setupAnnotationScanner() {
		GGObjectReflectionHelper.annotationScanner = new ReflectionsAnnotationScanner();
	}
	
	@Test
	public void test() throws CoreException {
		DomainsFactory dof = new DomainsFactory(List.of("com"));
		Collection<IDomain> domains = dof.getDomains();
		EntityFactoriesFactory daf = new EntityFactoriesFactory(domains, null);
		IFactoriesRegistry reg = daf.getRegistry();
		
		CallerFactoriesFactory callerFactoriesFactory = new CallerFactoriesFactory(domains, reg, null);
		ICallerFactoriesRegistry registry = callerFactoriesFactory.getRegistry();

		assertNotNull(callerFactoriesFactory);
		assertNotNull(registry);
	}
	
}
