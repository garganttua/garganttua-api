package com.garganttua.api.core.interfasse;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.ReflectionsAnnotationScanner;
import com.garganttua.api.core.domain.DomainsFactory;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.interfasse.IInterfacesRegistry;
import com.garganttua.reflection.beans.GGBeanLoaderFactory;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

public class InterfacesFactoryTest {

	@BeforeAll
	public static void setupAnnotationScanner() {
		GGObjectReflectionHelper.annotationScanner = new ReflectionsAnnotationScanner();
	}
	
	@Test
	public void test() throws CoreException {
		IGGBeanLoader beanLoader = GGBeanLoaderFactory.getLoader(null, List.of("com"));
		DomainsFactory dof = new DomainsFactory(List.of("com"));
		Collection<IDomain> domains = dof.getDomains();
		
		InterfacesFactory daf = new InterfacesFactory(domains, beanLoader);
		IInterfacesRegistry reg = daf.getRegistry();

		assertNotNull(daf);
		assertNotNull(reg);
	}
}
