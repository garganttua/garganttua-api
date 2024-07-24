package com.garganttua.api.security.core.entity.checker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.garganttua.api.spec.security.GGAPIEntitySecurityInfos;
import com.garganttua.api.spec.service.GGAPIServiceAccess;

public class GGAPIEntitySecurityCheckerTest {

	@Test
	public void testEntityWithNoAnnotation() {
		GGAPIEntitySecurityInfos infos = GGAPIEntitySecurityChecker.checkEntityClass(TestEntityWithNoSecurityAnnotation.class);
		
		assertNotNull(infos);
		assertEquals(GGAPIServiceAccess.tenant, infos.creationAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.readAllAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.readOneAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.updateOneAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.deleteOneAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.deleteAllAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.countAccess());
		
		assertEquals(true, infos.creationAuthority());
		assertEquals(true, infos.readAllAuthority());
		assertEquals(true, infos.readOneAuthority());
		assertEquals(true, infos.updateOneAuthority());
		assertEquals(true, infos.deleteOneAuthority());
		assertEquals(true, infos.deleteAllAuthority());
		assertEquals(true, infos.countAuthority());	
	}
	
	@Test
	public void testEntityWithAnnotationButEmpty() {
		
		GGAPIEntitySecurityInfos infos = GGAPIEntitySecurityChecker.checkEntityClass(TestEntityWithAnnotationButEmpty.class);
		
		assertNotNull(infos);
		assertEquals(GGAPIServiceAccess.tenant, infos.creationAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.readAllAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.readOneAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.updateOneAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.deleteOneAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.deleteAllAccess());
		assertEquals(GGAPIServiceAccess.tenant, infos.countAccess());
		
		assertEquals(true, infos.creationAuthority());
		assertEquals(true, infos.readAllAuthority());
		assertEquals(true, infos.readOneAuthority());
		assertEquals(true, infos.updateOneAuthority());
		assertEquals(true, infos.deleteOneAuthority());
		assertEquals(true, infos.deleteAllAuthority());
		assertEquals(true, infos.countAuthority());	
		
	}
	
	@Test
	public void testEntityWithAnnotation() {
		
		GGAPIEntitySecurityInfos infos = GGAPIEntitySecurityChecker.checkEntityClass(TestEntityWithAnnotation.class);
		
		assertNotNull(infos);
		assertEquals(GGAPIServiceAccess.anonymous, infos.creationAccess());
		assertEquals(GGAPIServiceAccess.anonymous, infos.readAllAccess());
		assertEquals(GGAPIServiceAccess.anonymous, infos.readOneAccess());
		assertEquals(GGAPIServiceAccess.anonymous, infos.updateOneAccess());
		assertEquals(GGAPIServiceAccess.anonymous, infos.deleteOneAccess());
		assertEquals(GGAPIServiceAccess.anonymous, infos.deleteAllAccess());
		assertEquals(GGAPIServiceAccess.anonymous, infos.countAccess());
		
		assertEquals(false, infos.creationAuthority());
		assertEquals(false, infos.readAllAuthority());
		assertEquals(false, infos.readOneAuthority());
		assertEquals(false, infos.updateOneAuthority());
		assertEquals(false, infos.deleteOneAuthority());
		assertEquals(false, infos.deleteAllAuthority());
		assertEquals(false, infos.countAuthority());	
	}
}
