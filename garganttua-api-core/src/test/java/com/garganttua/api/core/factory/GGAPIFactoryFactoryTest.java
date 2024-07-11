package com.garganttua.api.core.factory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.domain.GGAPIDomainsFactory;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.factory.IGGAPIFactoriesRegistry;

public class GGAPIFactoryFactoryTest {

	@Test
	public void test() throws GGAPIException {
		GGAPIDomainsFactory dof = new GGAPIDomainsFactory(List.of("com"));
		Collection<IGGAPIDomain> domains = dof.getDomains();
		
		GGAPIEntityFactoriesFactory daf = new GGAPIEntityFactoriesFactory(domains);
		IGGAPIFactoriesRegistry reg = daf.getRegistry();

		assertNotNull(daf);
		assertNotNull(reg);	
	}
}
