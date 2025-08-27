package com.garganttua.api.core.repository;

import org.junit.jupiter.api.BeforeAll;

import com.garganttua.api.core.ReflectionsAnnotationScanner;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

public class RepositoryTest {
	
	@BeforeAll
	public static void setupAnnotationScanner() {
		GGObjectReflectionHelper.annotationScanner = new ReflectionsAnnotationScanner();
	}
	
//	@Test
//	public void testFilterPublicEntityAndNotSuperTenant() throws JsonProcessingException {
//		
//		Caller caller = new Caller();
//		caller.setRequestedTenantId("0");
//		caller.setSuperTenant(false);
//		
//		Domain domain = new Domain(null, null, null, null, null, null, null, null, false, false, false, false, false, false, false, null, null, null, null, null, null, null, false, false, false, false, false, false, false, false, true, null, null, false, null, null, false, false, false, false);
//		
//		Literal filter = Repository.getFilterFromCallerInfosAndDomainInfos(caller, domain, null);
//		
//		assertNull(filter);
//		
//	}
//	
//	@Test
//	public void testFilterSuperTenant() throws JsonProcessingException {
//		
//		Caller caller = new Caller();
//		caller.setTenantId("1");
//		caller.setRequestedTenantId("0");
//		caller.setSuperTenant(true);
//		
//		Domain domain = new Domain(null, null, null, null, null,  null, null, null, false, false, false, false, false, false, false, null, null, null, null, null, null, null, false, false, false, false, false, false, false, false, false, null, null, false, null, null, false, false, false, false);
//		
//		Literal filter = Repository.getFilterFromCallerInfosAndDomainInfos(caller, domain, null);
//		
//		assertNotNull(filter);	
//		
//		ObjectMapper mapper = new ObjectMapper();
//		assertEquals("{\"name\":\"$field\",\"value\":\"tenantId\",\"literals\":[{\"name\":\"$eq\",\"value\":\"0\"}]}", mapper.writeValueAsString(filter));
//	}
//	
//	@Test
//	public void testFilterSuperTenantAndNoRequestedTenantId() throws JsonProcessingException {
//		
//		Caller caller = new Caller();
//		caller.setRequestedTenantId(null);
//		caller.setSuperTenant(true);
//		
//		Domain domain = new Domain(null, null, null, null, null, null, null, null, false, false, false, false, false, false, false, null, null, null, null, null, null, null, false, false, false, false, false, false, false, false, false, null, null, false, null, null, false, false, false, false);
//		
//		Literal filter = Repository.getFilterFromCallerInfosAndDomainInfos(caller, domain, null);
//		
//		assertNull(filter);	
//	}
//	
//	@Test
//	public void testFilterAnonymous() throws JsonProcessingException {
//		
//		Caller caller = new Caller();
//		caller.setRequestedTenantId(null);
//		caller.setSuperTenant(false);
//		caller.setAnonymous(true);
//		
//		Domain domain = new Domain(null, null, null, null, null, null, null, null, false, false, false, false, false, false, false, null, null, null, null, null, null, null, false, false, false, false, false, false, false, false, false, null, null, false, null, null, false, false, false, false);
//		
//		Literal filter = Repository.getFilterFromCallerInfosAndDomainInfos(caller, domain, null);
//		
//		assertNull(filter);	
//	}
//	
//	@Test
//	public void testFilterSharedEntity() throws JsonProcessingException {
//		
//		Caller caller = new Caller();
//		caller.setRequestedTenantId("0");
//		caller.setSuperTenant(false);
//		
//		Domain domain = new Domain(null, null, null, null, null, null, null, null, false, false, false, false, false, false, false, null, null, null, null, null, null, null, false, false, false, false, false, false, false, false, false, "shareField", null, false, null, null, false, false, false, false);
//		
//		Literal filter = Repository.getFilterFromCallerInfosAndDomainInfos(caller, domain, null);
//		
//		assertNotNull(filter);	
//		
//		ObjectMapper mapper = new ObjectMapper();
//		assertEquals("{\"name\":\"$or\",\"literals\":[{\"name\":\"$field\",\"value\":\"tenantId\",\"literals\":[{\"name\":\"$eq\",\"value\":\"0\"}]},{\"name\":\"$field\",\"value\":\"shareField\",\"literals\":[{\"name\":\"$eq\",\"value\":\"0\"}]}]}", mapper.writeValueAsString(filter));
//	}
//	
//	@Test
//	public void testFilterSharedEntityAndSuperTenant() throws JsonProcessingException {
//		
//		Caller caller = new Caller();
//		caller.setRequestedTenantId("0");
//		caller.setSuperTenant(true);
//		
//		Domain domain = new Domain(null, null, null, null, null, null, null, null, false, false, false, false, false, false, false, null, null, null, null, null, null, null, false, false, false, false, false, false, false, false, false, "shareField", null, false, null, null, false, false, false, false);
//		
//		Literal filter = Repository.getFilterFromCallerInfosAndDomainInfos(caller, domain, null);
//		
//		assertNotNull(filter);	
//		
//		ObjectMapper mapper = new ObjectMapper();
//		assertEquals("{\"name\":\"$or\",\"literals\":[{\"name\":\"$field\",\"value\":\"tenantId\",\"literals\":[{\"name\":\"$eq\",\"value\":\"0\"}]},{\"name\":\"$field\",\"value\":\"shareField\",\"literals\":[{\"name\":\"$eq\",\"value\":\"0\"}]}]}", mapper.writeValueAsString(filter));
//		
//	}
//	
//	@Test
//	public void testFilterSharedEntityAndHiddenable() throws JsonProcessingException {
//		
//		Caller caller = new Caller();
//		caller.setRequestedTenantId("0");
//		caller.setSuperTenant(false);
//		
//		Domain domain = new Domain(null, null, null, null, null, null, null, null, false, false, false, false, false, false, false, null, null, null, null, null, null, null, false, false, false, false, false, false, false, true, false, "shareField", null, false, null, null, false, false, false, false);
//		
//		Literal filter = Repository.getFilterFromCallerInfosAndDomainInfos(caller, domain, null);
//		
//		assertNotNull(filter);	
//		
//		ObjectMapper mapper = new ObjectMapper();
//		assertEquals("{\"name\":\"$or\",\"literals\":[{\"name\":\"$field\",\"value\":\"tenantId\",\"literals\":[{\"name\":\"$eq\",\"value\":\"0\"}]},{\"name\":\"$and\",\"literals\":[{\"name\":\"$field\",\"value\":\"shareField\",\"literals\":[{\"name\":\"$eq\",\"value\":\"0\"}]},{\"name\":\"$field\",\"value\":\"visible\",\"literals\":[{\"name\":\"$eq\",\"value\":true}]}]}]}", mapper.writeValueAsString(filter));
//	}
//	
//	@Test
//	public void testFilterNotSharedEntityAndHiddenable() throws JsonProcessingException {
//		
//		Caller caller = new Caller();
//		caller.setRequestedTenantId("0");
//		caller.setSuperTenant(false);
//		
//		Domain domain = new Domain(null, null, null, null, null, null, null, null, false, false, false, false, false, false, false, null, null, null, null, null, null, null, false, false, false, false, false, false, false, true, false, null, null, false, null, null, false, false, false, false);
//		
//		Literal filter = Repository.getFilterFromCallerInfosAndDomainInfos(caller, domain, null);
//		
//		assertNotNull(filter);	
//		
//		ObjectMapper mapper = new ObjectMapper();
//		assertEquals("{\"name\":\"$field\",\"value\":\"tenantId\",\"literals\":[{\"name\":\"$eq\",\"value\":\"0\"}]}", mapper.writeValueAsString(filter));
//	}
//	
//	@Test
//	public void testFilterSharedEntityAndHiddenableWithOtherFilter() throws JsonProcessingException {
//		
//		Caller caller = new Caller();
//		caller.setRequestedTenantId("0");
//		caller.setSuperTenant(false);
//		
//		Domain domain = new Domain(null, null, null, null, null, null, null, null, false, false, false, false, false, false, false, null, null, null, null, null, null, null, false, false, false, false, false, false, false, true, false, "shareField", null, false, null, null, false, false, false, false);
//		
//		Literal otherfilterequal = Literal.eq("aField", "test");
//		
//		Literal filter = Repository.getFilterFromCallerInfosAndDomainInfos(caller, domain, otherfilterequal);
//		
//		assertNotNull(filter);	
//		
//		ObjectMapper mapper = new ObjectMapper();
//		assertEquals("{\"name\":\"$and\",\"literals\":[{\"name\":\"$field\",\"value\":\"aField\",\"literals\":[{\"name\":\"$eq\",\"value\":\"test\"}]},{\"name\":\"$or\",\"literals\":[{\"name\":\"$field\",\"value\":\"tenantId\",\"literals\":[{\"name\":\"$eq\",\"value\":\"0\"}]},{\"name\":\"$and\",\"literals\":[{\"name\":\"$field\",\"value\":\"shareField\",\"literals\":[{\"name\":\"$eq\",\"value\":\"0\"}]},{\"name\":\"$field\",\"value\":\"visible\",\"literals\":[{\"name\":\"$eq\",\"value\":true}]}]}]}]}", mapper.writeValueAsString(filter));
//	}
//	
//	
//	@Test
//	public void testFilterPublicEntityAndHiddenable() throws JsonProcessingException {
//		
//		Caller caller = new Caller();
//		caller.setRequestedTenantId(null);
//		caller.setSuperTenant(false);
//		
//		Domain domain = new Domain(null, null, null, null, null, null, null, null, false, false, false, false, false, false, false, null, null, null, null, null, null, null, false, false, false, false, false, false, false, true, true, null, null, false, null, null, false, false, false, false);
//		
//		Literal filter = Repository.getFilterFromCallerInfosAndDomainInfos(caller, domain, null);
//		
//		assertNotNull(filter);	
//		
//		ObjectMapper mapper = new ObjectMapper();
//		assertEquals("{\"name\":\"$field\",\"value\":\"visible\",\"literals\":[{\"name\":\"$eq\",\"value\":true}]}", mapper.writeValueAsString(filter));
//	}
//	
//	@Test
//	public void testFilterPublicEntityAndHiddenableAndRequestedTenantId() throws JsonProcessingException {
//		
//		Caller caller = new Caller();
//		caller.setRequestedTenantId("0");
//		caller.setSuperTenant(false);
//		
//		Domain domain = new Domain(null, null, null, null, null, null, null, null, false, false, false, false, false, false, false, null, null, null, null, null, null, null, false, false, false, false, false, false, false, true, true, null, null, false, null, null, false, false, false, false);
//		
//		Literal filter = Repository.getFilterFromCallerInfosAndDomainInfos(caller, domain, null);
//		
//		assertNotNull(filter);	
//		
//		ObjectMapper mapper = new ObjectMapper();
//		assertEquals("{\"name\":\"$and\",\"literals\":[{\"name\":\"$field\",\"value\":\"visible\",\"literals\":[{\"name\":\"$eq\",\"value\":true}]},{\"name\":\"$field\",\"value\":\"tenantId\",\"literals\":[{\"name\":\"$eq\",\"value\":\"0\"}]}]}", mapper.writeValueAsString(filter));
//	}
//	
//	
//	@Test
//	public void testFilterPublicEntityAndHiddenableAndRequestedTenantIdAndOwnerId() throws JsonProcessingException {
//		
//		Caller caller = new Caller();
//		caller.setRequestedTenantId("0");
//		caller.setOwnerId("1");
//		caller.setSuperTenant(false);
//		
//		Domain domain = new Domain(null, null, null, null, null, null, null, null, false, false, false, false, false, false, false, null, null, null, null, null, null, null, false, false, false, false, false, false, false, true, true, null, null, false, null, null, false, true, false, false);
//		
//		Literal filter = Repository.getFilterFromCallerInfosAndDomainInfos(caller, domain, null);
//		
//		assertNotNull(filter);	
//		
//		ObjectMapper mapper = new ObjectMapper();
//		assertEquals("{\"name\":\"$and\",\"literals\":[{\"name\":\"$field\",\"value\":\"visible\",\"literals\":[{\"name\":\"$eq\",\"value\":true}]},{\"name\":\"$field\",\"value\":\"tenantId\",\"literals\":[{\"name\":\"$eq\",\"value\":\"0\"}]},{\"name\":\"$field\",\"value\":\"ownerId\",\"literals\":[{\"name\":\"$eq\",\"value\":\"1\"}]}]}", mapper.writeValueAsString(filter));
//	}
	

}
