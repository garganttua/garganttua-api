package com.garganttua.api.core.security.entity.checker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.ReflectionsAnnotationScanner;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.security.EntitySecurityInfos;
import com.garganttua.api.spec.service.ServiceAccess;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

public class EntitySecurityCheckerTest {
	@BeforeAll
	public static void setupAnnotationScanner() {
		GGObjectReflectionHelper.annotationScanner = new ReflectionsAnnotationScanner();
	}
	@Test
	public void testEntityWithNoAnnotation() throws CoreException {
		EntitySecurityInfos infos = EntitySecurityChecker.checkEntityClass(TestEntityWithNoSecurityAnnotation.class, null);
		
		assertNotNull(infos);
		assertEquals(ServiceAccess.tenant, infos.getCreationAccess());
		assertEquals(ServiceAccess.tenant, infos.getReadAllAccess());
		assertEquals(ServiceAccess.tenant, infos.getReadOneAccess());
		assertEquals(ServiceAccess.tenant, infos.getUpdateOneAccess());
		assertEquals(ServiceAccess.tenant, infos.getDeleteOneAccess());
		assertEquals(ServiceAccess.tenant, infos.getDeleteAllAccess());
		assertEquals(ServiceAccess.tenant, infos.getCountAccess());
		
		assertEquals(true, infos.isCreationAuthority());
		assertEquals(true, infos.isReadAllAuthority());
		assertEquals(true, infos.isReadOneAuthority());
		assertEquals(true, infos.isUpdateOneAuthority());
		assertEquals(true, infos.isDeleteOneAuthority());
		assertEquals(true, infos.isDeleteAllAuthority());
		assertEquals(true, infos.isCountAuthority());	
	} 
	
	@Test
	public void testEntityWithAnnotationButEmpty() throws CoreException {
		
		EntitySecurityInfos infos = EntitySecurityChecker.checkEntityClass(TestEntityWithAnnotationButEmpty.class, null);
		
		assertNotNull(infos);
		assertEquals(ServiceAccess.tenant, infos.getCreationAccess());
		assertEquals(ServiceAccess.tenant, infos.getReadAllAccess());
		assertEquals(ServiceAccess.tenant, infos.getReadOneAccess());
		assertEquals(ServiceAccess.tenant, infos.getUpdateOneAccess());
		assertEquals(ServiceAccess.tenant, infos.getDeleteOneAccess());
		assertEquals(ServiceAccess.tenant, infos.getDeleteAllAccess());
		assertEquals(ServiceAccess.tenant, infos.getCountAccess());
		
		assertEquals(true, infos.isCreationAuthority());
		assertEquals(true, infos.isReadAllAuthority());
		assertEquals(true, infos.isReadOneAuthority());
		assertEquals(true, infos.isUpdateOneAuthority());
		assertEquals(true, infos.isDeleteOneAuthority());
		assertEquals(true, infos.isDeleteAllAuthority());
		assertEquals(true, infos.isCountAuthority());	
		
	}
	
	@Test
	public void testEntityWithAnnotation() throws CoreException {
		
		EntitySecurityInfos infos = EntitySecurityChecker.checkEntityClass(TestEntityWithAnnotation.class, null);
		
		assertNotNull(infos);
		assertEquals(ServiceAccess.anonymous, infos.getCreationAccess());
		assertEquals(ServiceAccess.anonymous, infos.getReadAllAccess());
		assertEquals(ServiceAccess.anonymous, infos.getReadOneAccess());
		assertEquals(ServiceAccess.anonymous, infos.getUpdateOneAccess());
		assertEquals(ServiceAccess.anonymous, infos.getDeleteOneAccess());
		assertEquals(ServiceAccess.anonymous, infos.getDeleteAllAccess());
		assertEquals(ServiceAccess.anonymous, infos.getCountAccess());
		
		assertEquals(false, infos.isCreationAuthority());
		assertEquals(false, infos.isReadAllAuthority());
		assertEquals(false, infos.isReadOneAuthority());
		assertEquals(false, infos.isUpdateOneAuthority());
		assertEquals(false, infos.isDeleteOneAuthority());
		assertEquals(false, infos.isDeleteAllAuthority());
		assertEquals(false, infos.isCountAuthority());		
	}
	
	@Test
	public void testAuthenticator() throws CoreException {
		
		EntitySecurityInfos infos = EntitySecurityChecker.checkEntityClass(TestAuthenticator.class, null);
		
		assertNotNull(infos);
		assertEquals(ServiceAccess.anonymous, infos.getCreationAccess());
		assertEquals(ServiceAccess.anonymous, infos.getReadAllAccess());
		assertEquals(ServiceAccess.anonymous, infos.getReadOneAccess());
		assertEquals(ServiceAccess.anonymous, infos.getUpdateOneAccess());
		assertEquals(ServiceAccess.anonymous, infos.getDeleteOneAccess());
		assertEquals(ServiceAccess.anonymous, infos.getDeleteAllAccess());
		assertEquals(ServiceAccess.anonymous, infos.getCountAccess());
		
		assertEquals(false, infos.isCreationAuthority());
		assertEquals(false, infos.isReadAllAuthority());
		assertEquals(false, infos.isReadOneAuthority());
		assertEquals(false, infos.isUpdateOneAuthority());
		assertEquals(false, infos.isDeleteOneAuthority());
		assertEquals(false, infos.isDeleteAllAuthority());
		assertEquals(false, infos.isCountAuthority());	
	}
}
