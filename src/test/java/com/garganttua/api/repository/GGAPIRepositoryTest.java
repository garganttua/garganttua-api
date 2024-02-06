package com.garganttua.api.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.engine.GGAPIDynamicDomain;

public class GGAPIRepositoryTest {
	
	@Test
	public void testFilterPublicEntityAndNotSuperTenant() throws JsonProcessingException {
		
		GGAPICaller caller = new GGAPICaller();
		caller.setRequestedTenantId("0");
		caller.setSuperTenant(false);
		
		GGAPIDynamicDomain domain = new GGAPIDynamicDomain(null, null, null, null, null, null, null, null, null, null, null, false, false, false, false, false, false, false, null, null, null, null, null, null, null, false, false, false, false, false, false, false, false, true, null, null, false, null, null, false, false, false, false, null);
		
		GGAPILiteral filter = GGAPIRepository.getFilterFromCallerInfosAndDomainInfos(caller, domain, null);
		
		assertNull(filter);
		
	}
	
	@Test
	public void testFilterSuperTenant() throws JsonProcessingException {
		
		GGAPICaller caller = new GGAPICaller();
		caller.setRequestedTenantId("0");
		caller.setSuperTenant(true);
		
		GGAPIDynamicDomain domain = new GGAPIDynamicDomain(null, null, null, null, null, null, null, null, null, null, null, false, false, false, false, false, false, false, null, null, null, null, null, null, null, false, false, false, false, false, false, false, false, false, null, null, false, null, null, false, false, false, false, null);
		
		GGAPILiteral filter = GGAPIRepository.getFilterFromCallerInfosAndDomainInfos(caller, domain, null);
		
		assertNotNull(filter);	
		
		ObjectMapper mapper = new ObjectMapper();
		assertEquals("{\"name\":\"$field\",\"value\":\"tenantId\",\"literals\":[{\"name\":\"$eq\",\"value\":\"0\"}]}", mapper.writeValueAsString(filter));
	}
	
	@Test
	public void testFilterSuperTenantAndNoRequestedTenantId() throws JsonProcessingException {
		
		GGAPICaller caller = new GGAPICaller();
		caller.setRequestedTenantId(null);
		caller.setSuperTenant(true);
		
		GGAPIDynamicDomain domain = new GGAPIDynamicDomain(null, null, null, null, null, null, null, null, null, null, null, false, false, false, false, false, false, false, null, null, null, null, null, null, null, false, false, false, false, false, false, false, false, false, null, null, false, null, null, false, false, false, false, null);
		
		GGAPILiteral filter = GGAPIRepository.getFilterFromCallerInfosAndDomainInfos(caller, domain, null);
		
		assertNull(filter);	
	}
	
	@Test
	public void testFilterAnonymous() throws JsonProcessingException {
		
		GGAPICaller caller = new GGAPICaller();
		caller.setRequestedTenantId(null);
		caller.setSuperTenant(false);
		caller.setAnonymous(true);
		
		GGAPIDynamicDomain domain = new GGAPIDynamicDomain(null, null, null, null, null, null, null, null, null, null, null, false, false, false, false, false, false, false, null, null, null, null, null, null, null, false, false, false, false, false, false, false, false, false, null, null, false, null, null, false, false, false, false, null);
		
		GGAPILiteral filter = GGAPIRepository.getFilterFromCallerInfosAndDomainInfos(caller, domain, null);
		
		assertNull(filter);	
	}
	
	@Test
	public void testFilterSharedEntity() throws JsonProcessingException {
		
		GGAPICaller caller = new GGAPICaller();
		caller.setRequestedTenantId("0");
		caller.setSuperTenant(false);
		
		GGAPIDynamicDomain domain = new GGAPIDynamicDomain(null, null, null, null, null, null, null, null, null, null, null, false, false, false, false, false, false, false, null, null, null, null, null, null, null, false, false, false, false, false, false, false, false, false, "shareField", null, false, null, null, false, false, false, false, null);
		
		GGAPILiteral filter = GGAPIRepository.getFilterFromCallerInfosAndDomainInfos(caller, domain, null);
		
		assertNotNull(filter);	
		
		ObjectMapper mapper = new ObjectMapper();
		assertEquals("{\"name\":\"$or\",\"literals\":[{\"name\":\"$field\",\"value\":\"tenantId\",\"literals\":[{\"name\":\"$eq\",\"value\":\"0\"}]},{\"name\":\"$field\",\"value\":\"shareField\",\"literals\":[{\"name\":\"$eq\",\"value\":\"0\"}]}]}", mapper.writeValueAsString(filter));
	}
	
	@Test
	public void testFilterSharedEntityAndSuperTenant() throws JsonProcessingException {
		
		GGAPICaller caller = new GGAPICaller();
		caller.setRequestedTenantId("0");
		caller.setSuperTenant(true);
		
		GGAPIDynamicDomain domain = new GGAPIDynamicDomain(null, null, null, null, null, null, null, null, null, null, null, false, false, false, false, false, false, false, null, null, null, null, null, null, null, false, false, false, false, false, false, false, false, false, "shareField", null, false, null, null, false, false, false, false, null);
		
		GGAPILiteral filter = GGAPIRepository.getFilterFromCallerInfosAndDomainInfos(caller, domain, null);
		
		assertNotNull(filter);	
		
		ObjectMapper mapper = new ObjectMapper();
		assertEquals("{\"name\":\"$or\",\"literals\":[{\"name\":\"$field\",\"value\":\"tenantId\",\"literals\":[{\"name\":\"$eq\",\"value\":\"0\"}]},{\"name\":\"$field\",\"value\":\"shareField\",\"literals\":[{\"name\":\"$eq\",\"value\":\"0\"}]}]}", mapper.writeValueAsString(filter));
		
	}
	
	@Test
	public void testFilterSharedEntityAndHiddenable() throws JsonProcessingException {
		
		GGAPICaller caller = new GGAPICaller();
		caller.setRequestedTenantId("0");
		caller.setSuperTenant(false);
		
		GGAPIDynamicDomain domain = new GGAPIDynamicDomain(null, null, null, null, null, null, null, null, null, null, null, false, false, false, false, false, false, false, null, null, null, null, null, null, null, false, false, false, false, false, false, false, true, false, "shareField", null, false, null, null, false, false, false, false, null);
		
		GGAPILiteral filter = GGAPIRepository.getFilterFromCallerInfosAndDomainInfos(caller, domain, null);
		
		assertNotNull(filter);	
		
		ObjectMapper mapper = new ObjectMapper();
		assertEquals("{\"name\":\"$or\",\"literals\":[{\"name\":\"$field\",\"value\":\"tenantId\",\"literals\":[{\"name\":\"$eq\",\"value\":\"0\"}]},{\"name\":\"$and\",\"literals\":[{\"name\":\"$field\",\"value\":\"shareField\",\"literals\":[{\"name\":\"$eq\",\"value\":\"0\"}]},{\"name\":\"$field\",\"value\":\"visible\",\"literals\":[{\"name\":\"$eq\",\"value\":\"true\"}]}]}]}", mapper.writeValueAsString(filter));
	}
	
	@Test
	public void testFilterNotSharedEntityAndHiddenable() throws JsonProcessingException {
		
		GGAPICaller caller = new GGAPICaller();
		caller.setRequestedTenantId("0");
		caller.setSuperTenant(false);
		
		GGAPIDynamicDomain domain = new GGAPIDynamicDomain(null, null, null, null, null, null, null, null, null, null, null, false, false, false, false, false, false, false, null, null, null, null, null, null, null, false, false, false, false, false, false, false, true, false, null, null, false, null, null, false, false, false, false, null);
		
		GGAPILiteral filter = GGAPIRepository.getFilterFromCallerInfosAndDomainInfos(caller, domain, null);
		
		assertNotNull(filter);	
		
		ObjectMapper mapper = new ObjectMapper();
		assertEquals("{\"name\":\"$field\",\"value\":\"tenantId\",\"literals\":[{\"name\":\"$eq\",\"value\":\"0\"}]}", mapper.writeValueAsString(filter));
	}
	
	@Test
	public void testFilterSharedEntityAndHiddenableWithOtherFilter() throws JsonProcessingException {
		
		GGAPICaller caller = new GGAPICaller();
		caller.setRequestedTenantId("0");
		caller.setSuperTenant(false);
		
		GGAPIDynamicDomain domain = new GGAPIDynamicDomain(null, null, null, null, null, null, null, null, null, null, null, false, false, false, false, false, false, false, null, null, null, null, null, null, null, false, false, false, false, false, false, false, true, false, "shareField", null, false, null, null, false, false, false, false, null);
		
		GGAPILiteral otherfilterequal = new GGAPILiteral(GGAPILiteral.OPERATOR_EQUAL, "test", null);
		List<GGAPILiteral> l = new ArrayList<GGAPILiteral>();
		l.add(otherfilterequal);
		GGAPILiteral otherfilter = new GGAPILiteral(GGAPILiteral.OPERATOR_FIELD, "aField", l);
		
		GGAPILiteral filter = GGAPIRepository.getFilterFromCallerInfosAndDomainInfos(caller, domain, otherfilter );
		
		assertNotNull(filter);	
		
		ObjectMapper mapper = new ObjectMapper();
		assertEquals("{\"name\":\"$and\",\"literals\":[{\"name\":\"$field\",\"value\":\"aField\",\"literals\":[{\"name\":\"$eq\",\"value\":\"test\"}]},{\"name\":\"$or\",\"literals\":[{\"name\":\"$field\",\"value\":\"tenantId\",\"literals\":[{\"name\":\"$eq\",\"value\":\"0\"}]},{\"name\":\"$and\",\"literals\":[{\"name\":\"$field\",\"value\":\"shareField\",\"literals\":[{\"name\":\"$eq\",\"value\":\"0\"}]},{\"name\":\"$field\",\"value\":\"visible\",\"literals\":[{\"name\":\"$eq\",\"value\":\"true\"}]}]}]}]}", mapper.writeValueAsString(filter));
	}
	
	
	@Test
	public void testFilterPublicEntityAndHiddenable() throws JsonProcessingException {
		
		GGAPICaller caller = new GGAPICaller();
		caller.setRequestedTenantId("0");
		caller.setSuperTenant(false);
		
		GGAPIDynamicDomain domain = new GGAPIDynamicDomain(null, null, null, null, null, null, null, null, null, null, null, false, false, false, false, false, false, false, null, null, null, null, null, null, null, false, false, false, false, false, false, false, true, true, null, null, false, null, null, false, false, false, false, null);
		
		GGAPILiteral filter = GGAPIRepository.getFilterFromCallerInfosAndDomainInfos(caller, domain, null);
		
		assertNotNull(filter);	
		
		ObjectMapper mapper = new ObjectMapper();
		assertEquals("{\"name\":\"$field\",\"value\":\"visible\",\"literals\":[{\"name\":\"$eq\",\"value\":\"true\"}]}", mapper.writeValueAsString(filter));
	}

}
