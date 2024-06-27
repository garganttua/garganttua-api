package com.garganttua.api.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIDomainsRegistry;

public class GGAPIDomainFactoryTest {

	@Test
	public void testGetDomains() {
		GGAPIDomainsFactory df = new GGAPIDomainsFactory(List.of("com"));
		Collection<IGGAPIDomain> domains = df.getDomains();
		
		assertNotNull(domains);
		assertEquals(1, domains.size());
	}
	
	@Test
	public void testGetDomainsRegistry() {
		GGAPIDomainsFactory df = new GGAPIDomainsFactory(List.of("com"));
		IGGAPIDomainsRegistry reg = df.getRegistry();
		
		assertNotNull(reg);
		assertNotNull(reg.getDomains());
		
	}
}
