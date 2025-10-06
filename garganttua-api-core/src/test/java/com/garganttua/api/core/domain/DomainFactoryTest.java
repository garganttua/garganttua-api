package com.garganttua.api.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.ReflectionsAnnotationScanner;
import com.garganttua.api.core.old.domain.DomainsFactory;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.domain.IDomainsRegistry;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

public class DomainFactoryTest {

	@BeforeAll
	public static void setupAnnotationScanner() {
		GGObjectReflectionHelper.annotationScanner = new ReflectionsAnnotationScanner();
	}
	
	@Test
	public void testGetDomains() throws CoreException {
		DomainsFactory df = new DomainsFactory(List.of("com"));
		Collection<IDomain> domains = df.getDomains();
		
		assertNotNull(domains);
		assertEquals(2, domains.size());
	}
	
	@Test
	public void testGetDomainsRegistry() throws CoreException {
		DomainsFactory df = new DomainsFactory(List.of("com"));
		IDomainsRegistry reg = df.getRegistry();
		
		assertNotNull(reg);
		assertNotNull(reg.getDomains());
	}
}
