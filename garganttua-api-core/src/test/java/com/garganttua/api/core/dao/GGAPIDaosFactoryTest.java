package com.garganttua.api.core.dao;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.domain.GGAPIDomainsFactory;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.dao.IGGAPIDaosRegistry;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.reflection.beans.GGBeanLoaderFactory;
import com.garganttua.reflection.beans.IGGBeanLoader;

public class GGAPIDaosFactoryTest {

	@Test
	public void test() throws GGAPIException {
		IGGBeanLoader beanLoader = GGBeanLoaderFactory.getLoader(List.of("com"));
		GGAPIDomainsFactory dof = new GGAPIDomainsFactory(List.of("com"));
		Collection<IGGAPIDomain> domains = dof.getDomains();
		
		GGAPIDaosFactory daf = new GGAPIDaosFactory(domains, beanLoader);
		IGGAPIDaosRegistry reg = daf.getRegistry();

		assertNotNull(daf);
		assertNotNull(reg);
	}
}
