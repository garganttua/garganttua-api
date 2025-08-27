package com.garganttua.api.core.security.entity.checker;

import org.junit.jupiter.api.BeforeAll;

import com.garganttua.api.core.ReflectionsAnnotationScanner;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

public class EntityAuthorizationCheckerTest {
	@BeforeAll
	public static void setupAnnotationScanner() {
		GGObjectReflectionHelper.annotationScanner = new ReflectionsAnnotationScanner();
	}
//	@Test
//	public void testJWTAuthorization() throws CoreException {
//		AuthorizationInfos infos = EntityAuthorizationChecker.checkEntityAuthorizationClass(JWTAuthorization.class);
//		assertNotNull(infos.expirationFieldAddress());	
//	}
}
