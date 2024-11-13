package com.garganttua.api.security.core.entity.checker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.GGAPIEntitySecurityInfos;
import com.garganttua.api.spec.service.GGAPIServiceAccess;

public class GGAPIEntitySecurityCheckerTest {

	@Test
	public void testEntityWithNoAnnotation() throws GGAPIException {
		GGAPIEntitySecurityInfos infos = GGAPIEntitySecurityChecker.checkEntityClass(TestEntityWithNoSecurityAnnotation.class, null);
		
		assertNotNull(infos);
		assertEquals(GGAPIServiceAccess.tenant, infos.getCreationAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.getReadAllAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.getReadOneAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.getUpdateOneAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.getDeleteOneAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.getDeleteAllAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.getCountAccess());
		
		assertEquals(true, infos.isCreationAuthority());
		assertEquals(true, infos.isReadAllAuthority());
		assertEquals(true, infos.isReadOneAuthority());
		assertEquals(true, infos.isUpdateOneAuthority());
		assertEquals(true, infos.isDeleteOneAuthority());
		assertEquals(true, infos.isDeleteAllAuthority());
		assertEquals(true, infos.isCountAuthority());	
	}
	
	@Test
	public void testEntityWithAnnotationButEmpty() throws GGAPIException {
		
		GGAPIEntitySecurityInfos infos = GGAPIEntitySecurityChecker.checkEntityClass(TestEntityWithAnnotationButEmpty.class, null);
		
		assertNotNull(infos);
		assertEquals(GGAPIServiceAccess.tenant, infos.getCreationAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.getReadAllAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.getReadOneAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.getUpdateOneAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.getDeleteOneAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.getDeleteAllAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.getCountAccess());
		
		assertEquals(true, infos.isCreationAuthority());
		assertEquals(true, infos.isReadAllAuthority());
		assertEquals(true, infos.isReadOneAuthority());
		assertEquals(true, infos.isUpdateOneAuthority());
		assertEquals(true, infos.isDeleteOneAuthority());
		assertEquals(true, infos.isDeleteAllAuthority());
		assertEquals(true, infos.isCountAuthority());	
		
	}
	
	@Test
	public void testEntityWithAnnotation() throws GGAPIException {
		
		GGAPIEntitySecurityInfos infos = GGAPIEntitySecurityChecker.checkEntityClass(TestEntityWithAnnotation.class, null);
		
		assertNotNull(infos);
		assertEquals(GGAPIServiceAccess.anonymous, infos.getCreationAccess());
		assertEquals(GGAPIServiceAccess.anonymous, infos.getReadAllAccess());
		assertEquals(GGAPIServiceAccess.anonymous, infos.getReadOneAccess());
		assertEquals(GGAPIServiceAccess.anonymous, infos.getUpdateOneAccess());
		assertEquals(GGAPIServiceAccess.anonymous, infos.getDeleteOneAccess());
		assertEquals(GGAPIServiceAccess.anonymous, infos.getDeleteAllAccess());
		assertEquals(GGAPIServiceAccess.anonymous, infos.getCountAccess());
		
		assertEquals(false, infos.isCreationAuthority());
		assertEquals(false, infos.isReadAllAuthority());
		assertEquals(false, infos.isReadOneAuthority());
		assertEquals(false, infos.isUpdateOneAuthority());
		assertEquals(false, infos.isDeleteOneAuthority());
		assertEquals(false, infos.isDeleteAllAuthority());
		assertEquals(false, infos.isCountAuthority());		
	}
	
	@Test
	public void testAuthenticator() throws GGAPIException {
		
		GGAPIEntitySecurityInfos infos = GGAPIEntitySecurityChecker.checkEntityClass(TestAuthenticator.class, null);
		
		assertNotNull(infos);
		assertEquals(GGAPIServiceAccess.anonymous, infos.getCreationAccess());
		assertEquals(GGAPIServiceAccess.anonymous, infos.getReadAllAccess());
		assertEquals(GGAPIServiceAccess.anonymous, infos.getReadOneAccess());
		assertEquals(GGAPIServiceAccess.anonymous, infos.getUpdateOneAccess());
		assertEquals(GGAPIServiceAccess.anonymous, infos.getDeleteOneAccess());
		assertEquals(GGAPIServiceAccess.anonymous, infos.getDeleteAllAccess());
		assertEquals(GGAPIServiceAccess.anonymous, infos.getCountAccess());
		
		assertEquals(false, infos.isCreationAuthority());
		assertEquals(false, infos.isReadAllAuthority());
		assertEquals(false, infos.isReadOneAuthority());
		assertEquals(false, infos.isUpdateOneAuthority());
		assertEquals(false, infos.isDeleteOneAuthority());
		assertEquals(false, infos.isDeleteAllAuthority());
		assertEquals(false, infos.isCountAuthority());	
	}
}
