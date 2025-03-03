package com.garganttua.api.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.ReflectionsAnnotationScanner;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.domain.IGGAPIDomainsRegistry;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

public class GGAPIDomainFactoryTest {

	@BeforeAll
	public static void setupAnnotationScanner() {
		GGObjectReflectionHelper.annotationScanner = new ReflectionsAnnotationScanner();
	}
	
	@Test
	public void testGetDomains() throws GGAPIException {
		GGAPIDomainsFactory df = new GGAPIDomainsFactory(List.of("com"));
		Collection<IGGAPIDomain> domains = df.getDomains();
		
		assertNotNull(domains);
		assertEquals(2, domains.size());
	}
	
	@Test
	public void testGetDomainsRegistry() throws GGAPIException {
		GGAPIDomainsFactory df = new GGAPIDomainsFactory(List.of("com"));
		IGGAPIDomainsRegistry reg = df.getRegistry();
		
		assertNotNull(reg);
		assertNotNull(reg.getDomains());
	}
}
